package com.ezrol.terry.minecraft.wastelands.gui;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OverrideCustomizeButton extends ButtonWidget {
    public static interface getType{
        int getCurrentType();
    }
    private getType gui;
    ButtonWidget original;
    private static final Logger LOGGER = LogManager.getLogger("CustomizedBTN");

    public OverrideCustomizeButton(int width, ButtonWidget orig, getType nw) {
        super(8, width / 2 + 5, 120, 150, 20, I18n.translate("selectWorld.customizeType"));
        gui = nw;
        original = orig;
    }

    public void onPressed(double var1, double var3) {
        if (LevelGeneratorType.TYPES[gui.getCurrentType()] == EzwastelandsFabric.WASTELANDS_LEVEL_TYPE) {
            LOGGER.info("Customize EzWastelands");
        }
        else{
            LOGGER.info("Fallback to normal customization logic");
            original.onPressed(var1, var3);
        }

    }
}
