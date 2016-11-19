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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Logic both for the config gui and providing access to
 * Created by ezterry on 9/9/16.
 */
@SuppressWarnings("unused")
public class ConfigGui extends GuiConfig {
    private static configFile config = null;
    private static Logger log = new Logger(false);

    public ConfigGui(GuiScreen parent) {
        super(parent, initList(), EzWastelands.MODID, true, false,
                I18n.format("sysconfig.ezwastelands.config.title"));
    }

    private static List<IConfigElement> initList() {
        List<IConfigElement> lst = new ArrayList<>();

        if (config != null) {
            Property prop = config.cfg.get("hasGravity", "wastelandblock", false,
                    "If set to true the wasteland blocks will fall like sand");
            prop.setRequiresMcRestart(true);
            lst.add(new gravityElement(prop));
        }

        return (lst);
    }

    @SuppressWarnings("unused")
    public static class configFile {
        private Configuration cfg = null;

        public configFile(Configuration cfg) {
            ConfigGui.config = this;
            this.cfg = cfg;
        }

        @SubscribeEvent
        public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            String eventModId = event.getModID();

            if (eventModId.equals(EzWastelands.MODID) && cfg != null) {
                log.info("Updating config: " + EzWastelands.MODID);
                if (cfg.hasChanged()) {
                    log.status("Write config to disk");
                    cfg.save();
                    log.status("You must now restart the client");
                }
            }
        }
    }

    @SuppressWarnings("unused,WeakerAccess")
    private static class gravityElement extends ConfigElement{
        private final String title;
        public gravityElement(Property prop){
            super(prop);
            title = I18n.format("sysconfig.ezwastelands." + prop.getName() + ".gravity");
        }
        @Override
        public String getName(){
            return title;
        }
    }
}
