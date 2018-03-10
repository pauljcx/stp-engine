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
 *  @version v1.0 ~ 11/25/2017
 *  HISTORY: Version 1.0 HudIcon ~ 11/25/2017
 */
public class HudIcon extends HudComponent {
	protected Texture2D texture;
	
	public HudIcon(String name, Texture2D texture, HudStyle style, int width, int height) {
		super(name, style, width, height);
		setTexture(texture);
	}
	@Override
	public Texture2D getTexture() {
		if (texture != null) {
			return texture;
		} else {
			return super.getTexture();
		}
	}
	public void setTexture(Texture2D texture) {
		this.texture = texture;
	}
}