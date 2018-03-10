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
import com.jme3.scene.control.Control;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** @author Paul Collins
 *  @version v0.1 ~ 04/24/2015
 *  HISTORY: Version 0.1 created StructureControl 04/24/2015
 */
public class StructureControl extends GameControl {
	// Field Params
	public static final Param EMITTER = new Param("Emitter", String.class, 0);
	public static final Param LENGTH = new Param("Length", Float.class, 1);
	public static final Param WIDTH = new Param("Width", Float.class, 2);
	public static final Param CATEGORY = new Param("Category", Category.class, 3);
	
	public static final Param[] STRUCT_PARAMS = { EMITTER, LENGTH, WIDTH, CATEGORY };
	public static final ObjectType STRUCTURE_TYPE = new ObjectType<StructureControl>("Structure", StructureControl.class, STRUCT_PARAMS);
	
	public static final String[] DOMICILE_ACTIONS = { "sleep" };
	
	// Structure Categories
	public enum Category {
		NONE("none"),
		DOMICILE("domicile"),
		WALL("wall");
		private String text;
		private Category(String text) { this.text = text; }
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

	protected long owner = 0;

	public StructureControl() {
		super();
	}
	
	public StructureControl(Schematic schema) {
		super(schema);
	}
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final StructureControl control = new StructureControl(schema);
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
		}
	}
	public void copyFields(PlantControl control) {
		super.copyFields(control);
		if (spatial != null) {
		}
	}
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
	}
	@Override
	public void readFields(InputStream is) throws IOException	{
		super.readFields(is);
	}
	public String getEmitter() {
		return (schema != null) ? schema.getString(EMITTER) : "none";
	}
	public Category getCategory() {
		return (schema != null) ? Category.values()[schema.getInt(CATEGORY)] : Category.NONE;
	}
	public void setOwner(CreatureControl creature) {
		if (creature != null) {
			this.owner = creature.getUniqueId();
		} else {
			this.owner = 0;
		}
	}
	public boolean isOwned() {
		return owner != 0;
	}
	@Override
	public String[] getActionList() {
		if (getCategory().matches(Category.DOMICILE)) {
			return DOMICILE_ACTIONS;
		}
		return super.getActionList();
	}
}