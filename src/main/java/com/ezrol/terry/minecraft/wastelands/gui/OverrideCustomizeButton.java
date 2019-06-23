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

package com.ezrol.terry.minecraft.wastelands.gui;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OverrideCustomizeButton extends ButtonWidget {
    public static interface getType{
        int getCurrentType();
        CreateWorldScreen getGuiObject();
    }
    private getType gui;
    ButtonWidget original;
    private static final Logger LOGGER = LogManager.getLogger("CustomizedBTN");

    public OverrideCustomizeButton(int width, ButtonWidget orig, getType nw) {
        super(width / 2 + 5, 120, 150, 20, I18n.translate("selectWorld.customizeType"),
                (btn)-> ((OverrideCustomizeButton)btn).userPress());

        gui = nw;
        original = orig;
    }

    public void userPress() {
        LOGGER.info("Press of Customization");
        if (LevelGeneratorType.TYPES[gui.getCurrentType()] == EzwastelandsFabric.WASTELANDS_LEVEL_TYPE) {
            LOGGER.info("Customize EzWastelands");
            CreateWorldScreen nlg = gui.getGuiObject();

            MinecraftClient.getInstance().openScreen(new WastelandCustomization(nlg, nlg.generatorOptionsTag));
        }
        else{
            LOGGER.info("Fallback to normal customization logic");
            original.onPress();
        }
    }
}
