package com.ezrol.terry.minecraft.wastelands.world;

import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.types.templates.CompoundList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.OverworldChunkGeneratorSettings;
import net.minecraft.world.gen.feature.StructureFeature;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class WastelandChunkGeneratorSettings extends OverworldChunkGeneratorSettings {
    private String generatorOps="";
    private WeakReference<RegionCore> core;

    public WastelandChunkGeneratorSettings(){
        super();

        //stronghold - distance
        field_13142 = 48;
        //stronghold - count
        field_13141 = 196;
        //stronghold - spread
        field_13140 = 5;

    }

    public WastelandChunkGeneratorSettings(CompoundTag tags){
        super();
        //stronghold - distance
        field_13142 = 48;
        //stronghold - count
        field_13141 = 196;
        //stronghold - spread
        field_13140 = 5;

        if(generatorOps != null) {
            generatorOps = CompoundToJson(tags);
        }
    }

    public void assignCore(RegionCore c){
        core = new WeakReference<>(c);
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
