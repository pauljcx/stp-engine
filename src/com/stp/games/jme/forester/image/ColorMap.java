/*
 * Copyright (c) 2011, Andreas Olofsson
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
package com.stp.games.jme.forester.image;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import com.stp.games.jme.forester.paging.interfaces.Page;

/**
 * This class is used with the GrassGrid to color grass meshes.
 * It borrows from the terrain height map system.
 * 
 * @author Andreas
 */
public class ColorMap extends ImageReader {
    
    protected int size;

    /**
     * Create a color map based on a texture.
     * 
     * @param tex The texture.
     * @param tileSize The size of the geometry tiles.
     */
    public ColorMap(Texture tex, int tileSize) {
        setupImage(tex.getImage());
        size = tileSize;
    }

    /**
     * 
     * @param page The page.
     * @return An array of colorvalues.
     */
    public ColorRGBA[] getColorsUnfiltered(Page page) {

        int width = (int) page.getBounds().getWidth();
        int height = (int) page.getBounds().getHeight();

        int offsetX = page.getX() * width;
        int offsetZ = imageHeight - 1 - page.getZ() * height;

        ColorRGBA[] colors = new ColorRGBA[width * height];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                colors[i + width * j] = getColor(i + offsetX, offsetZ - j);
            }
        }
        this.buf.clear();
        return colors;
    }
}//ColorMap
