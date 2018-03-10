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
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 10/05/2015
 *  HISTORY: Version 1.0 Hud Button ~ 10/05/2015
 */
public class HudCheckbox extends HudContainer {
	public static final HudStyle DEFAULT_CHECK_BOX_STYLE = new HudStyle("HudCheckBox");
	
	protected boolean selected;
	protected float[] txCoords0;
	protected float[] txCoords1;
	protected HudComponent check;
	protected HudLabel label;
	
	public HudCheckbox(String name, String textValue, HudStyle style, int width, int height) {
		super(name, style, width, height);
		txCoords0 = generateTxCoords(0, 0, 2);
		txCoords1 = generateTxCoords(1, 0, 2);
		
		check = new HudComponent(name + "-check", style, 1, 0);
		check.setRelative();
		setSelected(false);

		label = new HudLabel(textValue, name + "-label", style, 1, 0);
		label.setRelative();
		
		add(check);
		add(label);
	}
	@Override
	public boolean hasBackground() {
		return false;
	}
	public boolean hasText() {
		return false;
	}
	@Override
	public void updateLayout() {
		check.setWidth(getHeight());
		label.setWidth(getWidth()-getHeight());
		label.setX(getHeight());
	}
	public boolean toggleSelected() {
		setSelected(!selected);
		return selected;
	}
	@Override
	public String getTextValue() {
		return label.getTextValue();
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected) {
			check.setTxCoords(txCoords1);
		} else {
			check.setTxCoords(txCoords0);
		}
	}
	public boolean isSelected() {
		return selected;
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && this.contains(x, y)) {
			// Toggle state only on valid click up events
			if (!isPressed) {
				toggleSelected();
			}
			return this;
		}
		return null;
	}
}