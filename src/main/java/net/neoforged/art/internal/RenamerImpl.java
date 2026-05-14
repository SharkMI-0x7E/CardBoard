package net.neoforged.art.internal;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.neoforged.art.api.ClassProvider;
import net.neoforged.art.api.Renamer;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.api.ClassProvider.Builder;
import net.neoforged.art.api.Transformer.ClassEntry;
import net.neoforged.art.api.Transformer.Entry;
import net.neoforged.art.api.Transformer.JavadoctorEntry;
import net.neoforged.art.api.Transformer.ManifestEntry;
import net.neoforged.art.api.Transformer.ResourceEntry;
import net.neoforged.cliutils.JarUtils;
import net.neoforged.cliutils.progress.ProgressReporter;

public class RenamerImpl implements Renamer {
	private static final ProgressReporter PROGRESS = ProgressReporter.getDefault();
	static final int MAX_ASM_VERSION = 589824;
	private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
	private final List<File> libraries;
	private final List<Transformer> transformers;
	private final SortedClassProvider sortedClassProvider;
	private final List<ClassProvider> classProviders;
	private final int threads;
	private final Consumer<String> logger;
	private final Consumer<String> debug;
	private boolean setup = false;
	private ClassProvider libraryClasses;

	RenamerImpl(
			List<File> libraries,
			List<Transformer> transformers,
			SortedClassProvider sortedClassProvider,
			List<ClassProvider> classProviders,
			int threads,
			Consumer<String> logger,
			Consumer<String> debug
			) {
		this.libraries = libraries;
		this.transformers = transformers;
		this.sortedClassProvider = sortedClassProvider;
		this.classProviders = Collections.unmodifiableList(classProviders);
		this.threads = threads;
		this.logger = logger;
		this.debug = debug;
	}

	private void setup() {
		if (!this.setup) {
			this.setup = true;
			net.neoforged.art.api.ClassProvider.Builder libraryClassesBuilder = ClassProvider.builder().shouldCacheAll(true);
			this.logger.accept("Adding Libraries to Inheritance");
			this.libraries.forEach(f -> libraryClassesBuilder.addLibrary(f.toPath()));
			this.libraryClasses = libraryClassesBuilder.build();
		}
	}

	public void run(File input, File output) {
		this.run(input, output, false);
	}

