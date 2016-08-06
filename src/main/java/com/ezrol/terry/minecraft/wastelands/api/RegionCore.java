package com.ezrol.terry.minecraft.wastelands.api;

import com.ezrol.terry.minecraft.wastelands.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.*;

public class RegionCore {
    static private LinkedList<IRegionElement> mainFeatures = new LinkedList<IRegionElement>();
    static private LinkedList<IRegionElement> overrideFeatures = new LinkedList<IRegionElement>();
    static private Logger log = new Logger(false);
    private Map<String, List<Param>> elementParams = null;
    private int cachedX = 0;
    private int cachedZ = 0;
    private Map<String, List<Object>> cachedElements = null;

    /* register a new wasteland element */
    static public void register(IRegionElement element) {
        register(element, false);
    }

    static public void register(IRegionElement element, boolean isOverride) {
        if (isOverride) {
            overrideFeatures.add(element);
        } else {
            mainFeatures.add(element);
        }
    }

    public RegionCore(String properties) {
        IRegionElement element;
        elementParams = new HashMap<String, List<Param>>(mainFeatures.size() + overrideFeatures.size());

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

    private Random regionRandom(int x, int z, long seed) {
        //Cord to 64x64 region
        x = x >> 6;
        z = z >> 6;
        //return a new random number generator
        return new Random((long) ((x + seed) << 16 + (z + seed)));
    }

    private Map<String, List<Object>> getRegionElements(int x, int z, long seed) {
        IRegionElement element;
        Random rand;
        List<Object> current;
        String elementName;


        if (cachedElements != null && cachedX == x && cachedZ == z) {
            return (cachedElements);
        }

        cachedElements = new HashMap<String, List<Object>>(mainFeatures.size() + overrideFeatures.size());

        for (int localX = -128; localX <= 128; localX += 64) {
            for (int localZ = -128; localZ <= 128; localZ += 64) {
                rand = regionRandom(localX + x, localZ + z, seed);

                for (Iterator<IRegionElement> i = new FeatureIterator(); i.hasNext(); ) {
                    element = i.next();
                    elementName = element.getElementName();

                    if (cachedElements.containsKey(elementName)) {
                        current = cachedElements.get(elementName);
                    } else {
                        current = new ArrayList<Object>();
                    }
                    current.addAll(element.calcElements(rand, (localX + x) >> 6,
                            (localZ + z) >> 6, elementParams.get(elementName)));
                    cachedElements.put(elementName, current);
                }
            }
        }

        return (cachedElements);
    }

    public int addElementHeight(int currentoffset, int x, int z, long seed){
        IRegionElement element;
        String elementName;
        Map<String, List<Object>> worldElements = getRegionElements(x,z,seed);
        Random rand = regionRandom(x, z, seed);

        for (Iterator<IRegionElement> i = new FeatureIterator(); i.hasNext(); ) {
            element = i.next();
            elementName = element.getElementName();

            currentoffset=element.addElementHeight(currentoffset,rand,x,z,this,worldElements.get(elementName));
        }
        return(currentoffset);
    }
    /***
     * Private iterator to loop mainFeatures then overrideFeatures
     **/
    static private class FeatureIterator implements Iterator<IRegionElement> {
        private boolean main;
        private Iterator<IRegionElement> par;

        public FeatureIterator() {
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
            Iterator<Param> pIter;
            Param curParam;

            //load feature parameters
            for (i = new FeatureIterator(); i.hasNext(); ) {
                element = i.next();
                elementName = element.getElementName();

                configParams = ((JsonObject) tree).get(elementName);
                if (!configParams.isJsonObject()) {
                    log.error("Expected JsonObject for " + elementName);
                    continue;
                }
                pIter = (elementParams.get(elementName)).iterator();
                while (pIter.hasNext()) {
                    curParam = pIter.next();
                    configParam = ((JsonObject) configParams).get(curParam.getName());
                    if (configParam != null) {
                        curParam.importJson(configParam);
                    }
                }
            }
        }
    }

    public String getJson() {
        return "";
    }
}
