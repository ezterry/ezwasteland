package com.ezrol.terry.minecraft.wastelands.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.List;

/** Parameters of the region elements **/

public class Param {
	public enum ParamTypes {
		INTEGER,
		FLOAT,
		BOOLEAN,
		STRING,
		NUL
	}
	
	private String name;
	protected ParamTypes type;
	private String comment;
	
	/** Make a null param type **/
	public Param(String name){
		this.comment = "";
		this.name = name;
		this.type = ParamTypes.NUL;
	}
	
	public Param(String name,String comment){
		this.comment = comment;
		this.name = name;
		this.type = ParamTypes.NUL;
	}
	
	/**
	 * Look up a Param in a list by name
	 * returns null if not found
	 */
	static Param lookUp(List<Param> lst,String name){
		Param entry;
		for(int i=0;i<lst.size();i++){
			entry = lst.get(i);
			if(entry.getName().equals(name)){
				return(entry);
			}
		}
		return(null);
	}
	
	/** Get the parameter type **/
	public ParamTypes getType(){
		return(type);
	}
	
	/** Get the parameter name **/
	public String getName(){
		return(name);
	}
	
	/** Get the parameter's help description **/
	public String getComment(){
		return(comment);
	}
	
	/** Import Json, attempts to import a json element, return true if success, else false
	 **/
	public boolean importJson(JsonElement e){
		return(e.isJsonNull());
	}
	
	public JsonElement exportJson(){
		return(JsonNull.INSTANCE);
	}
	
	/** String sub type **/
	public static class StringParam extends Param{
		private String defvalue;
		private String value;
		
		public StringParam(String name,String comment,String def){
			super(name,comment);
			this.type = ParamTypes.STRING;
			this.defvalue = def;
			this.value = def;
		}
		
		/** Get the string value, or return the default if "" or null **/
		public String validate(String v){
			if(v == null || v.equals("")){
				return defvalue;
			}
			return(v);
		}
		
		public String getDefault(){
			return(defvalue);
		}
		
		public void set(String v){
			this.value = validate(v);
		}
		
		public String get(){
			return value;
		}
		
		@Override
		public boolean importJson(JsonElement e){
			if(e.isJsonPrimitive()){
				try{
					set(e.getAsString());
					return true;
				}
				catch(ClassCastException err){
					//invalid type
				}
			}
			return false;
		}
		
		@Override
		public JsonElement exportJson(){
			return(new JsonPrimitive(this.value));
		}
	}
	
	/** Boolean sub type **/
	public static class BooleanParam extends Param{
		private boolean defvalue;
		private boolean value;
		
		public BooleanParam(String name,String comment,boolean def){
			super(name,comment);
			this.type = ParamTypes.BOOLEAN;
			this.defvalue = def;
			this.value = def;
		}
		
		/** Get the string value, or return the default if "" or null **/
		public boolean validate(boolean v){
			return(v);
		}
		
		public boolean getDefault(){
			return(defvalue);
		}
		
		public void set(boolean v){
			this.value = validate(v);
		}
		
		public boolean get(){
			return value;
		}
		
		@Override
		public boolean importJson(JsonElement e){
			if(e.isJsonPrimitive()){
				try{
					set(e.getAsBoolean());
					return true;
				}
				catch(ClassCastException err){
					//invalid type
				}
			}
			return false;
		}
		
		@Override
		public JsonElement exportJson(){
			return(new JsonPrimitive(this.value));
		}
	}
	
	/** Float sub type **/
	public static class FloatParam extends Param{
		private float defvalue;
		private float value;
		private float min;
		private float max;
		
		public FloatParam(String name,String comment,float def){
			super(name,comment);
			this.type = ParamTypes.FLOAT;
			this.defvalue = def;
			this.min = Float.MIN_VALUE;
			this.max = Float.MAX_VALUE;
			this.value = def;
		}
		
		public FloatParam(String name,String comment,float def,float min,float max){
			super(name,comment);
			this.type = ParamTypes.FLOAT;
			this.defvalue = def;
			this.min = min;
			this.max = max;
			this.value = def;
		}
		
		/** Get the string value, or return the default if "" or null **/
		public float validate(float v){
			if(v < min){
				return(defvalue);
			}
			if(v > max){
				return(defvalue);
			}
			return(v);
		}
		
		public float getDefault(){
			return(defvalue);
		}
		
		public void set(float v){
			this.value = validate(v);
		}
		
		public float get(){
			return value;
		}
		
		@Override
		public boolean importJson(JsonElement e){
			if(e.isJsonPrimitive()){
				try{
					set(e.getAsFloat());
					return true;
				}
				catch(ClassCastException err){
					//invalid type
				}
			}
			return false;
		}
		
		@Override
		public JsonElement exportJson(){
			return(new JsonPrimitive(this.value));
		}
	}
	
	/** Integer sub type **/
	public static class IntegerParam extends Param{
		private int defvalue;
		private int value;
		private int min;
		private int max;
		
		public IntegerParam(String name,String comment,int def){
			super(name,comment);
			this.type = ParamTypes.INTEGER;
			this.defvalue = def;
			this.min = Integer.MIN_VALUE;
			this.max = Integer.MAX_VALUE;
			this.value = def;
		}
		
		public IntegerParam(String name,String comment,int def,int min,int max){
			super(name,comment);
			this.type = ParamTypes.INTEGER;
			this.defvalue = def;
			this.min = min;
			this.max = max;
			this.value = def;
		}
		
		/** Get the string value, or return the default if "" or null **/
		public int validate(int v){
			if(v < min){
				return(defvalue);
			}
			if(v > max){
				return(defvalue);
			}
			return(v);
		}
		public int getDefault(){
			return(defvalue);
		}
		
		public void set(int v){
			this.value = validate(v);
		}
		
		public int get(){
			return value;
		}
		
		@Override
		public boolean importJson(JsonElement e){
			if(e.isJsonPrimitive()){
				try{
					set(e.getAsInt());
					return true;
				}
				catch(ClassCastException err){
					//invalid type
				}
			}
			return false;
		}
		
		@Override
		public JsonElement exportJson(){
			return(new JsonPrimitive(this.value));
		}
	}
}
