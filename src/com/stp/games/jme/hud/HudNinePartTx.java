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
 *  @version v1.0 ~ 11/08/2015
 *  HISTORY: Version 1.0 Created control HudNinePartTx ~ 11/08/2017
 */
public class HudNinePartTx extends HudContainer {
	protected HudComponent topLeft;
	protected HudComponent topCenter;
	protected HudComponent topRight;
	protected HudComponent midLeft;
	protected HudComponent midCenter;
	protected HudComponent midRight;
	protected HudComponent bottomLeft;
	protected HudComponent bottomCenter;
	protected HudComponent bottomRight;
	protected int edgeSize;

	public HudNinePartTx(String name, HudStyle style, int edgeSize) {
		super (name, style, 0, 0);
		this.edgeSize = edgeSize;
		int size = edgeSize * 3;
		if (edgeSize > 0) {
			topLeft = createPart(0, 0, 0, 0, 0, edgeSize, edgeSize);
			topCenter = createPart(1, 0, 1, edgeSize, 0, size-(edgeSize*2), edgeSize);
			topRight = createPart(2, 0, 2, size-edgeSize, 0, edgeSize, edgeSize);
			midLeft = createPart(3, 1, 0, 0, edgeSize, edgeSize, size-(edgeSize*2));
			midCenter = createPart(4, 1, 1, edgeSize, edgeSize, size-(edgeSize*2), size-(edgeSize*2));
			midRight = createPart(5, 1, 2, size-edgeSize, edgeSize, edgeSize, size-(edgeSize*2));
			bottomLeft = createPart(6, 2, 0, 0, size-edgeSize, edgeSize, edgeSize);
			bottomCenter = createPart(7, 2, 1, edgeSize, size-edgeSize, size-(edgeSize*2), edgeSize);
			bottomRight = createPart(8, 2, 2, size-edgeSize, size-edgeSize, edgeSize, edgeSize);
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
	public void doLayout(int parentWidth, int parentHeight) {
		if (edgeSize > 0) {
			topCenter.setWidth(parent.getScaledWidth()-(edgeSize*2));
			topRight.setX(parent.getScaledWidth()-edgeSize);
			midLeft.setHeight(parent.getScaledHeight()-(edgeSize*2));
			midCenter.setWidth(parent.getScaledWidth()-(edgeSize*2));
			midCenter.setHeight(parent.getScaledHeight()-(edgeSize*2));
			midRight.setX(parent.getScaledWidth()-edgeSize);
			midRight.setHeight(parent.getScaledHeight()-(edgeSize*2));
			bottomLeft.setY(parent.getScaledHeight()-edgeSize);
			bottomCenter.setY(parent.getScaledHeight()-edgeSize);
			bottomCenter.setWidth(parent.getScaledWidth()-(edgeSize*2));
			bottomRight.setX(parent.getScaledWidth()-edgeSize);
			bottomRight.setY(parent.getScaledHeight()-edgeSize);
			// System.out.println(getName() + " w=" + parent.getScaledWidth() + " h=" + parent.getScaledHeight());
		}
	}
	private HudComponent createPart(int index, int r, int c, int x, int y, int w, int h) {
		HudComponent part = new HudComponent(getName() + "-part" + index, getStyle(), w, h);
		part.setTxCoords(generateTxCoords(c, r, 3));
		part.setRelative();
		part.setLocation(x, y);
		part.setParent(this);
		part.setInheritScale(true);
		part.setZ(-1);
		components.add(part);
		return part;
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		return null;
	}
	@Override
	public float getScale() {
		return 1f;
	}
}