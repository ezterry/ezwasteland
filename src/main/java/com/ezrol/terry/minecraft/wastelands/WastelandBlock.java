package com.ezrol.terry.minecraft.wastelands;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class WastelandBlock extends Block {
	public WastelandBlock (Material material)
	{
		super(material);
		this.setHardness((float) 0.7);
		this.setStepSound(this.soundTypeSand);
		this.setBlockName("ezwastelandblock");
		this.setBlockTextureName("ezwastelands:ezwastelandblock");
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHarvestLevel("shovel",0);
	}
}
