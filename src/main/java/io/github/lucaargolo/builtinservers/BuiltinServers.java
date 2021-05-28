package io.github.lucaargolo.builtinservers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class BuiltinServers implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Builtin Servers");
    public static ModConfig CONFIG;

    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {

        Path configPath = FabricLoader.getInstance().getConfigDir();
        File configFile = new File(configPath + File.separator + "builtinservers.json");

        LOGGER.info("Trying to read servers file...");
        try {
            if (configFile.createNewFile()) {
                LOGGER.info("No servers file found, creating a new one...");
                String json = gson.toJson(parser.parse(gson.toJson(new ModConfig())));
                try (PrintWriter out = new PrintWriter(configFile)) {
                    out.println(json);
                }
                CONFIG = new ModConfig();
                LOGGER.info("Successfully created default servers file.");
            } else {
                LOGGER.info("A servers file was found, loading it..");
                CONFIG = gson.fromJson(new String(Files.readAllBytes(configFile.toPath())), ModConfig.class);
                if(CONFIG == null) {
                    throw new NullPointerException("The servers file was empty.");
                }else{
                    LOGGER.info("Successfully loaded servers file.");
                }
            }
        }catch (Exception exception) {
            LOGGER.error("There was an error creating/loading the servers file!", exception);
            CONFIG = new ModConfig();
            LOGGER.warn("Defaulting to original servers file.");
        }

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ServerList serverList = new ServerList(client);
            if(serverList.size() == 0) {
                CONFIG.getBuiltinServers().forEach( serverConfig ->  {
                    ServerInfo builtinServer = new ServerInfo(serverConfig.getName(), serverConfig.getAddress(), false);
                    serverList.add(builtinServer);
                });
            }
            serverList.saveFile();
        });

    }

}
