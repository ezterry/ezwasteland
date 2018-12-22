package com.ezrol.terry.minecraft.wastelands;

import net.fabricmc.fabric.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ShovelItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class EzWastelandBlock extends Block {
    private static Settings blockSettings(){
        FabricBlockSettings settings = FabricBlockSettings.of(Material.EARTH);
        settings.hardness((float) 0.7);
        settings.sounds(BlockSoundGroup.SAND);

        return(settings.build());
    }
    public EzWastelandBlock(){
        super(blockSettings());
    }
}
