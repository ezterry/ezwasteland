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

package com.ezrol.terry.minecraft.wastelands.village;

import com.ezrol.terry.minecraft.wastelands.EzWastelands;
import com.ezrol.terry.minecraft.wastelands.Logger;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.*;

import java.util.Random;

public class WastelandGenVillage extends MapGenVillage {
    static private Logger log = new Logger(false);
    private float rate;
    private long worldSeed;
    private static IBlockState wastelandPathBlock=Blocks.STAINED_HARDENED_CLAY.getDefaultState().
            withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
    private static IBlockState brownPowder = Blocks.CONCRETE_POWDER.getDefaultState().
            withProperty(BlockColored.COLOR,EnumDyeColor.BROWN);

    public WastelandGenVillage(long seed, float villagerate) {
        super();
        worldSeed = seed;
        rate = villagerate;
    }

    private Random RegionRNG(int regionX, int regionZ) {
        Random r;
        long localSeed;

        localSeed = (((long) regionX) << 32) + (((long) regionZ) * 31);
        localSeed = localSeed ^ worldSeed;
        localSeed += 5147;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        /* Generate the minecraft villages if enabled */

        int region_x = (chunkX >> 2);
        int region_z = (chunkZ >> 2);
        boolean valid=false;
        //only 1 in 4 regions are valid for a potential village
        if (region_x % 2 == 0 || region_z % 2 == 0) {
            return false;
        }
        Random r = RegionRNG(region_x, region_z);
        if ((r.nextFloat() * 10000) <= (rate * rate)) {
            // this region has a village, now determine if the chunk has one
            if (chunkX == (region_x << 2) + r.nextInt(3)) {
                if (chunkZ == (region_z << 2) + r.nextInt(3)) {
                    // this is the chunk
                    log.info(String.format("Village to spawn at: %d,%d", chunkX << 4, chunkZ << 4));
                    valid = true;
                }
            }
        }
        return valid;
    }

    public boolean isPositionInVillageRegion(BlockPos p){
        int region_x = (p.getX() >> 6);
        int region_z = (p.getZ() >> 6);

        //only 1 in 4 regions are valid for a potential village
        if (region_x % 2 == 0 || region_z % 2 == 0) {
            return false;
        }
        Random r = RegionRNG(region_x, region_z);
        return (r.nextFloat() * 10000) <= (rate * rate);
    }

    public synchronized void fixPathBlock(World worldIn, ChunkPos chunkCoord, RegionCore core)
    {
        this.initializeStructureData(worldIn);

        Chunk chunkDat = worldIn.getChunkFromChunkCoords(chunkCoord.x,chunkCoord.z);
        boolean changed=false;

        for (StructureStart structurestart : this.structureMap.values()) {
            if (structurestart.isSizeableStructure()) {
                //its a valid village in the list
                if(structurestart.getBoundingBox().maxX+8 < chunkCoord.getXStart() ||
                        structurestart.getBoundingBox().maxZ+8 < chunkCoord.getZStart() ||
                        structurestart.getBoundingBox().minX-8 > chunkCoord.getXEnd() ||
                        structurestart.getBoundingBox().minZ-8 > chunkCoord.getZEnd()){
                    //we are not inside the structure
                    continue;
                }
                for(int x = chunkCoord.getXStart(); x<= chunkCoord.getXEnd();x++){
                    for(int z = chunkCoord.getZStart(); z<= chunkCoord.getZEnd();z++){
                        for(StructureComponent c : structurestart.getComponents()){
                            StructureBoundingBox box = c.getBoundingBox();
                            if(x >= box.minX && x<= box.maxX && z>= box.minZ && z<= box.maxZ &&
                                    c.getClass() == StructureVillagePieces.Path.class) {
                                BlockPos pos = new BlockPos(x,core.addElementHeight(x,z),z);
                                //check the path is still a wasteland block
                                //if so change it to light gray terracotta
                                if(chunkDat.getBlockState(pos).getBlock() == EzWastelands.wastelandBlock){
                                    chunkDat.setBlockState(pos, wastelandPathBlock);
                                    changed=true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(changed){
            chunkDat.markDirty();
        }
    }

    public synchronized void reduceDirt(World worldIn, ChunkPos chunkCoord, RegionCore core)
    {
        this.initializeStructureData(worldIn);

        Chunk chunkDat = worldIn.getChunkFromChunkCoords(chunkCoord.x,chunkCoord.z);
        boolean changed=false;

        for (StructureStart structurestart : this.structureMap.values()) {
            if (structurestart.isSizeableStructure()) {
                //its a valid village in the list
                if(structurestart.getBoundingBox().maxX+8 < chunkCoord.getXStart() ||
                        structurestart.getBoundingBox().maxZ+8 < chunkCoord.getZStart() ||
                        structurestart.getBoundingBox().minX-8 > chunkCoord.getXEnd() ||
                        structurestart.getBoundingBox().minZ-8 > chunkCoord.getZEnd()){
                    //we are not inside the structure
                    continue;
                }
                for(int x = chunkCoord.getXStart(); x<= chunkCoord.getXEnd();x++){
                    for(int z = chunkCoord.getZStart(); z<= chunkCoord.getZEnd();z++){
                        for(StructureComponent c : structurestart.getComponents()){
                            StructureBoundingBox box = c.getBoundingBox();
                            if(x >= box.minX && x<= box.maxX && z>= box.minZ && z<= box.maxZ) {
                                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x,core.addElementHeight(x,z),z);
                                while(pos.getY() < 255){
                                    Block CurrentBlock = chunkDat.getBlockState(pos).getBlock();
                                    pos.setY(pos.getY()+1);
                                    Block BlockAbove = chunkDat.getBlockState(pos).getBlock();

                                    if(CurrentBlock == EzWastelands.wastelandBlock ){
                                        continue;
                                    }
                                    if(CurrentBlock == Blocks.DIRT){
                                        if(BlockAbove != Blocks.AIR && BlockAbove != Blocks.WATER &&
                                                !(BlockAbove instanceof BlockFence)){
                                            pos.setY(pos.getY()-1);
                                            chunkDat.setBlockState(pos, brownPowder);
                                            changed = true;
                                            pos.setY(pos.getY()+1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(changed){
            chunkDat.markDirty();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getStructureName() {
        return "WastelandVillage";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected synchronized StructureStart getStructureStart(int chunkX, int chunkZ) {
        MapGenVillage.Start village;

        village = new MapGenVillage.Start(this.world, this.rand, chunkX, chunkZ, 0);

        return village;
    }
}
