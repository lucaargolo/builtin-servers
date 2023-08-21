package io.github.lucaargolo.builtinservers.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ButtonWidget.class)
public interface AccessorButtonWidget {

    @Accessor
    ButtonWidget.PressAction getOnPress();


}
