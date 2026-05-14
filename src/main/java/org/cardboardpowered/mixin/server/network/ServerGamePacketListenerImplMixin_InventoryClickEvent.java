package org.cardboardpowered.mixin.server.network;

import org.bukkit.inventory.InventoryView;
import org.cardboardpowered.bridge.world.inventory.AbstractContainerMenuBridge;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"InventoryClickEvent", "CraftItemEvent"})
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 800)
public class ServerGamePacketListenerImplMixin_InventoryClickEvent {

    @Shadow 
    public ServerPlayer player;

    private boolean doCl = false;
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"), method = "handleContainerClick")
    public void doBukkitEvent_InventoryClickedEvent_skipOriginalProcess(AbstractContainerMenu handler, int i, int j, net.minecraft.world.inventory.ClickType actionType, Player playerEntity) {
        //
        if (doCl) handler.clicked(i, j, actionType, playerEntity);
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", 
            shift = At.Shift.BEFORE), method = "handleContainerClick", cancellable = true)
    public void doBukkitEvent_InventoryClickedEvent(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if(packet.slotNum() < -1 && packet.slotNum() != -999)
            return;

        this.doCl = false;
        InventoryView inventory = ((AbstractContainerMenuBridge) player.containerMenu).getBukkitView();

        InventoryType.SlotType type = inventory.getSlotType(packet.slotNum());

        InventoryClickEvent event;
        ClickType click = ClickType.UNKNOWN;
        InventoryAction action = InventoryAction.UNKNOWN;

        switch (packet.clickType()) {
            case PICKUP:
                click = packet.buttonNum() == 0 ? ClickType.LEFT : (packet.buttonNum() == 1 ? ClickType.RIGHT : ClickType.UNKNOWN);

                if(packet.buttonNum() == 0 || packet.buttonNum() == 1) {
                    action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                    if(packet.slotNum() == -999) {
                        if(!player.containerMenu.getCarried().isEmpty())
                            action = packet.buttonNum() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                    } else if(packet.slotNum() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.containerMenu.getSlot(packet.slotNum());
                        if(slot != null) {
                            ItemStack clickedItem = slot.getItem();
                            ItemStack cursor = player.containerMenu.getCarried();
                            if(clickedItem.isEmpty()) {
                                if(!cursor.isEmpty()) {
                                    action = packet.buttonNum() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                }
                            } else if(slot.mayPickup(player)) {
                                if(cursor.isEmpty()) {
                                    action = packet.buttonNum() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                } else if(slot.mayPlace(cursor)) {
                                	// 1.19.2: isItemEqualIgnoreDamage
                                	// 1.19.4: isItemEqual
                                    if(clickedItem.isSameItem(clickedItem, cursor) && ItemStack.matches(clickedItem, cursor)) { // Banner TODO
                                        int toPlace = packet.buttonNum() == 0 ? cursor.getCount() : 1;
                                        toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.getCount());
                                        toPlace = Math.min(toPlace, slot.container.getMaxStackSize() - clickedItem.getCount());
                                        if(toPlace == 1) {
                                            action = InventoryAction.PLACE_ONE;
                                        } else if(toPlace == cursor.getCount()) {
                                            action = InventoryAction.PLACE_ALL;
                                        } else if(toPlace < 0) {
                                            action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                                        } else if(toPlace != 0)
                                            action = InventoryAction.PLACE_SOME;
                                    } else if(cursor.getCount() < slot.getMaxStackSize()) {
                                        action = InventoryAction.SWAP_WITH_CURSOR;
                                    } else if(cursor.getItem() == clickedItem.getItem() && ItemStack.matches(cursor ,clickedItem)) {
                                        if(clickedItem.getCount() >= 0)
                                            if(clickedItem.getCount() + cursor.getCount() <= cursor.getMaxStackSize())
                                                action = InventoryAction.PICKUP_ALL;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case QUICK_MOVE:
                if(packet.buttonNum() == 0) {
                    click = ClickType.LEFT;
                } else if(packet.buttonNum() == 1) {
                    click = ClickType.RIGHT;
                }
                if(packet.buttonNum() == 0 || packet.buttonNum() == 1) {
                    if(packet.slotNum() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.containerMenu.getSlot(packet.slotNum());
                        if(slot != null && slot.mayPickup(this.player) && slot.hasItem()) {
                            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
                break;
            case SWAP:
                if((packet.buttonNum() >= 0 && packet.buttonNum() <= 9) || packet.buttonNum() == 40) {
                    click = (packet.buttonNum() == 40) ? ClickType.SWAP_OFFHAND : ClickType.NUMBER_KEY;
                    Slot clickedSlot = this.player.containerMenu.getSlot(packet.slotNum());
                    if(clickedSlot != null && clickedSlot.mayPickup(player)) {
                        ItemStack hotbar = this.player.inventory.getItem(packet.buttonNum());
                        boolean canCleanSwap = hotbar.isEmpty() || (clickedSlot.container == player.inventory); // the slot will accept the hotbar item
                        if(clickedSlot.hasItem()) {
                            if(canCleanSwap) {
                                action = InventoryAction.HOTBAR_SWAP;
                            } else {
                                action = InventoryAction.HOTBAR_MOVE_AND_READD;
                            }
                        } else if(!clickedSlot.hasItem() && !hotbar.isEmpty() && clickedSlot.mayPlace(hotbar)) {
                            action = InventoryAction.HOTBAR_SWAP;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else {
                        action = InventoryAction.NOTHING;
                    }
                }
                break;
            case CLONE:
                if(packet.buttonNum() == 2) {
                    click = ClickType.MIDDLE;
                    if(packet.slotNum() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.containerMenu.getSlot(packet.slotNum());
                        if(slot != null && slot.hasItem() && player.abilities.instabuild && player.containerMenu.getCarried().isEmpty()) {
                            action = InventoryAction.CLONE_STACK;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    click = ClickType.UNKNOWN;
                    action = InventoryAction.UNKNOWN;
                }
                break;
            case THROW:
                if(packet.slotNum() >= 0) {
                    if(packet.buttonNum() == 0) {
                        click = ClickType.DROP;
                        Slot slot = this.player.containerMenu.getSlot(packet.slotNum());
                        if(slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                            action = InventoryAction.DROP_ONE_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else if(packet.buttonNum() == 0) {
                        click = ClickType.DROP;
                        Slot slot = this.player.containerMenu.getSlot(packet.slotNum());
                        if(slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                            action = InventoryAction.DROP_ALL_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    click = ClickType.LEFT;
                    if(packet.buttonNum() == 1) {
                        click = ClickType.RIGHT;
                    }
                    action = InventoryAction.NOTHING;
                }
                break;
            case QUICK_CRAFT:
                this.player.containerMenu.clicked(packet.slotNum(), packet.buttonNum(), packet.clickType(), this.player);
                break;
            case PICKUP_ALL:
                click = ClickType.DOUBLE_CLICK;
                action = InventoryAction.NOTHING;
                if(packet.slotNum() >= 0 && !this.player.containerMenu.getCarried().isEmpty()) {
                    ItemStack cursor = this.player.containerMenu.getCarried();
                    action = InventoryAction.NOTHING;
                    // Quick check for if we have any of the item
                    if(inventory.getTopInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem())) || inventory.getBottomInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem()))) {
                        action = InventoryAction.COLLECT_TO_CURSOR;
                    }
                }
                break;
            default:
                break;
        }
        if(packet.clickType() != net.minecraft.world.inventory.ClickType.QUICK_CRAFT) {
            if(click == ClickType.NUMBER_KEY) {
                event = new InventoryClickEvent(inventory, type, packet.slotNum(), click, action, packet.buttonNum());
            } else {
                event = new InventoryClickEvent(inventory, type, packet.slotNum(), click, action);
            }

            Inventory top = inventory.getTopInventory();
            if(packet.slotNum() == 0 && top instanceof org.bukkit.inventory.CraftingInventory) {
                org.bukkit.inventory.Recipe recipe = ((org.bukkit.inventory.CraftingInventory) top).getRecipe();
                if(recipe != null) {
                    if(click == ClickType.NUMBER_KEY) {
                        event = new CraftItemEvent(recipe, inventory, type, packet.slotNum(), click, action, packet.buttonNum());
                    } else {
                        event = new CraftItemEvent(recipe, inventory, type, packet.slotNum(), click, action);
                    }
                }
            }

            event.setCancelled(player.isSpectator());
            AbstractContainerMenu oldContainer = player.containerMenu;
            Bukkit.getServer().getPluginManager().callEvent(event);
            if(player.containerMenu != oldContainer) {
                ci.cancel();
                return;
            }

            switch (event.getResult()) {
                case ALLOW:
                case DEFAULT:
                    this.doCl = true;
                    break;
                case DENY:
                    // [DELETED COMMENTS FROM BUKKIT]
                    switch (action) {
                        // Modified other slots
                        case PICKUP_ALL:
                        case MOVE_TO_OTHER_INVENTORY:
                        case HOTBAR_MOVE_AND_READD:
                        case HOTBAR_SWAP:
                        case COLLECT_TO_CURSOR:
                        case UNKNOWN:
                            player.containerMenu.sendAllDataToRemote();
                            break;
                        // Modified cursor and clicked
                        case PICKUP_SOME:
                        case PICKUP_HALF:
                        case PICKUP_ONE:
                        case PLACE_ALL:
                        case PLACE_SOME:
                        case PLACE_ONE:
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.containerMenu.containerId, packet.slotNum(), this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getSlot(packet.slotNum()).getItem()));
                            break;
                        // Modified clicked only
                        case DROP_ALL_SLOT:
                        case DROP_ONE_SLOT:
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.containerMenu.containerId, packet.slotNum(), this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getSlot(packet.slotNum()).getItem()));
                            break;
                        // Modified cursor only
                        case DROP_ALL_CURSOR:
                        case DROP_ONE_CURSOR:
                        case CLONE_STACK:
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                            break;
                        case NOTHING:
                        default:
                            break;
                    }
                    ci.cancel();
                    return;
            }
            if(event instanceof CraftItemEvent) player.containerMenu.sendAllDataToRemote();
        }
    }

}
