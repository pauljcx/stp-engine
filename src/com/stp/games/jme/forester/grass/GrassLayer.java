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
package com.stp.games.jme.forester.grass;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.Geometry;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
// Internal Dependencies
import com.stp.games.jme.forester.SPLightMaterial;
import com.stp.games.jme.forester.Forester;
import com.stp.games.jme.forester.grass.algorithms.GPAUniform;
import com.stp.games.jme.forester.grass.algorithms.GrassPlantingAlgorithm;
import com.stp.games.jme.forester.image.FormatReader.Channel;

public class GrassLayer {

    protected GrassLoader grassLoader;
    protected Material material;
	protected Mesh baseMesh;
    protected GrassPlantingAlgorithm pa;
    
    protected float densityMultiplier = 1f;
	protected float frequency = 0.5f;
    
    //The individual grass-patches height and width range.
    protected float maxHeight = 1.2f, minHeight = 1f;
    protected float maxWidth = 1.2f, minWidth = 1f;
    
    protected ShadowMode shadowMode = ShadowMode.Off;
    
    //Related to density maps.
    protected Channel dmChannel = Channel.Luminance;
    protected int dmTexNum = 0;
	protected int layerId;
	protected String name;
	
	public GrassLayer(String name, Spatial grassModel, Material material, float minSize, float maxSize, float frequency, int layerId) {
		this.layerId = layerId;
		this.name = name;
		setMaxHeight(maxSize);
		setMaxWidth(maxSize);
		setMinHeight(minSize);
		setMinWidth(minSize);
		setFrequency(frequency);
		setBaseMesh(grassModel);	
		this.material = material;
		this.pa = new GPAUniform();
	}
    public int getLayerId() {
		return layerId;
	}
	public String getName() {
		return name;
	}
    public void update(){
    }
	public Mesh getBaseMesh() {
		return baseMesh;
	}
	public void setBaseMesh(Mesh baseMesh) {
		this.baseMesh = baseMesh;
	}
	public void setBaseMesh(Spatial spatial) {
		if (spatial instanceof Geometry) {
			this.baseMesh = ((Geometry)spatial).getMesh();
		}
	}
	public boolean hasBaseMesh() {
		return baseMesh != null;
	}
	public float getFrequency() {
		return frequency;
	}
	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

    public float getDensityMultiplier() {
        return densityMultiplier;
    }

    public void setDensityMultiplier(float density) {
        densityMultiplier = density;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }

    public Material getMaterial() {
        return material;
    }

    public GrassLoader getGrassLoader() {
        return grassLoader;
    }

    public void setGrassLoader(GrassLoader grassLoader) {
        this.grassLoader = grassLoader;
    }
    
    public GrassPlantingAlgorithm getPlantingAlgorithm() {
        return pa;
    }

    public void setPlantingAlgorithm(GrassPlantingAlgorithm plantingAlgorithm) {
        this.pa = plantingAlgorithm;
    }

    public Channel getDmChannel() {
        return dmChannel;
    }
    
    public int getDmTexNum(){
        return dmTexNum;
    }
    public void setDensityTextureData(int dmTexNum, Channel channel){
        this.dmChannel = channel;
        this.dmTexNum = dmTexNum;
    }

    public ShadowMode getShadowMode() {
        return shadowMode;
    }
	
	public void setWind(Vector2f wind) {
		
	}
	public boolean equals(Object obj) {
		if (obj instanceof GrassLayer) {
			return ((GrassLayer)obj).getLayerId() == layerId;
		}
		return false;
	}
}
