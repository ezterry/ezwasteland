/*
 * Copyright (c) 2015-2016, Terrence Ezrol (ezterry)
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

package com.ezrol.terry.minecraft.wastelands;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = EzWastelands.MODID, version = EzWastelands.VERSION, name = EzWastelands.NAME)
public class EzWastelands {
	public static final String MODID = "ezwastelands";
	public static final String VERSION = "${version}";
	public static final String NAME = "EzWastelands";

	public static Block wastelandBlock;
	public static WorldType wastelandsWorldType;

	private static boolean wastelandBlockGravity = false;
	public static int villageRate = 0;
	public static boolean modTriggers = false;
	public static int terainVariation = 0;
	public static boolean enableStrongholds = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		ItemBlock wastelandBlockItm;

		cfg.load();
		wastelandBlockGravity = cfg.getBoolean("hasGravity", "wastelandblock", wastelandBlockGravity,
				"If set to true the wasteland blocks will fall like sand");
		villageRate = cfg.getInt("village rate", "structures", villageRate, 0, 100, "Frequency villages spawn");
		modTriggers = cfg.getBoolean("mod triggers", "structures", modTriggers, "Trigger 3rd party mod generation");
		enableStrongholds = cfg.getBoolean("strongholds", "structures", enableStrongholds,
				"Generate strongholds/endportals in the world");
		terainVariation = cfg.getInt("variation", "terrain ", terainVariation, 0, 30,
				"The ground level variation in blocks");

		cfg.save();
		if (wastelandBlockGravity) {
			wastelandBlock = new FallingWastelandBlock(Material.ground);
		} else {
			wastelandBlock = new WastelandBlock(Material.ground);
		}
		GameRegistry.register(wastelandBlock);
		wastelandBlockItm = new ItemBlock(wastelandBlock);
		wastelandBlockItm.setRegistryName(wastelandBlock.getRegistryName());
		wastelandBlockItm.setUnlocalizedName(wastelandBlockItm.getRegistryName().toString());
		GameRegistry.register(wastelandBlockItm);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Where are we?");
		if (event.getSide() == Side.CLIENT) {
			// set up item renderer?
			net.minecraft.client.renderer.RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			renderItem.getItemModelMesher().register(Item.getItemFromBlock(wastelandBlock), 0,
					new ModelResourceLocation(MODID + ":" + "ezwastelandblock"));
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		wastelandsWorldType = new WastelandsWorldType();
		int a;

		System.out.println("World Types:");
		for (a = 0; a < WorldType.worldTypes.length; a++) {
			if (WorldType.worldTypes[a] != null)
				System.out.println(Integer.toString(a + 1) + ": " + WorldType.worldTypes[a].getWorldTypeName());
		}
	}
}