	public void run(File input, File output, boolean remappingSelf) {
		if (!this.setup) {
			this.setup();
		}

		if (Boolean.getBoolean("net.neoforged.progressmanager.enabled")) {
			try {
				PROGRESS.setMaxProgress(JarUtils.getFileCountInZip(input));
			} catch (IOException var37) {
				this.logger.accept("Failed to read zip file count: " + var37);
			}
		}

		input = Objects.requireNonNull(input).getAbsoluteFile();
		output = Objects.requireNonNull(output).getAbsoluteFile();
		if (!input.exists()) {
			throw new IllegalArgumentException("Input file not found: " + input.getAbsolutePath());
		} else {
			this.logger.accept("Reading Input: " + input.getAbsolutePath());
			PROGRESS.setStep("Reading input jar");
			List<Entry> oldEntries = new ArrayList<>();

			try (ZipFile in = new ZipFile(input)) {
				int amount = 0;
				Enumeration<? extends ZipEntry> entries = in.entries();

				while (entries.hasMoreElements()) {
					ZipEntry e = entries.nextElement();
					if (!e.isDirectory()) {
						String name = e.getName();

						byte[] data;
						try (InputStream entryInput = in.getInputStream(e)) {
							data = entryInput.readAllBytes();
						}

						if (name.endsWith(".class") && !name.contains("META-INF/")) {
							oldEntries.add(ClassEntry.create(name, e.getTime(), data));
						} else if (name.equals("META-INF/MANIFEST.MF")) {
							oldEntries.add(ManifestEntry.create(e.getTime(), data));
						} else if (name.equals("javadoctor.json")) {
							oldEntries.add(JavadoctorEntry.create(e.getTime(), data));
						} else {
							oldEntries.add(ResourceEntry.create(name, e.getTime(), data));
						}

						if (++amount % 10 == 0) {
							PROGRESS.setProgress(amount);
						}
					}
				}
			} catch (IOException var44) {
				throw new RuntimeException("Could not parse input: " + input.getAbsolutePath(), var44);
			}

			this.sortedClassProvider.clearCache();
			ArrayList<ClassProvider> classProviders = new ArrayList<>(this.classProviders);
			classProviders.add(0, this.libraryClasses);
			this.sortedClassProvider.classProviders = classProviders;
			AsyncHelper async = new AsyncHelper(this.threads);

			try {
				PROGRESS.setProgress(0);
				PROGRESS.setIndeterminate(true);
				PROGRESS.setStep("Processing entries");
				List<ClassEntry> ourClasses = oldEntries.stream()
						.filter(ex -> ex instanceof ClassEntry && !ex.getName().startsWith("META-INF/"))
						.map(ClassEntry.class::cast)
						.collect(Collectors.toList());
				this.logger.accept("Adding input to inheritance map");
				net.neoforged.art.api.ClassProvider.Builder inputClassesBuilder = ClassProvider.builder();
				async.consumeAll(
						ourClasses, ClassEntry::getClassName, c -> inputClassesBuilder.addClass(c.getName().substring(0, c.getName().length() - 6), c.getData())
						);
				classProviders.add(0, inputClassesBuilder.build());
				this.logger.accept("Processing entries");
				List<Entry> newEntries = async.invokeAll(oldEntries, Entry::getName, this::processEntry);

				this.logger.accept("Adding extras");
				List<Entry> finalNewEntries = newEntries;
				transformers.forEach(t -> finalNewEntries.addAll(t.getExtras()));

				// this.transformers.forEach(t -> newEntries.addAll(t.getExtras()));
				Set<String> seen = new HashSet<>();
				
				if (!remappingSelf) {
					String dupes = newEntries.stream().map(Entry::getName).filter(n -> !seen.add(n)).sorted().collect(Collectors.joining(", "));

					if (!dupes.isEmpty()) {
						throw new IllegalStateException("Duplicate entries detected: " + dupes);
					}
				} else {
					List<Entry> n = new ArrayList<>();

					for (Entry e : newEntries) {
						if (seen.add(e.getName())) {
							n.add(e);
						}
					}

					newEntries = n;
				}

				this.logger.accept("Sorting");
				Collections.sort(newEntries, this::compare);
				if (!output.getParentFile().exists()) {
					output.getParentFile().mkdirs();
				}

				seen.clear();
				PROGRESS.setMaxProgress(newEntries.size());
				PROGRESS.setStep("Writing output");
				this.logger.accept("Writing Output: " + output.getAbsolutePath());

				try (
						OutputStream fos = new BufferedOutputStream(Files.newOutputStream(output.toPath()));
						ZipOutputStream zos = new ZipOutputStream(fos);
						) {
					int amount = 0;

					for (Entry ex : newEntries) {
						String name = ex.getName();
						int idx = name.lastIndexOf(47);
						if (idx != -1) {
							this.addDirectory(zos, seen, name.substring(0, idx));
						}

						this.logger.accept("  " + name);
						ZipEntry entry = new ZipEntry(name);
						entry.setTime(ex.getTime());
						zos.putNextEntry(entry);
						zos.write(ex.getData());
						zos.closeEntry();
						if (++amount % 10 == 0) {
							PROGRESS.setProgress(amount);
						}
					}

					PROGRESS.setProgress(amount);
				} catch (IOException var41) {
					throw new RuntimeException("Could not write output to file: " + output.getAbsolutePath(), var41);
				}
			} finally {
				async.shutdown();
			}
		}
	}

	private byte[] readAllBytes(InputStream in, long size) throws IOException {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream(size >= 0L ? (int)size : 0);
		byte[] buffer = new byte[8192];

		int read;
		while ((read = in.read(buffer)) != -1) {
			tmp.write(buffer, 0, read);
		}

		return tmp.toByteArray();
	}

	private void addDirectory(ZipOutputStream zos, Set<String> seen, String path) throws IOException {
		if (seen.add(path)) {
			int idx = path.lastIndexOf(47);
			if (idx != -1) {
				this.addDirectory(zos, seen, path.substring(0, idx));
			}

			this.logger.accept("  " + path + "/");
			ZipEntry dir = new ZipEntry(path + "/");
			dir.setTime(946684800L);
			zos.putNextEntry(dir);
			zos.closeEntry();
		}
	}

	private Entry processEntry(Entry start) {
		Entry entry = start;

		for (Transformer transformer : this.transformers) {
			entry = entry.process(transformer);
			if (entry == null) {
				return null;
			}
		}

		return entry;
	}

	private int compare(Entry o1, Entry o2) {
		if ("META-INF/MANIFEST.MF".equals(o1.getName())) {
			return "META-INF/MANIFEST.MF".equals(o2.getName()) ? 0 : -1;
		} else if ("META-INF/MANIFEST.MF".equals(o2.getName())) {
			return "META-INF/MANIFEST.MF".equals(o1.getName()) ? 0 : 1;
		} else {
			return o1.getName().compareTo(o2.getName());
		}
	}

	public void close() throws IOException {
		this.sortedClassProvider.close();
	}
}
