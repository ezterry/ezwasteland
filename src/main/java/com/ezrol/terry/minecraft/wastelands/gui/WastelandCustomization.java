/*
 * Copyright (c) 2015-2017, Terrence Ezrol (ezterry)
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
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorSettings;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class WastelandCustomization extends Gui {
    private static final int DONE_ID = 900;
    private static final int DEFAULTS_ID = 901;
    private static final int PRESETS_ID = 902;
    private static final int CANCEL_ID = 903;
    private static final Logger log = LogManager.getLogger("WastelandCustomization");
    private final NewLevelGui parent;
    private final Map<Integer, Param> idMap = new HashMap<>();
    private RegionCore core;
    private WastelandParamListWidget list=null;

    public WastelandCustomization(NewLevelGui p, CompoundTag tags) {
        super();
        this.parent = p;
        core = new RegionCore(WastelandChunkGeneratorSettings.CompoundToJson(tags),null, null);
    }

    private class SimpleButton extends ButtonWidget{

        public SimpleButton(int id, int x, int y, int width, int height, String text){
            super(id,x,y,width,height,text);
        }

        @Override
        public void onPressed(double double_1, double double_2) {
            super.onPressed(double_1, double_2);
            actionPerformed(this);

        }
    }

    @Override
    public boolean canClose(){
        return false;
    }

    @Override
    public void onInitialized() {
        this.client.keyboard.enableRepeatEvents(true);

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
        this.list.scroll(0);
        //this.list.setEnabled(true);
    }

    private void reloadList() {
        int oldscroll = 0;
        if(list != null){
            oldscroll = list.getScrollY();
            listeners.remove(list);
        }
        list = new WastelandParamListWidget(client, width, height, 32, height-32, 25);
        Map<String, List<Param>> pmap = core.getCurrentParamMap();
        int startid = 4000;
        for(String entry : pmap.keySet()){
            List<Param> paramlst = pmap.get(entry);
            startid = list.addGroup(startid, entry, paramlst);
        }
        this.listeners.add(list);
        list.scroll(oldscroll);
        this.focusOn(list);
    }

    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        boolean b = super.mouseReleased(double_1, double_2, int_1);
        this.focusOn(list);
        return b;
    }

    protected void actionPerformed(ButtonWidget button){
        if (button.enabled) {
            switch (button.id) {
                case DONE_ID:
                    String json = core.getJson();
                    log.info("Wasteland Settings: " + json);
                    parent.field_3200 = WastelandChunkGeneratorSettings.CoreConfigToCompound(core);
                    this.client.openGui(this.parent);
                    break;
                case DEFAULTS_ID:
                    updateFromJson("");
                    break;
                case CANCEL_ID:
                    log.info("Cancel Customization, using previous settings.");
                    this.client.openGui(this.parent);
                    break;
                case PRESETS_ID:
                    log.info("Opening Presets");
                    this.client.openGui(new WastelandPresets(this, core.getJson()));
                    //MinecraftClient.getInstance().openGui(new WastelandPresets(this, core.getJson()));
            }
        }
    }

    void updateFromJson(String json) {
        core = new RegionCore(json,null,null);
        reloadList();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground();
        this.list.draw(mouseX, mouseY, partialTicks);
        this.drawStringCentered(this.fontRenderer, I18n.translate("config.ezwastelands.WorldConfigGUI"), this.width / 2, 2, 0xFFFFFF);
        super.draw(mouseX, mouseY, partialTicks);
    }
}
