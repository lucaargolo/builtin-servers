package io.github.lucaargolo.builtinservers.fabric;

import io.github.lucaargolo.builtinservers.BuiltinServers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public class BuiltinServersFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BuiltinServers.initializeClient(
                FabricLoader.getInstance().getConfigDir(),
                c -> ClientLifecycleEvents.CLIENT_STARTED.register(c::accept)
        );
    }
}
