package org.cardboardpowered.mixin.server.players;

import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.cardboardpowered.bridge.server.players.CachedUserNameToIdResolverBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CachedUserNameToIdResolver.class)
public class CachedUserNameToIdResolverMixin implements CachedUserNameToIdResolverBridge {

	/*
    @Shadow private Map<UUID, Entry> byUuid;
    @Shadow private Map<String, Entry> byName;
    @Shadow private GameProfileRepository profileRepository;

    @Override
    public Optional<GameProfile> card_getByUuid(UUID uuid) {
        Entry entry = this.byUuid.get(uuid);
        if (entry == null)
            return Optional.empty();
        entry.setLastAccessed(this.incrementAndGetAccessCount());
        return Optional.of(entry.getProfile());
    }

    @Override
    public Optional<GameProfile> card_findByName(String name) {
        Optional<GameProfile> optional2 = null;
        String string = name.toLowerCase(Locale.ROOT);
        Entry entry = this.byName.get(string);
        boolean bl = false;
        if (entry != null && new Date().getTime() >= entry.getExpirationDate().getTime()) {
            this.byUuid.remove(entry.getProfile().getId());
            this.byName.remove(entry.getProfile().getName().toLowerCase(Locale.ROOT));
            bl = true;
            entry = null;
        }
        if (entry != null) {
            entry.setLastAccessed(this.incrementAndGetAccessCount());
            optional2 = Optional.of(entry.getProfile());
        } else {
            optional2 = card_findProfileByName(this.profileRepository, string);
            if (optional2.isPresent()) {
                server.getUserCache().add(optional2.get());
                bl = false;
            }
        }
        if (bl)
            server.getUserCache().save();
        return optional2;
    }
 
    private static Optional<PlayerConfigEntry> card_findProfileByName(GameProfileRepository repository, String name) {
        final AtomicReference<PlayerConfigEntry> atomicReference = new AtomicReference();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){
            public void onProfileLookupSucceeded(PlayerConfigEntry profile) {
                atomicReference.set(profile);
            }
            @Override
            public void onProfileLookupFailed(String profileName, Exception exception) {
                atomicReference.set(null);
            }
			@Override
			public void onProfileLookupSucceeded(String profileName, UUID profileId) {
				
			}
        };
        repository.findProfilesByNames(new String[]{name}, profileLookupCallback);
        PlayerConfigEntry gameProfile = (PlayerConfigEntry)atomicReference.get();
        if (!shouldUseRemote() && gameProfile == null) {
            
        	
        	// TODO: 1.19
        	// UUID uUID = DynamicSerializableUuid.getUuidFromProfile(new GameProfile((UUID)null, name));
        	UUID uUID = ICommonMod.getIServer().get_uuid_from_profile(new GameProfile((UUID)null, name));
        	
        	// 1.18: UUID uUID = PlayerEntity.getUuidFromProfile((GameProfile)new GameProfile(null, name));
            return Optional.of(new GameProfile(uUID, name));
        }

        return Optional.ofNullable(gameProfile);
    }

    @Shadow
    private static boolean shouldUseRemote() {
        return false;
    }

    @Shadow
    private long incrementAndGetAccessCount() {
        return 0;
    }
    */

}
