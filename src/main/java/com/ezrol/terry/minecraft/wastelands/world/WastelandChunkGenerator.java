package com.ezrol.terry.minecraft.wastelands.world;

import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;;

public class WastelandChunkGenerator extends ChunkGenerator<WastelandChunkGeneratorSettings>{

    public WastelandChunkGenerator(World world, BiomeSource biomeGen, WastelandChunkGeneratorSettings settings){
        super(world,biomeGen,settings);
    }

    @Override
    public void buildSurface(Chunk chunk) {

    }

    @Override
    public int method_12100() {
        return 1;
    }

    @Override
    public void populateNoise(IWorld iWorld, Chunk chunk) {

    }

    @Override
    public int produceHeight(int i, int i1, Heightmap.Type type) {
        return 52;
    }
}