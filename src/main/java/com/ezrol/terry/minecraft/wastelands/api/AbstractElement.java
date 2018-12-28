package com.ezrol.terry.minecraft.wastelands.api;

import net.minecraft.class_3485;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractElement implements IRegionElement{
    public AbstractElement(){
        RegionCore.register(this);
    }

    @Override
    public int addElementHeight(int currentOffset, int x, int z, RegionCore core, List<Object> elements) {
        return currentOffset;
    }

    @Override
    public List<Param> getParamTemplate() {
        return new ArrayList<>();
    }

    @Override
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        return new ArrayList<>();
    }

    @Override
    public void postFill(Chunk curchunk, int height, int x, int z, RegionCore core) {

    }

    @Override
    public void additionalTriggers(String event, ChunkPos cords, Chunk curchunk, class_3485 resources, RegionCore core) {

    }

    @Override
    public BlockPos getNearestStructure(String name, BlockPos curPos, int tries, boolean findUnexplored, RegionCore core) {
        return null;
    }

    @Override
    public boolean hasStructure(String name, RegionCore core) {
        return false;
    }

    @Override
    public int getWorldHeight(int h,int x,int z,RegionCore core){
        return h;
    }

    @Override
    public List<Biome.SpawnEntry> getSpawnable(List<Biome.SpawnEntry> lst, EntityCategory creatureCategory, BlockPos pos, RegionCore core) {
        return lst;
    }
}
