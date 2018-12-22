package com.ezrol.terry.minecraft.wastelands.mixin;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(MiningToolItem.class)
abstract public class CommonMixinToolItem  implements EzwastelandsFabric.SetEffectiveTool {
    @Shadow
    private  Set<Block> effectiveBlocks;


    @Override
    public void ezAddToEfectiveToolList(Block block) {
        final Object self = this;
        final Logger LOGGER = LogManager.getLogger("ToolItemMixin");

        LOGGER.info("Adding block: " + block.getTranslationKey() + " to " + ((Item)self).getTranslationKey());
        effectiveBlocks.add(block);
    }
}
