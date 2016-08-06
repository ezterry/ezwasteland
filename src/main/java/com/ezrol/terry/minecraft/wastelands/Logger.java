package com.ezrol.terry.minecraft.wastelands;

import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;


public class Logger {
	//set to true to output all debug messages, will be forced to true after a critical error
	static private boolean GlobalDebugMode = false;
	//if the local sub instance is in debug mode even if global is not
	private boolean LocalDebugMode;
	
	public Logger(boolean debug){
		LocalDebugMode = debug;
	}
	/** Always send as log level INFO **/
	public void status(String msg){
		FMLLog.log(EzWastelands.MODID, Level.INFO, "STATUS: " + msg);
	}
	/** Send an info level message if debug mode is on **/
	public void info(String msg){
		if(GlobalDebugMode || LocalDebugMode){
			FMLLog.log(EzWastelands.MODID, Level.INFO, msg);
		}
	}
	/** Send a Warning Level message if debug mode is on **/
	public void warn(String msg){
		if(GlobalDebugMode || LocalDebugMode){
			FMLLog.log(EzWastelands.MODID, Level.WARN, msg);
		}
	}
	/** Send a Error Level message (regardless of debug mode) **/
	public void error(String msg){
		FMLLog.log(EzWastelands.MODID, Level.ERROR, msg);
	}
	/** Send a Faital error message and force global debug mode **/
	public void crit(String msg){
		FMLLog.log(EzWastelands.MODID, Level.FATAL, msg);
		GlobalDebugMode=true;
	}
}
