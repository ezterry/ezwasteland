package com.ezrol.terry.minecraft.wastelands.api;

import java.util.List;
import java.util.Random;

public interface IRegionElement {
	/** update current offset to include your elements change, and return the result **/
	public int addElementHeight(int currentoffset, Random r, int x, int z, RegionCore core, List<Object> elements);
	/** element name **/
	public String getElementName();
	/** get the clean list of parameters and types **/
	public List<Param> getParamTemplate();
	/** calculate a regions elements **/
	public List<Object> calcElements(Random r, int x, int z,List<Param> p);
}
