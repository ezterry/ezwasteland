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

package com.ezrol.terry.minecraft.wastelands;

import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.ezrol.terry.minecraft.wastelands.client.ConfigGui;
import com.ezrol.terry.minecraft.wastelands.gen.elements.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("unused,WeakerAccess")
@Mod(modid = EzWastelands.MODID, version = EzWastelands.VERSION, name = EzWastelands.NAME,
        guiFactory = "com.ezrol.terry.minecraft.wastelands.client.GuiFactory")
public class EzWastelands {
    public static final String MODID = "ezwastelands";
    public static final String VERSION = "${version}";
    public static final String NAME = "EzWastelands";

    public static Block wastelandBlock;
    public static WorldType wastelandsWorldType;
    @SuppressWarnings("FieldCanBeLocal")
    private static boolean wastelandBlockGravity = false;
    private static Logger log;

    private Configuration cfg;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = new Logger(event.getModLog(),false);
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        ItemBlock wastelandBlockItm;

        cfg.load();
        Property prop = cfg.get("hasGravity", "wastelandblock", false,
                "If set to true the wasteland blocks will fall like sand");
        wastelandBlockGravity = prop.getBoolean();
        cfg.save();

        if (wastelandBlockGravity) {
            log.status("No tunneling now");
            wastelandBlock = new FallingWastelandBlock(Material.GROUND);
        } else {
            log.status("What is this gravity you speak of");
            wastelandBlock = new WastelandBlock(Material.GROUND);
        }
        ForgeRegistries.BLOCKS.register(wastelandBlock);
        wastelandBlockItm = new ItemBlock(wastelandBlock);
        //noinspection ConstantConditions
        wastelandBlockItm.setRegistryName(wastelandBlock.getRegistryName());
        //noinspection ConstantConditions
        wastelandBlockItm.setUnlocalizedName(wastelandBlockItm.getRegistryName().toString());
        ForgeRegistries.ITEMS.register(wastelandBlockItm);

    }

    /*
       Load in the wasteland elements to initialize them
     */
    private void initWastelandElements() {
        new Spires();
        new Domes();
        new Shallows();
        new TerrainVariation();
        new RandomOptions();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        log.status("Where are we?");
        if (event.getSide() == Side.CLIENT) {
            // set up item renderer?
            net.minecraft.client.renderer.RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(wastelandBlock), 0,
                    new ModelResourceLocation(MODID + ":" + "ezwastelandblock"));
            MinecraftForge.EVENT_BUS.register(new ConfigGui.configFile(cfg));
        }
        initWastelandElements();
        RegionCore.registerPreset(new ResourceLocation(MODID, "presets/list.txt"));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        wastelandsWorldType = new WastelandsWorldType();
    }
}
