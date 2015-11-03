package com.ezrol.terry.minecraft.wastelands;

import scala.Int;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.WorldType;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;
//import net.minecraftforge.common.config.Configuration;
//import net.minecraftforge.common.config.Property;


@Mod(modid = EzWastelands.MODID, version = EzWastelands.VERSION, name = EzWastelands.NAME)
public class EzWastelands
{
    public static final String MODID = "ezwastelands";
    public static final String VERSION = "0.0.1";
    public static final String NAME = "EzWastelands";
    
    public static Block wastelandBlock;
    public static WorldType wastelandsWorldType;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	wastelandBlock = new WastelandBlock(Material.ground);
    	GameRegistry.registerBlock(wastelandBlock, "ezwastelandblock");
    	
    }
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("Where are we?");
        
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
		// some example code
    	wastelandsWorldType = new WastelandsWorldType();
    	int a;
    	
    	System.out.println("World Types:");
    	for(a=0;a< WorldType.worldTypes.length ;a++){
    		if(WorldType.worldTypes[a] != null)
    			System.out.println(Integer.toString(a+1) + ": " + 
    		               WorldType.worldTypes[a].getWorldTypeName());
    	}
    }
}
