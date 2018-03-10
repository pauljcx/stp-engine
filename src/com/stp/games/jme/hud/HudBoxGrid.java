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
 *  @version v1.0 ~ 04/24/2015
 *  HISTORY: Version 1.0 Created control HudBoxGrid ~ 04/24/2015
 */
public class HudBoxGrid extends HudGrid {
	protected int spacing;
	protected int rows;
	protected int columns;

	public HudBoxGrid(String name, HudStyle style, int size, int spacing, int rows, int columns) {
		super (name, style, (columns*size)+(columns*spacing)+spacing, (rows*size)+(rows*spacing)+spacing, size);
		this.spacing = spacing;
		this.rows = rows;
		this.columns = columns;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				cells.add(new Cell(cells.getName() + "-cell" + cells.getChildCount(), style, size, size, cells.getChildCount()));
			}
		}
	}
	public Cell getCell(int row, int column) {
		int index = (row*columns) + column;
		if (index >= 0 && index < cells.getChildCount()) {
			return (Cell)cells.getChildAt(index);
		}
		return null;
	}
	public void set(int row, int column, HudComponent component) {
		add(component);
		component.setRelative();
		component.setLocation(0, 0);
		component.setParent(getCell(row, column));
	}
	public void updateSpacing(int spacing) {
		this.spacing = spacing;
		setSize((columns*size)+(columns*spacing)+spacing, (rows*size)+(rows*spacing)+spacing);
	}
	@Override
	public void updateSize(int newSize) {
		super.updateSize(newSize);
		setSize((columns*size)+(columns*spacing)+spacing, (rows*size)+(rows*spacing)+spacing);
	}
	@Override
	public void updateLayout() {
		int row = 0;
		int col = 0;
		for (HudComponent comp : cells.getChildren()) {
			int x0 = (col*size) + (col*spacing) + spacing;
			int y0 = (row* size) + (row*spacing) + spacing;
			comp.setRelative();
			comp.setLocation(x0, y0);
			comp.setSize(size, size);
			col++;
			if (col == columns) {
				col = 0;
				row++;
			}
		}
	}
}