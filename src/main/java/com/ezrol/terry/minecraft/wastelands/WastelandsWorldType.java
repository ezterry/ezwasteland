package com.ezrol.terry.minecraft.wastelands;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

public class WastelandsWorldType extends WorldType {
	
	public WastelandsWorldType(){
		super("ezwastelands");
	}
	@Override
	public boolean hasVoidParticles(boolean flag){
		return false;
	}
	@Override
	public double voidFadeMagnitude(){
		return 1.0d;
	}
	@Override
	public float getCloudHeight(){
		return 200.0f;
	}
	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions){
		return(
		  new WastelandChunkProvider(world, world.getSeed()));
	}
	@Override
	public double getHorizon(World world)
    {
        return 50.0D;
    }
	@Override
	public int getMinimumSpawnHeight(World world)
    {
        return 50;
    }
}
