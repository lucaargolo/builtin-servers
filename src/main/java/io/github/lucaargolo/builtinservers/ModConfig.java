package io.github.lucaargolo.builtinservers;

import java.util.Arrays;
import java.util.List;

public class ModConfig {

    public static class ServerConfig {

        private final String name, address;

        public ServerConfig(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }

    private final List<ServerConfig> builtinServers = Arrays.asList(new ServerConfig("Default Server", "127.0.0.1"));

    public List<ServerConfig> getBuiltinServers() {
        return builtinServers;
    }

}
