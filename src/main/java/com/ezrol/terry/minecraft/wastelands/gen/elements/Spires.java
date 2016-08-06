package com.ezrol.terry.minecraft.wastelands.gen.elements;

import com.ezrol.terry.minecraft.wastelands.Logger;
import com.ezrol.terry.minecraft.wastelands.gen.IRegionElement;
import com.ezrol.terry.minecraft.wastelands.gen.Param;
import com.ezrol.terry.minecraft.wastelands.gen.RegionCore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by ezterry on 8/5/16.
 * Generates 1x1 spires in the terrian generation
 */
public class Spires implements IRegionElement{
    private int SPIRE_COUNT=0;
    private int MAX_SIZE=1;
    static private Logger log = new Logger(false);

    /*Points of interest object*/
    private class poi{
        protected int x;
        protected int z;
        protected int size;

        @Override
        public boolean equals(Object o){
            if(o != null && o instanceof poi){
                if(((poi)o).x == x && ((poi)o).z == z) {
                    return true;
                }
            }
            return false;
        }
    }

    public Spires(){
        RegionCore.register(this);
    }

    /** update current offset to include your elements change, and return the result **/
    public int addElementHeight(int currentOffset, Random r, int x, int z, RegionCore core, List<Object> elements){
        poi spire;
        for (Iterator i = elements.iterator(); i.hasNext(); ) {
            spire = (poi)i.next();

            if(spire.x == x && spire.z == z){
                currentOffset += spire.size;
            }
        }
        return(currentOffset);
    }

    /** element name **/
    public String getElementName(){
        return("spire");
    }

    /** get the clean list of parameters and types **/
    public List<Param> getParamTemplate(){
        List<Param> lst = new ArrayList<Param>();

        lst.add(new Param.IntegerParam("count","number of spires to include",1,0,20));
        lst.add(new Param.IntegerParam("size","size (max) of the spires",5,2,10));
        return lst;
    }

    /** calculate a regions elements **/
    public List<Object> calcElements(Random r, int x, int z,List<Param> p){
        int count = ((Param.IntegerParam)p.get(SPIRE_COUNT)).get();
        List<Object> elements = new ArrayList<Object>(count*2);
        poi spire;

        for(int i=0;i<count;i++){
            spire = new poi();
            do {
                spire.x = r.nextInt(64) + x * 64;
                spire.z = r.nextInt(64) + z * 64;
                spire.size = r.nextInt(((Param.IntegerParam)p.get(MAX_SIZE)).get());
            }while(elements.contains(spire));
            elements.add(spire);
        }
        return(elements);
    }
}
