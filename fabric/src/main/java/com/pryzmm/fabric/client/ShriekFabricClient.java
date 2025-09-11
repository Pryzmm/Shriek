package com.pryzmm.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import com.pryzmm.ShriekClient;

public final class ShriekFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShriekClient.init();
    }
}
