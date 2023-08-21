package io.github.lucaargolo.builtinservers;

import net.minecraft.client.network.ServerInfo;

import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class ModConfig {

    private boolean badgeVisible = true;

    public static class ServerConfig {

        private final String name, address;
        private final boolean forced;

        public ServerConfig(String name, String address) {
            this.name = name;
            this.address = address;
            this.forced = false;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public boolean isForced() {
            return forced;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServerConfig that = (ServerConfig) o;

            if (!name.equals(that.name)) return false;
            return address.equals(that.address);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + address.hashCode();
            return result;
        }

        public static ServerConfig fromServerInfo(ServerInfo serverInfo) {
            return new ModConfig.ServerConfig(serverInfo.name, serverInfo.address);
        }
    }

    private final List<ServerConfig> builtinServers = List.of(new ServerConfig("Default Server", "127.0.0.1"));

    public boolean isBadgeVisible() {
        return badgeVisible;
    }

    public List<ServerConfig> getBuiltinServers() {
        return builtinServers;
    }

}
