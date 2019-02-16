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

/* and abstract version of IRegionElenment
 * provides a "nop" module to base others off of without implementing every function
 */
package com.ezrol.terry.minecraft.wastelands.api;

import net.minecraft.entity.EntityCategory;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractElement implements IRegionElement{
    public AbstractElement(){
        RegionCore.register(this);
    }

    @Override
    public int addElementHeight(int currentOffset, int x, int z, RegionCore core, List<Object> elements) {
        return currentOffset;
    }

    @Override
    public List<Param> getParamTemplate() {
        return new ArrayList<>();
    }

    @Override
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        return new ArrayList<>();
    }

    @Override
    public void postFill(Chunk curchunk, int height, int x, int z, RegionCore core) {

    }

    @Override
    public void additionalTriggers(String event, ChunkPos cords, Chunk curchunk, StructureManager resources, RegionCore core) {

    }

    @Override
    public BlockPos getNearestStructure(String name, BlockPos curPos, int tries, boolean findUnexplored, RegionCore core) {
        return null;
    }

    @Override
    public boolean hasStructure(String name, RegionCore core) {
        return false;
    }

    @Override
    public int getWorldHeight(int h,int x,int z,RegionCore core){
        return h;
    }

    @Override
    public List<Biome.SpawnEntry> getSpawnable(List<Biome.SpawnEntry> lst, EntityCategory creatureCategory, BlockPos pos, RegionCore core) {
        return lst;
    }
}
