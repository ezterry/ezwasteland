package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import com.ezrol.terry.minecraft.wastelands.gui.OverrideCustomizeButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.CustomizeBuffetLevelGui;
import net.minecraft.client.gui.menu.CustomizeFlatLevelGui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewLevelGui.class)
public abstract class ClientMixinNewWorldGui extends Gui implements OverrideCustomizeButton.getType{
    @Shadow
    private ButtonWidget buttonCustomizeType;
    @Shadow
    private int generatorType;
    @Shadow
    private boolean field_3202;


    @Shadow
    abstract void method_2710(boolean arg);

    private static final Logger LOGGER = LogManager.getLogger("NewGiuMixin");

    @Inject(at = @At("RETURN"), method = "onInitialized() V", cancellable = false)
    public void onInitialized(CallbackInfo info) {
        LOGGER.info("Updating Button");

        //remove original config button
        buttons.remove(buttonCustomizeType);
        listeners.remove(buttonCustomizeType);

        ButtonWidget original = buttonCustomizeType;

        //create new button using our callback
        buttonCustomizeType = this.addButton(new OverrideCustomizeButton(this.width,original,this));
        this.buttonCustomizeType.visible = false;

        this.method_2710(this.field_3202);
    }

    @Override
    public int getCurrentType() {
        LOGGER.info("Getting Type: " + generatorType);
        return generatorType;
    }

    @Override
    public NewLevelGui getGuiObject() {
        return (NewLevelGui) (Object) this;
    }
}
