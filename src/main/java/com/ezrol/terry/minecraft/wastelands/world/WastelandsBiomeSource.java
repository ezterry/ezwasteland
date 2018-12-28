package com.ezrol.terry.minecraft.wastelands.world;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.LinkedHashSet;
import java.util.Set;

public class WastelandsBiomeSource extends VanillaLayeredBiomeSource {
    private WastelandChunkGeneratorSettings sysGen;
    private Set<BlockState> topMaterials;

    public WastelandsBiomeSource(VanillaLayeredBiomeSourceConfig cfg, WastelandChunkGeneratorSettings gen){
        super(cfg);
        sysGen = gen;
        topMaterials = null;
    }

    @Override
    public boolean hasStructureFeature(StructureFeature<?> structureFeature_1) {
        return sysGen.checkIfHasStructure(structureFeature_1);
    }

    @Override
    public Set<BlockState> getTopMaterials() {
        if(topMaterials == null) {
            Set<BlockState> vanilla = super.getTopMaterials();
            topMaterials = new LinkedHashSet<>(vanilla);
            topMaterials.add(EzwastelandsFabric.WastelandsBlock.getDefaultState());
        }

        return topMaterials;
    }
}
