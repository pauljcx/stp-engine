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

/** @author Paul Collins
 *  @version v1.0 ~ 03/05/2018
 *  HISTORY: Version 1.0 Created control HudThreePartTx ~ 03/05/2018
 */
public class HudThreePartTx extends HudContainer {
	protected HudComponent left;
	protected HudComponent center;
	protected HudComponent right;
	protected int edgeSize;

	public HudThreePartTx(String name, HudStyle style, int edgeSize) {
		super (name, style, 0, 0);
		this.edgeSize = edgeSize;
		if (edgeSize > 0) {
			float x1 = 1f/3f;
			float x2 = 2f/3f;
			left = new HudComponent(name + "-left", style, edgeSize, 0);
			left.setTxCoords(new float[] { 0, 1, x1, 1, x1, 0, 0, 0 });
			left.setRelative();
			left.setLocation(0, 0);
			left.setZ(-1);
			components.add(left);

			center = new HudComponent(name + "-center", style, edgeSize, 0);
			center.setTxCoords(new float[] { x1, 1, x2, 1, x2, 0, x1, 0 });
			center.setRenderBackground(true);
			center.setRelative();
			center.setLocation(edgeSize, 0);
			center.setZ(-1);
			components.add(center);
			
			right = new HudComponent(name + "-right", style, edgeSize, 0);
			right.setTxCoords(new float[] { x2, 1, 1, 1, 1, 0, x2, 0 });
			right.setRelative();
			right.setLocation(getWidth()-edgeSize, 0);
			right.setZ(-1);
			components.add(right);
		}
	}
	@Override
	public void setTextureName(String textureName) {
		for (HudComponent child : getChildren()) {
			child.setTextureName(textureName);
		}
	}
	@Override
	public void setColorName(String colorName) {
		for (HudComponent child : getChildren()) {
			child.setColorName(colorName);
		}
	}
	@Override
	public boolean hasBackground() {
		return false;
	}
	@Override
	public boolean hasText() {
		return false;
	}
	@Override
	public boolean hasBorder() {
		return false;
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		return null;
	}
	@Override
	public float getScale() {
		return 1f;
	}
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		if (parent != null) {
			this.edgeSize = (int)(parent.getScaledWidth()/3f);
			if (edgeSize > 0) {
				left.setWidth(edgeSize);
				left.setHeight(parent.getScaledHeight());
				center.setWidth(parent.getScaledWidth()-(edgeSize*2));
				center.setHeight(parent.getScaledHeight());
				center.setX(edgeSize);
				right.setWidth(edgeSize);
				right.setHeight(parent.getScaledHeight());
				right.setX(parent.getScaledWidth()-edgeSize);
			}
		}
	}
}