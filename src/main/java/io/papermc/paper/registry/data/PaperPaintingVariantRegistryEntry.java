package io.papermc.paper.registry.data;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.registry.PaperRegistryBuilder;
import io.papermc.paper.registry.data.PaintingVariantRegistryEntry;
import io.papermc.paper.registry.data.util.Checks;
import io.papermc.paper.registry.data.util.Conversions;
import java.util.Optional;
import java.util.OptionalInt;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import org.bukkit.Art;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class PaperPaintingVariantRegistryEntry
implements PaintingVariantRegistryEntry {
    protected OptionalInt width = OptionalInt.empty();
    protected OptionalInt height = OptionalInt.empty();
    protected net.minecraft.network.chat.@Nullable Component title;
    protected net.minecraft.network.chat.@Nullable Component author;
    protected @Nullable Identifier assetId;
    protected final Conversions conversions;

    public PaperPaintingVariantRegistryEntry(Conversions conversions, @Nullable PaintingVariant internal) {
        this.conversions = conversions;
        if (internal == null) {
            return;
        }
        this.width = OptionalInt.of(internal.width());
        this.height = OptionalInt.of(internal.height());
        this.title = internal.title().orElse(null);
        this.author = internal.author().orElse(null);
        this.assetId = internal.assetId();
    }

    public @Range(from=1L, to=16L) int width() {
        return Checks.asConfigured(this.width, "width");
    }

    public @Range(from=1L, to=16L) int height() {
        return Checks.asConfigured(this.height, "height");
    }

    public @Nullable Component title() {
        return this.title == null ? null : this.conversions.asAdventure(this.title);
    }

    public @Nullable Component author() {
        return this.author == null ? null : this.conversions.asAdventure(this.author);
    }

    public Key assetId() {
        return PaperAdventure.asAdventure(Checks.asConfigured(this.assetId, "assetId"));
    }

    public static final class PaperBuilder
    extends PaperPaintingVariantRegistryEntry
    implements PaintingVariantRegistryEntry.Builder,
    PaperRegistryBuilder<PaintingVariant, Art> {
        public PaperBuilder(Conversions conversions, @Nullable PaintingVariant internal) {
            super(conversions, internal);
        }

        public PaintingVariantRegistryEntry.Builder width(@Range(from=1L, to=16L) int width) {
            this.width = OptionalInt.of(Checks.asArgumentRange(width, "width", 1, 16));
            return this;
        }

        public PaintingVariantRegistryEntry.Builder height(@Range(from=1L, to=16L) int height) {
            this.height = OptionalInt.of(Checks.asArgumentRange(height, "height", 1, 16));
            return this;
        }

        public PaintingVariantRegistryEntry.Builder title(@Nullable Component title) {
            this.title = this.conversions.asVanilla(title);
            return this;
        }

        public PaintingVariantRegistryEntry.Builder author(@Nullable Component author) {
            this.author = this.conversions.asVanilla(author);
            return this;
        }

        public PaintingVariantRegistryEntry.Builder assetId(Key assetId) {
            this.assetId = PaperAdventure.asVanilla(Checks.asArgument(assetId, "assetId"));
            return this;
        }

        @Override
        public PaintingVariant build() {
            return new PaintingVariant(this.width(), this.height(), Checks.asConfigured(this.assetId, "assetId"), Optional.ofNullable(this.title), Optional.ofNullable(this.author));
        }
    }
}

