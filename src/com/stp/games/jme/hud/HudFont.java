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

/** @author Paul Collins
 *  @version v1.0 ~ 11/04/2017
 *  HISTORY: Version 1.0 Created the HudFont object to store the path information for loaded fonts ~ 11/4/2017
 */
public class HudFont {
	protected String path = "";
	protected BitmapFont font;
	
	public HudFont(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setFont(BitmapFont font) {
		this.font = font;
	}
	public BitmapFont getFont() {
		return font;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HudFont) {
			return ((HudFont)obj).getPath().equals(path);
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return path;
	}
}