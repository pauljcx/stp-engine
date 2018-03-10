/* MIT License
 *
 * Copyright (c) 2018 Paul Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.stp.games.jme.terrain.layer;
// JME3 Dependencies
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.material.Material;
// Internal Dependencies
import com.stp.games.jme.terrain.ChunkControl;
import com.stp.games.jme.forester.paging.ChunkPage;
import com.stp.games.jme.forester.RectBounds;
import com.stp.games.jme.forester.util.FastRandom;

/**
 * The TreeLayer class...full description here.
 *
 */
public class TreeLayer {
    protected String name;
    protected Spatial model;
    protected float minScale;
    protected float maxScale;  
    protected ShadowMode shadowMode;
	protected int lod;
	protected boolean batched;

	public TreeLayer(Spatial model) {
		this (model, 0.8f, 1.2f, 0, false);
	}
    public TreeLayer(Spatial model, float minScale, float maxScale, int lod, boolean batched) {
		setModel(model);
		setMinScale(minScale);
		setMaxScale(maxScale);
		setShadowMode(ShadowMode.Cast);
		setLod(lod);
		setBatched(batched);
    }
    public Spatial getModel() {
        return model;
    }
	public void setModel(Spatial model) {
		this.model = model;
		this.name = model.getName();
	}
    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }
    public float getMaxScale() {
        return maxScale;
    }
    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }
    public float getMinScale() {
        return minScale;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
	public int getLod() {
		return lod;
	}
	public void setLod(int lod) {
		this.lod = lod;
	}
	public boolean getBatched() {
		return batched;
	}
	public void setBatched(boolean batched) {
		this.batched = batched;
	}
    public ShadowMode getShadowMode() {
        return shadowMode;
    }
    public void setShadowMode(ShadowMode shadowMode) {
        this.shadowMode = shadowMode;
    }
    @Override
    public boolean equals(Object obj) {
		if (obj instanceof TreeLayer) {
			return (this.name == null) ? ((TreeLayer)obj).getName() != null : !this.name.equals(((TreeLayer)obj).getName() );
        }
		return false;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
	// Adds tree geometry for the given chunk and page
	public static Node addTreeGeometry(TreeLayer layer, ChunkControl chunk, ChunkPage page, Node node) {
        RectBounds bounds = page.getBounds();
        // Calculate the area of the page
        float area = bounds.getWidth()*bounds.getWidth();		
		// Limit the total possible tree count by the chunks density parameter
        int treeCount = (int) (area * chunk.getTreeDensity());        
        // Each tree data point consists of coords (x,z), scale and rotation angle
        float[] treeData = new float[treeCount*4];        
        // The planting algorithm returns the final tree count
        treeCount = generateTreeData(chunk, page, treeData, treeCount);
		
		// Tree data iterator
        int index = 0;
		
		// Get the dimensions and page location
        float minScale = layer.getMinScale();
        float maxScale = layer.getMaxScale();
        float cX = bounds.getCenter().x;
        float cZ = bounds.getCenter().z;

		for(int i = 0; i < treeCount; i++) {			
			// Tree data points
			float x = treeData[index++];
			float z = treeData[index++];
			float size = treeData[index++];
			float angle = treeData[index++];
			float scale = minScale + size*(maxScale - minScale);
			float y = chunk.getHeight(x, z);

			// Make a copy from the source model
			Spatial model = layer.getModel().clone(true);
			model.setQueueBucket(Bucket.Transparent);
			model.setShadowMode(layer.getShadowMode());
			model.setLocalTranslation(x - cX, y, z - cZ);
			model.setLocalScale(scale, scale, scale);
			model.setLocalRotation(new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y));				
			node.attachChild(model);
		}
		return node;
	}
	// Basic random tree planting algorithm
	private static int generateTreeData(ChunkControl chunk, ChunkPage page, float[] treeData, int treeCount) {
		RectBounds bounds = page.getBounds();
        //Populating the array of locations (and also getting the total amount of quads).
        FastRandom rand = new FastRandom();
        //Iterator
        int iIt = 0;

        for (int i = 0; i < treeCount; i++) {
            float x = rand.unitRandom() * (bounds.getWidth() - 0.01f);
            float z = rand.unitRandom() * (bounds.getWidth() - 0.01f);
			if (chunk.getHeight(x + bounds.getxMin(), z + bounds.getzMin()) < 2) {
				continue;
			}			

            if (rand.unitRandom()*100 < 2) {
				treeData[iIt++] = x + bounds.getxMin(); // the x world coord
				treeData[iIt++] = z + bounds.getzMin(); // the z world coord
				treeData[iIt++] = rand.unitRandom(); // the tree scale
				treeData[iIt++] = (-0.5f + rand.unitRandom())*3.141593f; // the tree rotation -pi/2 -> pi/2
            }
        }
        // The iterator divided by four is the tree count
        return iIt/4;
	}
}
