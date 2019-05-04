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

import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorConfig;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.menu.NewLevelScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class WastelandCustomization extends Screen {
    private static final int DONE_ID = 900;
    private static final int DEFAULTS_ID = 901;
    private static final int PRESETS_ID = 902;
    private static final int CANCEL_ID = 903;
    private static final Logger log = LogManager.getLogger("WastelandCustomization");
    private final NewLevelScreen parent;
    private final Map<Integer, Param> idMap = new HashMap<>();
    private RegionCore core;
    private WastelandParamListWidget list=null;

    public WastelandCustomization(NewLevelScreen p, CompoundTag tags) {
        super(new TranslatableTextComponent("config.ezwastelands.WorldConfigGUI"));
        this.parent = p;
        core = new RegionCore(WastelandChunkGeneratorConfig.CompoundToJson(tags),null, null);
    }

    private class SimpleButton extends ButtonWidget{
        private int  btnId;
        public SimpleButton(int id, int x, int y, int width, int height, String text){
            super(x,y,width,height,text,(btn) -> ((SimpleButton)btn).action());
            btnId=id;
        }

        private void action(){
            actionPerformed(this);
        }

        public int getId(){
            return btnId;
        }
    }

    @Override
    public boolean shouldCloseOnEsc(){
        return false;
    }

    @Override
    public void init() {
        this.minecraft.keyboard.enableRepeatEvents(true);

        this.addButton(new SimpleButton(DONE_ID, this.width / 2 + 98, this.height - 27, 90, 20,
                I18n.translate("gui.done")));
        this.addButton(new SimpleButton(PRESETS_ID, this.width / 2 + 3, this.height - 27, 90, 20,
                I18n.translate("config.ezwastelands.BTNPresets")));
        this.addButton(new SimpleButton(DEFAULTS_ID, this.width / 2 - 187, this.height - 27, 90, 20,
                I18n.translate("config.ezwastelands.BTNDefault")));
        this.addButton(new SimpleButton(CANCEL_ID, this.width / 2 - 92, this.height - 27, 90, 20,
                I18n.translate("config.ezwastelands.BTNCancel")));

        reloadList();
        //this.list.setActive(true);
        //this.list.setPage(0);
        this.list.setScrollAmount(0.0);
        //this.list.setEnabled(true);
    }

    private void reloadList() {
        double oldscroll = 0;
        if(list != null){
            oldscroll = list.getScrollAmount();
        }
        list = new WastelandParamListWidget(minecraft, width, height, 32, height-32, 25);
        Map<String, List<Param>> pmap = core.getCurrentParamMap();
        for(String entry : pmap.keySet()){
            List<Param> paramlst = pmap.get(entry);
            list.addGroup(entry, paramlst);
        }
        list.setScrollAmount(oldscroll);
        this.focusOn(list);
    }

    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        boolean b = super.mouseReleased(double_1, double_2, int_1);
        this.focusOn(list);
        return b;
    }

    protected void actionPerformed(SimpleButton button){
        if (button.visible) {
            switch (button.getId()) {
                case DONE_ID:
                    String json = core.getJson();
                    log.info("Wasteland Settings: " + json);
                    parent.generatorOptionsTag = WastelandChunkGeneratorConfig.CoreConfigToCompound(core);
                    this.minecraft.openScreen(this.parent);
                    break;
                case DEFAULTS_ID:
                    updateFromJson("");
                    break;
                case CANCEL_ID:
                    log.info("Cancel Customization, using previous settings.");
                    this.minecraft.openScreen(this.parent);
                    break;
                case PRESETS_ID:
                    log.info("Opening Presets");
                    this.minecraft.openScreen(new WastelandPresets(this, core.getJson()));
                    //MinecraftClient.getInstance().openGui(new WastelandPresets(this, core.getJson()));
            }
        }
    }

    void updateFromJson(String json) {
        core = new RegionCore(json,null,null);
        reloadList();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        this.list.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, this.getTitle().getString(), this.width / 2, 2, 0xFFFFFF);
        super.render(mouseX, mouseY, partialTicks);
    }
}
