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
import com.ezrol.terry.minecraft.wastelands.api.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.class_3485;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Spire Generating Module
 * Generate vertical spires in the wastelands
 * <p>
 * Created by ezterry on 8/5/16.
 */
public class Spires extends AbstractElement {
    static private Logger log = LogManager.getLogger("Spires");

    public int addElementHeight(int currentOffset, int x, int z, RegionCore core, List<Object> elements) {
        poi spire;
        for (Object s : elements) {
            spire = (poi) s;

            if (spire.x == x && spire.z == z) {
                currentOffset += spire.size;
            }
        }
        return (currentOffset);
    }

    /**
     * element name
     **/
    public String getElementName() {
        return ("spire");
    }

    /**
     * get the clean list of parameters and types
     **/
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();

        lst.add(new Param.IntegerParam("count", "config.ezwastelands.spire.count.help", 2, 0, 30));
        lst.add(new Param.IntegerParam("size", "config.ezwastelands.spire.size.help", 6, 2, 10));
        return lst;
    }

    /**
     * calculate a regions elements
     **/
    @SuppressWarnings("ConstantConditions")
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        int count = ((Param.IntegerParam) core.lookupParam(this, "count")).get();
        int maxSize = ((Param.IntegerParam) core.lookupParam(this, "size")).get();

        List<Object> elements = new ArrayList<>(count * 2);
        poi spire;

        for (int i = 0; i < count; i++) {
            spire = new poi();
            do {
                int randX = r.nextInt(64);
                int randZ = r.nextInt(64);
                spire.x = randX + (x << 6);
                spire.z = randZ + (z << 6);
                spire.size = r.nextInt(maxSize);
            } while (elements.contains(spire));

            elements.add(spire);
        }
        return (elements);
    }

    /**
     * Point of interest class used internally
     */
    private class poi {
        protected int x;
        protected int z;
        protected int size;

        /**
         * equals here is a point at the same x,z irrelevant of the size factor
         *
         * @param o - object to compare to
         * @return - true if they are a poi instance with the same x/z
         */
        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof poi) {
                if (((poi) o).x == x && ((poi) o).z == z) {
                    return true;
                }
            }
            return false;
        }
    }
}
