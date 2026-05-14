package com.destroystokyo.paper;

import com.google.common.base.Objects;
import java.util.StringJoiner;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PaperSkinParts implements SkinParts {

	private final int raw;

	public PaperSkinParts(int raw) {
		this.raw = raw;
	}

	private static boolean isPartShown(int raw, PlayerModelPart part) {
		return ((byte) raw & part.getMask()) == part.getMask();
	}

	private static int setPartShown(int raw, PlayerModelPart part, boolean shown) {
		if (shown) {
			raw |= part.getMask();
		} else {
			raw &= ~part.getMask();
		}

		return raw;
	}

	public boolean hasCapeEnabled() {
		return isPartShown(this.raw, PlayerModelPart.CAPE);
	}

	public boolean hasJacketEnabled() {
		return isPartShown(this.raw, PlayerModelPart.JACKET);
	}

	public boolean hasLeftSleeveEnabled() {
		return isPartShown(this.raw, PlayerModelPart.LEFT_SLEEVE);
	}

	public boolean hasRightSleeveEnabled() {
		return isPartShown(this.raw, PlayerModelPart.RIGHT_SLEEVE);
	}

	public boolean hasLeftPantsEnabled() {
		return isPartShown(this.raw, PlayerModelPart.LEFT_PANTS_LEG);
	}

	public boolean hasRightPantsEnabled() {
		return isPartShown(this.raw, PlayerModelPart.RIGHT_PANTS_LEG);
	}

	public boolean hasHatsEnabled() {
		return isPartShown(this.raw, PlayerModelPart.HAT);
	}

	public int getRaw() {
		return this.raw;
	}

	public com.destroystokyo.paper.SkinParts.Mutable mutableCopy() {
		return new PaperSkinParts.Mutable(this.raw);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && this.getClass() == o.getClass()) {
			PaperSkinParts that = (PaperSkinParts) o;
			return this.raw == that.raw;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(new Object[] { this.raw });
	}

	@Override
	public String toString() {
		return toString(this, "PaperSkinParts");
	}

	private static String toString(SkinParts parts, String name) {
		return new StringJoiner(", ", name + "[", "]").add("raw=" + parts.getRaw())
				.add("cape=" + parts.hasCapeEnabled()).add("jacket=" + parts.hasJacketEnabled())
				.add("leftSleeve=" + parts.hasLeftSleeveEnabled()).add("rightSleeve=" + parts.hasRightSleeveEnabled())
				.add("leftPants=" + parts.hasLeftPantsEnabled()).add("rightPants=" + parts.hasRightPantsEnabled())
				.add("hats=" + parts.hasHatsEnabled()).toString();
	}

	public static final class Mutable implements com.destroystokyo.paper.SkinParts.Mutable {
		private int raw;

		public Mutable(int raw) {
			this.raw = raw;
		}

		public void setCapeEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.CAPE, enabled);
		}

		public void setJacketEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.JACKET, enabled);
		}

		public void setLeftSleeveEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.LEFT_SLEEVE, enabled);
		}

		public void setRightSleeveEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.RIGHT_SLEEVE, enabled);
		}

		public void setLeftPantsEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.LEFT_PANTS_LEG, enabled);
		}

		public void setRightPantsEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.RIGHT_PANTS_LEG, enabled);
		}

		public void setHatsEnabled(boolean enabled) {
			this.raw = PaperSkinParts.setPartShown(this.raw, PlayerModelPart.HAT, enabled);
		}

		public boolean hasCapeEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.CAPE);
		}

		public boolean hasJacketEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.JACKET);
		}

		public boolean hasLeftSleeveEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.LEFT_SLEEVE);
		}

		public boolean hasRightSleeveEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.RIGHT_SLEEVE);
		}

		public boolean hasLeftPantsEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.LEFT_PANTS_LEG);
		}

		public boolean hasRightPantsEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.RIGHT_PANTS_LEG);
		}

		public boolean hasHatsEnabled() {
			return PaperSkinParts.isPartShown(this.raw, PlayerModelPart.HAT);
		}

		public int getRaw() {
			return this.raw;
		}

		public SkinParts immutableCopy() {
			return new PaperSkinParts(this.raw);
		}

		public com.destroystokyo.paper.SkinParts.Mutable mutableCopy() {
			return new PaperSkinParts.Mutable(this.raw);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o != null && this.getClass() == o.getClass()) {
				PaperSkinParts.Mutable that = (PaperSkinParts.Mutable) o;
				return this.raw == that.raw;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(new Object[] { this.raw });
		}

		@Override
		public String toString() {
			return PaperSkinParts.toString(this, "PaperSkinParts.Mutable");
		}
	}

}