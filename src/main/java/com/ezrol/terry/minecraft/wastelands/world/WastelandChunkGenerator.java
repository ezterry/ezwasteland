package com.ezrol.terry.minecraft.wastelands.world;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_3233;
import net.minecraft.class_3485;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class WastelandChunkGenerator extends ChunkGenerator<WastelandChunkGeneratorSettings>{
    private RegionCore core;
    static private Logger log = LogManager.getLogger("WastelandChunkGenerator");

    public WastelandChunkGenerator(World world, BiomeSource biomeGen, WastelandChunkGeneratorSettings settings){
        super(world,biomeGen,settings);

        core = new RegionCore(settings.getGeneratorJson(), world, this);
        settings.assignCore(core);
    }

    public BlockPos verifySpawn(int blockx, int blockz, boolean scan) {
        BlockPos.Mutable pos = new BlockPos.Mutable(blockx, 0, blockz);
        pos.setY(core.addElementHeight(blockx, blockz));

        int scanstart = Math.min(pos.getY(),RegionCore.WASTELAND_HEIGHT);

        if(scan && pos.getY() <=  RegionCore.WASTELAND_HEIGHT){
            return null;
        }

        Chunk chunk =core.getWorld().getChunk(blockx >> 4, blockz >> 4);
        Block block = chunk.getBlockState(pos).getBlock();

        if(block != EzwastelandsFabric.WastelandsBlock){
            log.info("no wasteland block");
            if(scan){
                return null;
            }
        }
        //scan for first air block above sea level
        pos.setY(scanstart);
        block = chunk.getBlockState(pos).getBlock();
        while (block != Blocks.AIR){

            if(block == Blocks.LAVA){
                return null;
            }

            if(pos.getY() == 255)
                return null;
            pos.setY(1 + pos.getY());
            block = chunk.getBlockState(pos).getBlock();
        }

        return pos;
    }

    @Override
    public void carve(Chunk chunk_1, GenerationStep.Carver generationStep$Carver_1) {
        //super.carve(chunk_1, generationStep$Carver_1);
    }

    @Override
    public BlockPos locateStructure(World world_1, String name, BlockPos pos, int tries, boolean unexplored) {
        //return super.locateStructure(world_1, string_1, blockPos_1, int_1, boolean_1);
        return core.getNearestStructure(name, pos, tries, unexplored);
    }

    private Random chunkBasedRNG(ChunkPos p, long seed) {
        Random r;
        long localSeed;

        long x = p.x;
        long z = p.z;

        localSeed = (x << 32) + (z * 31);
        localSeed = localSeed ^ seed;
        localSeed += 5147;

        r = new Random(localSeed);
        r.nextInt();
        r.nextInt();
        return (r);
    }

    @Override
    public void generateFeatures(class_3233 world) {
        ChunkPos pos = new ChunkPos(world.method_14336(), world.method_14339());
        int blockx = pos.x * 16;
        int blockz = pos.z * 16;

        BlockPos chunkCorner = new BlockPos(blockx, 0, blockz);
        Random rng = chunkBasedRNG(pos, world.getSeed());

        for (ConfiguredFeature<?> configuredFeature : RegionCore.getFeatureLst()) {
            configuredFeature.generate(world, this, rng, chunkCorner);
        }

        Chunk c = world.method_8392(pos.x, pos.z);
        core.additionalTriggers("featuresgen", c.getPos(), c, null);
    }

    @Override
    public void populateEntities(class_3233 class_3233_1) {
        super.populateEntities(class_3233_1);
    }

    @Override
    public void method_16129(Chunk chunk, ChunkGenerator<?> chunkGenerator_1, class_3485 resources) {
        core.additionalTriggers("populate", chunk.getPos(),chunk,resources);
    }

    @Override
    public void method_12099(World world_1, boolean boolean_1, boolean boolean_2) {
        super.method_12099(world_1, boolean_1, boolean_2);
    }

    @Override
    public List<Biome.SpawnEntry> getEntitySpawnList(EntityCategory entityCategory_1, BlockPos blockPos_1) {
        return core.getSpawnable(super.getEntitySpawnList(entityCategory_1, blockPos_1), entityCategory_1, blockPos_1);
    }

    @Override
    public void buildSurface(Chunk chunk) {
        core.additionalTriggers("surfacecleanup", chunk.getPos(), chunk, null);
    }

    @Override
    public void populateNoise(IWorld iWorld, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        int p_x = pos.x;
        int p_z = pos.z;

        BlockState bedrock = Blocks.BEDROCK.getDefaultState();
        BlockState wastelandblock = EzwastelandsFabric.WastelandsBlock.getDefaultState();

        Heightmap heights = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        int height;
        BlockState block;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                height = core.addElementHeight( x + (p_x * 16), z + (p_z * 16));
                for (int y = 0; y < 256; y++) {
                    block = null;
                    if (y <= 1) {
                        block = bedrock;
                    }
                    if (y == 1 && (((p_x + x) + (p_z + z)) % 3) == 0) {
                        block = wastelandblock;
                    }
                    if (y > 1 && y <= height/* y==height*/) {
                        block = wastelandblock;
                    }

                    if (block != null) {
                        chunk.setBlockState(new BlockPos(x,y,z),block, false);
                    }
                }
                //wasteland blocks have been filled in see if the modules have anything custom to add
                core.postPointFill(chunk, height, x + (p_x * 16), z + (p_z * 16));
                heights.method_12597(x,height,z, wastelandblock);
            }
        }

        core.additionalTriggers("chunkcleanup", pos, chunk, null);
    }

    @Override
    public int produceHeight(int x, int z, Heightmap.Type type) {
        int h = core.addElementHeight(x, z) + 1;

        return core.getWorldHeight(x,h,z);
    }

    @Override
    public int method_16398() {
        return RegionCore.WASTELAND_HEIGHT;
    }


    @Override
    public int method_12100() {
        Chunk chunk = this.world.method_8392(0, 0);
        return chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, 8, 8);
    }
}