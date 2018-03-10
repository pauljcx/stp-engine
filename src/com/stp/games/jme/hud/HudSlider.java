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
 *  @version v1.0 ~ 11/09/2015
 *  HISTORY: Version 1.0 Created control HudSlider ~ 11/09/2015
 */
public class HudSlider extends HudContainer {
	protected String[] values;
	protected HudLabel label;
	protected HudComponent bar;
	protected HudComponent mark;
	protected int barSize;
	protected int selection;
	
	public HudSlider(String name, HudStyle style, String[] values) {
		this(name, style, values, 0, 0);
	}
	public HudSlider(String name, HudStyle style, String[] values, int width, int height) {
		super (name, style, width, 1);
		this.values = values;
		this.barSize = (height != 0) ? (height / 2) : style.getHeight();
		this.bar = new HudComponent(name + "-bar", style, width, barSize);
		bar.setTxCoords(generateTxCoords(0, 0, 2));
		bar.setVerticalAlignment(MAX_JUSTIFY, 0);
		add(bar);
		this.mark = new HudComponent(name + "-mark", style, barSize, barSize);
		mark.setTxCoords(generateTxCoords(1, 0, 2));
		mark.setVerticalAlignment(MAX_JUSTIFY, 0);
		mark.setActive(true);
		add(mark);
		this.label = new HudLabel("0", name + "-label", style, 0, 0);
		label.setAutoWidth(true);
		label.setTextValue(values[0]);
		label.setVerticalAlignment(MIN_JUSTIFY, 0);
		add(label);
		setHeight(2*barSize);
	}
	@Override
	public boolean hasBackground() {
		return false;
	}
	public void setSelection(int index) {
		if (index <= 0) {
			this.selection = 0;
			mark.setX(0);
			label.setX(0);
			label.setTextValue(values[selection]);
		} else if (index >= (values.length-1)) {
			this.selection = values.length -1;
			label.setTextValue(values[selection]);
			mark.setX(getWidth()-mark.getWidth());
			label.setX(getWidth()-label.getWidth());
		} else {
			this.selection = index;
			label.setTextValue(values[selection]);
			int nx = selection * (getWidth()/(values.length-1));
			mark.setX(nx - mark.getCenterX());
			label.setX(nx - label.getCenterX());
		}
	}
	public int getSelection() {
		return selection;
	}
	public String getValue() {
		return values[selection];
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && this.contains(x, y)) {
			if (isPressed) {
				int deltaX = x - getAbsoluteX() + mark.getCenterX();
				int index = Math.round((float)deltaX/(float)(getScaledWidth()/(values.length-1)));
				setSelection(index);
			}
			return this;
		}
		return null;
	}
	@Override
	public HudComponent doScroll(int x, int y, int axis, float value) {
		if (isViewable() && this.contains(x, y)) {
			if (axis == SCROLL_UP) {
				setSelection(selection+1);
			}
			if (axis == SCROLL_DOWN) {
				setSelection(selection-1);
			}
			return this;
		}
		return null;
	}
}