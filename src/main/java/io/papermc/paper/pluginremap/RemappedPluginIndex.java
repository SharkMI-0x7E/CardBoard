package io.papermc.paper.pluginremap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.papermc.paper.util.Hashing;
import io.papermc.paper.util.MappingEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.spongepowered.configurate.loader.AtomicFiles;

@DefaultQualifier(NonNull.class)
class RemappedPluginIndex {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String INDEX_FILE_NAME = "index.json";
	protected final RemappedPluginIndex.State state;
	private final Path dir;
	private final Path indexFile;
	private final boolean handleDuplicateFileNames;

	RemappedPluginIndex(Path dir, boolean handleDuplicateFileNames) {
		this.dir = dir;
		this.handleDuplicateFileNames = handleDuplicateFileNames;
		if (!Files.exists(this.dir)) {
			try {
				Files.createDirectories(this.dir);
			} catch (IOException var4) {
				throw new UncheckedIOException(var4);
			}
		}

		this.indexFile = dir.resolve("index.json");
		if (Files.isRegularFile(this.indexFile)) {
			this.state = this.readIndex();
		} else {
			this.state = new RemappedPluginIndex.State();
		}
	}

	private RemappedPluginIndex.State readIndex() {
		RemappedPluginIndex.State state;
		try (BufferedReader reader = Files.newBufferedReader(this.indexFile)) {
			state = (RemappedPluginIndex.State)GSON.fromJson(reader, RemappedPluginIndex.State.class);
		} catch (Exception var9) {
			throw new RuntimeException("Failed to read index file '" + this.indexFile + "'", var9);
		}

		if (state.mappingsHash.equals(MappingEnvironment.mappingsHash())) {
			return state;
		} else {
			for (String fileName : state.hashes.values()) {
				Path path = this.dir.resolve(fileName);

				try {
					Files.deleteIfExists(path);
				} catch (IOException var7) {
					throw new UncheckedIOException("Failed to delete no longer needed file '" + path + "'", var7);
				}
			}

			return new RemappedPluginIndex.State();
		}
	}

	Path dir() {
		return this.dir;
	}

	List<Path> getAllIfPresent(List<Path> paths) {
		Map<Path, String> hashCache = new HashMap<>();
		Function<Path, String> inputFileHash = pathx -> hashCache.computeIfAbsent(pathx, Hashing::sha256);
		Iterator<Entry<String, String>> iterator = this.state.hashes.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String inputHash = entry.getKey();
			String fileName = entry.getValue();
			if (!paths.stream().anyMatch(pathx -> inputFileHash.apply(pathx).equals(inputHash))) {
				iterator.remove();
				Path filePath = this.dir.resolve(fileName);

				try {
					Files.deleteIfExists(filePath);
				} catch (IOException var10) {
					throw new UncheckedIOException("Failed to delete no longer needed file '" + filePath + "'", var10);
				}
			}
		}

		this.state.skippedHashes.removeIf(hash -> paths.stream().noneMatch(pathx -> inputFileHash.apply(pathx).equals(hash)));
		List<Path> ret = new ArrayList<>();

		for (Path path : paths) {
			String inputHash = inputFileHash.apply(path);
			if (this.state.skippedHashes.contains(inputHash)) {
				ret.add(path);
			} else {
				Path cached = this.getIfPresent(inputHash);
				if (cached == null) {
					return null;
				}

				ret.add(cached);
			}
		}

		return ret;
	}

	private String createCachedFileName(Path in) {
		if (this.handleDuplicateFileNames) {
			String fileName = in.getFileName().toString();
			int i = fileName.lastIndexOf(".jar");
			return fileName.substring(0, i) + "-" + System.currentTimeMillis() + ".jar";
		} else {
			return in.getFileName().toString();
		}
	}

	@Nullable
	Path getIfPresent(Path in) {
		String inHash = Hashing.sha256(in);
		return this.state.skippedHashes.contains(inHash) ? in : this.getIfPresent(inHash);
	}

	protected Path getIfPresent(String inHash) {
		String fileName = this.state.hashes.get(inHash);
		if (fileName == null) {
			return null;
		} else {
			Path path = this.dir.resolve(fileName);
			return Files.exists(path) ? path : null;
		}
	}

	Path input(Path in) {
		return this.input(in, Hashing.sha256(in));
	}

	void skip(Path in) {
		this.state.skippedHashes.add(Hashing.sha256(in));
	}

	protected Path input(Path in, String hashString) {
		String name = this.createCachedFileName(in);
		this.state.hashes.put(hashString, name);
		return this.dir.resolve(name);
	}

	void write() {
		try (BufferedWriter writer = AtomicFiles.atomicBufferedWriter(this.indexFile, StandardCharsets.UTF_8)) {
			GSON.toJson(this.state, writer);
		} catch (IOException var6) {
			LOGGER.warn("Failed to write index file '{}'", this.indexFile, var6);
		}
	}

	static final class State {
		final Map<String, String> hashes = new HashMap<>();
		final Set<String> skippedHashes = new HashSet<>();
		private final String mappingsHash = MappingEnvironment.mappingsHash();
	}
}
