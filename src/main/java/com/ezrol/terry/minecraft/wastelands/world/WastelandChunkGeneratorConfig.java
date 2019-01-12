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

import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.gen.chunk.OverworldChunkGeneratorConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class WastelandChunkGeneratorConfig extends OverworldChunkGeneratorConfig {
    private String generatorOps="";
    private WeakReference<RegionCore> core;
    private boolean isBuffet;

    public WastelandChunkGeneratorConfig(){
        super();

        //stronghold - distance
        strongholdDistance = 48;
        //stronghold - count
        strongholdCount = 196;
        //stronghold - spread
        strongholdSpread = 5;

    }

    public WastelandChunkGeneratorConfig(CompoundTag tags){
        super();
        //stronghold - distance
        strongholdDistance = 48;
        //stronghold - count
        strongholdCount = 196;
        //stronghold - spread
        strongholdSpread = 5;

        if(generatorOps != null) {
            generatorOps = CompoundToJson(tags);
        }
    }

    public void assignCore(RegionCore c){
        core = new WeakReference<>(c);
    }

    public void initBuffet(boolean b){
        isBuffet = b;
    }

    public boolean buffetGen(){
        return isBuffet;
    }

    public boolean checkIfHasStructure(StructureFeature feature){
        RegionCore c = core.get();
        if(c != null){
            return c.hasStructure(feature.getName());
        }
        return false;
    }

    public String getGeneratorJson(){
        return generatorOps;
    }

    static public String CompoundToJson(CompoundTag tags){
        RegionCore core = new RegionCore("", null,null);

        System.out.println("Compound: " + tags);
        Map<String, List<Param>> mapping = core.getCurrentParamMap();
        CompoundTag child;

        for(String name : mapping.keySet()){
            try{
                if(!tags.containsKey(name)){
                    //skip
                    continue;
                }
                child = tags.getCompound(name);
            }
            catch (Exception e){
                System.out.println("Error reading tags " +name +": " + e);
                continue;
            }
            for(Param p : mapping.get(name)){
                try {
                    if(!child.containsKey(p.getName())){
                        //skip
                        continue;
                    }
                    switch (p.getType()) {
                        case FLOAT:
                            ((Param.FloatParam)p).set(child.getFloat(p.getName()));
                            break;
                        case INTEGER:
                            ((Param.IntegerParam)p).set(child.getInt(p.getName()));
                            break;
                        case BOOLEAN:
                            ((Param.BooleanParam)p).set(child.getBoolean(p.getName()));
                            break;
                        case STRING:
                            ((Param.StringParam)p).set(child.getString(p.getName()));
                            break;
                        case NUL:
                            break;
                    }
                }
                catch (Exception e){
                    System.out.println("Error reading param tag " + name + "." + p.getName() + ": " + e);
                }
            }
        }

        System.out.println("Core: " + core.getJson());
        return(core.getJson());
    }

    static public CompoundTag CoreConfigToCompound(RegionCore core){
        CompoundTag parent=new CompoundTag();
        CompoundTag child;
        Map<String, List<Param>> mapping = core.getCurrentParamMap();

        System.out.println("Core: " + core.getJson());
        for(String name : mapping.keySet()){
            child = new CompoundTag();
            for(Param p : mapping.get(name)){
                switch (p.getType()){
                    case FLOAT:
                        child.putFloat(p.getName(), ((Param.FloatParam)p).get());
                        break;
                    case INTEGER:
                        child.putInt(p.getName(), ((Param.IntegerParam)p).get());
                        break;
                    case BOOLEAN:
                        child.putBoolean(p.getName(), ((Param.BooleanParam)p).get());
                        break;
                    case STRING:
                        child.putString(p.getName(), ((Param.StringParam)p).get());
                        break;
                    case NUL:
                        break;
                }
            }
            parent.put(name, child);
        }

        System.out.println("Compound: " + parent);
        return  parent;
    }
}
