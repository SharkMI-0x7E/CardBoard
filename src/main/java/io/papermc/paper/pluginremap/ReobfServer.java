package io.papermc.paper.pluginremap;

import com.mojang.logging.LogUtils;
import io.papermc.paper.util.AtomicFiles;
import io.papermc.paper.util.MappingEnvironment;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.neoforged.art.api.Renamer;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.internal.RenamerImpl;
import net.neoforged.srgutils.IMappingFile;

import org.cardboardpowered.CardboardLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
final class ReobfServer {
	private static final Logger LOGGER = CardboardLogger.getSLF4J();// LogUtils.getClassLogger();
	private final Path remapClasspathDir;
	private final CompletableFuture<Void> load;

	ReobfServer(Path remapClasspathDir, CompletableFuture<IMappingFile> mappings, Executor executor) {
		this.remapClasspathDir = remapClasspathDir;
		if (this.mappingsChanged()) {
			this.load = mappings.thenAcceptAsync(this::remap, executor);
		} else {
			if (PluginRemapper.DEBUG_LOGGING) {
				LOGGER.info("Have cached reobf server for current mappings.");
			}

			this.load = CompletableFuture.completedFuture(null);
		}
	}

	CompletableFuture<Path> remapped() {
		return this.load.thenApply($ -> this.remappedPath());
	}

	private Path remappedPath() {
		return this.remapClasspathDir.resolve(MappingEnvironment.mappingsHash() + ".jar");
	}

	private boolean mappingsChanged() {
		return !Files.exists(this.remappedPath());
	}

	private void remap(IMappingFile mappings) {
		try {
			if (!Files.exists(this.remapClasspathDir)) {
				Files.createDirectories(this.remapClasspathDir);
			}

			for (Path file : PluginRemapper.list(this.remapClasspathDir, x$0 -> Files.isRegularFile(x$0))) {
				Files.delete(file);
			}
		} catch (IOException var10) {
			throw new RuntimeException(var10);
		}

		LOGGER.info("Remapping server...");
		long startRemap = System.currentTimeMillis();

		try (DebugLogger log = DebugLogger.forOutputFile(this.remappedPath())) {
			AtomicFiles.atomicWrite(
					this.remappedPath(),
					writeTo -> {
						RenamerImpl renamer = (RenamerImpl)Renamer.builder()
								.logger(log)
								.debug(log.debug())
								.threads(1)
								.add(Transformer.renamerFactory(mappings, false))
								.add(InsertManifestAttribute.addNamespaceManifestAttribute("spigot"))
								.build();

						try {
							renamer.run(serverJar().toFile(), writeTo.toFile(), true);
						} catch (Throwable var7) {
							if (renamer != null) {
								try {
									renamer.close();
								} catch (Throwable var6) {
									var7.addSuppressed(var6);
								}
							}

							throw var7;
						}

						if (renamer != null) {
							renamer.close();
						}
					}
					);
		} catch (Exception var9) {
			throw new RuntimeException("Failed to remap server jar", var9);
		}

		LOGGER.info("Done remapping server in {}ms.", System.currentTimeMillis() - startRemap);
	}

	private static Path serverJar() {
		try {
			
			Class cl = net.minecraft.server.level.ServerPlayer.class;
			
			Path path = Path.of(/*ReobfServer.class*/cl.getProtectionDomain().getCodeSource().getLocation().toURI());
			// System.out.println(path);
			// CardboardLogger.getSLF4J().info("serverJar: " + path);
			return path;
		} catch (URISyntaxException var1) {
			throw new RuntimeException(var1);
		}
	}
}
