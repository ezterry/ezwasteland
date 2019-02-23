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
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;

import java.util.List;

public class WastelandParamListWidget extends EntryListWidget<WastelandParamListWidget.Entry> {
    public WastelandParamListWidget(MinecraftClient client, int int_1, int int_2, int int_3, int int_4, int int_5) {
        super(client, int_1, int_2, int_3, int_4, int_5);
    }

    private HoverableWidget nextWidget(String type, int x, int y, int w, int h, Param p){
        switch (p.getType()){
            case BOOLEAN:
                return( new BooleanParamButton(type, x, y, w, h, (Param.BooleanParam)p));
            case INTEGER:
                return( new IntParamButton(type, x, y, w, h, (Param.IntegerParam)p));
            case FLOAT:
                return( new FloatParamButton(type, x, y, w, h, (Param.FloatParam)p));
            case STRING:
                return( new StringParamButton(type, x, y, w, h, (Param.StringParam)p));
            default:
                throw(new IllegalArgumentException("Unsupported Parameter Type"));
        }
    }
    /** Add a group of parameters with a title to the list widget
     *
     * @param title title of the seciton
     * @param sectionParams the parameters in the section to create widgets for
     * @return the last id used +1
     */
    public void addGroup(String title, List<Param> sectionParams){
        ButtonWidget a = null;
        ButtonWidget b = null;
        int xoff = (width - getEntryWidth())/2;

        addEntry(new Entry("ezwastelands.config." + title + ".title"));

        for(Param p : sectionParams){
            {
                if(a==null){
                    a= nextWidget(title,xoff,0,(getEntryWidth()/2)-5,20,p);
                }
                else{
                    b= nextWidget(title,(width/2)+5,0,(getEntryWidth()/2)-5,20,p);
                }
            }
            if(a != null && b != null) {
                addEntry(new Entry(a, b));
                a = null;
                b = null;
            }
        }
        if(a != null){
            addEntry(new Entry(a, null));
        }
    }

    /** Hoverably button **/
    private abstract class HoverableWidget extends ButtonWidget{
        long hoverStart = -1;
        int lastX, lastY;

        HoverableWidget(int x, int y, int w, int h, String t){
            super(x,y,w,h,t);
            lastX = -1;
            lastY = -1;
        }

        @Override
        public void draw(int mouseX, int mouseY, float partialTicks) {
            super.draw(mouseX, mouseY, partialTicks);
            this.drawHover(mouseX, mouseY);
        }

        protected void drawHover(int mouseX, int mouseY) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            if(hovered && (hoverStart == -1 || lastX != mouseX || lastY != mouseY)){
                hoverStart = System.currentTimeMillis();
                lastX = mouseX;
                lastY = mouseY;
            }
            else if(hovered && (System.currentTimeMillis() - hoverStart) > 1800){
                //draw hover text
                String text = getHoverText();
                int w = client.textRenderer.getStringWidth(text);

                drawRect(x+6,y-3,x+6+w+6,y-3+13,0xFF0000CC);
                drawRect(x+7,y-2,x+7+w+4,y-2+11,0xFF000000);
                drawString(client.textRenderer, text, x+8, y-1,0xCCFFCC);
            }
            if(!hovered){
                hoverStart = -1;
            }
        }

        @Override
        public boolean mouseDragged(double mx1, double my1, int mb, double mx2, double my2) {
            return false;
        }

        @Override
        public boolean isHovered() {
            return hoverStart != -1;
        }

