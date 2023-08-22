package io.github.lucaargolo.builtinservers.mixin;

import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiplayerScreen.class)
public abstract class MixinMultiplayerScreen extends Screen {
    @Shadow private ButtonWidget buttonEdit;
    @Shadow private ButtonWidget buttonDelete;
    @Shadow protected MultiplayerServerListWidget serverListWidget;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    private static final Text builtinservers_FORCED_TOOLTIP = Text.translatable("builtinservers.forced");

    @Inject(at = @At("HEAD"), method = "render")
    public void builtinservers_changeButtonTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(buttonEdit.isHovered() || buttonDelete.isHovered()) {
            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if(entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                MixedServerInfo mixedServerInfo = (MixedServerInfo) serverEntry.getServer();
                if(mixedServerInfo.builtinservers_isForced()) {
                    this.setTooltip(List.of(builtinservers_FORCED_TOOLTIP.asOrderedText()));
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "updateButtonActivationStates")
    public void builtinservers_changeButtonsActivationStates(CallbackInfo ci) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if(entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
            MixedServerInfo mixedServerInfo = (MixedServerInfo) serverEntry.getServer();
            if(mixedServerInfo.builtinservers_isForced()) {
                this.buttonEdit.active = false;
                this.buttonDelete.active = false;
            }
        }

    }

}
