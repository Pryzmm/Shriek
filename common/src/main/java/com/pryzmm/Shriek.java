package com.pryzmm;

import net.minecraft.resources.ResourceLocation;
import com.pryzmm.networking.ShriekPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Shriek {
    public static final String MOD_ID = "shriek";
    public static boolean exampleEnabled = false;

    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ResourceLocation id(String text) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID,text);
    }
    public static void init() {
        ShriekPackets.register();
        // Example (Since this is a library, and some players may not want this enabled by default, this has been commented out)
        // ShriekExample.init();
    }
}
