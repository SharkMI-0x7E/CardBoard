package org.cardboardpowered.mixin.paper;

import org.cardboardpowered.impl.PaperServerInternalAPIBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io.papermc.paper.InternalAPIBridge;

@Mixin(InternalAPIBridge.class)
public interface PaperInternalAPIBridgeMixin {

	/**
     * Paper: Yields the instance of this API bridge by lazily requesting it from the java service loader API.
     * 
     * @author Cardboard
     * @reason Avoid Services
     *
     * TODO: Cannot replace with @ModifyReturnValue - this static method completely replaces
     * the original Paper service loader logic with direct access to PaperServerInternalAPIBridge.INSTANCE.
     */
	@Overwrite(remap = false)
    static InternalAPIBridge get() {
        /*
		class Holder {
        	public static final InternalAPIBridge INSTANCE = PaperServerInternalAPIBridge.INSTANCE; // Services.service(InternalAPIBridge.class).orElseThrow();
            // public static final InternalAPIBridge INSTANCE = Services.service(InternalAPIBridge.class).orElseThrow();
        }
        */

        return PaperServerInternalAPIBridge.INSTANCE;
        // return Holder.INSTANCE;
    }
	
}
