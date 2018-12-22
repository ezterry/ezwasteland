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
