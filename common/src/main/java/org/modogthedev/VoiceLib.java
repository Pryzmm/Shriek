package org.modogthedev;

import net.minecraft.resources.ResourceLocation;
import org.modogthedev.api.VoiceLibExample;
import org.modogthedev.networking.VoiceLibPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VoiceLib {
    public static final String MOD_ID = "voicelib";
    public static boolean exampleEnabled = false;

    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ResourceLocation id(String text) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID,text);
    }
    public static void init() {
        VoiceLibPackets.register();
        // Example (Since this is a library, and some players may not want this enabled by default, this has been commented out)
        // VoiceLibExample.init();
    }
}
