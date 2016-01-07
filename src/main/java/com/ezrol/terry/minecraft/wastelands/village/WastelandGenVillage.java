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

import java.util.Random;

import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;

import com.ezrol.terry.minecraft.wastelands.EzWastelands;
import com.ezrol.terry.minecraft.wastelands.WastelandChunkManager;

public class WastelandGenVillage extends MapGenVillage {
	private long worldSeed;

	public WastelandGenVillage(long seed) {
		super();
		worldSeed = seed;
	}

	@Override
	protected boolean canSpawnStructureAtCoords(int chunk_x, int chunk_z) {
		/* Generate the minecraft villages if enabled */

		int region_x = (chunk_x >> 2);
		int region_z = (chunk_z >> 2);
		if (region_x % 2 == 0 || region_z % 2 == 0) {
			return false;
		}
		Random r = new Random((region_x) * worldSeed + (region_z * 5737)
				+ worldSeed + (long) 10);
		// 787 = prime near 800 for better random numbers
		if (r.nextInt(787) <= EzWastelands.villageRate) {
			// this region has a village, now determine if the chunk has one
			if (chunk_x == (region_x << 2) + r.nextInt(3)) {
				if (chunk_z == (region_z << 2) + r.nextInt(3)) {
					// this is the chunk
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getStructureName() {
		return "WastelandVillage";
	}

	@Override
	protected synchronized StructureStart getStructureStart(int chunk_x,
			int chunk_z) {
		WorldChunkManager world = this.worldObj.getWorldChunkManager();
		MapGenVillage.Start village;
		if (world instanceof WastelandChunkManager) {
			((WastelandChunkManager) world).setAllBiomesViable();
			village = new MapGenVillage.Start(this.worldObj, this.rand,
					chunk_x, chunk_z, 0);
			((WastelandChunkManager) world).unsetAllBiomesViable();
		} else {
			village = new MapGenVillage.Start(this.worldObj, this.rand,
					chunk_x, chunk_z, 0);
		}

		return village;
	}

}
