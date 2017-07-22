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

import com.ezrol.terry.minecraft.wastelands.Logger;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.*;

public class WastelandCustomization extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder {
    private static final int DONE_ID = 900;
    private static final int DEFAULTS_ID = 901;
    private static final int PRESETS_ID = 902;
    private static final int CANCEL_ID = 903;
    static private final Logger log = new Logger(false);
    private final GuiCreateWorld parent;
    private final Map<Integer, Param> idMap = new HashMap<>();
    private RegionCore core;
    private GuiPageButtonList list;
    //The last position of the mouse
    //and last time it was updated (for hover events)
    private int lastMouseX;
    private int lastMouseY;
    private long lastMouseUpdate;

    public WastelandCustomization(GuiCreateWorld p) {
        super();
        this.parent = p;
        core = new RegionCore(p.chunkProviderSettingsJson,null);
    }

    @Override
    public void initGui() {

        this.addButton(new GuiButton(DONE_ID, this.width / 2 + 98, this.height - 27, 90, 20,
                I18n.format("gui.done")));
        this.addButton(new GuiButton(PRESETS_ID, this.width / 2 + 3, this.height - 27, 90, 20,
                I18n.format("config.ezwastelands.BTNPresets")));
        this.addButton(new GuiButton(DEFAULTS_ID, this.width / 2 - 187, this.height - 27, 90, 20,
                I18n.format("config.ezwastelands.BTNDefault")));
        this.addButton(new GuiButton(CANCEL_ID, this.width / 2 - 92, this.height - 27, 90, 20,
                I18n.format("config.ezwastelands.BTNCancel")));

        lastMouseUpdate = System.currentTimeMillis();
        lastMouseX = 0;
        lastMouseY = 0;

        reloadList();
        this.list.setActive(true);
        this.list.setPage(0);
        this.list.scrollBy(0);
        this.list.setEnabled(true);


    }

    private void reloadList() {
        this.list = new GuiPageButtonList(this.mc, this.width, this.height, 32, this.height - 32, 25, this,
                new GuiPageButtonList.GuiListEntry[][]{this.BuildConfigList()});
    }

    public void setEntryValue(int id, boolean value) {
        if (idMap.containsKey(id)) {
            Param p = idMap.get(id);
            if (p.getType() == Param.ParamTypes.BOOLEAN) {
                ((Param.BooleanParam) p).set(value);
            }
        }
    }

