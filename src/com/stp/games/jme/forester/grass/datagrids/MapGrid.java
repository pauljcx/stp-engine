/*
 * Copyright (c) 2012, Andreas Olofsson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.stp.games.jme.forester.grass.datagrids;

import com.jme3.texture.Texture;

import com.stp.games.jme.forester.MapBlock;
import com.stp.games.jme.forester.grass.GrassTile;
import com.stp.games.jme.forester.image.ColorMap;
import com.stp.games.jme.forester.image.DensityMap;
import com.stp.games.jme.forester.grid.GenericCell2D;
import com.stp.games.jme.forester.grid.Grid2D;

/**
 * The mapgrid is used to store density/colormaps, and feeding them
 * to the grassloader.
 * 
 * @author Andreas
 */
public class MapGrid implements MapProvider {
    
    protected Grid2D<MapCell> grid;
    protected int tileSize;

    public MapGrid(int tileSize) {
        this.tileSize = tileSize;
        grid = new Grid2D<MapCell>();
    }
    
    @Override
    public MapBlock getMaps(GrassTile tile) {
        MapCell mapCell = grid.getCell(tile.getX(), tile.getZ());
        
        //Return null to set the page as being idle (no processing).
        if(mapCell == null || mapCell.maps.getDensityMaps().isEmpty()){
            return null;
        }
        return mapCell.maps;
    }
    
    /**
     * @deprecated
     */
    public void addDensityMap(Texture tex, int x, int z, int layer, boolean flipX, boolean flipY){
        loadMapCell(x,z).addDensityMap(tex,layer);
    }
    
    /**
     * Load a texture as densitymap.
     * 
     * @param tex The texture.
     * @param x The grasstile x-index.
     * @param z The grasstile z-index.
     * @param index The order of the map (use 0 for first density map, 1 for second etc.).
     */
    public void addDensityMap(Texture tex, int x, int z, int index){
        loadMapCell(x,z).addDensityMap(tex, index);
    }
    
    /**
     * Adds a colormap to the grid.
     * 
     * @param tex The texture to be used.
     * @param x The x-index (or coordinate) within the grid.
     * @param z The z-index within the grid.
     * @param index The index of the grass-layer using this densitymap.
     */
    public void addColorMap(Texture tex, int x, int z, int index){
        MapCell cell = grid.getCell(x,z);
        if(cell == null){
            throw new RuntimeException("Tried loading color map to empty cell: " + cell.toString());
        }
        cell.addColorMap(tex,index);
    }
    
    protected MapCell loadMapCell(int x, int z){
        MapCell mapCell = grid.getCell(x,z);
        if(mapCell == null){
            mapCell = new MapCell(x,z);
            grid.add(mapCell);
        }
        return mapCell;
    }
    
    /**
     * This class is used to store density and colormaps.
     */
    protected class MapCell extends GenericCell2D {
        
        MapBlock maps;
        
        protected MapCell(int x, int z){
            super(x,z);
            maps = new MapBlock();
        }
        
        protected void addDensityMap(Texture tex, int idx){
            DensityMap map = new DensityMap(tex,tileSize);
            maps.getDensityMaps().put(idx, map);
        }
        
        protected void addColorMap(Texture tex, int index){
            ColorMap map = new ColorMap(tex,tileSize);
            maps.getColorMaps().put(index, map);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GenericCell2D other = (GenericCell2D) obj;
            if (this.hash != other.hashCode()) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode(){
            return hash;
        }
        
        @Override
        public String toString() {
            return "MapCell: (" + Short.toString(x) + ',' + Short.toString(z) + ')';
        }
        
    }//MapCell
    
}
