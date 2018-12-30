/*
 * Copyright (c) 2015-2019, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.gui.OverrideCustomizeButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
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

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/gui/menu/NewLevelGui;onInitialized()V", cancellable = false)
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
