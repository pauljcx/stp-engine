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

/** @author Paul Collins
 *  @version v1.0 ~ 10/27/2015
 *  HISTORY: Version 1.0 Hud Label ~ 10/27/2015
 */
public class HudLabel extends HudComponent {
	public static final HudStyle DEFAULT_LABEL_STYLE = new HudStyle("HudLabel");
	
	protected boolean autoWidth;
	protected boolean obscured;
	protected String textValue = "";
	
	public HudLabel(String textValue, String name) {
		this (textValue, name, 0, 0);
	}
	public HudLabel(String textValue, String name, int width, int height) {
		this (textValue, name, DEFAULT_LABEL_STYLE, width, height);
	}
	public HudLabel(String textValue, String name, HudStyle style, int width, int height) {
		super(name, style, width, height);
		this.autoWidth = (width == 0 && style.getWidth() == 0 && style.getFloat("widthFactor") == 0);
		setTextValue(textValue);
		setTextObscurity(false);
		setRenderBackground(false);
		setRelative();
	}
	public void setAutoWidth(boolean autoWidth) {
		this.autoWidth = autoWidth;
		setInheritWidth(!autoWidth);
	}
	@Override
	public String getTextValue() {
		return textValue;
	}
	@Override
	public void setTextValue(String input) {
		this.textValue = input;
		if (autoWidth) {
			setWidth(10 + style.getTextDisplayWidth(textValue, getFont(), getFontSize()));
		}
	}
	@Override
	public boolean isTextObscured() {
		return obscured;
	}
	public void setTextObscurity(boolean obscured) {
		this.obscured = obscured;
	}
}