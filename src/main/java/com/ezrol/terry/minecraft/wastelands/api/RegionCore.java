/*
 * Copyright (c) 2015-2017, Terrence Ezrol (ezterry)
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

package com.ezrol.terry.minecraft.wastelands.api;

import com.ezrol.terry.minecraft.wastelands.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("WeakerAccess,unused")
public class RegionCore {
    public static final int WASTELAND_HEIGHT = 52;
    //main set of features
    static private LinkedList<IRegionElement> mainFeatures = new LinkedList<>();
    //additional features (ran after main set)
    static private LinkedList<IRegionElement> overrideFeatures = new LinkedList<>();
    //local logger
    static private Logger log = new Logger(false);
    //preset locations (for preset screen on world creation)
    static private LinkedList<ResourceLocation> presets = new LinkedList<>();

    //parameter set for each region
    private Map<String, List<Param>> elementParams = null;

    //current cached region
    private int cachedX = 0;
    private int cachedZ = 0;
    //the cached reagon cached parameters
    private Map<String, List<Object>> cachedElements = null;

    //the current world (null if in config screen)
    private World ourWorld;


    public RegionCore(String properties,World w) {
        IRegionElement element;
        elementParams = new HashMap<>(mainFeatures.size() + overrideFeatures.size());

        ourWorld=w;
        //prepare the default parameters
        for (Iterator<IRegionElement> i = new FeatureIterator(); i.hasNext(); ) {
            element = i.next();
            elementParams.put(element.getElementName(), element.getParamTemplate());
        }

        //if provided load the json string
        if (properties != null && !properties.equals("")) {
            importJson(properties);
        }
    }

    /**
     * register a new element for world generation
     * (always creates a non override element to be processed in order they are registered)
     *
     * @param element: Your custom IRegionElement
     */
    static public void register(IRegionElement element) {
        register(element, false);
    }

    /**
     * register a new element for world generation
     *
     * @param element:    Your custom IRegionElement
     * @param isOverride: Event for override elements are processed after all the non overrides
     *                    process
     */
    static public void register(IRegionElement element, boolean isOverride) {
        if (isOverride) {
            overrideFeatures.add(element);
        } else {
            mainFeatures.add(element);
        }
    }

    /**
     * Add a presets.txt file append to the resources in the presets screen
     * @param p the location of the presets.txt file to append to the presets screen
     */
    static public void registerPreset(ResourceLocation p){
        presets.add(p);
    }

    static public List<ResourceLocation> getPresetLocations(){
        return(Collections.unmodifiableList(presets));
    }
    /**
     * The current parameter map (for all loaded elements)
     * this is primarily used by the config screen
     *
     * @return the map of element parameters
     */
    public Map<String, List<Param>> getCurrentParamMap() {
        return (elementParams);
    }

    /**
     * Check if world features (ie structures) are enabled.
     * @return true if the world was created with structures enabled.
     */
    public boolean isStructuresEnabled() {
        return ourWorld != null && ourWorld.getWorldInfo().isMapFeaturesEnabled();
    }

    /**
     * return all the parameters for a specific element
     *
     * @param e the element to find the parameters of
     * @return the list of parameters
     */
    public List<Param> getElementParams(IRegionElement e){
        return elementParams.get(e.getElementName());
    }

    /**
     * Lookup an elements sepcific parameter (shortcut)
     *
     * @param e the element we are looking up
     * @param name the name of the parameter we want
     * @return the found parameter (or null)
     */
    public Param lookupParam(IRegionElement e,String name){
        List<Param> eParam = getElementParams(e);
        if(eParam != null){
            return(Param.lookUp(eParam,name));
        }
        return null;
    }

    /**
     * From a global X/Z block cord return the local random number generator
     **/
    private Random regionRandom(int x, int z, long seed) {
        Random r;
        long localSeed;

        //Cord to 64x64 region
        x = x >> 6;
        z = z >> 6;

        // generate a local seed from cords/seed
        localSeed = (((long) x) << 26) + (((long) z) << 2);
        localSeed = localSeed ^ seed;
        localSeed += 2791;

        log.info("seed: " + String.valueOf(localSeed) + " @ " + String.valueOf(x) + "x" + String.valueOf(z));

        r = new Random(localSeed);
        /*
         * ignore the first random result for near seed issue
         * http://stackoverflow.com/questions/12282628/why-are-initial-random-numbers-similar-when-using-similar-seeds
         */
        r.nextInt();
        r.nextInt();
        return r;
    }

    private synchronized Map<String, List<Object>> getRegionElements(int x, int z, long seed) {
        IRegionElement element;
        Random rand;
        List<Object> current;
        String elementName;


        if (cachedElements != null && cachedX == (x >> 6) && cachedZ == (z >> 6)) {
            return (cachedElements);
        }

        cachedElements = new HashMap<>(mainFeatures.size() + overrideFeatures.size());

        for (int localX = -128; localX <= 128; localX += 64) {
            for (int localZ = -128; localZ <= 128; localZ += 64) {
                rand = regionRandom(localX + x, localZ + z, seed);

                for (Iterator<IRegionElement> i = new FeatureIterator(); i.hasNext(); ) {
                    element = i.next();
                    elementName = element.getElementName();

                    if (cachedElements.containsKey(elementName)) {
                        current = cachedElements.get(elementName);
                    } else {
                        current = new ArrayList<>();
                    }
                    current.addAll(element.calcElements(rand, (localX + x) >> 6,
                            (localZ + z) >> 6,this));
                    cachedElements.put(elementName, current);
                }
            }
        }
        cachedX = (x >> 6);
        cachedZ = (z >> 6);
        return (cachedElements);
    }

    /**
     * get our world object
     *
     * @return the terrain generators world
     */
    public World getWorld(){
        return ourWorld;
    }

    /**
     * A function for the elements to request the parameters at a position (such as in postPointFill)
     * @param x - x cord to lookup
     * @param z - z cord to lookup
     * @param e - return the parameters to IRegionElement e
     * @return list of parameters from calcElements
     */
    public List<Object> getRegionElements(int x, int z,IRegionElement e) {
        Map<String, List<Object>> worldElements = getRegionElements(x, z, ourWorld.getSeed());
        return(worldElements.get(e.getElementName()));
    }

    public int addElementHeight(int x, int z) {
        return(forEachElement((e,h) -> (
                e.addElementHeight(h,x,z,this,getRegionElements(x,z,e))
        ), WASTELAND_HEIGHT));
    }

    public void postPointFill(ChunkPrimer chunkprimer, int height, int x, int z) {
        forEachElement((e,nop)->{
            e.postFill(chunkprimer,height,x,z,this);
            return nop;
        },null);
    }

    public void additionalTriggers(String event, IChunkGenerator gen, ChunkPos chunkCord,
                                   ChunkPrimer chunkprimer) {
        forEachElement((e,nop)->{
            e.additionalTriggers(event, gen, chunkCord, chunkprimer,this);
            return nop;
        },null);
    }

    public BlockPos getNearestStructure(String name,BlockPos curPos,boolean findUnexplored){
        //search for the nearest instance of the structure
        return forEachElement((e,p) ->{
            BlockPos pos = e.getNearestStructure(name,curPos,findUnexplored,this);
            if(pos == null){
                return p;
            }
            else if(p == null){
                return pos;
            }
            else{
                if(curPos.distanceSq(p)>curPos.distanceSq(pos)){
                    return pos;
                }
                return p;
            }
        },null);
    }

    public boolean isInsideStructure(String structureName, BlockPos pos) {
        return forEachElement((e,found) -> found || e.isInsideStructure(structureName, pos, this),false);
    }

    /**
     * Get the json string of the current configuration
     *
     * @return json string
     */
    public String getJson() {
        JsonObject root = new JsonObject();
        JsonObject elementObj;

        //Map<String, List<Param>> elementParams
        Iterator<String> elementItr;
        Iterator<Param> paramIter;
        List<Param> element;
        Param p;
        String key;

        String json;

        for (elementItr = elementParams.keySet().iterator(); elementItr.hasNext(); ) {
            key = elementItr.next();
            element = elementParams.get(key);
            elementObj = new JsonObject();

            for (paramIter = element.iterator(); paramIter.hasNext(); ) {
                p = paramIter.next();
                elementObj.add(p.getName(), p.exportJson());
            }
            root.add(key, elementObj);
        }
        json = root.toString();
        json = json.replace("\n", "");
        return (json);
    }

    /**
     * Import params from a json string
     **/
    private void importJson(String json) {
        JsonParser parser = new JsonParser();
        JsonElement tree = null;

        try {
            tree = parser.parse(json);
            if (!tree.isJsonObject()) {
                tree = null;
            }
        } catch (JsonSyntaxException e) {
            log.error("Error reading world json, using default");
            log.error(e.toString());
        }

        if (tree != null) {
            Iterator<IRegionElement> i;
            IRegionElement element;
            JsonElement configParams;
            JsonElement configParam;
            String elementName;

            //load feature parameters
            for (i = new FeatureIterator(); i.hasNext(); ) {
                element = i.next();
                elementName = element.getElementName();

                if(!((JsonObject) tree).has(elementName)){
                    log.warn(String.format("Missing params for \"%s,\" using defaults",elementName));
                    continue;
                }
                configParams = ((JsonObject) tree).get(elementName);
                if (!configParams.isJsonObject()) {
                    log.error("Expected JsonObject for " + elementName);
                    continue;
                }
                for(Param p : (elementParams.get(elementName))){
                    configParam = ((JsonObject) configParams).get(p.getName());
                    if (configParam != null) {
                        p.importJson(configParam);
                    }
                }
            }
        }
    }

    /***
     * Private iterator to loop mainFeatures then overrideFeatures
     **/
    static private class FeatureIterator implements Iterator<IRegionElement> {
        private boolean main;
        private Iterator<IRegionElement> par;

        private FeatureIterator() {
            main = true;
            par = mainFeatures.iterator();
        }

        public boolean hasNext() {
            if (par.hasNext()) {
                return true;
            }
            if (main) {
                main = false;
                par = overrideFeatures.iterator();
                return (par.hasNext());
            }
            return false;
        }

        public IRegionElement next() {
            if (main) {
                try {
                    return (par.next());
                } catch (NoSuchElementException e) {
                    main = false;
                    par = overrideFeatures.iterator();
                }
            }
            return (par.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    /**
     * apply an action for each registered IRegionElement
     *
     * @param op Operation to conduct (lambda result op(IRegionElement,currentValue))
     * @param initial the initial value (passed in as currentValue on the first call
     * @param <RVAL> the object you are operating on
     * @return the final result after the last call
     */
    static private <RVAL> RVAL forEachElement(BiFunction<IRegionElement,RVAL,RVAL> op,RVAL initial){
        RVAL current=initial;
        for(Iterator<IRegionElement> itr = new FeatureIterator();itr.hasNext();){
            current=op.apply(itr.next(),current);
        }
        return current;
    }
}
