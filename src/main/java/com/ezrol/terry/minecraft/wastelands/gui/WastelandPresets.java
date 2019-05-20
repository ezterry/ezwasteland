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

import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to display the Wasteland Preset Gui, allows both for choosing between some pre-made presets as well
 * as manually exporting/importing a JSON from an external source such as a web page or chat.
 *
 * Created by ezterry on 9/8/16.
 */
@SuppressWarnings("CanBeFinal")
public class WastelandPresets extends Screen {
    final private static int CANCEL_BTN_ID = 80;
    final private static int SELECT_BTN_ID = 81;
    private static final Logger log = LogManager.getLogger("WastelandPreset");
    private WastelandCustomization parent;
    private String currentJson = "";
    private String title = "Presets";
    private TextFieldWidget presetInput = null;
    private PresetSlotList listGUI;

    private class SimpleButton extends ButtonWidget {
        private int btnId;

        SimpleButton(int id, int x, int y, int width, int height, String text){
            super(x,y,width,height,text, (btn) -> {
                ((SimpleButton)btn).action();
            });
            btnId = id;
        }

        private void action(){
            actionPerformed(this);
        }
        public int getId(){
            return btnId;
        }

    }

    public WastelandPresets(WastelandCustomization par, String json) {
        super(new TranslatableTextComponent("config.ezwastelands.presets.title"));
        this.parent = par;
        this.currentJson = json;
    }

    private String readpresetline(BufferedReader stream) throws IOException {
        String line;
        while (true) {
            line = stream.readLine();
            if (line == null) {
                return null;
            }
            line = line.trim();
            log.info("checking line: '" + line + "'");
            if (line.equals("")) {
                log.info("line is empty");
                continue;
            }
            if (line.startsWith("#")) {
                log.info("line is comment");
                continue;
            }
            log.info("line accepted");
            return (line);
        }
    }

