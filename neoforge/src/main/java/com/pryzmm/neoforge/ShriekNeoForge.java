package com.pryzmm.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.pryzmm.Shriek;

@Mod(Shriek.MOD_ID)
public final class ShriekNeoForge {
    public ShriekNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::clientSetup);
        Shriek.init();
    }
    public void clientSetup(FMLClientSetupEvent event) {
    }
}
