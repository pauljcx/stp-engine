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
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.control.Control;
import com.jme3.material.Material;
import com.jme3.export.binary.ByteUtils;
import com.jme3.util.TempVars;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
// Internal Dependencies
import com.stp.games.jme.terrain.VoxelShape;
import com.stp.games.jme.terrain.IsoSurface;

/** @author Paul Collins
 *  @version v1.0 ~ 05/09/2015
 *  HISTORY: Version 1.0 created StoneControl for setting up and accessing user data for stones 05/09/2015
 */
public class StoneControl extends GameControl {
	// Field Params
	public static final Param STYLE = new Param("Style", Style.class, 0);	
	public static final Param[] STONE_PARAMS = { STYLE, };

	public static final String[] STONE_ACTIONS = { "pick" };
	
	// Stone Styles
	public enum Style {
		NONE("none"),
		GRANITE("granite"),
		SANDSTONE("sandstone"),
		LIMESTONE("limestone");
		private String text;
		private Style(String text) { this.text = text; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	protected VoxelShape shape;
	protected Mesh mesh;
	protected Geometry model;
	protected Material material;
	protected volatile boolean updated;
	
	public StoneControl() {
		super();
		this.updated = false;
	}
	public StoneControl(Schematic schema) {
		super(schema);
		this.updated = false;
	}
	@Override
	protected void controlUpdate(float tpf)	{
		if (updated) {
			doUpdate();
		}
	}
	@Override
	public void initializeAssets(AssetManager assetManager) {
		if (spatial != null) {
			String materialPath = getString("material_path");
			if (materialPath.length() > 0) {
				this.material = assetManager.loadMaterial(materialPath);
				return;
			}
		}
		this.material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	}
	public void construct(int width, int height, int length) {
		this.shape = new VoxelShape(width, height, length);
		shape.generateRockShape();
		mesh = IsoSurface.generateVoxelMesh(shape);
		if (inSceneGraph()) {
			updated = true;
		} else {
			doUpdate();
		}
	}	
	private void reconstruct() {
		mesh = IsoSurface.generateVoxelMesh(shape);
		if (inSceneGraph()) {
			updated = true;
		} else {
			doUpdate();
		}	
	}
	private void doUpdate() {
		updated = false;
		if (mesh != null && material != null) {
			if (model != null) {
				model.setMesh(mesh);
			} else {
				model = new Geometry(getStyle().text(), mesh);
				model.setShadowMode(ShadowMode.Cast);
				model.setMaterial(material);
			}
			attach();
		} else {
			detach();
		}
	}
	private void attach() {
		if (model != null && spatial instanceof Node) {
			if (!model.hasAncestor((Node)spatial)) {
				((Node)spatial).attachChild(model);
			}
		}
	}
	private void detach() {
		if (spatial instanceof Node) {
			((Node)spatial).detachAllChildren();
		}
	}
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final StoneControl control = new StoneControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
	}
	@Override
	public void resetFields() {
		super.resetFields();
		if (spatial != null) {
			// Setup UserData
		}
	}
	public void copyFields(PlantControl control) {
		super.copyFields(control);
		if (spatial != null) {
			// Copy UserData
		}
	}
	public Style getStyle() {
		return (schema != null) ? Style.values()[schema.getInt(STYLE)] : Style.NONE;
	}
	public int getVolumeWidth() {
		return (shape != null) ? shape.getWidth() : 0;
	}
	public int getVolumeHeight() {
		return (shape != null) ? shape.getHeight() : 0;
	}
	public int getVolumeLength() {
		return (shape != null) ? shape.getLength() : 0;
	}
	public VoxelShape getVolumeData() {
		return shape;
	}
	public Mesh getMesh() {
		return mesh;
	}
	public Geometry getModel() {
		return model;
	}
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
	}
	@Override
	public void readFields(InputStream is) throws IOException	{
		super.readFields(is);
	}
	public String[] getActionList() {
		return STONE_ACTIONS;
	}
	public int decimate(Vector3f coords, float force) {
		int result = 0;
		if (shape != null) {
			TempVars vars = TempVars.get();
			vars.vect1.set(getWorldTranslation()).subtractLocal(coords);
			result = shape.decimate(vars.vect1, force);
			vars.release();
		}
		if (result > 0) {
			 reconstruct();
		}
		return result;
	}
	public int getResourceQuantity(String resource) {
		if (resource.equals("stone") && shape != null) {
			return shape.getVolume();
		}
		return 0;
	}
	public boolean isDepleted() {
		return (shape != null) ? shape.getVolume() < 1 : true;
	}
}