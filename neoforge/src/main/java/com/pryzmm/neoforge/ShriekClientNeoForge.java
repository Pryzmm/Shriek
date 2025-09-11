package com.pryzmm.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.pryzmm.ShriekClient;

@Mod(value = "shriek", dist = Dist.CLIENT)
public class ShriekClientNeoForge {
    public ShriekClientNeoForge(IEventBus modBus) {
        ShriekClient.init();
    }
}
