package com.pryzmm.api;

import com.pryzmm.Shriek;
import com.pryzmm.client.event.EventHandler;

import java.awt.*;

public class ShriekExample {
    public static void init() {
        EventHandler.loadVoskModel("vosk-model-small-en-us-0.15");
        ShriekApi.registerServerPlayerSpeechListener((serverPlayerTalkEvent -> {
            Shriek.LOGGER.info(serverPlayerTalkEvent.getText());
            if (Shriek.exampleEnabled && serverPlayerTalkEvent.getText().contains("one"))
                serverPlayerTalkEvent.getPlayer().igniteForSeconds(2);
        }));
    }
}