        abstract public String getHoverText();
    }
    /** Simple true/false toggle **/
    private class BooleanParamButton extends HoverableWidget{
        private Param.BooleanParam param;
        private final String type;

        BooleanParamButton(String type, int x, int y, int width, int height, Param.BooleanParam p){
            super(x,y,width,height,"");
            this.type = type;
            param = p;
            setBtnText();
        }

        private void setBtnText(){
            String value = param.get() ?
                    I18n.translate("config.ezwastelands.boolean.true") :
                    I18n.translate("config.ezwastelands.boolean.false");
            String name = I18n.translate("config.ezwastelands." + type + "." + param.getName() + ".name");

            //set text
            setText(name + ": " + value);
        }

        @Override
        public String getHoverText(){
            return(I18n.translate(param.getComment()));
        }

        @Override
        public void onPressed(double var1, double var3) {
            boolean newval = !param.get();
            param.set(newval);
            setBtnText();
        }


    }
    /** A Integer Slider **/
    private class IntParamButton extends HoverableWidget{
        Param.IntegerParam param;
        String type;
        boolean isPressed=false;

        IntParamButton(String type, int x, int y, int width, int height, Param.IntegerParam p){
            super(x,y,width,height,"");
            this.type = type;
            param = p;
            setBtnText();
        }

        @Override
        public String getHoverText(){
            return(I18n.translate(param.getComment()));
        }

        private float valOffset(){
            float val = param.get();
            val -= param.getMin();
            val /= (param.getMax() - param.getMin());
            return val;
        }

        /** Force button background style **/
        @Override
        protected int getTextureId(boolean boolean_1) {
            return 0;
        }

        @Override
        public void onPressed(double double_1, double double_2) {
            super.onPressed(double_1, double_2);
            isPressed=true;
        }

        @Override
        public void onReleased(double double_1, double double_2) {
            isPressed=false;
            super.onReleased(double_1, double_2);
        }

        /** Draw the custom background for the slider **/
        @Override
        protected void drawBackground(MinecraftClient mc, int mouseX, int mouseY) {
            boolean interaction = (isHovered() && enabled && isPressed);

            if(mouseX < x || mouseX > (x+width)){
                interaction = false;
                isPressed = false;
            }

            if(interaction){
                double pos = mouseX - x;
                int range = param.getMax() - param.getMin();
                int oldVal = param.get();
                int newVal;

                pos = pos / (double)width;
                if(pos>1.0){
                    pos = 1.0;
                }
                if(pos < 0.0){
                    pos = 0.0;
                }
                newVal = param.getMin() + ((int)(pos * ((double)range + 0.5)));

                if(newVal > param.getMax()){
                    newVal = param.getMax();
                }
                if(newVal != oldVal) {
                    param.set(newVal);
                    setBtnText();
                }
            }

            mc.getTextureManager().bindTexture(WIDGET_TEX);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedRect(x + (int)(valOffset() * (double)(width - 8)), y, 0, 66, 4, 20);
            drawTexturedRect(x + (int)(valOffset() * (double)(width - 8)) + 4, y, 196, 66, 4, 20);
        }

        private void setBtnText(){
            String name = I18n.translate("config.ezwastelands." + type + "." + param.getName() + ".name");

            //set text
            setText(name + ": " + param.get());
        }
    }

    /** A Float Slider **/
    private class FloatParamButton extends HoverableWidget{
        Param.FloatParam param;
        String type;
        boolean isPressed=false;

        FloatParamButton(String type, int x, int y, int width, int height, Param.FloatParam p){
            super(x,y,width,height,"");
            this.type = type;
            param = p;
            setBtnText();
        }

        @Override
        public String getHoverText(){
            return(I18n.translate(param.getComment()));
        }

        private float valOffset(){
            float val = param.get();
            val -= param.getMin();
            val /= (param.getMax() - param.getMin());
            return val;
        }

        /** Force button background style **/
        @Override
        protected int getTextureId(boolean boolean_1) {
            return 0;
        }

        @Override
        public void onPressed(double double_1, double double_2) {
            super.onPressed(double_1, double_2);
            isPressed=true;
        }

        @Override
        public void onReleased(double double_1, double double_2) {
            isPressed=false;
            super.onReleased(double_1, double_2);
        }

        /** Draw the custom background for the slider **/
        @Override
        protected void drawBackground(MinecraftClient mc, int mouseX, int mouseY) {
            boolean interaction = (isHovered() && enabled && isPressed);

            if(mouseX < x || mouseX > (x+width)){
                interaction = false;
                isPressed = false;
            }

            if(interaction){
                double pos = mouseX - x;
                float range = param.getMax() - param.getMin();
                float oldVal = param.get();
                float newVal;

                pos = pos / (double)(width-2);
                if(pos>1.0){
                    pos = 1.0;
                }
                if(pos < 0.0){
                    pos = 0.0;
                }
                newVal = param.getMin() + ((float)(pos * ((double)range)));

                if(newVal > param.getMax()){
                    newVal = param.getMax();
                }
                if(newVal != oldVal) {
                    param.set(newVal);
                    setBtnText();
                }
            }

            mc.getTextureManager().bindTexture(WIDGET_TEX);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedRect(x + (int)(valOffset() * (double)(width - 8)), y, 0, 66, 4, 20);
            drawTexturedRect(x + (int)(valOffset() * (double)(width - 8)) + 4, y, 196, 66, 4, 20);
        }

        private void setBtnText(){
            String name = I18n.translate("config.ezwastelands." + type + "." + param.getName() + ".name");
            //set text
            setText(name + ": " + param.get());
        }
    }

    private class StringParamButton extends HoverableWidget{
        Param.StringParam param;
        String type;
        TextFieldWidget textfield=null;
        private int yloc=-1;
        private boolean focused = false;

        StringParamButton(String type, int x, int y, int width, int height, Param.StringParam p){
            super(x,y,width,height,"");
            this.type = type;
            param = p;
            //set text
            setText(param.get());
        }

        @Override
        public void draw(int mouseX, int mouseY, float partialTicks) {
            if(textfield == null || y!=yloc){
                yloc = y;
                String value = param.get();
                if(textfield!=null)
                    value = textfield.getText();
                textfield = new TextFieldWidget(client.textRenderer,x,y,width,height);
                textfield.setText(value);
                textfield.setHasFocus(focused);
                param.set(value);
            }
            textfield.draw(mouseX,mouseY,partialTicks);

            //hover logic?
            //method_18326(mouseX, mouseY,partialTicks);
            drawHover(mouseX,mouseY);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int mb) {
            if(textfield != null){
                return textfield.mouseClicked(mx,my,mb);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int mb) {
            if(textfield != null){
                return textfield.mouseReleased(mx,my,mb);
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mx1, double my1, int mb, double mx2, double my2) {
            if(textfield != null){
                return textfield.mouseDragged(mx1,my1,mb,mx2,my2);
            }
            return false;
        }

        @Override
        public void mouseMoved(double a, double b) {
            if(textfield != null){
                textfield.mouseMoved(a,b);
            }
        }

        @Override
        public boolean mouseScrolled(double amount) {
            if(textfield != null){
                return textfield.mouseScrolled(amount);
            }
            return false;
        }

        @Override
        public boolean keyPressed(int a, int b, int c) {
            if(textfield != null){
                return textfield.keyPressed(a,b,c);
            }
            return false;
        }

        @Override
        public boolean keyReleased(int a, int b, int c) {
            if(textfield != null){
                return textfield.keyReleased(a,b,c);
            }
            return false;
        }

        @Override
        public boolean charTyped(char input, int num) {
            if(textfield != null){
                boolean r = textfield.charTyped(input, num);
                if(r)
                    param.set(textfield.getText());
                return r;
            }
            return false;
        }

        @Override
        public void setHasFocus(boolean focus) {
            if(textfield != null){
                focused = focus;
                textfield.setHasFocus(focus);
            }
        }

        @Override
        public boolean hasFocus() {
            if(textfield != null){
                return textfield.hasFocus();
            }
            return false;
        }

        @Override
        public String getHoverText(){
            return(I18n.translate(param.getComment()));
        }
    }

    /** width of the entry list **/
    @Override
    public int getEntryWidth() {
        if(width < 422){
            return width - 12;
        }
        return 410;
    }

    /** Location of the scrollbar **/
    @Override
    protected int getScrollbarPosition() {
        return (width / 2) + (getEntryWidth() / 2) + 2;
    }

    public class Entry extends EntryListWidget.Entry<WastelandParamListWidget.Entry>{
        private ButtonWidget widget1=null;
        private ButtonWidget widget2=null;
        private String title=null;
        private ButtonWidget inputbox=null;
        private ButtonWidget selected=null;

        public Entry(String title){
            //a simple title widget
            this.title = title;
        }
        public Entry(ButtonWidget a, ButtonWidget b){
            this.widget1 = a;
            this.widget2 = b;
        }

        @Override
        public void mouseMoved(double x, double y) {
            super.mouseMoved(x,y);
            if(x < width / 2 && widget1 != null){
                widget1.mouseMoved(x,y);
            }
            if(x > width / 2 && widget2 != null){
                widget2.mouseMoved(x,y);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int i) {
            boolean r=false;
            selected = null;
            if(x < width / 2 && widget1 != null){
                r=widget1.mouseClicked(x,y,i);
                if(r){
                    selected = widget1;
                }
            }
            if(x > width / 2 && widget2 != null){
                r=widget2.mouseClicked(x,y,i);
                if(r){
                    selected = widget2;
                }
            }
            if(!r){
                selected=null;
                r=super.mouseClicked(x,y,i);
            }
            return r;
        }

        @Override
        public boolean mouseReleased(double x, double y, int i) {
            boolean r=false;
            if(x < width / 2 && widget1 != null){
                r=widget1.mouseReleased(x,y,i);
            }
            if(x > width / 2 && widget2 != null){
                r=widget2.mouseReleased(x,y,i);
            }
            if(!r){
                r=super.mouseReleased(x,y,i);
            }
            return r;
        }

        @Override
        public boolean mouseDragged(double x, double y, int i, double x2, double y2) {
            boolean r=false;
            if(x < width / 2 && widget1 != null){
                r=widget1.mouseDragged(x,y,i,x2,y2);
            }
            if(x > width / 2 && widget2 != null){
                r=widget2.mouseDragged(x,y,i,x2,y2);
            }
            if(!r){
                r=super.mouseDragged(x,y,i,x2,y2);
            }
            return r;
        }

        @Override
        public boolean keyPressed(int int_1, int int_2, int int_3) {
            if(selected != null){
                selected.keyPressed(int_1, int_2, int_3);
            }
            return false;
        }

        @Override
        public boolean keyReleased(int int_1, int int_2, int int_3) {
            if(selected != null && selected.hasFocus()){
                selected.keyReleased(int_1, int_2, int_3);
            }
            return false;
        }

        @Override
        public boolean charTyped(char char_1, int int_1) {
            if(selected != null && selected.hasFocus()){
                selected.charTyped(char_1, int_1);
            }
            return false;
        }


        @Override
        public void setHasFocus(boolean focus) {

            if(selected != null){
                selected.setHasFocus(focus);
            }
            super.setHasFocus(focus);
        }

        @Override
        public boolean hasFocus() {
            if(selected != null){
                if(selected.hasFocus()){
                    return true;
                }
            }
            return super.hasFocus();
        }

        @Override
        public void draw(int var1, int var2, int mouseX, int mouseY, boolean var5, float subticks) {
            int widgety = this.getY();
            if(widget1 == null && inputbox == null){
                //Full width title
                drawStringCentered(client.textRenderer, I18n.translate(title), width /2, widgety + 4, 0xFFFFFF);
            }
            if(widget1 != null){
                //one or two widgets
                widget1.y = widgety;
                widget1.draw(mouseX, mouseY, subticks);

                if(widget2 != null){
                    widget2.y = widgety;
                    widget2.draw(mouseX,mouseY,subticks);
                }
            }
        }
    }
}
