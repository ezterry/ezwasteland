/*
 * Copyright (c) 2015-2016, Terrence Ezrol (ezterry)
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

package com.ezrol.terry.minecraft.wastelands.village;

import com.ezrol.terry.minecraft.wastelands.Logger;
import com.ezrol.terry.minecraft.wastelands.WastelandBiomeProvider;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.Random;

public class WastelandGenVillage extends MapGenVillage {
    static private Logger log = new Logger(false);
    float rate;
    private long worldSeed;

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

    @Override
    public String getStructureName() {
        return "WastelandVillage";
    }

    @Override
    protected synchronized StructureStart getStructureStart(int chunkX, int chunkZ) {
        BiomeProvider world = this.world.getBiomeProvider();
        MapGenVillage.Start village;
        if (world instanceof WastelandBiomeProvider) {
            ((WastelandBiomeProvider) world).setAllBiomesViable();
            village = new MapGenVillage.Start(this.world, this.rand, chunkX, chunkZ, 0);
            ((WastelandBiomeProvider) world).unsetAllBiomesViable();
        } else {
            village = new MapGenVillage.Start(this.world, this.rand, chunkX, chunkZ, 0);
        }

        return village;
    }
}
