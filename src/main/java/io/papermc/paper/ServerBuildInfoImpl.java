package io.papermc.paper;

// import com.github.bsideup.jabel.Desugar;
import com.google.common.base.Strings;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.util.JarManifests;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.jar.Manifest;
import net.kyori.adventure.key.Key;
import net.minecraft.SharedConstants;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;

// @Desugar
public record ServerBuildInfoImpl(
		Key brandId,
		String brandName,
		String minecraftVersionId,
		String minecraftVersionName,
		OptionalInt buildNumber,
		Instant buildTime,
		Optional<String> gitBranch,
		Optional<String> gitCommit
	) implements ServerBuildInfo
{
    private static final String ATTRIBUTE_BRAND_ID = "Brand-Id";
    private static final String ATTRIBUTE_BRAND_NAME = "Brand-Name";
    private static final String ATTRIBUTE_BUILD_TIME = "Build-Time";
    private static final String ATTRIBUTE_BUILD_NUMBER = "Build-Number";
    private static final String ATTRIBUTE_GIT_BRANCH = "Git-Branch";
    private static final String ATTRIBUTE_GIT_COMMIT = "Git-Commit";
    private static final String BRAND_PAPER_NAME = "Paper";
    private static final String BUILD_DEV = "DEV";

    public ServerBuildInfoImpl() {
        this(null);
    	// this(JarManifests.manifest(CraftServer.class));
    }

    private ServerBuildInfoImpl(Manifest manifest) {
    	this(
    			Key.key("papermc:paper"),
    			"Paper",
    			SharedConstants.getCurrentVersion().id(),
    			SharedConstants.getCurrentVersion().name(),
    			OptionalInt.of(15),
    			Instant.parse("2025-07-01T22:53:44.086687487Z"),
    			Optional.of("main"),
    			Optional.of("0cadaef")
    	);
    }

	public boolean isBrandCompatible(Key brandId) {
        return brandId.equals(this.brandId);
    }

    @NotNull
    public String asString(@NotNull ServerBuildInfo.StringRepresentation representation) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.minecraftVersionId);
        sb.append('-');
        if (this.buildNumber.isPresent()) {
            sb.append(this.buildNumber.getAsInt());
        } else {
            sb.append(BUILD_DEV);
        }
        boolean hasGitBranch = this.gitBranch.isPresent();
        boolean hasGitCommit = this.gitCommit.isPresent();
        if (hasGitBranch || hasGitCommit) {
            sb.append('-');
        }
        if (hasGitBranch && representation == ServerBuildInfo.StringRepresentation.VERSION_FULL) {
            sb.append(this.gitBranch.get());
            if (hasGitCommit) {
                sb.append('@');
            }
        }
        if (hasGitCommit) {
            sb.append(this.gitCommit.get());
        }
        if (representation == ServerBuildInfo.StringRepresentation.VERSION_FULL) {
            sb.append(' ');
            sb.append('(');
            sb.append(this.buildTime.truncatedTo(ChronoUnit.SECONDS));
            sb.append(')');
        }
        return sb.toString();
    }

    private static Optional<String> getManifestAttribute(Manifest manifest, String name) {
        String value = manifest != null ? manifest.getMainAttributes().getValue(name) : null;
        return Optional.ofNullable(Strings.emptyToNull((String)value));
    }
}

