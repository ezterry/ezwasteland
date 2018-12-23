package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
abstract public class ClientMixinWorld {
    @Shadow
    protected LevelProperties properties;

    @Inject(at = @At("HEAD"), method = "method_8540()D", cancellable = true)
    void method_8540(CallbackInfoReturnable ci){
        LevelGeneratorType gen = this.properties.getGeneratorType();
        if(gen == EzwastelandsFabric.WASTELANDS_LEVEL_TYPE){
            ci.setReturnValue((double)RegionCore.WASTELAND_HEIGHT);
        }
    }
}
