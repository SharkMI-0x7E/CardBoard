/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.impl;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.time.Instant;
import java.util.Date;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.bukkit.BanEntry;

public final class CraftProfileBanEntry implements BanEntry<PlayerProfile> {
   private static final Date minorDate = Date.from(Instant.parse("1899-12-31T04:00:00Z"));
   private final UserBanList list;
   private final NameAndId profile;
   private Date created;
   private String source;
   private Date expiration;
   private String reason;

   public CraftProfileBanEntry(NameAndId profile, UserBanListEntry entry, UserBanList list) {
      this.list = list;
      this.profile = profile;
      this.created = entry.getCreated() != null ? new Date(entry.getCreated().getTime()) : null;
      this.source = entry.getSource();
      this.expiration = entry.getExpires() != null ? new Date(entry.getExpires().getTime()) : null;
      this.reason = entry.getReason();
   }

   public String getTarget() {
      return this.profile.name();
   }

   public PlayerProfile getBanTarget() {
      return new CraftPlayerProfile(this.profile);
   }

   public Date getCreated() {
      return this.created == null ? null : (Date)this.created.clone();
   }

   public void setCreated(Date created) {
      this.created = created;
   }

   public String getSource() {
      return this.source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public Date getExpiration() {
      return this.expiration == null ? null : (Date)this.expiration.clone();
   }

   public void setExpiration(Date expiration) {
      if (expiration != null && expiration.getTime() == minorDate.getTime()) {
         expiration = null;
      }

      this.expiration = expiration;
   }

   public String getReason() {
      return this.reason;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public void save() {
      UserBanListEntry entry = new UserBanListEntry(this.profile, this.created, this.source, this.expiration, this.reason);
      this.list.add(entry);
   }

   public void remove() {
      this.list.remove(this.profile);
   }
}
