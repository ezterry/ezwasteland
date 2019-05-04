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

package com.ezrol.terry.minecraft.wastelands.world;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityCategory;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class WastelandChunkGenerator extends ChunkGenerator<WastelandChunkGeneratorConfig> {
    private RegionCore core;
    static private Logger log = LogManager.getLogger("WastelandChunkGenerator");

    public WastelandChunkGenerator(World world, BiomeSource biomeGen, WastelandChunkGeneratorConfig config){
        super(world,biomeGen,config);

        core = new RegionCore(config.getGeneratorJson(), world, this);
        config.assignCore(core);
    }

    public BlockPos verifySpawn(int blockx, int blockz, boolean scan) {
        BlockPos.Mutable pos = new BlockPos.Mutable(blockx, 0, blockz);
        pos.setY(core.addElementHeight(blockx, blockz));

        int scanstart = Math.min(pos.getY(),RegionCore.WASTELAND_HEIGHT);

        if(scan && pos.getY() <=  RegionCore.WASTELAND_HEIGHT){
            return null;
        }

        Chunk chunk =core.getWorld().getChunk(blockx >> 4, blockz >> 4);
        Block block = chunk.getBlockState(pos).getBlock();

        if(block != EzwastelandsFabric.WastelandsBlock){
            log.info("no wasteland block");
            if(scan){
                return null;
            }
        }
        //scan for first air block above sea level
        pos.setY(scanstart);
        block = chunk.getBlockState(pos).getBlock();
        while (block != Blocks.AIR){

            if(block == Blocks.LAVA){
                return null;
            }

            if(pos.getY() == 255)
                return null;
            pos.setY(1 + pos.getY());
            block = chunk.getBlockState(pos).getBlock();
        }

        return pos;
    }

    @Override
    public void carve(Chunk chunk, GenerationStep.Carver carver) {
        if(config.buffetGen()){
            super.carve(chunk,carver);
        }
    }

    @Override
    public BlockPos locateStructure(World world_1, String name, BlockPos pos, int tries, boolean unexplored) {
        //return super.locateStructure(world_1, string_1, blockPos_1, int_1, boolean_1);
        BlockPos b = core.getNearestStructure(name, pos, tries, unexplored);
        if(b == null && config.buffetGen()){
            b = super.locateStructure(world_1,name,pos,tries,unexplored);
        }
        return b;
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
    public void generateFeatures(ChunkRegion region) {
        ChunkPos pos = new ChunkPos(region.getCenterChunkX(), region.getCenterChunkZ());
        int blockx = pos.x * 16;
        int blockz = pos.z * 16;

        BlockPos chunkCorner = new BlockPos(blockx, 0, blockz);
        Random rng = chunkBasedRNG(pos, region.getSeed());

        for (ConfiguredFeature<?> configuredFeature : RegionCore.getFeatureLst()) {
            configuredFeature.generate(region, this, rng, chunkCorner);
        }

        Chunk c = region.getChunk(pos.x, pos.z);
        core.additionalTriggers("featuresgen", c.getPos(), c, null);

        if(config.buffetGen()){
            super.generateFeatures(region);
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        super.populateEntities(region);
    }

    @Override
    public void setStructureStarts(Chunk chunk, ChunkGenerator<?> chunkGenerator, StructureManager resources) {
        core.additionalTriggers("structure_start", chunk.getPos(),chunk,resources);

        if(config.buffetGen()){
            super.setStructureStarts(chunk, chunkGenerator, resources);
        }
    }

    @Override
    public List<Biome.SpawnEntry> getEntitySpawnList(EntityCategory entityCategory_1, BlockPos blockPos_1) {
        return core.getSpawnable(super.getEntitySpawnList(entityCategory_1, blockPos_1), entityCategory_1, blockPos_1);
    }

    @Override
    public void buildSurface(Chunk chunk) {
        core.additionalTriggers("surfacecleanup", chunk.getPos(), chunk, null);
    }

    @Override
    public void populateNoise(IWorld iWorld, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        int p_x = pos.x;
        int p_z = pos.z;

        BlockState bedrock = Blocks.BEDROCK.getDefaultState();
        BlockState wastelandblock = EzwastelandsFabric.WastelandsBlock.getDefaultState();

        Heightmap heights = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Heightmap heights_ocean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);

        int height;
        BlockState block;
        boolean buffetgen = config.buffetGen();
        BlockState blocksubsurface = wastelandblock;
        BlockState blocktop = wastelandblock;
        Biome curBiome;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                height = core.addElementHeight( x + (p_x * 16), z + (p_z * 16));
                if(buffetgen){
                    curBiome = chunk.getBiome(new BlockPos(x,height,z));
                    blocksubsurface = curBiome.getSurfaceConfig().getUnderMaterial();
                    blocktop = curBiome.getSurfaceConfig().getTopMaterial();
                }
                for (int y = 0; y < 256; y++) {
                    block = null;
                    if (y <= 1) {
                        block = bedrock;
                    }
                    if (y == 1 && (((p_x + x) + (p_z + z)) % 3) == 0) {
                        block = wastelandblock;
                    }
                    if (y > 1 && y <= height/* y==height*/) {
                        block = wastelandblock;
                        if(buffetgen){
                            if(y > height - 3){
                                block = blocksubsurface;
                            }
                            if(y == height){
                                block = blocktop;
                            }
                        }
                    }

                    if (block != null) {
                        chunk.setBlockState(new BlockPos(x,y,z),block, false);
                    }
                }
                //wasteland blocks have been filled in see if the modules have anything custom to add

                heights.trackUpdate(x,height,z, wastelandblock);
                heights_ocean.trackUpdate(x,height,z,wastelandblock);

                core.postPointFill(chunk, height, x + (p_x * 16), z + (p_z * 16));
            }
        }

        core.additionalTriggers("chunkcleanup", pos, chunk, null);
    }

    @Override
    public int getHeightOnGround(int x, int z, Heightmap.Type type) {
        int h = core.addElementHeight(x, z) + 1;

        return core.getWorldHeight(x,h,z);
    }

    @Override
    public int getSeaLevel() {
        return RegionCore.WASTELAND_HEIGHT;
    }


    @Override
    public int getSpawnHeight() {
        Chunk chunk = this.world.getChunk(0, 0);
        return chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, 8, 8);
    }

}