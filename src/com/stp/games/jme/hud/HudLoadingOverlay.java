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

/** @author Paul Collins
 *  @version v1.0 ~ 02/28/2018
 *  HISTORY: Version 1.0 HudLoading ~ 02/28/2018
 */
public abstract class HudLoadingOverlay extends HudContainer {
	
	public HudLoadingOverlay(String name) {
		super (name);
	}
	public HudLoadingOverlay(String name, int width, int height) {
		super (name, width, height);
	}
	public HudLoadingOverlay(String name, HudStyle style, int width, int height) {
		super (name, style, width, height);
	}
	public abstract void setLoading(boolean loading);
	public abstract boolean isLoading();
	public abstract void setIndeterminate();
	public abstract void setProgress(float progress);
	public abstract float getProgress();
	public abstract void setLoadingMessage(String message);
}