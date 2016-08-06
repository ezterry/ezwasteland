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

package com.ezrol.terry.minecraft.wastelands.gen;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import com.ezrol.terry.minecraft.wastelands.EzWastelands;
import com.ezrol.terry.minecraft.wastelands.village.WastelandGenVillage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.event.terraingen.TerrainGen;

public class WastelandChunkProvider extends ChunkProviderOverworld {
	private Biome[] mockGeneratedBiomes;
	private boolean structuresEnabled = true;
	private long worldSeed;
	private World localWorldObj;
    private RegionCore core;

	public WastelandChunkProvider(World dim, long seed, String generatorOptions) {
		super(dim, seed, false, null);
		localWorldObj = dim;
		worldSeed = seed;
		structuresEnabled = dim.getWorldInfo().isMapFeaturesEnabled();
        core=new RegionCore(generatorOptions);
	}

	@Override
	public void replaceBiomeBlocks(int p_180517_1_, int p_180517_2_, ChunkPrimer p_180517_3_,
			Biome[] p_180517_4_) {
		// biomes are devoid of features in our generation
	}

	@Override
	public Chunk provideChunk(int p_x, int p_z) {
		/* calculate the empty chunk */
		ChunkPrimer chunkprimer = new ChunkPrimer();

		this.mockGeneratedBiomes = this.localWorldObj.getBiomeProvider()
				.getBiomes(this.mockGeneratedBiomes, p_x * 16, p_z * 16, 16, 16);

		int z = 0;
		int x = 0;
		int height;
		IBlockState block;
		IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
		IBlockState wastelandblock = EzWastelands.wastelandBlock.getDefaultState();

		for (x = 0; x < 16; ++x) {
			for (z = 0; z < 16; ++z) {
				height = 52;
                height=core.addElementHeight(height, x+(p_x*16), z+(p_z*16), this.worldSeed);
				for (int y = 0; y < 96; ++y) {
					block = null;
					if (y <= 1) {
						block = bedrock;
					}
					if (y == 1 && (((p_x + x) + (p_z + z)) % 3) == 0) {
						block = wastelandblock;
					}
					if (y > 1 && y <= height) {
						block = wastelandblock;
					}

					if (block != null) {
						chunkprimer.setBlockState(x, y, z, block);
					}
				}
			}
		}

		Chunk chunk = new Chunk(this.localWorldObj, chunkprimer, p_x, p_z);
		//byte[] abyte = chunk.getBiomeArray();
		chunk.generateSkylightMap();

		return chunk;
	}

	@Override
	public void populate(int chunk_x, int chunk_z) {
		// Possible future structure population
		int region_x = (chunk_x >> 2);
		int region_z = (chunk_z >> 2);

		boolean flag = false;
		ChunkPos chunkCord = new ChunkPos(chunk_x, chunk_z);

		/*if (EzWastelands.modTriggers) {
			net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.localWorldObj,r2, chunk_x, chunk_z,
					flag);
		}
		if (this.structuresEnabled) {
			flag = this.villageGenerator.generateStructure(this.localWorldObj, r, chunkCord);
			if (EzWastelands.enableStrongholds) {
				this.strongholdGenerator.generateStructure(this.localWorldObj, r2, chunkCord);
			}
		}
		if (EzWastelands.modTriggers) {
			net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.localWorldObj,r2, chunk_x,
					chunk_z, flag);
		}*/
	}

	@Override
	public void recreateStructures(Chunk c, int chunk_x, int chunk_z) {
		if (this.structuresEnabled) {
			/*this.villageGenerator.generate(this.localWorldObj, chunk_x, chunk_z, (ChunkPrimer) null);
			if (EzWastelands.enableStrongholds) {
				this.strongholdGenerator.generate(this.localWorldObj, chunk_x, chunk_z, (ChunkPrimer) null);
			}*/
		}
	}

	// stronghold location (for eyes of ender
	@Override
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
		/*if (EzWastelands.enableStrongholds && this.structuresEnabled && "Stronghold".equals(structureName)) {
			return this.strongholdGenerator.getClosestStrongholdPos(worldIn, position);
		}*/
		return null;
	}

	// never generate ocean monuments in the wastelands
	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}
}