    public void setEntryValue(int id, float value) {
        if (idMap.containsKey(id)) {
            Param p = idMap.get(id);
            if (p.getType() == Param.ParamTypes.INTEGER) {
                ((Param.IntegerParam) p).set((int) value);
            } else if (p.getType() == Param.ParamTypes.FLOAT) {
                ((Param.FloatParam) p).set(value);
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    public void setEntryValue(int id, String value) {
        if (idMap.containsKey(id)) {
            Param p = idMap.get(id);
            if (p.getType() == Param.ParamTypes.STRING) {
                ((Param.StringParam) p).set(value);
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    public String getText(int id, String name, float value) {
        if (idMap.containsKey(id)) {
            Param p = idMap.get(id);

            if (p.getType() == Param.ParamTypes.INTEGER) {
                return String.format("%s: %d", name, ((Param.IntegerParam) p).validate((int) value));
            }
            if (p.getType() == Param.ParamTypes.FLOAT) {
                return String.format("%s: %.3f", name, ((Param.FloatParam) p).validate(value));
            }
        }
        return "0";
    }

    private GuiPageButtonList.GuiListEntry GetGuiButtonForParam(Param p, String elementName, Integer nextId) {
        String fieldLIC = "config.ezwastelands." + elementName + "." + p.getName() + ".name";
        GuiPageButtonList.GuiListEntry entry = null;

        switch (p.getType()) {
            case INTEGER:
                //we need an integer field
                Param.IntegerParam intParam = (Param.IntegerParam) p;

                entry = new GuiPageButtonList.GuiSlideEntry(nextId, I18n.format(fieldLIC), true, this,
                        (float) intParam.getMin(), (float) intParam.getMax(), (float) intParam.get());
                break;
            case FLOAT:
                //we need a float field
                Param.FloatParam floatParam = (Param.FloatParam) p;
                entry = new GuiPageButtonList.GuiSlideEntry(nextId, I18n.format(fieldLIC), true, this,
                        floatParam.getMin(), floatParam.getMax(), floatParam.get());
                break;
            case BOOLEAN:
                //we need a boolean field
                entry = new GuiPageButtonList.GuiButtonEntry(nextId, I18n.format(I18n.format(fieldLIC)), true,
                        ((Param.BooleanParam) p).get());
                break;
            case STRING:
                //we have a generic String for this field
                entry = new GuiPageButtonList.EditBoxEntry(nextId, ((Param.StringParam) p).get(), true, null);
                break;
            default:
                break;
        }
        if (entry != null) {
            idMap.put(nextId, p);
        }
        return entry;
    }

    private GuiPageButtonList.GuiListEntry[] BuildConfigList() {
        GuiPageButtonList.GuiListEntry[] finalList;
        List<GuiPageButtonList.GuiListEntry> buttonConfig = new LinkedList<>();
        Map<String, List<Param>> paramMap;
        String guiTitleLIC;
        List<Param> curParams;
        int nextid = 2000;
        boolean evencnt;
        GuiPageButtonList.GuiListEntry entry;

        paramMap = core.getCurrentParamMap();

        for (String curElement : paramMap.keySet()) {
            guiTitleLIC = "config.ezwastelands." + curElement + ".configtitle";
            curParams = paramMap.get(curElement);

            //element section title
            buttonConfig.add(new GuiPageButtonList.GuiLabelEntry(nextid, I18n.format(guiTitleLIC), true));
            nextid++;
            buttonConfig.add(null);
            //now add each parameter
            evencnt = true;
            for (Param param : curParams) {

                entry = this.GetGuiButtonForParam(param, curElement, nextid);
                if (entry != null) {
                    buttonConfig.add(entry);
                    evencnt = !evencnt;
                }
                nextid++;
            }
            if (!evencnt) {
                buttonConfig.add(null);
            }
        }
        paramMap.keySet();

        //now we need a static array from the list
        finalList = new GuiPageButtonList.GuiListEntry[buttonConfig.size()];
        for (int i = 0; i < finalList.length; i++) {
            finalList[i] = buttonConfig.get(i);
        }
        return finalList;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            switch (button.id) {
                case DONE_ID:
                    String json = core.getJson();
                    log.status("Wasteland Settings: " + json);
                    this.parent.chunkProviderSettingsJson = json;
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case DEFAULTS_ID:
                    updateFromJson("");
                    break;
                case CANCEL_ID:
                    log.status("Cancel Customization, using previous settings.");
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case PRESETS_ID:
                    log.info("Opening Presets");
                    this.mc.displayGuiScreen(new WastelandPresets(this, core.getJson()));
            }
        }
    }

    void updateFromJson(String json) {
        int pos = list.getAmountScrolled();
        core = new RegionCore(json,null);
        reloadList();
        list.scrollBy(pos);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("config.ezwastelands.WorldConfigGUI"), this.width / 2, 2, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            lastMouseUpdate = System.currentTimeMillis();
        } else if ((System.currentTimeMillis() - lastMouseUpdate) > 1800) {
            attemptToDrawHoverText();
        }
    }

    private void attemptToDrawHoverText() {
        //if the mouse is not set lets just skip this
        if (lastMouseY == 0 || lastMouseX == 0) {
            return;
        }
        if (lastMouseY > this.height - 28) {
            //we are over the buttons, don't show hover text
            lastMouseUpdate = System.currentTimeMillis();
            return;
        }

        Iterator<Integer> i;
        int id;
        Gui entry;
        int xoff;
        int yoff;
        int width;
        int height;
        List<String> comment;

        for (i = idMap.keySet().iterator(); i.hasNext(); ) {
            id = i.next();
            entry = list.getComponent(id);
            if (entry instanceof GuiButton) {
                xoff = ((GuiButton) entry).x;
                yoff = ((GuiButton) entry).y;
                width = ((GuiButton) entry).width;
                height = ((GuiButton) entry).height;

                if ((lastMouseX > xoff) && (lastMouseX < (xoff + width)) &&
                        (lastMouseY > yoff) && (lastMouseY < (yoff + height))) {
                    //we are in the bounding box
                    comment = new LinkedList<>();
                    comment.add("");
                    comment.set(0, I18n.format(idMap.get(id).getComment()));
                    this.drawHoveringText(comment, lastMouseX, lastMouseY);
                    return;
                }
            }
        }
        //there is no hover text at this point on the screen, reset time so we don't retry every frame
        lastMouseUpdate = System.currentTimeMillis();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
        lastMouseUpdate = System.currentTimeMillis();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        this.list.mouseReleased(mouseX, mouseY, mouseButton);
        lastMouseUpdate = System.currentTimeMillis();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.list.onKeyPressed(typedChar, keyCode);
        lastMouseUpdate = System.currentTimeMillis();
        lastMouseX = 0;
        lastMouseY = 0;
    }
}
