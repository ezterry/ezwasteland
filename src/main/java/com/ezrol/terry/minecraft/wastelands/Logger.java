/*
 * Copyright (c) 2015-2016, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
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
 */

package com.ezrol.terry.minecraft.wastelands;

import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;


@SuppressWarnings("unused")
public class Logger {
    //set to true to output all debug messages, will be forced to true after a critical error
    static private boolean GlobalDebugMode = false;
    //if the local sub instance is in debug mode even if global is not
    private boolean LocalDebugMode;
    static private org.apache.logging.log4j.Logger modLog = null;

    public Logger(boolean debug){
        if(modLog == null){
            throw(new RuntimeException("Logger called from subclass prior to mod pre-init"));
        }
        LocalDebugMode = debug;
    }

    public Logger(org.apache.logging.log4j.Logger modLog,boolean debug) {
        Logger.modLog=modLog;
        LocalDebugMode = debug;
    }

    /**
     * Always send as log level INFO
     **/
    public void status(String msg) {
        modLog.log(Level.INFO, "STATUS: " + msg);
    }

    /**
     * Send an info level message if debug mode is on
     **/
    public void info(String msg) {
        if (GlobalDebugMode || LocalDebugMode) {
            modLog.log(Level.INFO, msg);
        }
    }

    /**
     * Send a Warning Level message if debug mode is on
     **/
    public void warn(String msg) {
        if (GlobalDebugMode || LocalDebugMode) {
            modLog.log(Level.WARN, msg);
        }
    }

    /**
     * Send a Error Level message (regardless of debug mode)
     **/
    public void error(String msg) {
        modLog.log(Level.ERROR, msg);
    }

    /**
     * Send a Faital error message and force global debug mode
     **/
    public void crit(String msg) {
        modLog.log(Level.FATAL, msg);
        GlobalDebugMode = true;
    }
}
