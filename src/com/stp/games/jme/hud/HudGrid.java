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
package com.stp.games.jme.hud;
// JME3 Dependencies
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 03/22/2015
 *  HISTORY: Version 1.0 Created control HudGrid ~ 03/22/2015
 */
public class HudGrid extends HudContainer {
	
	public static class Cell extends HudComponent {
		private int address;
		
		public Cell(String name, HudStyle style, int width, int height, int address) {
			super (name, style, width, height);
			this.address = address;
		}
		public int getAddress() {
			return address;
		}
	}
	
	protected HudContainer cells;
	protected int size;

	public HudGrid(String name, HudStyle style, int width,  int height, int size) {
		super (name, style, width, height);
		this.size = size;
		setRenderBackground(false);
		
		cells = new HudContainer(name + "-grid", style, 0, 0);
		cells.setRenderBackground(false);
		add(-1, cells);
	}
	public Cell getCell(int index) {
		if (index >= 0 && index < cells.getChildCount()) {
			return (Cell)cells.getChildAt(index);
		}
		return null;
	}
	public void set(int index, HudComponent component) {
		add(component);
		component.setRelative();
		component.setLocation(0, 0);
		component.setParent(getCell(index));
	}
	public int getCellCount() {
		return cells.getChildCount();
	}
	public void updateSize(int newSize) {
		this.size = newSize;
		for (HudComponent c : cells.getChildren()) {
			c.setSize(newSize, newSize);
		}
	}
}