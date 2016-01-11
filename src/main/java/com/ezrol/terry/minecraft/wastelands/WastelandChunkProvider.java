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

package com.ezrol.terry.minecraft.wastelands;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import com.ezrol.terry.minecraft.wastelands.village.WastelandGenVillage;

public class WastelandChunkProvider extends ChunkProviderGenerate {
	private BiomeGenBase[] mockGeneratedBiomes;
	private long worldSeed;
	private World localWorldObj;
	/* The current region contained in pillars/domes/shallow */
	private String regionCache;
	// The cached formation elements for the 5x5 (25) regions around centered on
	// the cached region
	private int[] pillars = new int[25 * 3];
	private int[] domes = new int[25 * 3 * 4];
	private int[] shallow = new int[25 * 4];
	private int[] localvariation = new int[25 * 3];

	private boolean structuresEnabled = true;

	private MapGenVillage villageGenerator;
	private MapGenStronghold strongholdGenerator;

	public WastelandChunkProvider(World dim, long seed) {
		super(dim, seed, false);
		localWorldObj = dim;
		worldSeed = seed;
		regionCache = "";

		villageGenerator = new WastelandGenVillage(seed);
		villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(
				villageGenerator, VILLAGE);
		structuresEnabled = dim.getWorldInfo().isMapFeaturesEnabled();
		if (EzWastelands.enableStrongholds) {
			Map<String, String> args = new Hashtable();
			args.put("count", "5");
			args.put("distance", "48.0");
			args.put("spread", "5");
			strongholdGenerator = new MapGenStronghold(args);
			strongholdGenerator = (MapGenStronghold) TerrainGen
					.getModdedMapGen(strongholdGenerator, STRONGHOLD);
		}
	}

