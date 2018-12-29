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

package com.ezrol.terry.minecraft.wastelands;

import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGenerator;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorSettings;
import com.ezrol.terry.minecraft.wastelands.world.WastelandRegisterI;
import com.ezrol.terry.minecraft.wastelands.world.WastelandsLevelType;
import com.ezrol.terry.minecraft.wastelands.world.elements.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.ServerEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.block.BlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.level.LevelGeneratorType;

import java.util.function.Consumer;

public class EzwastelandsFabric implements ModInitializer {
    public static LevelGeneratorType WASTELANDS_LEVEL_TYPE = null;
    public static ChunkGeneratorType<WastelandChunkGeneratorSettings, WastelandChunkGenerator> WASTELANDS;
    public static Block WastelandsBlock;


    @Override
    public void onInitialize() {
        //create the wastelands level type
        WASTELANDS_LEVEL_TYPE = WastelandsLevelType.getType();

        //register the wasteland generator type
        WASTELANDS = new ChunkGeneratorType<>(null,true,WastelandChunkGeneratorSettings::new);
        //noinspection ConstantConditions
        ((WastelandRegisterI)WASTELANDS).enableWastelandGenerator(true);

        Registry.register(Registry.CHUNK_GENERATOR_TYPE, "ezwastelands:wastelands", WASTELANDS);

        //register block
        WastelandsBlock = new EzWastelandBlock();
        Registry.register(Registry.BLOCK, "ezwastelands:ezwastelandblock", WastelandsBlock);

        //register block item
        BlockItem itm = new BlockItem(WastelandsBlock, new Item.Settings().itemGroup(ItemGroup.BUILDING_BLOCKS));
        Registry.register(Registry.ITEM, "ezwastelands:ezwastelandblock", itm);


        //when the server starts make the Wasteland block effective on
        //shovels
        ServerEvent.START.register(new Consumer<MinecraftServer>() {
            @Override
            public void accept(MinecraftServer minecraftServer) {
                //debug, print all generator types
                for(Identifier id : Registry.CHUNK_GENERATOR_TYPE.keys()){
                    System.out.println(id);
                }
                //add wasteland block to all shovels
                for(Item i : Registry.ITEM){
                    if(i instanceof ShovelItem){
                        ((SetEffectiveTool)i).ezAddToEfectiveToolList(WastelandsBlock);
                    }
                }
            }
        });

        //register terrain generation elements
        // (simply create an instance they self register)
        new Spires();
        new Domes();
        new Shallows();
        new TerrainVariation();
        new RandomOptions();

        RegionCore.registerPreset(new Identifier("ezwastelands:presets/list.txt"));
    }

    public interface SetEffectiveTool{
        void ezAddToEfectiveToolList(Block block);
    }
}