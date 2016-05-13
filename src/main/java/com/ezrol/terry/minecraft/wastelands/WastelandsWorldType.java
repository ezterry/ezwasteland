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

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WastelandsWorldType extends WorldType {

	public WastelandsWorldType() {
		super("ezwastelands");
	}

	@Override
	public double voidFadeMagnitude() {
		return 1.0d;
	}

	@Override
	public float getCloudHeight() {
		return 200.0f;
	}

	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
		return (new WastelandChunkProvider(world, world.getSeed(), generatorOptions));
	}

	@Override
	public double getHorizon(World world) {
		return 50.0D;
	}

	@Override
	public int getMinimumSpawnHeight(World world) {
		return 50;
	}

	@Override
	public BiomeProvider getBiomeProvider(World world) {
		return new WastelandBiomeProvider(world.getWorldInfo());
	}
}
