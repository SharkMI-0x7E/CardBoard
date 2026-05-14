package org.bukkit.craftbukkit.packs;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftFeatureFlag;
import org.bukkit.craftbukkit.util.CraftChatMessage;
// import org.bukkit.packs.DataPack;

import me.isaiah.common.ICommonMod;
import me.isaiah.common.IDatapack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;

/**
 * Removed in Paper 1.21.9+
 */
@Deprecated
public class CraftDataPack /* implements DataPack */ {

    private final Pack handle;
    private PackMetadataSection resourcePackInfo;
    
    private IDatapack ipack;

    public CraftDataPack(Pack handler) {
        this.handle = handler;
        
        this.ipack = ICommonMod.getIServer().get_datapack(handler);
        this.resourcePackInfo = ipack.get_metadata();

        /*try (ResourcePack pack = this.handle.packFactory.open(this.handle.getId())) {
        	this.resourcePackInfo = pack.parseMetadata(PackResourceMetadata.SERIALIZER);
        } catch (IOException e) { // This is already called in NMS then if in NMS not happen is secure this not throw here
        	throw new RuntimeException(e);
        }*/
    }
    
    public IDatapack asICommon() {
    	return ipack;
    }

    public Pack getHandle() {
        return this.handle;
    }

    public String getRawId() {
    	return this.asICommon().get_raw_id();
    }

    // @Override
    public String getTitle() {
        return CraftChatMessage.fromComponent(this.getHandle().getTitle());
    }

    // @Override
    public String getDescription() {
        return CraftChatMessage.fromComponent(this.getHandle().getDescription());
    }

    // @Override
    public int getPackFormat() {
    	return resourcePackInfo.supportedFormats().maxInclusive().major();
    	// TODO
        // return resourcePackInfo.packFormat();
    }

    // @Override
    public boolean isRequired() {
        return this.asICommon().is_required();
    }

    // @Override
    /*
    public DataPack.Compatibility getCompatibility() {
        return switch (this.getHandle().getCompatibility()) {
            default -> throw new IncompatibleClassChangeError();
            case COMPATIBLE -> DataPack.Compatibility.COMPATIBLE;
            case TOO_NEW -> DataPack.Compatibility.NEW;
            case TOO_OLD -> DataPack.Compatibility.OLD;
        };
    }
    */

    // @Override
    public boolean isEnabled() {
    	return this.asICommon().is_enabled();
    }

    // @Override
    /*
    public DataPack.Source getSource() {
        if (this.getHandle().getSource() == ResourcePackSource.BUILTIN) {
            return DataPack.Source.BUILT_IN;
        }
        if (this.getHandle().getSource() == ResourcePackSource.FEATURE) {
            return DataPack.Source.FEATURE;
        }
        if (this.getHandle().getSource() == ResourcePackSource.WORLD) {
            return DataPack.Source.WORLD;
        }
        if (this.getHandle().getSource() == ResourcePackSource.SERVER) {
            return DataPack.Source.SERVER;
        }
        return DataPack.Source.DEFAULT;
    }
    */

    // @Override
    public Set<FeatureFlag> getRequestedFeatures() {
        return CraftFeatureFlag.getFromNMS(this.getHandle().getRequestedFeatures()).stream().map(FeatureFlag.class::cast).collect(Collectors.toUnmodifiableSet());
    }

    // @Override
    public NamespacedKey getKey() {
        return NamespacedKey.fromString((String)this.getRawId());
    }

    // @Override
    public String toString() {
        String requestedFeatures = this.getRequestedFeatures().stream().map(featureFlag -> featureFlag.getKey().toString()).collect(Collectors.joining(","));
        return "CraftDataPack{rawId=" + this.getRawId() + ",id=" + this.getKey() + ",title=" + this.getTitle() + ",description=" + this.getDescription() + ",packformat=" + this.getPackFormat() + ",compatibility=" + "voidptr" + ",source=" + "voidptr" + ",enabled=" + this.isEnabled() + ",requestedFeatures=[" + requestedFeatures + "]}";
    }

    // 1.20.2 API:
    
	// @Override
	public int getMinSupportedPackFormat() {
		
		return this.resourcePackInfo.supportedFormats().minInclusive().major();
		
		// return this.resourcePackInfo.supportedFormats().orElse(new Range<Integer>(this.getPackFormat())).minInclusive();
	}

	// @Override
	public int getMaxSupportedPackFormat() {
		
		return this.resourcePackInfo.supportedFormats().maxInclusive().major();
		
		// return this.resourcePackInfo.supportedFormats().orElse(new Range<Integer>(this.getPackFormat())).maxInclusive();
	}
}

