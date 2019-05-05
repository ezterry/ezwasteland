package com.ezrol.terry.minecraft.wastelands.world.elements;

import com.ezrol.terry.minecraft.wastelands.api.AbstractElement;
import com.ezrol.terry.minecraft.wastelands.api.Param;
import com.ezrol.terry.minecraft.wastelands.api.RegionCore;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmoothEngine extends AbstractElement {
    private enum MODE{
        DISABLED,
        BOX,
        GAUSSIAN
    }

    final static private double[] GAUSSIAN_WEIGHTS = {0.06136, 0.24477, 0.38774, 0.24477, 0.06136};

    private final AtomicBoolean inChildCalc= new AtomicBoolean();

    /**
     * The name of our generation element
     * @return "smooth"
     */
    @Override
    public String getElementName() {
        return "smooth";
    }

    /**
     * The template of the smoothing configuration options
     * (Enable box blur by default)
     *
     * @return list of possible params
     */
    @Override
    public List<Param> getParamTemplate() {
        List<Param> lst = new ArrayList<>();

        lst.add(new Param.BooleanParam(
                "enable",
                "config.ezwastelands.smooth.enable.help",
                true));
        lst.add(new Param.BooleanParam(
                "gaussian",
                "config.ezwastelands.smooth.gaussian.help",
                false));

        return lst;
    }

    /**
     * for speed calculate the world blur settings, and pass it as  our "calculated elements"
     * particuarly important for disabled mode
     *
     * @param r an rng for the region (unused)
     * @param x x cord of the region
     * @param z z cord of the region
     * @param core the region core object
     * @return a list of 1 element (MODE)
     */
    @Override
    public List<Object> calcElements(Random r, int x, int z, RegionCore core) {
        List<Object> ret = new ArrayList<>();
        MODE localMode = MODE.DISABLED;

        boolean isEnabled  = ((Param.BooleanParam) core.lookupParam(this, "enable")).get();
        boolean isGaussian = ((Param.BooleanParam) core.lookupParam(this, "gaussian")).get();

        if(isEnabled){
            localMode = MODE.BOX;
            if(isGaussian){
                localMode = MODE.GAUSSIAN;
            }
        }

        ret.add(localMode);
        return(ret);
    }

    @Override
    public int addElementHeight(int currentOffset, int x, int z, RegionCore core, List<Object> elements) {
        MODE mode = (MODE) elements.get(0);
        switch (mode){
            case DISABLED:
                return super.addElementHeight(currentOffset, x, z, core, elements);
            case BOX:
                return calcBoxBlur(currentOffset, x, z, core);
            case GAUSSIAN:
                return calcGaussianBlur(currentOffset, x, z, core);
        }
        throw(new InvalidParameterException("Unknown MODE " + mode));
    }

    private int calcBoxBlur(int current, int x, int z, RegionCore core){
        int result = current;

        synchronized (inChildCalc){
            if(!inChildCalc.get()){
                //we need to calculate the blur
                inChildCalc.set(true);
                long sum = 0;
                int orig;
                sum+=core.addElementHeight(x-1,z-1);
                sum+=core.addElementHeight(x-1,z);
                sum+=core.addElementHeight(x-1,z+1);

                sum+=core.addElementHeight(x,z-1);
                orig = core.addElementHeight(x,z);
                sum+=orig;
                sum+=core.addElementHeight(x,z+1);

                sum+=core.addElementHeight(x+1,z-1);
                sum+=core.addElementHeight(x+1,z);
                sum+=core.addElementHeight(x+1,z+1);

                inChildCalc.set(false);

                result = (int)(sum / 9L);

                //remove any unprocessed offset
                result -= (orig - current);
            }
        }
        return result;
    }

    private int calcGaussianBlur(int current, int x, int z, RegionCore core){
        int result = current;
        double[][] table = null;

        synchronized (inChildCalc){
            if(!inChildCalc.get()) {
                //we need to calculate the blur,
                //to minimize lock time, get all values, then unlock before calculating actual blur

                table = new double[5][5];

                inChildCalc.set(true);

                for(int xoff = 0; xoff < 5; xoff++){
                    for(int zoff = 0; zoff < 5; zoff++) {
                        table[xoff][zoff] = (double)core.addElementHeight((x-2)+xoff, (z-2) + zoff);
                    }
                }
                inChildCalc.set(false);
            }
        }
        if(table != null){
            //calculate final value
            double[] internal=new double[5];

            //pass 1 (the 5 strips in the table
            for(int i=0;i<5;i++){
                internal[i]=calcGaussianStrip(table[i]);
            }
            //pass 2 the result strip
            result=(int)(calcGaussianStrip(internal));

            //remove any unprocessed offset
            result -= ((int)table[2][2] - current);
        }
        return result;
    }

    private double calcGaussianStrip(double[] elements){
        if(elements.length != 5){
            throw(new InvalidParameterException("Expected double[5] as elements parameter, however got length of: " + elements.length));
        }
        double result = 0;
        for(int i=0;i<5;i++){
            result += elements[i] * GAUSSIAN_WEIGHTS[i];
        }
        return result;
    }
}
