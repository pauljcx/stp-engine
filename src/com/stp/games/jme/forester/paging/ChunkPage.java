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
package com.stp.games.jme.forester.paging;
// JME3 Dependencies
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Spatial;
// Internal Dependencies
import com.stp.games.jme.forester.paging.interfaces.Page;
import com.stp.games.jme.forester.paging.interfaces.PagingEngine;
import com.stp.games.jme.forester.RectBounds;
import com.stp.games.jme.forester.grid.GenericCell2D;

public class ChunkPage extends GenericCell2D implements Page {

    protected Node parentNode;
    protected Node[] nodes;
    protected boolean[] stateVec;
    protected float overlap;
    protected RectBounds bounds;

    /**
     * Constructor based on x and z coordinates.
     * 
     * @param x The x-coordinate of the page.
     * @param z The z-coordinate of the page.
     * @param center The center of the page.
     * @param engine The paging engine used with this page(type).
     */
    public ChunkPage(int x, int z, Vector3f center, Node parentNode, float size) {
        super(x, z);
        bounds = new RectBounds(center, size);
		this.parentNode = parentNode;
    }

    @Override
    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setLocalTranslation(bounds.getCenter());
        }
        stateVec = new boolean[nodes.length];
    }

    @Override
    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public Node getNode(int detailLevel) {
        return nodes[detailLevel];
    }

    @Override
    public boolean isVisible(int detailLevel) {
        return stateVec[detailLevel];
    }

    @Override
    public void setVisible(boolean visible, int detailLevel) {
        if (visible == true && stateVec[detailLevel] == false) {
            parentNode.attachChild(nodes[detailLevel]);
            stateVec[detailLevel] = visible;
        } else if (visible == false && stateVec[detailLevel] == true) {
            nodes[detailLevel].removeFromParent();
            stateVec[detailLevel] = visible;
        }
    }

    @Override
    public void setFade(boolean enabled, float fadeStart, float fadeEnd, int detailLevel) {
        float fadeRange = fadeEnd - fadeStart;
        Material material = null;
       /* for (Spatial spat : nodes[detailLevel].getChildren()) {
            material = ((BatchNode) spat).getMaterial();
            material.setFloat("FadeEnd", fadeEnd);
            material.setFloat("FadeRange", fadeRange);
            material.setBoolean("FadeEnabled", enabled);
        }*/
    }

    @Override
    public void unload() {
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                setVisible(false, i);
            }
            nodes = null;
            stateVec = null;
        }
    }

    @Override
    public Vector3f getCenterPoint() {
        return bounds.getCenter();
    }
    
    @Override
    public RectBounds getBounds(){
        return bounds;
    }

    @Override
    public float getOverlap() {
        return bounds.getWidth() * 0.5f;//overlap;
    }

    //Calculate the largest overlap.
    @Override
    public void calculateOverlap(float halfPageSize, int detailLevel) {

        Node node = nodes[detailLevel];
        if(node.getWorldBound() == null || true){
            overlap = halfPageSize;
            return;
        }
        node.updateGeometricState();
        
        float ol = halfPageSize;
        BoundingVolume wb = node.getWorldBound();
        float dX = wb.getCenter().x - bounds.getCenter().x;
        float dZ = wb.getCenter().z - bounds.getCenter().z;
        
        if (wb instanceof BoundingSphere) {
            BoundingSphere bs = (BoundingSphere) wb;
            float radius = bs.getRadius();
            
            float temp = dX + radius;
            if (temp > ol) {
                ol = temp;
            } else {
                temp = radius - dX;
                if(temp > ol){
                    ol = temp;
                }
            }
            temp = dZ + radius;
            if ( temp > ol) {
                ol = temp;
            } else {
                temp = radius - dZ;
                if(temp > ol){
                    ol = temp;
                }
            }
            
        } else if (node.getWorldBound() instanceof BoundingBox){
            BoundingBox bb = (BoundingBox) wb;
            
            float temp = dX + bb.getXExtent();
            if (temp > ol) {
                ol = temp;
            } else {
                temp = bb.getXExtent() - dX;
                if(temp > ol){
                    ol = temp;
                }
            }
            temp = dZ + bb.getZExtent();
            if (temp > ol) {
                ol = temp;
            } else {
                temp = bb.getZExtent() - dZ;
                if(temp > ol){
                    ol = temp;
                }
            }
        }

        if (ol > halfPageSize) {
            overlap = ol;
        } else {
            overlap = halfPageSize;
        }
    }

    @Override
    public void update(float tpf) {
    }
}