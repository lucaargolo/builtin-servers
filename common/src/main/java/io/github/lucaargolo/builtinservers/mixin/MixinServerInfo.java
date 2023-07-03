package io.github.lucaargolo.builtinservers.mixin;

import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerInfo.class)
public class MixinServerInfo implements MixedServerInfo {

    private boolean builtinservers_isBuiltin;
    private boolean builtinservers_isForced;

    @Override
    public boolean builtinservers_isBuiltin() {
        return builtinservers_isBuiltin;
    }

    public void builtinservers_setBuiltin(boolean value) {
        this.builtinservers_isBuiltin = value;
    }

    @Override
    public boolean builtinservers_isForced() {
        return builtinservers_isForced;
    }

    @Override
    public void builtinservers_setForced(boolean value) {
        this.builtinservers_isForced = value;
    }

    @Inject(at = @At("TAIL"), method = "toNbt", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void builtinservers_injectCustomDataToNbt(CallbackInfoReturnable<NbtCompound> cir, NbtCompound nbtCompound) {
        nbtCompound.putBoolean("builtinservers_isBuiltin", builtinservers_isBuiltin);
        nbtCompound.putBoolean("builtinservers_isForced", builtinservers_isForced);
    }

    @Inject(at = @At("TAIL"), method = "fromNbt", locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void builtinservers_readCustomDataFromNbt(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir, ServerInfo serverInfo) {
        MixedServerInfo mixedServerInfo = (MixedServerInfo) serverInfo;
        mixedServerInfo.builtinservers_setBuiltin(root.getBoolean("builtinservers_isBuiltin"));
        mixedServerInfo.builtinservers_setForced(root.getBoolean("builtinservers_isForced"));
    }

}
