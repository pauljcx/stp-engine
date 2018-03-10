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
package com.stp.games.jme.actions;
// JME3 Dependencies
import com.jme3.math.Vector3f;
// Internal Dependencies

/**
 * The PathNode class...full description here.
 *
 */
public class PathNode {
	private Vector3f location;
	private PathNode parent;
	private boolean walkable;
	private int g; // distance from start
	private int h; // distance from end
	
	public PathNode(Vector3f worldLocation) {
		this ((int)worldLocation.x, (int)worldLocation.z);
	}
	public PathNode(int x, int y) {
		this.location = new Vector3f(x, 0, y);
	}
	public Vector3f getLocation() {
		return location;
	}
	public PathNode getParent() {
		return parent;
	}
	public int getG() {
		return g;
	}
	public int getH() {
		return h;
	}
	public int getF() {
		return g + h;
	}
	public float getX() {
		return location.x;
	}
	public float getY() {
		return location.z;
	}
	public void setLocation(Vector3f in) {
		setLocation(in.x, in.z);
	}
	public void setLocation(float x, float y) {
		location.set(x, 0, y);
	}
	public void setParent(PathNode parent) {
		this.parent = parent;
	}
	public void setG(int g) {
		this.g = g;
	}
	public void setH(int h) {
		this.h = h;
	}
	public void set(int g, int h) {
		this.g = g;
		this.h = h;
	}
	@Override
	public String toString() {
		return location.toString() + " gCost=" + g + " hCost=" + h + " fCost=" +getF();
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof PathNode) {
			return ((PathNode)other).getLocation().equals(location);
		}
		return false;
	}
	@Override
	public int hashCode() {
        return location.hashCode() + (13 * getF());
    }
	public static int getDistance(PathNode nodeA, PathNode nodeB) {
		int dx = (int)Math.abs(nodeA.getX() - nodeB.getX());
		int dy = (int)Math.abs(nodeA.getY() - nodeB.getY());
		if (dx > dy) {
			return 14*dy + 10*(dx-dy);
		} else {
			return 14*dx + 10*(dy-dx);
		}
	}
}