package org.cardboardpowered.impl;

import net.minecraft.server.players.UserBanList;

/**
 * @deprecated Use CraftProfileBanList
 */
@Deprecated
public class ProfileBanList extends CraftProfileBanList {

	public ProfileBanList(UserBanList list) {
		super(list);
	}
}