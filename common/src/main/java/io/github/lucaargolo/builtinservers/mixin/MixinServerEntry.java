package io.github.lucaargolo.builtinservers.mixin;

import io.github.lucaargolo.builtinservers.BuiltinServers;
import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public class MixinServerEntry {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private MultiplayerScreen screen;

    @Shadow @Final private ServerInfo server;

    private static final MutableText builtinservers_BADGE = Text.translatable("builtinservers.badge");
    private static final MutableText builtinservers_DESCRIPTION = Text.translatable("builtinservers.description");

    @Inject(at = @At("TAIL"), method = "render")
    public void bultinservers_injectBadge(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        MixedServerInfo mixedServerInfo = (MixedServerInfo) this.server;
        if(BuiltinServers.CONFIG.isBadgeVisible() && mixedServerInfo.builtinservers_isBuiltin()) {
            int badgeWidth = this.client.textRenderer.getWidth(builtinservers_BADGE);
            this.client.textRenderer.draw(matrices, builtinservers_BADGE.formatted(Formatting.GOLD), x +entryWidth - badgeWidth - 3, y + 1 + 9, 0xFFFFFF);
            if(mouseX >= x + entryWidth - badgeWidth - 3 && mouseX <= x + entryWidth - 3 && mouseY >= y + 1 + 9 && mouseY <= y + 1 + 9 + 9) {
                this.screen.setTooltip(List.of(builtinservers_DESCRIPTION));
            }
        }
    }
}
