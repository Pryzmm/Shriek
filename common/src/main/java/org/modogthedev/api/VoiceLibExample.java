package org.modogthedev.api;

import org.modogthedev.VoiceLib;

public class VoiceLibExample {
    public static void init() {
        VoiceLibApi.registerServerPlayerSpeechListener((serverPlayerTalkEvent -> {
            System.out.println(serverPlayerTalkEvent.getText());
            if (VoiceLib.exampleEnabled && serverPlayerTalkEvent.getText().contains("ouch"))
                serverPlayerTalkEvent.getPlayer().igniteForSeconds(2);
        }));
    }
}
