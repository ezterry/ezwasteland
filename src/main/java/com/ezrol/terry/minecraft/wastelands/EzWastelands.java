/*
 * Copyright (c) 2015, Terrence Ezrol (ezterry)
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
import net.minecraft.world.WorldType;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = EzWastelands.MODID, version = EzWastelands.VERSION, name = EzWastelands.NAME)
public class EzWastelands {
	public static final String MODID = "ezwastelands";
	public static final String VERSION = "0.0.2";
	public static final String NAME = "EzWastelands";

	public static Block wastelandBlock;
	public static WorldType wastelandsWorldType;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wastelandBlock = new WastelandBlock(Material.ground);
		GameRegistry.registerBlock(wastelandBlock, "ezwastelandblock");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Where are we?");

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		wastelandsWorldType = new WastelandsWorldType();
		int a;

		System.out.println("World Types:");
		for (a = 0; a < WorldType.worldTypes.length; a++) {
			if (WorldType.worldTypes[a] != null)
				System.out.println(Integer.toString(a + 1) + ": "
						+ WorldType.worldTypes[a].getWorldTypeName());
		}
	}
}
