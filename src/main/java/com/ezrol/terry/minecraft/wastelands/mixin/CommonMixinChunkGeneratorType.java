package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGenerator;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorSettings;
import com.ezrol.terry.minecraft.wastelands.world.WastelandRegisterI;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGeneratorType.class)
abstract public class CommonMixinChunkGeneratorType<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> implements WastelandRegisterI {
    private boolean isWastelandGen = false;
    private boolean isLocked= false;

    private static final Logger LOGGER = LogManager.getLogger("ChunkGenType");

    @Inject(at = @At("HEAD"), method = "create", cancellable = true)
    public void create(World world_1, BiomeSource biomeSource_1, C chunkGeneratorSettings_1, CallbackInfoReturnable ci){
        isLocked = true;
        if(isWastelandGen){
            LOGGER.info("Creating generator type per wasteland logic");
            //noinspection unchecked
            ci.setReturnValue(new WastelandChunkGenerator(world_1, biomeSource_1, (WastelandChunkGeneratorSettings)chunkGeneratorSettings_1));
        }
        LOGGER.info("Creating generator type per default logic");
    }

    @Override
    public void enableWastelandGenerator(boolean state) {
        if(!isLocked){
            isLocked = true;
            isWastelandGen = true;
            LOGGER.info("Object is mapped to ezwastelands");
        }
    }
}
