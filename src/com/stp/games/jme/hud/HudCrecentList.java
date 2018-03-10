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
 *  @version v1.0 ~ 01/05/2015
 *  HISTORY: Version 1.0 Created control HudCrecentList ~ 01/11/2015
 */
public class HudCrecentList extends HudContainer {
	protected int selected = 1;

	public HudCrecentList(String name, int width, int height) {
		this (name, null, width, height);
	}
	public HudCrecentList(String name, Texture2D texture, int width, int height) {
		super (name, width, height);
	}
	public void addListItem(HudComponent item) {
		item.setVerticalAlignment(RELATIVE, 0);
		item.setHorizontalAlignment(RELATIVE, 0);
		add(item);
	}
	// Update the Hud components position given it parents dimensions
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		int cy = (getHeight()/2)-16;
		for (int c = 0; c < components.size(); c++) {
			// Update Positions
			int delta = Math.abs(c-selected)*4;
			components.get(c).setLocation(32-delta, cy+((c-selected)*48));
			components.get(c).setSize(32-delta, 32-delta);
		}
		super.doLayout(parentWidth, parentHeight);
	}
}