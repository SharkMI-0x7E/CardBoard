package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.world.item.trading.MerchantOfferBridge;

import java.util.List;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MerchantRecipe;

public interface CraftMerchant extends org.bukkit.inventory.Merchant {

	net.minecraft.world.item.trading.Merchant getMerchant();

	default List<MerchantRecipe> getRecipes() {
		return List.copyOf(Lists.transform(this.getMerchant().getOffers(), new Function<MerchantOffer, MerchantRecipe>() {
			public MerchantRecipe apply(MerchantOffer recipe) {
				return ((MerchantOfferBridge)recipe).asBukkit();
			}
		}));
	}

	default void setRecipes(List<MerchantRecipe> recipes) {
		MerchantOffers recipesList = this.getMerchant().getOffers();
		recipesList.clear();

		for (MerchantRecipe recipe : recipes) {
			recipesList.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
		}
	}

	default MerchantRecipe getRecipe(int i) {
		return ((MerchantOfferBridge)this.getMerchant().getOffers().get(i)).asBukkit();
	}

	default void setRecipe(int i, MerchantRecipe merchantRecipe) {
		this.getMerchant().getOffers().set(i, CraftMerchantRecipe.fromBukkit(merchantRecipe).toMinecraft());
	}

	default int getRecipeCount() {
		return this.getMerchant().getOffers().size();
	}

	default boolean isTrading() {
		return this.getTrader() != null;
	}

	default HumanEntity getTrader() {
		net.minecraft.world.entity.player.Player eh = this.getMerchant().getTradingPlayer();
		return eh == null ? null : (Player)((ServerPlayerBridge)eh).getBukkitEntity();
	}

	/*
    protected final Merchant merchant;

    public CraftMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    @Override
    public List<MerchantRecipe> getRecipes() {
        return Collections.unmodifiableList(Lists.transform(merchant.getOffers(), new Function<net.minecraft.village.TradeOffer, MerchantRecipe>() {
            @Override
            public MerchantRecipe apply(net.minecraft.village.TradeOffer recipe) {
                return ((IMixinTradeOffer)recipe).asBukkit();
            }
        }));
    }

    @Override
    public void setRecipes(List<MerchantRecipe> recipes) {
        TradeOfferList recipesList = merchant.getOffers();
        recipesList.clear();
        for (MerchantRecipe recipe : recipes)
            recipesList.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
    }

    @Override
    public MerchantRecipe getRecipe(int i) {
        return ((IMixinTradeOffer)merchant.getOffers().get(i)).asBukkit();
    }

    @Override
    public void setRecipe(int i, MerchantRecipe merchantRecipe) {
        merchant.getOffers().set(i, CraftMerchantRecipe.fromBukkit(merchantRecipe).toMinecraft());
    }

    @Override
    public int getRecipeCount() {
        return merchant.getOffers().size();
    }

    @Override
    public boolean isTrading() {
        return getTrader() != null;
    }

    @Override
    public HumanEntity getTrader() {
        PlayerEntity eh = merchant.getCustomer();
        return eh == null ? null : (Player)((IMixinServerEntityPlayer)eh).getBukkitEntity();
    }

    @Override
    public int hashCode() {
        return merchant.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof CraftMerchant && ((CraftMerchant) obj).merchant.equals(this.merchant);
    }

*/

}
