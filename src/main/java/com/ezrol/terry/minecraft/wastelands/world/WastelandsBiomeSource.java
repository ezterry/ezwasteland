/*
 * Copyright (c) 2015-2019, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.ezrol.terry.minecraft.wastelands.world;

import com.ezrol.terry.minecraft.wastelands.EzwastelandsFabric;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.LinkedHashSet;
import java.util.Set;

public class WastelandsBiomeSource extends VanillaLayeredBiomeSource {
    private WastelandChunkGeneratorConfig sysGen;
    private Set<BlockState> topMaterials;

    public WastelandsBiomeSource(VanillaLayeredBiomeSourceConfig cfg, WastelandChunkGeneratorConfig gen){
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
