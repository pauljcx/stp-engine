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
import com.jme3.math.Vector3f;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** @author Paul Collins
 *  @version v1.0 ~ 05-01-2015
 *  HISTORY: Version 1.0 created Waypoint class 05-01-2015
 */
public class Waypoint extends GameControl {
	public static final ObjectType WAYPOINT_TYPE = new ObjectType<Waypoint>("Waypoint", Waypoint.class);

	public Waypoint(Vector3f translation) {
		this (translation.x, translation.y, translation.z);
	}
	public Waypoint(float x, float y, float z) {
		super();
		setStoredLocation(x, y, z);
	}
	@Override
	public ObjectType getType() {
		return WAYPOINT_TYPE;
	}
	// Write all unique parameter values to the given output stream
	@Override
	public void writeFields(OutputStream os) throws IOException {
		os.write(ByteUtils.convertToBytes(location.x));
		os.write(ByteUtils.convertToBytes(location.y));
		os.write(ByteUtils.convertToBytes(location.z));
	}
	// Read all unique parameter values from the given input stream
	@Override
	public void readFields(InputStream is) throws IOException {
		setStoredLocation(ByteUtils.readFloat(is), ByteUtils.readFloat(is), ByteUtils.readFloat(is));
	}
	// Write all unique parameter values to the given buffer
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException {
		buffer.putFloat(location.x);
		buffer.putFloat(location.y);
		buffer.putFloat(location.z);
	}
	// Read all unique parameter values from the given buffer
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		setStoredLocation(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
	}
}