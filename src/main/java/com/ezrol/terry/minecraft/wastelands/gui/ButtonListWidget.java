package com.ezrol.terry.minecraft.wastelands.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

public class ButtonListWidget extends EntryListWidget<ButtonListWidget.Entry> {
    public ButtonListWidget(MinecraftClient client, int int_1, int int_2, int int_3, int int_4, int int_5) {
        super(client, int_1, int_2, int_3, int_4, int_5);
    }

    public class Entry extends EntryListWidget.Entry<ButtonListWidget.Entry>{

        @Override
        public void draw(int var1, int var2, int var3, int var4, boolean var5, float var6) {

        }
    }
}