	private int getCordOffset(int x, int z) {
		/*
		 * returns the height (y cord of the ground for position x,y in a
		 * cleanly generated world
		 * 
		 * Apologies for this code, its about as clean as manually moving a
		 * turtle in logo writer
		 */
		int base = 52;
		int offset = 0;
		long variation = 0;
		double dist;

		// a string indicating out "region" x/y this is not perfect around the
		// axis (x=0 or y=0)
		// a region is 64*64
		String region = Integer.toString(x >> 6) + "/"
				+ Integer.toString(z >> 6);
		// if this is the last region we calculated don't recalculate
		if (!region.equals(this.regionCache)) {
			// cache miss - calculate the features in the 5x5 areas around the
			// current area
			int pos = 0;
			for (int cx = -2; cx <= 2; cx++) {
				for (int cz = -2; cz <= 2; cz++) {
					Random r = new Random(((cx * 64 + x) >> 6) * worldSeed
							+ ((cz * 64 + z) >> 6) + worldSeed);
					// get pillar location
					pillars[(pos * 3) + 0] = r.nextInt(64)
							+ ((cx + (x >> 6)) * 64); // x
					pillars[(pos * 3) + 1] = r.nextInt(64)
							+ ((cz + (z >> 6)) * 64); // z
					pillars[(pos * 3) + 2] = r.nextInt(5); // y

					// dome 1 (smaller) location
					domes[(pos * 12) + 0] = r.nextInt(64)
							+ ((cx + (x >> 6)) * 64); // x
					domes[(pos * 12) + 1] = r.nextInt(64)
							+ ((cz + (z >> 6)) * 64); // z
					domes[(pos * 12) + 2] = r.nextInt(5); // y
					domes[(pos * 12) + 3] = r.nextInt(16) + 2; // breadth
					// dome 2 (larger) location
					domes[(pos * 12) + 4] = r.nextInt(64)
							+ ((cx + (x >> 6)) * 64); // x
					domes[(pos * 12) + 5] = r.nextInt(64)
							+ ((cz + (z >> 6)) * 64); // z
					domes[(pos * 12) + 6] = r.nextInt(7); // y
					domes[(pos * 12) + 7] = r.nextInt(27) + 4; // breadth
					// dome 3 (medium) location
					domes[(pos * 12) + 8] = r.nextInt(64)
							+ ((cx + (x >> 6)) * 64); // x
					domes[(pos * 12) + 9] = r.nextInt(64)
							+ ((cz + (z >> 6)) * 64); // z
					domes[(pos * 12) + 10] = r.nextInt(6); // y
					domes[(pos * 12) + 11] = r.nextInt(20) + 3; // breadth

					// shallow location
					shallow[(pos * 4) + 0] = r.nextInt(64)
							+ ((cx + (x >> 6)) * 64); // x
					shallow[(pos * 4) + 1] = r.nextInt(64)
							+ ((cz + (z >> 6)) * 64); // z
					shallow[(pos * 4) + 2] = r.nextInt(4); // y
					shallow[(pos * 4) + 3] = r.nextInt(32) + 10; // breadth

					if (EzWastelands.terainVariation > 0) {
						localvariation[(pos * 3) + 0] = r.nextInt(64)
								+ ((cx + (x >> 6)) * 64); // x
						localvariation[(pos * 3) + 1] = r.nextInt(64)
								+ ((cz + (z >> 6)) * 64); // z
						localvariation[(pos * 3) + 2] = r
								.nextInt(EzWastelands.terainVariation); // distance
					}

					pos += 1;
				}
			}
			this.regionCache = region;
		}
		// loop over the features and add the influence to the offset
		for (int i = 0; i < 25; i++) {
			// pillar
			if (pillars[(i * 3) + 0] == x && pillars[(i * 3) + 1] == z) {
				offset += pillars[(i * 3) + 2];
			}
			// dome 1
			dist = Math.sqrt((x - domes[(i * 12) + 0])
					* (x - domes[(i * 12) + 0]) + (z - domes[(i * 12) + 1])
					* (z - domes[(i * 12) + 1]));
			if (dist < domes[(i * 12) + 3]) {
				if (dist < 0.09) {
					offset += domes[(i * 12) + 2];
				} else {
					offset += (int) (((-1 * (dist - domes[(i * 12) + 3])) / domes[(i * 12) + 3]) * domes[(i * 12) + 2]) + 0.5;
				}
			}
			// dome 2
			dist = Math.sqrt((x - domes[(i * 12) + 4])
					* (x - domes[(i * 12) + 4]) + (z - domes[(i * 12) + 5])
					* (z - domes[(i * 12) + 5]));
			if (dist < domes[(i * 12) + 7]) {
				if (dist < 0.09) {
					offset += domes[(i * 12) + 6];
				} else {
					offset += (int) (((-1 * (dist - domes[(i * 12) + 7])) / domes[(i * 12) + 7]) * domes[(i * 12) + 6]) + 0.5;
				}
			}
			// dome 3
			dist = Math.sqrt((x - domes[(i * 12) + 8])
					* (x - domes[(i * 12) + 8]) + (z - domes[(i * 12) + 9])
					* (z - domes[(i * 12) + 9]));
			if (dist < domes[(i * 12) + 11]) {
				if (dist < 0.09) {
					offset += domes[(i * 12) + 10];
				} else {
					offset += (int) (((-1 * (dist - domes[(i * 12) + 11])) / domes[(i * 12) + 11]) * domes[(i * 12) + 10]) + 0.5;
				}
			}

			// shallow 1
			dist = Math.sqrt((x - shallow[(i * 4) + 0])
					* (x - shallow[(i * 4) + 0]) + (z - shallow[(i * 4) + 1])
					* (z - shallow[(i * 4) + 1]));
			if (dist < shallow[(i * 4) + 3]) {
				if (dist < 0.09) {
					offset -= shallow[(i * 4) + 2];
				} else {
					offset -= (int) (((-1 * (dist - shallow[(i * 4) + 3])) / shallow[(i * 4) + 3]) * shallow[(i * 4) + 2]) + 0.5;
				}
			}
		}
		if (EzWastelands.terainVariation > 0) {
			int weight;
			int total_weight = 0;
			for (int i = 0; i < 25; i++) {

				dist = Math.sqrt((x - localvariation[(i * 3) + 0])
						* (x - localvariation[(i * 3) + 0])
						+ (z - localvariation[(i * 3) + 1])
						* (z - localvariation[(i * 3) + 1]));
				weight = 110 - ((int) dist);
				if (weight <= 0) {
					continue;
				}
				total_weight += weight;
				variation += ((long) weight)
						* ((long) localvariation[(i * 3) + 2]);
			}
			if (total_weight > 0) {
				variation = variation / ((long) total_weight);
			} else {
				variation = 0;
			}
			// if variations > 20 requested, reduce all height by 10
			if (EzWastelands.terainVariation > 20) {
				variation -= 10;
			}
		}
		return (base + ((int) variation) + offset);
	}

	@Override
	public void replaceBlocksForBiome(int p_147422_1_, int p_147422_2_,
			Block[] p_147422_3_, byte[] p_147422_4_, BiomeGenBase[] p_147422_5_) {
		// biomes are devoid of features in our generation
	}

