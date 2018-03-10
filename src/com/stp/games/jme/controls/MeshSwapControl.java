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
package com.stp.games.jme.controls;
// JME3 Dependencies
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.AbstractControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.scene.control.LodControl;
// Java Dependencies
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;

/** @author Paul Collins
 *  @version v1.0 ~ 10/13/2015
 *  HISTORY: Version 1.0  created MeshSwapControl to allow multiple meshes to be swapped on a Spatial
 */
public class MeshSwapControl extends AbstractControl {
	private final HashMap<String,Mesh> meshTable = new HashMap<String,Mesh>();
	private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
	
	public MeshSwapControl() {
	}
	public void addMesh(String name, Mesh mesh) {
		meshTable.put(name, mesh);
	}
	public void addMesh(String name, Spatial model) {
		if (model instanceof Geometry) {
			meshTable.put(name, ((Geometry)model).getMesh());
		}		
	}
	public Mesh getMesh(String meshName) {
		return meshTable.get(meshName);
	}
	public Spatial getNamedChild(String name) {
		if (spatial.getName() != null && spatial.getName().equals(name)) {
			return spatial;
		}
		if (spatial instanceof Node) {
			return ((Node)spatial).getChild(name);
		}
		return null;
	}
	public boolean swapMesh(String childName, String meshName) {
		if (meshTable.get(meshName) == null || !(getNamedChild(childName) instanceof Geometry)) {
			return false;
		} else {
			queue.offer(childName + ":" + meshName);
			return true;
		}
	}
	// Inherited from super class
	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)	{
	}
	// Process any mesh swaps requested during the update phase
	@Override
	protected void controlUpdate(float tpf)	{
		if (!queue.isEmpty()) {
			Iterator<String> i = queue.iterator();
			while (i.hasNext()) {
				String[] parts = i.next().split(":");
				Geometry shape = (Geometry)getNamedChild(parts[0]);
				shape.setMesh(meshTable.get(parts[1]));
				i.remove();
			}
		}
	}
	// Inherited from super class, assigns a spatial to this control
	@Override
	public void setSpatial(Spatial spatial)	{
		super.setSpatial(spatial);
	}
    // Inherited from super class, creates a copy of this control to add to a new spatial
    @Override
	public Control cloneForSpatial(Spatial spatial)	{
		final MeshSwapControl control = new MeshSwapControl();
		for (Map.Entry<String,Mesh> entry : meshTable.entrySet()) {
			control.addMesh(entry.getKey(), entry.getValue());
		}
		return control;
	}
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
		oc.write(meshTable.size(), "count", 0);
		int index = 0;
		for (Map.Entry<String,Mesh> entry : meshTable.entrySet()) {
			oc.write(entry.getKey(), "key" + index, "");
			oc.write(entry.getValue(), "value" + index, null);
		}
    }
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        int count = ic.readInt("count", 0);
		for (int i = 0; i < count; i++) {
			String key = ic.readString("key" + i, "");
			Mesh value = (Mesh)ic.readSavable("value" + i, null);
			if (key.length() > 0 && value != null) {
				meshTable.put(key, value);
			}
		}
    }
}