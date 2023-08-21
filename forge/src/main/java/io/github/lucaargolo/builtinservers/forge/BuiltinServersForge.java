package io.github.lucaargolo.builtinservers.forge;

import io.github.lucaargolo.builtinservers.BuiltinServers;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(BuiltinServers.MODID)
public class BuiltinServersForge {

    public BuiltinServersForge() {
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event){
        BuiltinServers.initializeClient(FMLPaths.CONFIGDIR.get(), null);

        BuiltinServers.onClientStarted(MinecraftClient.getInstance());
    }
}
