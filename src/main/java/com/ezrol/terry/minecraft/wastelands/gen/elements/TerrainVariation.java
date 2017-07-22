/*
 * Copyright (c) 2016, Terrence Ezrol (ezterry)
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

package com.ezrol.terry.minecraft.wastelands.gen.elements;

import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Terrain Variation,
 * Not quite rolling hills, but slow changes to the elevation of the land.
 * <p>
 * Created by ezterry on 9/6/16.
 */
public class TerrainVariation implements IRegionElement {

    public TerrainVariation() {
        RegionCore.register(this);
    }

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements) {
        int cnt = elements.size();
        int weight;
        int totalWeight = 0;
        long localVariation = 0;
        attractors attract;
        float dist;
        float distx;
        float distz;

        if (cnt == 0) {
            return currentoffset;
        }
        for (Object o : elements) {
            attract = (attractors) o;
            distx = (x - attract.x);
            distz = (z - attract.z);
            dist = (float) Math.sqrt((distx * distx) + (distz * distz));

            weight = 110 - ((int) dist);
            if (weight <= 0) {
                continue;
            }
            weight = (int) Math.pow((double) weight, 1.2);
            totalWeight += weight;
            localVariation += (long) weight * (long) attract.height;
        }
        if (totalWeight > 0) {
            localVariation = localVariation / ((long) totalWeight);
        } else {
            localVariation = 0;
        }
        return (currentoffset + (int) localVariation);
    }

    @Override
    public String getElementName() {
        return "terrainvariation";
    }

    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();
        lst.add(new Param.IntegerParam(
                "amplification", "config.ezwastelands.terrainvariation.amplification.help", 30, 0, 48));
        lst.add(new Param.IntegerParam(
                "variation", "config.ezwastelands.terrainvariation.variation.help", 1, 1, 5));
        return lst;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public List<Object> calcElements(Random r, int x, int z, List<Param> p, RegionCore core) {
        List<Object> elements = new ArrayList<>();
        int variation = ((Param.IntegerParam) Param.lookUp(p, "variation")).get();
        int amplification = ((Param.IntegerParam) Param.lookUp(p, "amplification")).get();
        boolean duplicate;
        attractors node;
        attractors testNode;

        if (amplification == 0) {
            //nothing to do
            return elements;
        }

        for (int i = 0; i < variation; i++) {
            node = new attractors();
            node.x = r.nextInt(64) + (x << 6);
            node.z = r.nextInt(64) + (z << 6);

            duplicate = false;
            for (Object o : elements) {
                testNode = (attractors) o;
                if (testNode.x == node.x && testNode.z == node.z) {
                    duplicate = true;
                }
            }
            if (duplicate) {
                i--;
                continue;
            }
            node.height = r.nextInt(amplification);
            elements.add(node);
        }

        return elements;
    }

    @Override
    public void postFill(ChunkPrimer chunkprimer, int height, int x, int z, long worldSeed, List<Param> p, RegionCore core) {

    }

    @Override
    public void additionalTriggers(String event, IChunkGenerator gen, ChunkPos cords, World worldobj,
                                   boolean structuresEnabled, ChunkPrimer chunkprimer, List<Param> p, RegionCore core) {

    }

    @Override
    public BlockPos getNearestStructure(String name, BlockPos curPos, boolean findUnexplored, RegionCore core) {
        return null;
    }

    @Override
    public boolean isInsideStructure(String structureName, BlockPos pos, RegionCore core) {
        return false;
    }

    private class attractors {
        protected int x;
        protected int z;
        protected int height;
    }
}
