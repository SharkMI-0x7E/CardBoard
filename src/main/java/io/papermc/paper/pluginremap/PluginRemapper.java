package io.papermc.paper.pluginremap;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.papermc.paper.util.AtomicFiles;
import io.papermc.paper.util.MappingEnvironment;
import io.papermc.paper.util.concurrent.ScalingThreadPool;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.util.ExceptionCollector;
import net.neoforged.art.api.Renamer;
import net.neoforged.art.api.SignatureStripperConfig;
import net.neoforged.art.api.Transformer;
import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IMappingFile.Format;

import org.cardboardpowered.CardboardLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public final class PluginRemapper {

	public static final boolean DEBUG_LOGGING = Boolean.getBoolean("Paper.PluginRemapperDebug");
	private static final String PAPER_REMAPPED = ".paper-remapped";
	private static final String UNKNOWN_ORIGIN = "unknown-origin";
	private static final String LIBRARIES = "libraries";
	private static final String EXTRA_PLUGINS = "extra-plugins";
	private static final String REMAP_CLASSPATH = "remap-classpath";
	private static final String REVERSED_MAPPINGS = "mappings/reversed";
	private static final Logger LOGGER = CardboardLogger.getSLF4J();// LogUtils.getClassLogger();
	private final ExecutorService threadPool = createThreadPool();
	private final ReobfServer reobf;
	private final RemappedPluginIndex remappedPlugins;
	private final RemappedPluginIndex extraPlugins;
	private final UnknownOriginRemappedPluginIndex unknownOrigin;
	private final UnknownOriginRemappedPluginIndex libraries;
	@Nullable
	private CompletableFuture<IMappingFile> reversedMappings;

	public PluginRemapper(Path pluginsDir) {
		CompletableFuture<IMappingFile> mappings = CompletableFuture.supplyAsync(PluginRemapper::loadReobfMappings, this.threadPool);
		Path remappedPlugins = pluginsDir.resolve(".paper-remapped");
		this.reversedMappings = this.reversedMappingsFuture(() -> mappings, remappedPlugins, this.threadPool);
		this.reobf = new ReobfServer(remappedPlugins.resolve("remap-classpath"), mappings, this.threadPool);
		this.remappedPlugins = new RemappedPluginIndex(remappedPlugins, false);
		this.extraPlugins = new RemappedPluginIndex(this.remappedPlugins.dir().resolve("extra-plugins"), true);
		this.unknownOrigin = new UnknownOriginRemappedPluginIndex(this.remappedPlugins.dir().resolve("unknown-origin"));
		this.libraries = new UnknownOriginRemappedPluginIndex(this.remappedPlugins.dir().resolve("libraries"));
	}

	@Nullable
	public static PluginRemapper create(Path pluginsDir) {
		if (!MappingEnvironment.reobf() && MappingEnvironment.hasMappings()) {
			try {
				return new PluginRemapper(pluginsDir);
			} catch (Exception var2) {
				throw new RuntimeException("Failed to create PluginRemapper, try deleting the '" + pluginsDir.resolve(".paper-remapped") + "' directory", var2);
			}
		} else {
			return null;
		}
	}

	public void shutdown() {
		this.threadPool.shutdown();
		this.save(true);

		boolean didShutdown;
		try {
			didShutdown = this.threadPool.awaitTermination(3L, TimeUnit.SECONDS);
		} catch (InterruptedException var3) {
			didShutdown = false;
		}

		if (!didShutdown) {
			this.threadPool.shutdownNow();
		}
	}

	public void save(boolean clean) {
		this.remappedPlugins.write();
		this.extraPlugins.write();
		this.unknownOrigin.write(clean);
		this.libraries.write(clean);
	}

	public void loadingPlugins() {
		if (this.reversedMappings == null) {
			this.reversedMappings = this.reversedMappingsFuture(
					() -> CompletableFuture.supplyAsync(PluginRemapper::loadReobfMappings, this.threadPool), this.remappedPlugins.dir(), this.threadPool
					);
		}
	}

	public void pluginsEnabled() {
		this.reversedMappings = null;
		this.save(false);
	}

	public List<Path> remapLibraries(List<Path> libraries) {
		List<CompletableFuture<Path>> tasks = new ArrayList<>();

		for (Path lib : libraries) {
			if (!lib.getFileName().toString().endsWith(".jar")) {
				if (DEBUG_LOGGING) {
					LOGGER.info("Library '{}' is not a jar.", lib);
				}

				tasks.add(CompletableFuture.completedFuture(lib));
			} else {
				Path cached = this.libraries.getIfPresent(lib);
				if (cached != null) {
					if (DEBUG_LOGGING) {
						LOGGER.info("Library '{}' has not changed since last remap.", lib);
					}

					tasks.add(CompletableFuture.completedFuture(cached));
				} else {
					tasks.add(this.remapLibrary(this.libraries, lib));
				}
			}
		}

		return waitForAll(tasks);
	}

	public Path rewritePlugin(Path plugin) {
		if (!plugin.getParent().equals(this.remappedPlugins.dir()) && !plugin.getParent().equals(this.extraPlugins.dir())) {
			Path cached = this.unknownOrigin.getIfPresent(plugin);
			if (cached != null) {
				if (DEBUG_LOGGING) {
					LOGGER.info("Plugin '{}' has not changed since last remap.", plugin);
				}

				return cached;
			} else {
				return this.remapPlugin(this.unknownOrigin, plugin).join();
			}
		} else {
			return plugin;
		}
	}

	public List<Path> rewriteExtraPlugins(List<Path> plugins) {
		List<Path> allCached = this.extraPlugins.getAllIfPresent(plugins);
		if (allCached != null) {
			if (DEBUG_LOGGING) {
				LOGGER.info("All extra plugins have a remapped variant cached.");
			}

			return allCached;
		} else {
			List<CompletableFuture<Path>> tasks = new ArrayList<>();

			for (Path file : plugins) {
				Path cached = this.extraPlugins.getIfPresent(file);
				if (cached != null) {
					if (DEBUG_LOGGING) {
						LOGGER.info("Extra plugin '{}' has not changed since last remap.", file);
					}

					tasks.add(CompletableFuture.completedFuture(cached));
				} else {
					tasks.add(this.remapPlugin(this.extraPlugins, file));
				}
			}

			return waitForAll(tasks);
		}
	}

	public List<Path> rewritePluginDirectory(List<Path> jars) {
		List<Path> remappedJars = this.remappedPlugins.getAllIfPresent(jars);
		if (remappedJars != null) {
			if (DEBUG_LOGGING) {
				LOGGER.info("All plugins have a remapped variant cached.");
			}

			return remappedJars;
		} else {
			List<CompletableFuture<Path>> tasks = new ArrayList<>();

			for (Path file : jars) {
				Path existingFile = this.remappedPlugins.getIfPresent(file);
				if (existingFile != null) {
					if (DEBUG_LOGGING) {
						LOGGER.info("Plugin '{}' has not changed since last remap.", file);
					}

					tasks.add(CompletableFuture.completedFuture(existingFile));
				} else {
					tasks.add(this.remapPlugin(this.remappedPlugins, file));
				}
			}

			return waitForAll(tasks);
		}
	}

	private static IMappingFile reverse(IMappingFile mappings) {
		if (DEBUG_LOGGING) {
			LOGGER.info("Reversing mappings...");
		}

		long start = System.currentTimeMillis();
		IMappingFile reversed = mappings.reverse();
		if (DEBUG_LOGGING) {
			LOGGER.info("Done reversing mappings in {}ms.", System.currentTimeMillis() - start);
		}

		return reversed;
	}

	private CompletableFuture<IMappingFile> reversedMappingsFuture(
			Supplier<CompletableFuture<IMappingFile>> mappingsFuture, Path remappedPlugins, Executor executor
			) {
		return CompletableFuture.<CompletableFuture<IMappingFile>>supplyAsync(() -> {
			try {
				String mappingsHash = MappingEnvironment.mappingsHash();
				String fName = mappingsHash + ".tiny";
				Path reversedMappings1 = remappedPlugins.resolve("mappings/reversed");
				Path file = reversedMappings1.resolve(fName);
				if (Files.isDirectory(reversedMappings1)) {
					if (Files.isRegularFile(file)) {
						return CompletableFuture.completedFuture(loadMappings("Reversed", Files.newInputStream(file)));
					}

					for (Path oldFile : list(reversedMappings1, x$0 -> Files.isRegularFile(x$0))) {
						Files.delete(oldFile);
					}
				} else {
					Files.createDirectories(reversedMappings1);
				}

				return mappingsFuture.get().thenApply(loadedMappings -> {
					IMappingFile reversed = reverse(loadedMappings);

					try {
						AtomicFiles.atomicWrite(file, writeTo -> reversed.write(writeTo, Format.TINY, false));
						return reversed;
					} catch (IOException var4x) {
						throw new RuntimeException("Failed to write reversed mappings", var4x);
					}
				});
			} catch (IOException var8) {
				throw new RuntimeException("Failed to load reversed mappings", var8);
			}
		}, executor).thenCompose(f -> (CompletionStage<IMappingFile>)f);
	}

	private CompletableFuture<Path> remapPlugin(RemappedPluginIndex index, Path inputFile) {
		return this.remap(index, inputFile, false);
	}

	private CompletableFuture<Path> remapLibrary(RemappedPluginIndex index, Path inputFile) {
		return this.remap(index, inputFile, true);
	}

	private CompletableFuture<Path> remap(RemappedPluginIndex index, Path inputFile, boolean library) {
		Path destination = index.input(inputFile);

		try {
			CompletableFuture var18;
			try (FileSystem fs = FileSystems.newFileSystem(inputFile, new HashMap<>())) {
				Path manifestPath = fs.getPath("META-INF/MANIFEST.MF");
				String ns;
				if (Files.exists(manifestPath)) {
					Manifest manifest;
					try (InputStream in = new BufferedInputStream(Files.newInputStream(manifestPath))) {
						manifest = new Manifest(in);
					}

					ns = manifest.getMainAttributes().getValue("paperweight-mappings-namespace");
				} else {
					ns = null;
				}

				if (ns != null && !InsertManifestAttribute.KNOWN_NAMESPACES.contains(ns)) {
					throw new RuntimeException("Failed to remap plugin " + inputFile + " with unknown mapping namespace '" + ns + "'");
				}

				boolean mojangMappedManifest = ns != null && (ns.equals("mojang") || ns.equals("mojang+yarn"));
				if (library) {
					if (mojangMappedManifest) {
						if (DEBUG_LOGGING) {
							LOGGER.info("Library '{}' is already Mojang mapped.", inputFile);
						}

						index.skip(inputFile);
						return CompletableFuture.completedFuture(inputFile);
					}

					if (ns != null) {
						return this.reobf
								.remapped()
								.thenApplyAsync(
										reobfServer -> {
											LOGGER.info("Remapping {} '{}'...", library ? "library" : "plugin", inputFile);
											long start = System.currentTimeMillis();

											try (DebugLogger logger = DebugLogger.forOutputFile(destination)) {
												Renamer renamer = Renamer.builder()
														.add(Transformer.renamerFactory(this.mappings(), false))
														.add(InsertManifestAttribute.addNamespaceManifestAttribute("mojang+yarn"))
														.add(Transformer.signatureStripperFactory(SignatureStripperConfig.ALL))
														.lib(reobfServer.toFile())
														.threads(1)
														.logger(logger)
														.debug(logger.debug())
														.build();

												try {
													renamer.run(inputFile.toFile(), destination.toFile());
												} catch (Throwable var13) {
													if (renamer != null) {
														try {
															renamer.close();
														} catch (Throwable var12) {
															var13.addSuppressed(var12);
														}
													}

													throw var13;
												}

												if (renamer != null) {
													renamer.close();
												}
											} catch (Exception var15x) {
												throw new RuntimeException("Failed to remap plugin jar '" + inputFile + "'", var15x);
											}

											LOGGER.info(
													"Done remapping {} '{}' in {}ms.", new Object[]{library ? "library" : "plugin", inputFile, System.currentTimeMillis() - start}
													);
											return destination;
										},
										this.threadPool
										);
					}

					if (DEBUG_LOGGING) {
						LOGGER.info("Library '{}' does not specify a mappings namespace (not remapping).", inputFile);
					}

					index.skip(inputFile);
					return CompletableFuture.completedFuture(inputFile);
				}

				if (mojangMappedManifest) {
					if (DEBUG_LOGGING) {
						LOGGER.info("Plugin '{}' is already Mojang mapped.", inputFile);
					}

					index.skip(inputFile);
					return CompletableFuture.completedFuture(inputFile);
				}

				if (ns != null || !Files.exists(fs.getPath("paper-plugin.yml"))) {
					return this.reobf
							.remapped()
							.thenApplyAsync(
									reobfServer -> {
										LOGGER.info("Remapping {} '{}'...", library ? "library" : "plugin", inputFile);
										long start = System.currentTimeMillis();

										try (DebugLogger logger = DebugLogger.forOutputFile(destination)) {
											Renamer renamer = Renamer.builder()
													.add(Transformer.renamerFactory(this.mappings(), false))
													.add(InsertManifestAttribute.addNamespaceManifestAttribute("mojang+yarn"))
													.add(Transformer.signatureStripperFactory(SignatureStripperConfig.ALL))
													.lib(reobfServer.toFile())
													.threads(1)
													.logger(logger)
													.debug(logger.debug())
													.build();

											try {
												renamer.run(inputFile.toFile(), destination.toFile());
											} catch (Throwable var13) {
												if (renamer != null) {
													try {
														renamer.close();
													} catch (Throwable var12) {
														var13.addSuppressed(var12);
													}
												}

												throw var13;
											}

											if (renamer != null) {
												renamer.close();
											}
										} catch (Exception var15x) {
											throw new RuntimeException("Failed to remap plugin jar '" + inputFile + "'", var15x);
										}

										LOGGER.info(
												"Done remapping {} '{}' in {}ms.", new Object[]{library ? "library" : "plugin", inputFile, System.currentTimeMillis() - start}
												);
										return destination;
									},
									this.threadPool
									);
				}

				if (DEBUG_LOGGING) {
					LOGGER.info("Plugin '{}' is a Paper plugin with no namespace specified.", inputFile);
				}

				index.skip(inputFile);
				var18 = CompletableFuture.completedFuture(inputFile);
			}

			return var18;
		} catch (IOException var16) {
			return CompletableFuture.failedFuture(new RuntimeException("Failed to open plugin jar " + inputFile, var16));
		}
	}

	private IMappingFile mappings() {
		CompletableFuture<IMappingFile> mappings = this.reversedMappings;
		return mappings == null
				? this.reversedMappingsFuture(
						() -> CompletableFuture.supplyAsync(PluginRemapper::loadReobfMappings, Runnable::run), this.remappedPlugins.dir(), Runnable::run
						)
						.join()
						: mappings.join();
	}

	private static IMappingFile loadReobfMappings() {
		return loadMappings("Reobf", MappingEnvironment.mappingsStream());
	}

	private static IMappingFile loadMappings(String name, InputStream stream) {
		try {
			InputStream ex = stream;

			IMappingFile var6;
			try {
				if (DEBUG_LOGGING) {
					LOGGER.info("Loading {} mappings...", name);
				}

				long start = System.currentTimeMillis();
				IMappingFile load = IMappingFile.load(stream);
				if (DEBUG_LOGGING) {
					LOGGER.info("Done loading {} mappings in {}ms.", name, System.currentTimeMillis() - start);
				}

				var6 = load;
			} catch (Throwable var8) {
				if (stream != null) {
					try {
						ex.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (stream != null) {
				stream.close();
			}

			return var6;
		} catch (IOException var9) {
			throw new RuntimeException("Failed to load " + name + " mappings", var9);
		}
	}

	static List<Path> list(Path dir, Predicate<Path> filter) {
		try {
			List var3;
			try (Stream<Path> stream = Files.list(dir)) {
				var3 = stream.filter(filter).toList();
			}

			return var3;
		} catch (IOException var7) {
			throw new RuntimeException("Failed to list directory '" + dir + "'", var7);
		}
	}

	private static List<Path> waitForAll(List<CompletableFuture<Path>> tasks) {
		ExceptionCollector<Exception> collector = new ExceptionCollector<>();
		List<Path> ret = new ArrayList<>();

		for (CompletableFuture<Path> task : tasks) {
			try {
				ret.add(task.join());
			} catch (CompletionException var7) {
				collector.add(var7);
			}
		}

		try {
			collector.throwIfPresent();
		} catch (Exception var6) {
			LOGGER.error("Encountered exception remapping plugins", var6);
		}

		return ret;
	}

	private static ThreadPoolExecutor createThreadPool() {
		return new ThreadPoolExecutor(
				0,
				4,
				5L,
				TimeUnit.SECONDS,
				ScalingThreadPool.createUnboundedQueue(),
				new ThreadFactoryBuilder()
				.setNameFormat("Paper Plugin Remapper Thread - %1$d")
				.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER))
				.build(),
				ScalingThreadPool.defaultReEnqueuePolicy()
				);
	}

}