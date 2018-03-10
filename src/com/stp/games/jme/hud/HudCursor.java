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
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 03/29/2015
 *  HISTORY: Version 1.0 Created control HudCursor  ~ 03/29/2015
 */
public class HudCursor extends HudComponent {
	protected Texture2D texture;
	
	public HudCursor(String name, Texture2D texture, int size) {
		this (name, texture, size, 0, 0);
	}
	public HudCursor(String name, Texture2D texture, int size, int xOffset, int yOffset) {
		super (name, DEFAULT_STYLE, size, size);
		setTexture(texture);
		setAbsolute();
		insets[LEFT] = xOffset;
		insets[TOP] = yOffset;
	}
	public void set(HudCursor in) {
		name = in.getName();
		setSize(in.getWidth(), in.getHeight());
		insets[LEFT] = in.getLeftOffset();
		insets[TOP] = in.getTopOffset();
		setTexture(in.getTexture());
	}
	@Override
	public boolean hasBackground() {
		return true;
	}
	@Override
	public ColorRGBA getBackground() {
		return ColorRGBA.White;
	}
	@Override
	public Texture2D getTexture() {
		return texture;
	}
	public void setTexture(Texture2D texture) {
		this.texture = texture;
	}
	public int getLeftOffset() {
		return insets[LEFT];
	}
	public int getTopOffset() {
		return insets[TOP];
	}
	@Override
	public boolean contains(int x, int y) {
		return false;
	}
	@Override
	public int getX() {
		return super.getX() - insets[LEFT];
	}
	@Override
	public int getY() {
		return super.getY() - insets[TOP];
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		return null;
	}
}