package com.ezrol.terry.minecraft.wastelands;

import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGenerator;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorSettings;
import com.ezrol.terry.minecraft.wastelands.world.WastelandRegisterI;
import com.ezrol.terry.minecraft.wastelands.world.WastelandsLevelType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.ServerEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.block.BlockItem;
import net.minecraft.server.MinecraftServer;
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
        WASTELANDS = new ChunkGeneratorType<>(null,false,WastelandChunkGeneratorSettings::new);
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
    }

    public interface SetEffectiveTool{
        void ezAddToEfectiveToolList(Block block);
    }
}