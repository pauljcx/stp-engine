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
 *  @version v1.0 ~ 10/17/2017
 *  HISTORY: Version 1.0 Created control HudScaledList ~ 10/17/2017
 */
public class HudScaledList extends HudContainer {
	protected int selected = 1;
	protected int orientation = LEFT;
	protected int size = 0;
	protected float[] xCoordinates;
	protected float[] yCoordinates;

	public HudScaledList(String name, int width, int height, int orientation, int itemsToShow) {
		super (name, null, width, height);
		this.orientation = orientation;
		this.size = (orientation == RIGHT || orientation == LEFT) ? width : height;
		this.xCoordinates = new float[itemsToShow];
		this.yCoordinates = new float[itemsToShow];
		HudComponent startCap = new HudComponent(name + "list-start", size, size);
		add(startCap);
		HudComponent endCap = new HudComponent(name + "list-end", size, size);
		add(endCap);
	}
	public void addListItem(HudComponent item) {
		components.add(getChildCount() - 1, item);
		item.setParent(this);
		item.setZ(lastZ);
		updateLayout();
		lastZ++;
	}
	@Override
	public void updateLayout() {
		
	}
}
	