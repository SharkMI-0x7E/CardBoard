package org.cardboardpowered.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.players.IpBanListEntry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.InetAddresses;

public class IpBanList implements org.bukkit.BanList {

    private final net.minecraft.server.players.IpBanList list;

    public IpBanList(net.minecraft.server.players.IpBanList list) {
        this.list = list;
    }

    @Override
    public org.bukkit.BanEntry getBanEntry(String target) {
        IpBanListEntry entry = list.get(target);
        return (entry == null) ? null : new IpBanEntry(target, entry, list);
    }

    @Override
    public org.bukkit.BanEntry addBan(String target, String reason, Date expires, String source) {
        IpBanListEntry entry = new IpBanListEntry(target, new Date(), StringUtils.isBlank(source) ? null : source, expires, StringUtils.isBlank(reason) ? null : reason);
        list.add(entry);

        try {
            list.save();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned ips, " + ex.getMessage());
        }
        return new IpBanEntry(target, entry, list);
    }

    @Override
    public Set<org.bukkit.BanEntry> getBanEntries() {
        ImmutableSet.Builder<org.bukkit.BanEntry> builder = ImmutableSet.builder();
        for (String target : list.getUserList())
            builder.add(new IpBanEntry(target, Objects.requireNonNull(list.get(target)), list));
        return builder.build();
    }

    @Override
    public boolean isBanned(String target) {
        return list.isBanned(InetSocketAddress.createUnresolved(target, 0));
    }

    @Override
    public void pardon(String target) {
        list.remove(target);
    }

    public static class IpBanEntry implements BanEntry {

        private final net.minecraft.server.players.IpBanList list;
        private final String target;
        private Date created;
        private String source;
        private Date expiration;
        private String reason;

        public IpBanEntry(String target, IpBanListEntry entry, net.minecraft.server.players.IpBanList list) {
            this.list = list;
            this.target = target;
            this.created = null; // TODO Cardboard
            this.source = entry.getSource();
            this.expiration = entry.getExpires() != null ? new Date(entry.getExpires().getTime()) : null;
            this.reason = entry.getReason();
        }

        @Override
        public String getTarget() {
            return this.target;
        }

        @Override
        public Date getCreated() {
            return this.created == null ? null : (Date) this.created.clone();
        }

        @Override
        public void setCreated(Date created) {
            this.created = created;
        }

        @Override
        public String getSource() {
            return this.source;
        }

        @Override
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public Date getExpiration() {
            return this.expiration == null ? null : (Date) this.expiration.clone();
        }

        @SuppressWarnings("deprecation")
        @Override
        public void setExpiration(Date expiration) {
            if (expiration != null && expiration.getTime() == new Date(0,0,0,0,0,0).getTime()) expiration = null; // Forces "forever"
            this.expiration = expiration;
        }

        @Override
        public String getReason() {
            return this.reason;
        }

        @Override
        public void setReason(String reason) {
            this.reason = reason;
        }

        @Override
        public void save() {
            IpBanListEntry entry = new IpBanListEntry(target, this.created, this.source, this.expiration, this.reason);
            this.list.add(entry);
            try {
                this.list.save();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned ips json, " + ex.getMessage());
            }
        }

	    public InetAddress getBanTarget() {
	        return InetAddresses.forString((String)this.target);
	    }

		public void remove() {
			this.list.remove(this.target);
		}

    }

	@Override
	public @Nullable BanEntry getBanEntry(@NotNull Object target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable BanEntry addBan(@NotNull Object target, @Nullable String reason, @Nullable Date expires,
			@Nullable String source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable BanEntry addBan(@NotNull Object target, @Nullable String reason, @Nullable Instant expires,
			@Nullable String source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable BanEntry addBan(@NotNull Object target, @Nullable String reason, @Nullable Duration duration,
			@Nullable String source) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<BanEntry<InetAddress>> getEntries() {
        ImmutableSet.Builder<BanEntry<InetAddress>> builder = ImmutableSet.builder();
        for (String target : this.list.getUserList()) {
            IpBanListEntry ipBanEntry = this.list.get(target);
            if (ipBanEntry == null) continue;
            builder.add(new IpBanEntry(target, ipBanEntry, this.list));
        }
        return builder.build();
    }

	@Override
	public boolean isBanned(@NotNull Object target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pardon(@NotNull Object target) {
		// TODO Auto-generated method stub
		
	}
	
	

}