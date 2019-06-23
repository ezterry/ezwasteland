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

package com.ezrol.terry.minecraft.wastelands.world.elements;

import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityCategory;
import net.minecraft.structure.StructureFeatures;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorConfig;
import net.minecraft.world.gen.feature.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * Random Options Generation module
 * This module takes care of many non-standard wasteland generations including:
 * - Global vertical offset
 * - Oceans/ponds
 * - Village generation
 * - Stronghold generation
 * - Additional inter-mod compatibility support
 * <p>
 * Created by ezterry on 9/6/16.
 */
public class RandomOptions implements IRegionElement {
    static private Logger log = LogManager.getLogger("Random Element");
    public static final WastelandVillage WASTELAND_VILLAGE =
            (WastelandVillage)Registry.register(Registry.FEATURE,
            "ezwastelands:wasteland_village", new WastelandVillage(WastelandVillage::WastelandVillageConfig));

    public RandomOptions() {
        Registry.register(Registry.STRUCTURE_FEATURE,  "ezwastelands:" + WASTELAND_VILLAGE.getName().toLowerCase(), WASTELAND_VILLAGE);

        Feature.STRUCTURES.put(WASTELAND_VILLAGE.getName(), WASTELAND_VILLAGE);

        ConfiguredFeature lst[] = {
                Biome.configureFeature(Feature.STRONGHOLD, FeatureConfig.DEFAULT, Decorator.NOPE, DecoratorConfig.DEFAULT),
                Biome.configureFeature(WASTELAND_VILLAGE, WastelandVillage.WastelandVillageConfig(null), Decorator.NOPE, DecoratorConfig.DEFAULT)
        };
        RegionCore.register(this,false,lst);
    }

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements) {
        return currentoffset + (Integer) elements.get(0);
    }

    @Override
    public String getElementName() {
        return "randopts";
    }

    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();
        lst.add(new Param.BooleanParam(
                "oceans",
                "config.ezwastelands.randopts.oceans.help",
                false));
        lst.add(new Param.StringParam("oceanblock",
                "config.ezwastelands.randopts.oceanblock.help",
                "minecraft:water"));
        lst.add(new Param.IntegerParam("globaloffset",
                "config.ezwastelands.randopts.globaloffset.help",
                0, -25, 25));
        lst.add(new Param.BooleanParam("villages",
                "config.ezwastelands.randopts.villages.help",
                false));
        lst.add(new Param.FloatParam("villagechance",
                "config.ezwastelands.randopts.villagechance.help",
                10.0f, 0.0f, 100.0f));
        lst.add(new Param.BooleanParam("strongholds",
                "config.ezwastelands.randopts.strongholds.help",
                false));
        return lst;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        List<Object> placeholder = new ArrayList<>();
        placeholder.add(((Param.IntegerParam) core.lookupParam(this,"globaloffset")).get());
        return placeholder;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void postFill(Chunk chunk, int height, int x, int z, RegionCore core) {
        BlockPos.Mutable position = new BlockPos.Mutable(x & 0xF, 0, z & 0xF);
        if (((Param.BooleanParam) core.lookupParam(this, "oceans")).get()) {
            BlockState water;
            Identifier oceanBlockName = new Identifier(
                    ((Param.StringParam) core.lookupParam(this, "oceanblock")).get());

            try {
                water = Registry.BLOCK.get(oceanBlockName).getDefaultState();
                if(water == null || water.getBlock() == Blocks.AIR){
                    water = Blocks.WATER.getDefaultState();
                }
            }
            catch (Exception e){
                water = Blocks.WATER.getDefaultState();
            }

            if (height < 0) {
                height = 0;
            }
            for (int i = height + 1; i <= RegionCore.WASTELAND_HEIGHT; i++) {
                position.setY(i);
                chunk.setBlockState(position, water,true);
            }
        }
    }

    private Random chunkBasedRNG(ChunkPos p, long seed) {
        Random r;
        long localSeed;

        long x = p.x;
        long z = p.z;

        localSeed = (x << 32) + (z * 31);
        localSeed = localSeed ^ seed;
        localSeed += 5147;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void additionalTriggers(String event, ChunkPos cords, Chunk chunk, StructureManager resources, RegionCore core) {
        boolean genStrongholds = ((Param.BooleanParam) core.lookupParam(this, "strongholds")).get();
        boolean genVillages = ((Param.BooleanParam) core.lookupParam(this, "villages")).get();
        float villageRate = ((Param.FloatParam) core.lookupParam(this, "villagechance")).get();
        World w = core.getWorld();

        if (event.equals("structure_start")) {
            Random rng = chunkBasedRNG(cords, w.getSeed());
            StructureFeature feature = StructureFeatures.STRONGHOLD;
            ChunkGenerator gen = core.getChunkGen();

            if (core.isStructuresEnabled() && genStrongholds) {
                StructureStart struct = null;
                if (feature.shouldStartAt(gen, rng, cords.x, cords.z)) {
                    Biome biome = gen.getBiomeSource().getBiome(
                            new BlockPos(cords.getStartX() + 9, 0, cords.getStartZ() + 9));
                    StructureStart newstruct =  feature.getStructureStartFactory().create(feature, cords.x, cords.z, biome, MutableIntBoundingBox.empty(), 0, gen.getSeed());
                    newstruct.initialize(gen, resources, cords.x, cords.z, biome);
                    struct = newstruct.hasChildren() ? newstruct : StructureStart.DEFAULT;
                    if(struct != null){
                        chunk.setStructureStart(feature.getName(), struct);
                    }
                }
            }

            feature = WASTELAND_VILLAGE;
            if(core.isStructuresEnabled() && genVillages){
                StructureStart struct = null;
                if (WASTELAND_VILLAGE.canVillageSpawn(cords, villageRate, w.getSeed())) {
                    Biome biome = gen.getBiomeSource().getBiome(
                            new BlockPos(cords.getStartX() + 9, 0, cords.getStartZ() + 9));
                    StructureStart newstruct =  feature.getStructureStartFactory().create(feature, cords.x, cords.z, biome, MutableIntBoundingBox.empty(), 0, gen.getSeed());
                    newstruct.initialize(gen, resources, cords.x, cords.z, biome);
                    struct = newstruct.hasChildren() ? newstruct : StructureStart.DEFAULT;
                    if(struct != null){
                        chunk.setStructureStart(feature.getName(), struct);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasStructure(String name, RegionCore core) {
        log.info("test " + name);
        switch(name.toLowerCase()){
            case "stronghold":
                return ((Param.BooleanParam) core.lookupParam(this, "strongholds")).get();
            case "wasteland_village":
                return ((Param.BooleanParam) core.lookupParam(this,"villages")).get();
            default:
                return false;
        }
    }

    @Override
    public BlockPos getNearestStructure(String name, BlockPos curPos, int tries, boolean findUnexplored, RegionCore core) {
        World w = core.getWorld();
        log.info("find " + name);
        switch(name.toLowerCase()){
            case "stronghold":
                if (((Param.BooleanParam) core.lookupParam(this, "strongholds")).get()) {
                    //noinspection unchecked
                    return StructureFeatures.STRONGHOLD.locateStructure(
                            core.getWorld(), core.getChunkGen(), curPos, tries, findUnexplored);
                }
                break;
            case "wasteland_village":
                if (((Param.BooleanParam) core.lookupParam(this, "villages")).get()) {
                    //noinspection unchecked
                    return WASTELAND_VILLAGE.locateStructure(
                            core.getWorld(), core.getChunkGen(), curPos, tries, findUnexplored);
                }
                break;
        }
        return null;
    }

    @Override
    public int getWorldHeight(int h,int x,int z,RegionCore core){
        //if oceans are enabled this is required to be included in the calculation
        if (((Param.BooleanParam) core.lookupParam(this, "oceans")).get()) {
            return(Math.max(RegionCore.WASTELAND_HEIGHT + 1, h));
        }
        return h;
    }

    @Override
    public List<Biome.SpawnEntry> getSpawnable(List<Biome.SpawnEntry> lst, EntityCategory creatureCategory,
                                        BlockPos pos, RegionCore core){
        /*if(creatureType == EnumCreatureType.MONSTER &&
                ((Param.BooleanParam) core.lookupParam(this, "villages")).get()){

            float rate = ((Param.FloatParam) core.lookupParam(this, "villagechance")).get();
            WastelandGenVillage vg = getVillageGen(core.getWorld(),rate);
            if(vg != null && vg.isPositionInVillageRegion(pos)) {
                //we are near a village, make zombie villages more common
                lst.add(VILLAGE_ZOMBIE);
            }
        }*/
        return lst;
    }

    private static class WastelandVillage extends VillageFeature{
        WastelandVillage(Function<Dynamic<?>, ? extends VillageFeatureConfig> fn) {
            super(fn);
        }

        private Random RegionRNG(int regionX, int regionZ, long seed) {
            Random r;
            long localSeed;

            localSeed = (((long) regionX) << 32) + (((long) regionZ) * 31);
            localSeed = localSeed ^ seed;
            localSeed += 5147;

            r = new Random(localSeed);
            r.nextInt();
            r.nextInt();
            return (r);
        }

        public static <T> VillageFeatureConfig WastelandVillageConfig(Dynamic<T> dyn) {
            return new VillageFeatureConfig("village/desert/town_centers", 6);
        }

        @Override
        public StructureFeature.StructureStartFactory getStructureStartFactory() {
            return WastelandVillage.cityCore::new;
        }

        @Override
        public boolean shouldStartAt(ChunkGenerator<?> chunkGenerator_1, Random random_1, int int_1, int int_2) {
            return false;
        }

        public static class cityCore extends StructureStart {

            public cityCore(StructureFeature<?> structureFeature_1, int int_1, int int_2, Biome biome_1, MutableIntBoundingBox mutableIntBoundingBox_1, int int_3, long long_1) {
                super(structureFeature_1, int_1, int_2, biome_1, mutableIntBoundingBox_1, int_3, long_1);
            }

            @Override
            public void initialize(ChunkGenerator<?> gen, StructureManager sm, int x, int z, Biome var5) {
                BlockPos blockPos_1 = new BlockPos(x * 16, 0, z * 16);
                VillageGenerator.addPieces(gen, sm, blockPos_1, this.children, this.random, WastelandVillageConfig(null));
                this.setBoundingBoxFromChildren();
            }
        }

        public boolean canVillageSpawn(ChunkPos c, float rate, long seed){
            int region_x = (c.x >> 2);
            int region_z = (c.z >> 2);

            boolean valid = false;

            if(region_x % 2 == 0 || region_z %2 == 0){
                return false;
            }
            Random r = RegionRNG(region_x,region_z,seed);
            if ((r.nextFloat() * 10000) <= (rate * rate)) {
                if (c.x == (region_x << 2) + r.nextInt(3)) {
                    if (c.z == (region_z << 2) + r.nextInt(3)) {
                        // this is the chunk
                        log.info(String.format("Village to spawn at: %d,%d", c.x << 4, c.z << 4));
                        valid = true;
                    }
                }
            }
            return valid;
        }



        public String getName() {
            return "Wasteland_Village";
        }
    }
}
