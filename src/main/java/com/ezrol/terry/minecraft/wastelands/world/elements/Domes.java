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

package com.ezrol.terry.minecraft.wastelands.world.elements;

import com.ezrol.terry.minecraft.wastelands.api.AbstractElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dome Generator Module
 * Generates various size domes (flat cones) on the surface of the wastelands.  The three types allow us to mimic the
 * three domes generated per 64x64 region in the pre 1.10.x version of ezwastelands, however we can generate many more
 * domes at a wider range of sizes.
 * <p>
 * Created by ezterry on 9/6/16.
 */
public class Domes extends AbstractElement {

    @Override
    public int addElementHeight(int currentoffset, int x, int z, RegionCore core, List<Object> elements) {
        poi dome;
        float dist;
        float distx;
        float distz;

        for (Object obj : elements) {
            dome = (poi) obj;

            distx = (x - dome.x);
            distz = (z - dome.z);
            dist = (float) Math.sqrt((distx * distx) + (distz * distz));
            if ((dist < dome.radius)) {
                if (dist < 0.09) {
                    currentoffset += dome.height;
                } else {
                    currentoffset += (int) ((((-1 * (dist - dome.radius)) / dome.radius) * dome.height) + 0.5);
                }
            }
        }
        return currentoffset;
    }

    @Override
    public String getElementName() {
        return "domes";
    }

    /**
     * getParamTemplate: Get the world gen params used by this module (these will have the values set and be
     * returned during generation)
     *
     * @return - list of world gen params
     */
    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();

        lst.add(new Param.IntegerParam(
                "lgmincount",
                "config.ezwastelands.domes.lgmincount.help",
                3, 0, 24));
        lst.add(new Param.IntegerParam(
                "lgmaxcount",
                "config.ezwastelands.domes.lgmaxcount.help",
                4, 0, 24));
        lst.add(new Param.IntegerParam(
                "lgradius",
                "config.ezwastelands.domes.lgradius.help",
                34, 5, 64));
        lst.add(new Param.IntegerParam(
                "lgheight",
                "config.ezwastelands.domes.lgheight.help",
                7, 3, 16));

        lst.add(new Param.IntegerParam(
                "midmincount",
                "config.ezwastelands.domes.midmincount.help",
                2, 0, 24));
        lst.add(new Param.IntegerParam(
                "midmaxcount",
                "config.ezwastelands.domes.midmaxcount.help",
                3, 0, 24));
        lst.add(new Param.IntegerParam(
                "midradius",
                "config.ezwastelands.domes.midradius.help",
                23, 4, 48));
        lst.add(new Param.IntegerParam(
                "midheight",
                "config.ezwastelands.domes.midheight.help",
                8, 3, 16));

        lst.add(new Param.IntegerParam(
                "smmincount",
                "config.ezwastelands.domes.smmincount.help",
                1, 0, 24));
        lst.add(new Param.IntegerParam(
                "smmaxcount",
                "config.ezwastelands.domes.smmaxcount.help",
                2, 0, 24));
        lst.add(new Param.IntegerParam(
                "smradius",
                "config.ezwastelands.domes.smradius.help",
                18, 3, 32));
        lst.add(new Param.IntegerParam(
                "smheight",
                "config.ezwastelands.domes.smheight.help",
                9, 3, 16));

        return lst;
    }

    /**
     * appendDomes: append a batch of domes with the provided parameters
     *
     * @param lst       - list to append to
     * @param r         - local random
     * @param x         - region x
     * @param z         - region z
     * @param minradius - min radius of a dome
     * @param maxradius - max radius of a dome
     * @param height    - max height of a dome
     * @param count     - number of domes to add with the above specs
     */
    private void appendDomes(List<Object> lst, Random r, int x, int z, int minradius, int maxradius, int height, int count) {
        poi dome;

        for (int i = 0; i < count; i++) {
            dome = new poi();
            dome.x = r.nextInt(64) + (x << 6);
            dome.z = r.nextInt(64) + (z << 6);
            if (maxradius <= minradius) {
                dome.radius = minradius;
            } else {
                dome.radius = r.nextInt(maxradius - minradius) + minradius;
            }
            dome.height = r.nextInt(height);
            lst.add(dome);
        }
    }

    /**
     * calcElements: Calculate the elements for the region cache
     *
     * @param r - Locally seeded RNG
     * @param x - the x region cord
     * @param z - the z region cord
     * @param core - current core object
     * @return - list of the points of interest
     */
    @Override
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        List<Object> elements = new ArrayList<>();
        int i;
        int cnt;

        //large domes:

        i = ((Param.IntegerParam) core.lookupParam(this, "lgmincount")).get();
        cnt = ((Param.IntegerParam) core.lookupParam(this, "lgmaxcount")).get();

        if (cnt > i) {
            cnt = i + r.nextInt(cnt - i);
        }
        appendDomes(elements, r, x, z, 4,
                ((Param.IntegerParam) core.lookupParam(this, "lgradius")).get(),
                ((Param.IntegerParam) core.lookupParam(this, "lgheight")).get(), cnt);

        //medium domes

        i = ((Param.IntegerParam) core.lookupParam(this, "midmincount")).get();
        cnt = ((Param.IntegerParam) core.lookupParam(this, "midmaxcount")).get();

        if (cnt > i) {
            cnt = i + r.nextInt(cnt - i);
        }
        appendDomes(elements, r, x, z, 3,
                ((Param.IntegerParam) core.lookupParam(this, "midradius")).get(),
                ((Param.IntegerParam) core.lookupParam(this, "midheight")).get(), cnt);

        //small domes

        i = ((Param.IntegerParam) core.lookupParam(this, "smmincount")).get();
        cnt = ((Param.IntegerParam) core.lookupParam(this, "smmaxcount")).get();

        if (cnt > i) {
            cnt = i + r.nextInt(cnt - i);
        }
        appendDomes(elements, r, x, z, 2,
                ((Param.IntegerParam) core.lookupParam(this,"smradius")).get(),
                ((Param.IntegerParam) core.lookupParam(this, "smheight")).get(), cnt);

        return (elements);
    }

    /**
     * Point of interest class used internally
     * x - x cord of the dome center
     * z - z cord of the dome center
     * radius - the actual radius of the dome base
     * height - the height of the dome
     */
    private class poi {
        private int x;
        private int z;
        private int radius;
        private int height;
    }
}
