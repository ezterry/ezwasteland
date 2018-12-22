package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGenerator;
import com.ezrol.terry.minecraft.wastelands.world.WastelandChunkGeneratorSettings;
import com.ezrol.terry.minecraft.wastelands.world.WastelandsBiomeSource;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.OverworldChunkGeneratorSettings;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldDimension.class)
public abstract class CommonMixinOverworldDimension extends Dimension {
    public CommonMixinOverworldDimension(World world_1, DimensionType dimensionType_1) {
        super(world_1, dimensionType_1);
    }

    private static final Logger LOGGER = LogManager.getLogger("ChunkGenType");
    @Inject(at = @At(value="INVOKE",
            target="Lnet/minecraft/world/gen/chunk/ChunkGeneratorType;createSettings()Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;"), method = "createChunkGenerator", cancellable = true)
    public void createChunkGenerator(CallbackInfoReturnable ci){
        LevelGeneratorType type = this.world.getLevelProperties().getGeneratorType();
        if(type == EzwastelandsFabric.WASTELANDS_LEVEL_TYPE){
            LOGGER.info("Use wastelands generator");
            ChunkGeneratorType<WastelandChunkGeneratorSettings, WastelandChunkGenerator> ezwastelands = EzwastelandsFabric.WASTELANDS;

            WastelandChunkGeneratorSettings settings = new WastelandChunkGeneratorSettings();
            VanillaLayeredBiomeSourceConfig biomeSrcCfg = ((VanillaLayeredBiomeSourceConfig)BiomeSourceType.VANILLA_LAYERED.getConfig()).setGeneratorSettings(new OverworldChunkGeneratorSettings()).setLevelProperties(this.world.getLevelProperties());

            //noinspection unchecked
            ci.setReturnValue(
                    ezwastelands.create(this.world, new WastelandsBiomeSource(biomeSrcCfg, settings),settings)
            );
        }

    }
}
