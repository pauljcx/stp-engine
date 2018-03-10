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
import com.jme3.texture.Texture2D;
// Java Dependencies
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.0 ~ 04/14/2015
 *  HISTORY: Version 1.0 Created control HudRadialList ~ 04/14/2015
 */
public class HudRadialList extends HudGrid {
	protected int selected = 0;
	protected int radius = 1;
	protected double delta;
	protected double goal;
	protected boolean active;
	protected HudComponent selector;

	public HudRadialList(String name, int width, int height, int radius) {
		this (name, null, null, width, height, radius, 8);
	}
	public HudRadialList(String name, Texture2D texture, Texture2D selectTx, int width, int height, int radius, int count) {
		super (name, null, width, height, 48);
		setHorizontalAlignment(HudComponent.MIN_JUSTIFY, -(width/2));
		this.radius = radius;
		this.delta = 0;
		this.goal = 0;
		this.speed = 1.8f;
		/*selector = new HudComponent(name + "-selector", selectTx, width, height);
		selector.setRelative();
		add(selector);
		for (int c = 0; c < count; c++) {
			cells.add(new Cell(cells.getName() + "-cell" + c, null, 48, 48, this, c));
		}*/
		setActive(false);
	}
	public int getSelectedIndex() {
		return selected;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
		selector.setVisible(active);
	}
	@Override
	public boolean receiveInput(String command, float value) {
		if (command.equals("increment")) {
			selected--;
			if (selected < 0) {
				selected = getCellCount()-1;
			}
			goal = goal + ((Math.PI*2)/getCellCount());
			return true;
		}
		if (command.equals("decrement")) {
			selected++;
			if (selected >= getCellCount()) {
				selected = 0;
			}
			goal = goal - ((Math.PI*2)/getCellCount());
			return true;
		}
		return false;
	}
	/*@Override
	public HudComponent doClick(int x, int y) {
		for (int c = 0; c < components.size(); c++) {
			HudComponent result = components.get(c).doClick(x, y);
			if (result != null) {
				HudComponent parent = result.getParent();
				if (parent instanceof Cell) {
					int address = ((Cell)parent).getAddress();
					int half = getCellCount()/2;
					int p = address - selected;
					if (p > half) {
						p = p - getCellCount();
					}
					if (p < -half) {
						p = p + getCellCount();
					}
					goal = goal - (((Math.PI*2)/getCellCount())*p);
					selected = address;
					return this;
				}
			}
		}
		return null;
	}*/
	public void updateGrid() {
		double angle = (Math.PI*2)/getCellCount();
		for (int c = 0; c < getCellCount(); c++) {
			// Update Positions
			int x = (int)Math.floor(Math.cos((angle*c)+delta)*radius);
			int y = (int)Math.floor(Math.sin((angle*c)+delta)*radius);
			x = x - (cells.getChildAt(c).getWidth()/2);
			y = y - (cells.getChildAt(c).getHeight()/2);
			cells.getChildAt(c).setLocation(x+(getWidth()/2), y+(getHeight()/2));
			//cells.getChildAt(c).setAlpha(0.5f);
			//System.out.println("Update Grid: " + c + " | " + cells.getChildAt(c).getX() + " | " + cells.getChildAt(c).getY());
			//System.out.println("Cells: " + cells.getWidth() + " | " + cells.getHeight() + " | " + cells.getAbsoluteX() + " | " + cells.getAbsoluteY());
		}
		//cells.getChildAt(selected).setAlpha(1f);
		/*for (HudComponent c : components) {
			System.out.println(c.getName() + " | " + c.getWidth() + " | " + c.getHeight() + " | " + c.getAbsoluteX() + " | " + c.getAbsoluteY());
		}*/
	}
	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (delta != goal) {
			double d = tpf * speed * Math.PI;
			if (delta < goal) {
				delta = delta + d;
				if (delta > goal) {
					delta = goal;
				}
			} else {
				delta = delta - d;
				if (delta < goal) {
					delta = goal;
				}
			}
			updateGrid();
		}
	}
}