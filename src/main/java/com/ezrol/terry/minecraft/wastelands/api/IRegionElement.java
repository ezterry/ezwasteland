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

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Region Interface: must be defined by each wastelands generation module
 * To activate the module ensure it is registered with RegionCore in init
 * <p>
 * Created By Terrence Ezrol, Aug 2016
 */
public interface IRegionElement {
    /**
     * addElementHeight - using the list of elements in the 5x5 region area centered on the current
     * region determined the current height offset of the wastelands for the given point this can be absolute
     * however in most cases will be an additive operation on the current offset provided.
     *
     * @param currentOffset - the height of the wastelands prior to this element(s) accounting
     * @param x             - x block cord
     * @param z             - z block cord
     * @param core          - reference to the region core code being run
     * @param elements      - elements for the 25 regions around and including ours (concatenated from calcElements())
     * @return the height of this cord in the wastelands after our calculation
     */
    int addElementHeight(int currentOffset, int x, int z, RegionCore core, List<Object> elements);

    /**
     * Getter of your module/element name
     *
     * @return the name of your module (recommended this have no spaces, special symbols, or caps.)
     */
    String getElementName();

    /**
     * Get the param template (this is the template used to generate the world configuration dialog and json format)
     * Each module may have one or more named params in a list using the "Param" class and its typed components.
     *
     * @return list of parameters belonging to this element, will be passed back with parameters during generation.
     */
    List<Param> getParamTemplate();

    /**
     * Calculate any elements in a region, this can be returned as a list of Objects, these will be concatinated
     * and passed back into addElementHeight().
     *
     * @param r    - a rng for this region
     * @param x    - x region cord (ie 64x block cord)
     * @param z    - z region cord (ie 64x block cord)
     * @param core - the current region core object
     * @return data to include for this region, must be a list, but can be empty
     */
    List<Object> calcElements(Random r, int x, int z, RegionCore core);

    /**
     * Post filling in wasteland blocks to the result of calling addElementHeight() on all modules this will be called
     * to allow additional generation
     *
     * @param chunkprimer - chunkprimer for the current chunk
     * @param height      - height of the wasteland at this x/z cord
     * @param x           - x block cord
     * @param z           - z block cord
     * @param core        - the current region core object
     */
    void postFill(ChunkPrimer chunkprimer, int height, int x, int z, RegionCore core);

    /**
     * A few "event" trigger points for more advanced generation
     *
     * @param event             - "chunkcleanup" - last time to access chunkprimer before the chunk is returned
     *                          - "populate" - called on the populate event
     *                          - "recreateStructures" - called when its time to re-create the structures of the world
     * @param gen               - the wastelands chunk generator
     * @param cords             - Chunk cords
     * @param chunkprimer       - for "chunkcleanup" this is the chunkprimer, otherwise null
     * @param core              - current region core object
     */
    void additionalTriggers(String event, IChunkGenerator gen, ChunkPos cords,ChunkPrimer chunkprimer, RegionCore core);

    /**
     * Get the nearest structure of the given name to the given position (or null if unknown)
     *
     * @param name name of the structure we are looking for
     * @param curPos position we are looking from
     * @param findUnexplored if we are to find structures that are yet to be explored
     * @param core              - current region core object
     * @return the nearest known instance or null if unknown
     */
    BlockPos getNearestStructure(String name,BlockPos curPos, boolean findUnexplored, RegionCore core);

    /**
     * If we are currently inside a structure
     * @param structureName     - the name of the structure
     * @param pos               - the position being checked
     * @param core              - current region core object
     * @return true if its in the structure, else false
     */
    boolean isInsideStructure(String structureName, BlockPos pos, RegionCore core);

    /**
     * Allows tweaking spawnable creatures in a region
     *
     * @param lst the current list
     * @param creatureType the creature type to spawn
     * @param pos the position being checked
     * @param core the current region core object
     * @return the new list (or the original if no change/directly modified)
     */
    List<Biome.SpawnListEntry> getSpawnable(List<Biome.SpawnListEntry> lst, EnumCreatureType creatureType,
                                            BlockPos pos, RegionCore core);
}
