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
public class VehicleControl extends GameControl {
	// Field Params
	//public static final Param[] STRUCT_PARAMS = { EMITTER, LENGTH, WIDTH, CATEGORY };
	public static final ObjectType VEHICLE_TYPE = new ObjectType<VehicleControl>("Vehicle", VehicleControl.class);

	protected long owner = 0;

	public VehicleControl() {
		super();
	}
	public VehicleControl(Schematic schema) {
		super(schema);
	}
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final VehicleControl control = new VehicleControl(schema);
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
	@Override
	public void copyFields(GameControl control) {
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
}