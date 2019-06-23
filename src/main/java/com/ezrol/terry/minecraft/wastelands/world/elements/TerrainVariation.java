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

import com.ezrol.terry.minecraft.wastelands.api.AbstractElement;
import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Terrain Variation,
 * Not quite rolling hills, but slow changes to the elevation of the land.
 * <p>
 * Created by ezterry on 9/6/16.
 */
public class TerrainVariation extends AbstractElement {

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
            double power = 1.0;
            power += (0.2 * attract.cntperrgn);

            weight = (int) Math.pow((double) weight, power );
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
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        List<Object> elements = new ArrayList<>();
        int variation = ((Param.IntegerParam) core.lookupParam(this, "variation")).get();
        int amplification = ((Param.IntegerParam) core.lookupParam(this, "amplification")).get();
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
            node.cntperrgn = variation;
            elements.add(node);
        }

        return elements;
    }

    private class attractors {
        protected int x;
        protected int z;
        protected int height;
        protected int cntperrgn;
    }
}
