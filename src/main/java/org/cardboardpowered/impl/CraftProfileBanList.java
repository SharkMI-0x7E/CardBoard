package org.cardboardpowered.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.SharedPlayerProfile;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.bukkit.BanEntry;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.craftbukkit.CraftServer;

public class CraftProfileBanList implements ProfileBanList {

   private final UserBanList list;

   public CraftProfileBanList(UserBanList list) {
      this.list = list;
   }

   public BanEntry<PlayerProfile> getBanEntry(String target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      return this.getBanEntry(getProfile(target));
   }

   public BanEntry<PlayerProfile> getBanEntry(org.bukkit.profile.PlayerProfile target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      return this.getBanEntry(new NameAndId(((SharedPlayerProfile)target).buildGameProfile()));
   }

   public BanEntry<PlayerProfile> getBanEntry(PlayerProfile target) {
      Preconditions.checkArgument(target != null, "target cannot be null");
      return this.getBanEntry(new NameAndId(((SharedPlayerProfile)target).buildGameProfile()));
   }

   public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Date expires, String source) {
      Preconditions.checkArgument(target != null, "PlayerProfile cannot be null");
      Preconditions.checkArgument(target.getId() != null, "The PlayerProfile UUID cannot be null");
      return this.addBan(new NameAndId(((SharedPlayerProfile)target).buildGameProfile()), reason, expires, source);
   }

   public boolean isBanned(PlayerProfile target) {
      return this.isBanned((SharedPlayerProfile)target);
   }

   public void pardon(PlayerProfile target) {
      this.pardon((SharedPlayerProfile)target);
   }

   public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Instant expires, String source) {
      Date date = expires != null ? Date.from(expires) : null;
      return this.addBan(target, reason, date, source);
   }

   public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Duration duration, String source) {
      Instant instant = duration != null ? Instant.now().plus(duration) : null;
      return this.addBan(target, reason, instant, source);
   }

   public BanEntry<PlayerProfile> addBan(String target, String reason, Date expires, String source) {
      Preconditions.checkArgument(target != null, "Ban target cannot be null");
      return this.addBan(getProfileByName(target), reason, expires, source);
   }

   public BanEntry<PlayerProfile> addBan(org.bukkit.profile.PlayerProfile target, String reason, Date expires, String source) {
      Preconditions.checkArgument(target != null, "PlayerProfile cannot be null");
      Preconditions.checkArgument(target.getUniqueId() != null, "The PlayerProfile UUID cannot be null");
      return this.addBan(new NameAndId(((SharedPlayerProfile)target).buildGameProfile()), reason, expires, source);
   }

   public BanEntry<PlayerProfile> addBan(org.bukkit.profile.PlayerProfile target, String reason, Instant expires, String source) {
      Date date = expires != null ? Date.from(expires) : null;
      return this.addBan(target, reason, date, source);
   }

   public BanEntry<PlayerProfile> addBan(org.bukkit.profile.PlayerProfile target, String reason, Duration duration, String source) {
      Instant instant = duration != null ? Instant.now().plus(duration) : null;
      return this.addBan(target, reason, instant, source);
   }

   public Set<BanEntry> getBanEntries() {
      Builder<BanEntry> builder = ImmutableSet.builder();

      for (UserBanListEntry entry : this.list.getEntries()) {
         NameAndId profile = entry.getUser();
         builder.add(new CraftProfileBanEntry(profile, entry, this.list));
      }

      return builder.build();
   }

   public Set<BanEntry<PlayerProfile>> getEntries() {
      Builder<BanEntry<PlayerProfile>> builder = ImmutableSet.builder();

      for (UserBanListEntry entry : this.list.getEntries()) {
         NameAndId profile = entry.getUser();
         builder.add(new CraftProfileBanEntry(profile, entry, this.list));
      }

      return builder.build();
   }

   public boolean isBanned(org.bukkit.profile.PlayerProfile target) {
      return this.isBanned((SharedPlayerProfile)target);
   }

   private boolean isBanned(SharedPlayerProfile target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      return this.isBanned(new NameAndId(target.buildGameProfile()));
   }

   public boolean isBanned(String target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      return this.isBanned(getProfile(target));
   }

   public void pardon(org.bukkit.profile.PlayerProfile target) {
      this.pardon((SharedPlayerProfile)target);
   }

   private void pardon(SharedPlayerProfile target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      this.pardon(new NameAndId(target.buildGameProfile()));
   }

   public void pardon(String target) {
      Preconditions.checkArgument(target != null, "Target cannot be null");
      this.pardon(getProfile(target));
   }

   public BanEntry<PlayerProfile> getBanEntry(NameAndId profile) {
      if (profile == null) {
         return null;
      } else {
         UserBanListEntry entry = this.list.get(profile);
         return entry == null ? null : new CraftProfileBanEntry(profile, entry, this.list);
      }
   }

   public BanEntry<PlayerProfile> addBan(NameAndId profile, String reason, Date expires, String source) {
      if (profile == null) {
         return null;
      } else {
         UserBanListEntry entry = new UserBanListEntry(
            profile, new Date(), source != null && !source.isBlank() ? source : null, expires, reason != null && !reason.isBlank() ? reason : null
         );
         this.list.add(entry);
         return new CraftProfileBanEntry(profile, entry, this.list);
      }
   }

   private void pardon(NameAndId profile) {
      this.list.remove(profile);
   }

   private boolean isBanned(NameAndId profile) {
      return profile != null && this.list.isBanned(profile);
   }

   static NameAndId getProfile(String target) {
      UUID uuid = null;

      try {
         uuid = UUID.fromString(target);
      } catch (IllegalArgumentException var3) {
      }

      return uuid != null ? getProfileByUUID(uuid) : getProfileByName(target);
   }

   static NameAndId getProfileByUUID(UUID uuid) {
	   MinecraftServer server = CraftServer.server;
      return server != null ? server.services().nameToIdCache().get(uuid).orElse(null) : null;
   }

   static NameAndId getProfileByName(String name) {
	   MinecraftServer server = CraftServer.server;
      return server != null ? server.services().nameToIdCache().get(name).orElse(null) : null;
   }

}