	@Override
	public Chunk provideChunk(int p_x, int p_z) {
		/* calculate the empty chunk */

		Chunk chunk = new Chunk(this.localWorldObj, p_x, p_z);

		this.mockGeneratedBiomes = this.localWorldObj.getWorldChunkManager()
				.loadBlockGeneratorData(this.mockGeneratedBiomes, p_x * 16,
						p_z * 16, 16, 16);
		int z = 0;
		int x = 0;
		int height;
		Block block;

		// loop over the chunk (assume max possible generation height of 96)
		for (x = 0; x < 16; ++x) {
			for (z = 0; z < 16; ++z) {
				height = this.getCordOffset((p_x * 16) + x, (p_z * 16) + z);
				for (int y = 0; y < 96; ++y) {
					block = null;
					if (y <= 1) {
						block = Blocks.bedrock;
					}
					if (y == 1 && (((p_x + x) + (p_z + z)) % 3) == 0) {
						block = EzWastelands.wastelandBlock;
					}
					if (y > 1 && y <= height) {
						block = EzWastelands.wastelandBlock;
					}

					if (block != null) {
						ExtendedBlockStorage extendedblockstorage = chunk
								.getBlockStorageArray()[y >> 4];

						if (extendedblockstorage == null) {
							extendedblockstorage = new ExtendedBlockStorage(y,
									!this.localWorldObj.provider.hasNoSky);
							chunk.getBlockStorageArray()[y >> 4] = extendedblockstorage;
						}

						extendedblockstorage.func_150818_a(x, y & 15, z, block);
						extendedblockstorage.setExtBlockMetadata(x, y & 15, z,
								0);
					}
				}
			}
		}

		
		BiomeGenBase[] abiomegenbase = this.localWorldObj
				.getWorldChunkManager().loadBlockGeneratorData(
						(BiomeGenBase[]) null, p_x * 16, p_z * 16, 16, 16);
		// villages?
		if (this.structuresEnabled) {
			this.villageGenerator.func_151539_a(this, this.localWorldObj, p_x,
					p_z, (Block[]) null);
			if (EzWastelands.enableStrongholds) {
				this.strongholdGenerator.func_151539_a(this,
						this.localWorldObj, p_x, p_z, (Block[]) null);
			}
		}
		
		byte[] abyte = chunk.getBiomeArray();
		for (int l = 0; l < abyte.length; ++l) {
			abyte[l] = (byte) abiomegenbase[l].biomeID;
		}
		chunk.generateSkylightMap();

		return chunk;
	}

	@Override
	public void populate(IChunkProvider p_73153_1_, int chunk_x, int chunk_z) {
		// Possible future structure population
		int region_x = (chunk_x >> 2);
		int region_z = (chunk_z >> 2);
		Random r = new Random((region_x) * worldSeed + (region_z) + worldSeed
				+ (long) 10);
		Random r2 = new Random((chunk_x) * worldSeed + (chunk_z) + worldSeed
				+ region_x);

		boolean flag = false;

		if (EzWastelands.modTriggers) {
			MinecraftForge.EVENT_BUS
					.post(new PopulateChunkEvent.Pre(p_73153_1_,
							this.localWorldObj, r2, chunk_x, chunk_z, flag));
		}
		if (this.structuresEnabled) {
			flag = this.villageGenerator.generateStructuresInChunk(
					this.localWorldObj, r, chunk_x, chunk_z);
			if(flag){
				//try and fix village lighting
				this.localWorldObj.getChunkFromChunkCoords(chunk_x,chunk_z).generateSkylightMap();
			}
			if (EzWastelands.enableStrongholds) {
				this.strongholdGenerator.generateStructuresInChunk(
						this.localWorldObj, r, chunk_x, chunk_z);
			}
		}
		if (EzWastelands.modTriggers) {
			MinecraftForge.EVENT_BUS
					.post(new PopulateChunkEvent.Post(p_73153_1_,
							this.localWorldObj, r2, chunk_x, chunk_z, flag));
		}
	}

	@Override
	public void recreateStructures(int chunk_x, int chunk_z) {
		int region_x = (chunk_x >> 2);
		int region_z = (chunk_z >> 2);
		Random r = new Random((region_x) * worldSeed + (region_z) + worldSeed
				+ (long) 10);
		Random r2 = new Random((chunk_x) * worldSeed + (chunk_z) + worldSeed
				+ region_x);
		if (this.structuresEnabled) {
			this.villageGenerator.func_151539_a(this, this.localWorldObj,
					chunk_x, chunk_z, (Block[]) null);
			if (EzWastelands.enableStrongholds) {
				this.strongholdGenerator.func_151539_a(this,
						this.localWorldObj, chunk_x, chunk_z, (Block[]) null);
			}
		}
	}

	// stronghold location (for eyes of ender
	@Override
	public ChunkPosition func_147416_a(World worldIn, String structureName,
			int cord_x, int cord_y, int cord_z) {
		if (EzWastelands.enableStrongholds && this.structuresEnabled
				&& "Stronghold".equals(structureName)) {
			return this.strongholdGenerator.func_151545_a(worldIn, cord_x,
					cord_y, cord_z);
		}
		return null;
	}
}
