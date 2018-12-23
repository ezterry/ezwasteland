package com.ezrol.terry.minecraft.wastelands.world;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.OverworldChunkGeneratorSettings;

public class WastelandChunkGeneratorSettings extends OverworldChunkGeneratorSettings {
    public WastelandChunkGeneratorSettings(){
        super();

        //stronghold - distance
        field_13142 = 48;
        //stronghold - count
        field_13141 = 196;
        //stronghold - spread
        field_13140 = 5;

    }
}
