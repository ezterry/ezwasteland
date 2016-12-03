/*
 * Copyright (c) 2016, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
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
 */

package com.ezrol.terry.minecraft.wastelands.client;

import com.ezrol.terry.minecraft.wastelands.EzWastelands;
import com.ezrol.terry.minecraft.wastelands.Logger;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ezterry on 9/8/16.
 */
public class WastelandPresets extends GuiScreen implements GuiPageButtonList.GuiResponder {
    final private static int CANCEL_BTN_ID = 80;
    final private static int SELECT_BTN_ID = 81;
    final private static int PRESET_INPUT_BOX = 82;
    static private Logger log = new Logger(false);
    private WastelandCustomization parent;
    private String currentJson = "";
    private String title = "Presets";
    private GuiTextField presetInput = null;
    private PresetSlotList listGUI;

    public WastelandPresets(WastelandCustomization par, String json) {
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

    private void insertPresets(ResourceLocation r) {
        IResourceManager resourceManager = mc.getResourceManager();
        BufferedReader presetdata;

        try {
            IResource presetlist = resourceManager.getResource(r);
            presetdata = new BufferedReader(new InputStreamReader(presetlist.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            log.error("Could not resource for presets");
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

                listGUI.addItemToList(new WastelandPresetEntry(I18n.format(title),
                        new ResourceLocation(icon),
                        preset));
            }

        } catch (IOException e) {
            log.error("Error reading in presets");
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        title = I18n.format("config.ezwastelands.presets.title");

        presetInput = new GuiTextField(PRESET_INPUT_BOX, this.fontRendererObj, 50, 40, this.width - 100, 20);
        presetInput.setMaxStringLength(2048);
        presetInput.setText(currentJson);
        presetInput.setCursorPositionZero();
        presetInput.setGuiResponder(this);

        listGUI = new PresetSlotList(this.mc, this.width, this.height, 80, this.height - 32, 36);

        for(ResourceLocation p : RegionCore.getPresetLocations()) {
            insertPresets(p);
        }

        this.buttonList.add(new GuiButton(SELECT_BTN_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("config.ezwastelands.BTNSelect")));
        this.buttonList.add(new GuiButton(CANCEL_BTN_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("config.ezwastelands.BTNCancel")));
    }

    @Override
    public void updateScreen() {
        this.presetInput.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        listGUI.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, title, width / 2, 8, 0xffffff);
        presetInput.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == SELECT_BTN_ID) {
            parent.updateFromJson(currentJson);
            this.mc.displayGuiScreen(parent);
        } else if (button.id == CANCEL_BTN_ID) {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.presetInput.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.presetInput.textboxKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        listGUI.handleMouseInput();
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == PRESET_INPUT_BOX) {
            //the input text box has been modified
            currentJson = value;
            if (!listGUI.selectedJson.equals(value)) {
                listGUI.selected = -1;
                listGUI.selectedJson = "";
            }
        }
    }

    private class WastelandPresetEntry {
        protected String json;
        protected String title;
        protected ResourceLocation texture;

        public WastelandPresetEntry(String title, ResourceLocation icon, String json) {
            this.json = json;
            this.title = title;
            this.texture = icon;
        }
    }

    private class PresetSlotList extends GuiSlot {
        private int selected;
        private List<WastelandPresetEntry> selectionList;
        private String selectedJson;

        private PresetSlotList(Minecraft mc, int width, int height, int topOff, int bottomOff, int slotHight) {
            super(mc, width, height, topOff, bottomOff, slotHight);
            selectionList = new LinkedList<>();
            selectedJson="";
            selected = -1;
        }

        @Override
        protected int getSize() {
            return selectionList.size();
        }

        protected void addItemToList(WastelandPresetEntry entry) {
            selectionList.add(entry);
            if (entry.json == currentJson) {
                selected = selectionList.size() - 1;
                selectedJson = entry.json;
            }
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selected = slotIndex;
            if (selected < getSize()) {
                selectedJson = selectionList.get(selected).json;
                currentJson = selectedJson;
                presetInput.setText(currentJson);
                presetInput.setCursorPositionZero();
            }
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return (slotIndex == selected);
        }

        @Override
        protected void drawBackground() {

        }

        private void blitIcon(int xPos, int yPos, ResourceLocation icon) {
            int i = xPos + 5;
            drawHorizontalLine(i - 1, i + 32, yPos - 1, 0xffe0e0e0);
            drawHorizontalLine(i - 1, i + 32, yPos + 32, 0xffa0a0a0);
            drawVerticalLine(i - 1, yPos - 1, yPos + 32, 0xffe0e0e0);
            drawVerticalLine(i + 32, yPos - 1, yPos + 32, 0xffa0a0a0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(icon);
            int j = 32;
            int k = 32;
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos((double) (i + 0), (double) (yPos + 32), 0.0D).tex(0.0D, 1.0D).endVertex();
            vertexbuffer.pos((double) (i + 32), (double) (yPos + 32), 0.0D).tex(1.0D, 1.0D).endVertex();
            vertexbuffer.pos((double) (i + 32), (double) (yPos + 0), 0.0D).tex(1.0D, 0.0D).endVertex();
            vertexbuffer.pos((double) (i + 0), (double) (yPos + 0), 0.0D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
        }

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
            int fontcolor = 0xFFFFFF;
            WastelandPresetEntry entry = selectionList.get(entryID);

            blitIcon(insideLeft + 1, yPos, entry.texture);
            if (selected == entryID) {
                fontcolor = 0xFFFFCC;
            }
            fontRendererObj.drawString(entry.title, insideLeft + 33 + 10, yPos + ((insideSlotHeight / 2) - 4), fontcolor);

        }
    }
}
