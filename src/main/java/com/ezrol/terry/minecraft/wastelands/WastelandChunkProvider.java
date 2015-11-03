package com.ezrol.terry.minecraft.wastelands;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.MapGenBase;

public class WastelandChunkProvider extends ChunkProviderGenerate {
	private BiomeGenBase[] mockGeneratedBiomes;
	private long worldSeed;
	private World localWorldObj;
	private String regionCache;
	private int [] pillars = new int[25*3];
	private int [] domes = new int[25*3*4];
	private int [] shallow = new int[25*4];
	
	public WastelandChunkProvider(World dim,long seed){
		super(dim, seed, false);
		localWorldObj=dim;
		worldSeed=seed;
		regionCache="";
	}
	
	private int getCordOffset(int x,int z){
		int base=52;
		int offset = 0;
		double dist;
		
		String region = Integer.toString(x>>6) + "/" + Integer.toString(z>>6);
		if(! region.equals(this.regionCache)){
			//cache miss
			int pos = 0;
			for(int cx = -2;cx<=2;cx++){
				for(int cz = -2;cz<=2;cz++){
					Random r = new Random((long) ((cx*64+x)/64)*worldSeed + ((cz*64+z)/64)+worldSeed );
					//get pillar location
					pillars[(pos*3) + 0] = r.nextInt(64)+((cx+(x/64))*64); //x
					pillars[(pos*3) + 1] = r.nextInt(64)+((cz+(z/64))*64); //z
					pillars[(pos*3) + 2] = r.nextInt(5); //y
					
					//dome 1 (smaller) location
					domes[(pos*12) + 0] = r.nextInt(64)+((cx+(x/64))*64); //x
					domes[(pos*12) + 1] = r.nextInt(64)+((cz+(z/64))*64); //z
					domes[(pos*12) + 2] = r.nextInt(5); //y
					domes[(pos*12) + 3] = r.nextInt(16)+2; //y
					//dome 2 (larger) location
					domes[(pos*12) + 4] = r.nextInt(64)+((cx+(x/64))*64); //x
					domes[(pos*12) + 5] = r.nextInt(64)+((cz+(z/64))*64); //z
					domes[(pos*12) + 6] = r.nextInt(7); //y
					domes[(pos*12) + 7] = r.nextInt(27)+4; //y
					//dome 3 (medium) location
					domes[(pos*12) + 8] = r.nextInt(64)+((cx+(x/64))*64); //x
					domes[(pos*12) + 9] = r.nextInt(64)+((cz+(z/64))*64); //z
					domes[(pos*12) + 10] = r.nextInt(6); //y
					domes[(pos*12) + 11] = r.nextInt(20)+3; //y
					
					//shallow  location
					shallow[(pos*4) + 0] = r.nextInt(64)+((cx+(x/64))*64); //x
					shallow[(pos*4) + 1] = r.nextInt(64)+((cz+(z/64))*64); //z
					shallow[(pos*4) + 2] = r.nextInt(4); //y
					shallow[(pos*4) + 3] = r.nextInt(32)+10; //y
					pos+=1;
				}
			}
			this.regionCache = region;
		}
		for(int i=0;i<25;i++){
			//pillar
			if(pillars[(i*3)+0] == x && pillars[(i*3)+1] == z){
				offset += pillars[(i*3)+2];
			}
			//dome 1
			dist=Math.sqrt((x-domes[(i*12)+0])*(x-domes[(i*12)+0]) +
					              (z-domes[(i*12)+1])*(z-domes[(i*12)+1]));
			if(dist < domes[(i*12)+3]){
				if(dist < 0.09){
					offset += domes[(i*12)+2];
				} else {
					offset+=(int)(((-1*(dist-domes[(i*12)+3]))/(double)domes[(i*12)+3]) *
				             domes[(i*12)+2])+0.5;
				}
			}
			//dome 2
			dist=Math.sqrt((x-domes[(i*12)+4])*(x-domes[(i*12)+4]) +
					              (z-domes[(i*12)+5])*(z-domes[(i*12)+5]));
			if(dist < domes[(i*12)+7]){
				if(dist < 0.09){
					offset += domes[(i*12)+6];
				} else {
					offset+=(int)(((-1*(dist-domes[(i*12)+7]))/(double)domes[(i*12)+7]) *
				             domes[(i*12)+6])+0.5;
				}
			}
			//dome 3
			dist=Math.sqrt((x-domes[(i*12)+8])*(x-domes[(i*12)+8]) +
					              (z-domes[(i*12)+9])*(z-domes[(i*12)+9]));
			if(dist < domes[(i*12)+11]){
				if(dist < 0.09){
					offset += domes[(i*12)+10];
				} else {
					offset+=(int)(((-1*(dist-domes[(i*12)+11]))/(double)domes[(i*12)+11]) *
				             domes[(i*12)+10])+0.5;
				}
			}
			
			//shallow 1
			dist=Math.sqrt((x-shallow[(i*4)+0])*(x-shallow[(i*4)+0]) +
					              (z-shallow[(i*4)+1])*(z-shallow[(i*4)+1]));
			if(dist < shallow[(i*4)+3]){
				if(dist < 0.09){
					offset -= shallow[(i*4)+2];
				} else {
					offset-=(int)(((-1*(dist-shallow[(i*4)+3]))/(double)shallow[(i*4)+3]) *
							shallow[(i*4)+2])+0.5;
				}
			}
		}
		return(base + offset);
	}
	@Override
	public void replaceBlocksForBiome(int p_147422_1_, int p_147422_2_, Block[] p_147422_3_, byte[] p_147422_4_, BiomeGenBase[] p_147422_5_){
		//biomes are devoid of features
	}
	@Override
	public Chunk provideChunk(int p_x, int p_z){
		Chunk chunk = new Chunk(this.localWorldObj, p_x, p_z);
		
		this.mockGeneratedBiomes = this.localWorldObj.getWorldChunkManager().loadBlockGeneratorData(this.mockGeneratedBiomes, p_x * 16, p_z * 16, 16, 16);
		int z=0;
		int x=0;
		int height;
		Block block;
		
		for (x = 0; x < 16; ++x)
        {
			for (z = 0; z < 16; ++z){
		        for (int y = 0; y < 96; ++y)
		        {
		        	height=this.getCordOffset((p_x*16)+x,(p_z*16)+z);
		        	block=null;
		            if(y<=1){
		            	block=Blocks.bedrock;
		            }
		            if(y==1 && (((p_x+x)+(p_z+z))%3) == 0){
		            	block=EzWastelands.wastelandBlock;
		            }
		            if(y > 1 && y<= height){
		            	block=EzWastelands.wastelandBlock;
		            }
		
		            if (block != null)
		            {
		                ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[y >> 4];
		
		                if (extendedblockstorage == null)
		                {
		                    extendedblockstorage = new ExtendedBlockStorage(y, !this.localWorldObj.provider.hasNoSky);
		                    chunk.getBlockStorageArray()[y >> 4] = extendedblockstorage;
		                }
		
		                extendedblockstorage.func_150818_a(x, y & 15, z, block);
		                extendedblockstorage.setExtBlockMetadata(x, y & 15, z, 0);
		            }
	        	}
			}
        }
        chunk.generateSkylightMap();
        BiomeGenBase[] abiomegenbase = this.localWorldObj.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, p_x * 16, p_z * 16, 16, 16);
        byte[] abyte = chunk.getBiomeArray();

        for (int l = 0; l < abyte.length; ++l)
        {
            abyte[l] = (byte)abiomegenbase[l].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
	}
	@Override
	public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_){
		//Possible future structure population
	}
}
