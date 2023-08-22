package io.github.lucaargolo.builtinservers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BuiltinServers {

    public static final String MODID = "builtinservers";

    public static final Logger LOGGER = LogManager.getLogger("Builtin Servers");
    public static ModConfig CONFIG;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void initializeClient(Path configPath, @Nullable Consumer<Consumer<MinecraftClient>> eventRegister_clientStarted) {
        File configFile = new File(configPath + File.separator + "builtinservers.json");

        LOGGER.info("Trying to read servers file...");
        try {
            if (configFile.createNewFile()) {
                LOGGER.info("No servers file found, creating a new one...");
                String json = gson.toJson(JsonParser.parseString(gson.toJson(new ModConfig())));
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
                } else {
                    LOGGER.info("Successfully loaded servers file.");
                }
            }
        } catch (Exception exception) {
            LOGGER.error("There was an error creating/loading the servers file!", exception);
            CONFIG = new ModConfig();
            LOGGER.warn("Defaulting to original servers file.");
        }

        //ClientLifecycleEvents.CLIENT_STARTED
        if(eventRegister_clientStarted != null) eventRegister_clientStarted.accept(BuiltinServers::onClientStarted);
    }

    public static void onClientStarted(MinecraftClient client){
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        if(serverList.size() == 0) {
            CONFIG.getBuiltinServers().forEach( serverConfig ->  {
                ServerInfo builtinServer = new BuiltinServerInfo(serverConfig.getName(), serverConfig.getAddress(), serverConfig.isForced());
                serverList.add(builtinServer, false);
            });
        } else {
            List<ModConfig.ServerConfig> forcedConfigs = CONFIG.getBuiltinServers().stream().filter(ModConfig.ServerConfig::isForced).collect(Collectors.toList());
            List<ModConfig.ServerConfig> savedServers = new ArrayList<>(serverList.size());
            int i = 0;
            while (i != serverList.size()) {
                ServerInfo savedServer = serverList.get(i);
                if(((MixedServerInfo) savedServer).builtinservers_isForced()) {
                    ModConfig.ServerConfig savedServerConfig = ModConfig.ServerConfig.fromServerInfo(savedServer);
                    savedServers.add(savedServerConfig);
                    if(!forcedConfigs.contains(savedServerConfig)) {
                        ((MixedServerInfo) savedServer).builtinservers_setForced(false);
                    }
                }
                i++;
            }
            forcedConfigs.removeAll(savedServers);
            forcedConfigs.forEach(serverConfig -> {
                ServerInfo builtinServer = new BuiltinServerInfo(serverConfig.getName(), serverConfig.getAddress(), true);
                serverList.add(builtinServer, false);
            });
        }
        serverList.saveFile();
    }

}
