package io.github.lucaargolo.builtinservers.mixin;

import io.github.lucaargolo.builtinservers.mixed.MixedServerInfo;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
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
    @Shadow public abstract void setTooltip(List<Text> tooltip);

    @Shadow private ButtonWidget buttonDelete;
    @Shadow protected MultiplayerServerListWidget serverListWidget;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    private static final Text builtinservers_FORCED_TOOLTIP = Text.translatable("builtinservers.forced");
    private final ButtonWidget.TooltipSupplier builtinservers_TOOLTIP_SUPPLIER = (ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) -> {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if(entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
            MixedServerInfo mixedServerInfo = (MixedServerInfo) serverEntry.getServer();
            if(mixedServerInfo.builtinservers_isForced()) {
                this.setTooltip(List.of(builtinservers_FORCED_TOOLTIP));
            }
        }
    };

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 5), method = "init")
    public void builtinservers_modifyButtons(CallbackInfo ci) {
        remove(this.buttonEdit);
        remove(this.buttonDelete);
        this.buttonEdit = addDrawableChild(new ButtonWidget(this.buttonEdit.x, this.buttonEdit.y, this.buttonEdit.getWidth(), this.buttonEdit.getHeight(), this.buttonEdit.getMessage(), ((AccessorButtonWidget) this.buttonEdit).getOnPress(), builtinservers_TOOLTIP_SUPPLIER));
        this.buttonDelete = addDrawableChild(new ButtonWidget(this.buttonDelete.x, this.buttonDelete.y, this.buttonDelete.getWidth(), this.buttonDelete.getHeight(), this.buttonDelete.getMessage(), ((AccessorButtonWidget) this.buttonDelete).getOnPress(), builtinservers_TOOLTIP_SUPPLIER));
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
