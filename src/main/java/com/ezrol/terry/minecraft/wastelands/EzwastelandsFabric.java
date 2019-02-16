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
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorConfig;
import com.ezrol.terry.minecraft.wastelands.world.WastelandsLevelType;
import com.ezrol.terry.minecraft.wastelands.world.elements.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.block.BlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class EzwastelandsFabric implements ModInitializer {
    public static LevelGeneratorType WASTELANDS_LEVEL_TYPE = null;
    public static ChunkGeneratorType<WastelandChunkGeneratorConfig, WastelandChunkGenerator> WASTELANDS;
    public static Block WastelandsBlock;


    @Override
    public void onInitialize() {
        //create the wastelands level type
        WASTELANDS_LEVEL_TYPE = WastelandsLevelType.getType();

        //register the wasteland generator type
        createWastelandGenerator factory = new createWastelandGenerator();

        WASTELANDS = factory.getChunkGeneratorType(WastelandChunkGeneratorConfig::new);

        Registry.register(Registry.CHUNK_GENERATOR_TYPE, "ezwastelands:wastelands", WASTELANDS);

        //register block
        WastelandsBlock = new EzWastelandBlock();
        Registry.register(Registry.BLOCK, "ezwastelands:ezwastelandblock", WastelandsBlock);

        //register block item
        BlockItem itm = new BlockItem(WastelandsBlock, new Item.Settings().itemGroup(ItemGroup.BUILDING_BLOCKS));
        Registry.register(Registry.ITEM, "ezwastelands:ezwastelandblock", itm);


        //when the server starts make the Wasteland block effective on
        //shovels
        ServerStartCallback.EVENT.register(new ServerStartCallback() {
            @Override
            public void onStartServer(MinecraftServer server) {
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

    /**
     * This is a bit hacky,
     * The short of it is we want to register the wastelands as CHUNK_GENERATOR_TYPE
     *
     * However  ChunkGeneratorType requires a factory interface ChunkGeneratorFactory
     * that is package private.  (thus we can't use the interface directly)
     *
     * The folowing class uses reflection to become an instance of "ChunkGeneratorFactory"
     * as well as reflection to create the ChunkGeneratorType object to pass in the
     * interface object
     */
    private class createWastelandGenerator implements InvocationHandler
    {
        private Object factoryProxy;
        private Class factoryClass;

        createWastelandGenerator(){
            //reflection hack, dev = mapped in dev enviroment, prod = intermediate value
            String dev_name = "net.minecraft.world.gen.chunk.ChunkGeneratorFactory";
            String prod_name = "net.minecraft.class_2801";

            try {
                factoryClass = Class.forName(dev_name);
            } catch (ClassNotFoundException e1){
                try {
                    factoryClass = Class.forName(prod_name);
                }catch (ClassNotFoundException e2){
                    throw(new RuntimeException("Unable to find " + dev_name));
                }
            }
            factoryProxy = Proxy.newProxyInstance(factoryClass.getClassLoader(),
                    new Class[] {factoryClass},
                    this);
        }

        public WastelandChunkGenerator createProxy(World w, BiomeSource biomesource, WastelandChunkGeneratorConfig gensettings) {
            return new WastelandChunkGenerator(w,biomesource,gensettings);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(args.length == 3 &&
                args[0] instanceof World &&
                args[1] instanceof BiomeSource &&
                args[2] instanceof WastelandChunkGeneratorConfig
            ){

                return createProxy((World)args[0],
                        (BiomeSource)args[1],
                        (WastelandChunkGeneratorConfig)args[2]);
            }
            throw(new UnsupportedOperationException("Unknown Method: " + method.toString()));
        }

        public ChunkGeneratorType getChunkGeneratorType(Supplier<WastelandChunkGeneratorConfig> supplier){
            Constructor<?>[] initlst = ChunkGeneratorType.class.getDeclaredConstructors();
            final Logger log = LogManager.getLogger("ChunkGenErr");

            for(Constructor<?> init : initlst){
                init.setAccessible(true);
                if(init.getParameterCount() != 3){
                    continue; //skip
                }
                //lets try it
                try {
                    return (ChunkGeneratorType) init.newInstance(factoryProxy, true, supplier);
                }
                catch (Exception e){
                    log.error("Error in calling Chunk Generator Type", e);
                }
            }
            log.error("Unable to find constructor for ChunkGeneratorType");
            return null;
        }
    }

    public interface SetEffectiveTool{
        void ezAddToEfectiveToolList(Block block);
    }
}