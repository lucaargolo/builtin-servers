package io.github.lucaargolo.builtinservers;

import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.network.ServerInfo;

public class BuiltinServerInfo extends ServerInfo {

    public BuiltinServerInfo(String name, String address, boolean forced) {
        super(name, address, false);
        ((MixedServerInfo) this).builtinservers_setBuiltin(true);
        ((MixedServerInfo) this).builtinservers_setForced(forced);
    }

}
