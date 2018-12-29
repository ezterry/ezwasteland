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

import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class WastelandsLevelType{
    private static final Logger LOGGER = LogManager.getLogger("WastelandType");

    /** Code to add the new level to the type list
     * (1) extends the LevelGeneratorType list by one
     * (2) Creates a new LevelGeneratorType instance
     *
     * @return The LevelGeneratorType entry if successful
     */
    public static LevelGeneratorType getType(){
        LevelGeneratorType val;
        int id=7;
        Field types = null;

        for(Field f : LevelGeneratorType.class.getDeclaredFields()){
            if(f.getType()==LevelGeneratorType[].class){
                types = f;
            }
        }

        if(types != null){
            LOGGER.info("count =  " + LevelGeneratorType.TYPES.length);
            LOGGER.info("field found");

            try {
                LevelGeneratorType newTypes[] = new LevelGeneratorType[LevelGeneratorType.TYPES.length+1];

                System.arraycopy(LevelGeneratorType.TYPES, 0, newTypes, 0, LevelGeneratorType.TYPES.length);
                newTypes[newTypes.length-1] = null;

                types.setAccessible(true);
                Field modifies = Field.class.getDeclaredField("modifiers");
                modifies.setAccessible(true);

                modifies.setInt(types, types.getModifiers() & ~Modifier.FINAL);
                types.set(null,newTypes);
                id=LevelGeneratorType.TYPES.length - 1;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                LOGGER.error("Unable to find Generator Type Field");
                return null;
            }


            LOGGER.info("count =  " + LevelGeneratorType.TYPES.length + " / id = " + id);
        }
        else{
            LOGGER.error("Unable to find Generator Type Field");
            return null;
        }
        try {
            Constructor<LevelGeneratorType> c =
                    LevelGeneratorType.class.getDeclaredConstructor(int.class, String.class);
            c.setAccessible(true);
            LOGGER.info("Level Type Constructor Found");
            val = c.newInstance(id, "ezwastelands");
            val.setCustomizable(true);
        } catch (Exception e) {
            LOGGER.error("Unable to get and call LevelGeneratorType Constructor",e);
            return null;
        }

        return val;
    }
}