    private void insertPresets(Identifier r) {
        ResourceManager resourceManager = minecraft.getResourceManager();
        BufferedReader presetdata;

        try {
            Resource presetlist = resourceManager.getResource(r);
            presetdata = new BufferedReader(new InputStreamReader(presetlist.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            log.error("Could not find resource for presets");
            return;
        }
        try {
            String title;
            String icon;
            String preset;
            while (true) {
                title = readpresetline(presetdata);
                icon = readpresetline(presetdata);
                preset = readpresetline(presetdata);

                if (title == null) {
                    break;
                } else if (icon == null || preset == null) {
                    log.error("Invalid preset: " + title);
                    break;
                }
                log.info("reading preset: " + title);
                log.info("  icon = " + icon);
                log.info("  preset = " + preset);

                listGUI.addItemToList(new WastelandPresetEntry(I18n.translate(title),
                        new Identifier(icon),
                        preset));
            }

        } catch (IOException e) {
            log.error("Error reading in presets");
        }
    }

    @Override
    public void init() {
        this.buttons.clear();
        this.children.clear();

        this.minecraft.keyboard.enableRepeatEvents(true);

        title = getTitle().getString();

        presetInput = new TextFieldWidget(this.font, 50, 40, this.width - 100, 20, "");
        presetInput.setMaxLength(2048);
        presetInput.setText(currentJson);
        presetInput.setChangedListener(this::setEntryValue);
        this.children.add(presetInput);

        listGUI = new PresetSlotList(this.minecraft, this.width, this.height, 80, this.height - 32, 36);

        for(Identifier p : RegionCore.getPresetLocations()) {
            insertPresets(p);
        }

        this.addButton(new SimpleButton(SELECT_BTN_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translate("config.ezwastelands.BTNSelect")));
        this.addButton(new SimpleButton(CANCEL_BTN_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translate("config.ezwastelands.BTNCancel")));

        this.focusOn(listGUI);
        this.children.add(listGUI);
    }

    @Override
    public boolean shouldCloseOnEsc(){
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        listGUI.render(mouseX, mouseY, partialTicks);
        drawCenteredString(font, title, width / 2, 8, 0xffffff);
        presetInput.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

    protected void actionPerformed(SimpleButton button){
        if (button.getId() == SELECT_BTN_ID) {
            parent.updateFromJson(currentJson);
            this.minecraft.openScreen(parent);
        } else if (button.getId() == CANCEL_BTN_ID) {
            this.minecraft.openScreen(parent);
        }
    }

    public void setEntryValue(String value) {
        //the input text box has been modified
        currentJson = value;
        if (!listGUI.selectedJson.equals(value)) {
            listGUI.selected = -1;
            listGUI.selectedJson = "";
        }
    }

    private class WastelandPresetEntry {
        String json;
        String title;
        Identifier texture;

        private WastelandPresetEntry(String title, Identifier icon, String json) {
            this.json = json;
            this.title = title;
            this.texture = icon;
        }
    }

    private class PresetSlotList extends ListWidget {
        private int selected;
        private List<WastelandPresetEntry> selectionList;
        private String selectedJson;

        @SuppressWarnings("SameParameterValue")
        private PresetSlotList(MinecraftClient mc, int width, int height, int topOff, int bottomOff, int slotHight) {
            super(mc, width, height, topOff, bottomOff, slotHight);
            selectionList = new LinkedList<>();
            selectedJson="";
            selected = -1;
        }

        @Override
        protected int getItemCount() {
            return selectionList.size();
        }

        @SuppressWarnings("WeakerAccess")
        protected void addItemToList(WastelandPresetEntry entry) {
            selectionList.add(entry);
            if (entry.json.equals(currentJson)) {
                selected = selectionList.size() - 1;
                selectedJson = entry.json;
            }
        }

        @Override
        protected boolean selectItem(int entry, int btn, double xpos, double ypos){
            WastelandPresets.this.focusOn(listGUI);
            if(entry < 0 || entry == selected){
                return false;
            }
            selected = entry;
            if (selected < getItemCount()) {
                selectedJson = selectionList.get(selected).json;
                currentJson = selectedJson;
                presetInput.setText(currentJson);
                return true;
            }
            return false;
        }


        @Override
        protected boolean isSelectedItem(int slotIndex) {
            return (slotIndex == selected);
        }

        @Override
        protected void renderBackground() {

        }

        @SuppressWarnings("PointlessArithmeticExpression")
        private void blitIcon(int xPos, int yPos, Identifier icon) {
            xPos = xPos + 5;
            hLine(xPos - 1, xPos + 32, yPos - 1, 0xffe0e0e0);
            hLine(xPos - 1, xPos + 32, yPos + 32, 0xffa0a0a0);
            vLine(xPos - 1, yPos - 1, yPos + 32, 0xffe0e0e0);
            vLine(xPos + 32, yPos - 1, yPos + 32, 0xffa0a0a0);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            minecraft.getTextureManager().bindTexture(icon);

            try {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBufferBuilder();
                bufferbuilder.begin(7, VertexFormats.POSITION_UV); /*POSITION_TEX*/
                bufferbuilder.vertex((double) (xPos + 0), (double) (yPos + 32), 0.0D).texture(0.0D, 1.0D).next();
                bufferbuilder.vertex((double) (xPos + 32), (double) (yPos + 32), 0.0D).texture(1.0D, 1.0D).next();
                bufferbuilder.vertex((double) (xPos + 32), (double) (yPos + 0), 0.0D).texture(1.0D, 0.0D).next();
                bufferbuilder.vertex((double) (xPos + 0), (double) (yPos + 0), 0.0D).texture(0.0D, 0.0D).next();
                tessellator.draw();
            } catch (Exception e){
                log.error("Unable to draw icon: ",e);
            }
        }


        @Override
        protected void renderItem(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
            drawSlot(entryID,insideLeft,yPos,insideSlotHeight);
        }

        private void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight) {
            int fontcolor = 0xFFFFFF;
            WastelandPresetEntry entry = selectionList.get(entryID);

            blitIcon(insideLeft + 1, yPos, entry.texture);
            if (selected == entryID) {
                fontcolor = 0xFFFFCC;
            }
            font.draw(entry.title, insideLeft + 33 + 10, yPos + ((insideSlotHeight / 2) - 4), fontcolor);
        }
    }
}
