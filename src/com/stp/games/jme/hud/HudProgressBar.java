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
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
// Java Dependencies
import java.util.ArrayList;

/* @author Paul Collins
 *  @version v1.1 ~ 03/22/2017
 *  HISTORY: Version 1.1 added indeterminate option ~ 03/22/2017
 *      Version 1.0 Created control HudProgressBar ~ 10/06/2015
 */
/**
 * The HudProgressBar class defines a custom Hud component for rendering a
 * progress bar by using two images overlaying one over the other. The overlaying image
 * is drawn with it's width as a fraction of the actual width based on the current progress.
 *
 */
public class HudProgressBar extends HudContainer {
	protected HudComponent overlay;
	protected float progress;
	protected boolean indeterminate;
	protected int indeterminateWidth;
	protected boolean advancing;
	protected float overlayX;
	
	public HudProgressBar(String name, HudStyle style, int width, int height) {
		super(name, style, width, height);
		this.overlay = new HudComponent(name + "-overlay", style, 1, 0);
		overlay.setTextureName("overlay");
		add(overlay);
		
		this.progress = 1f;
		this.indeterminate = false;
		this.advancing = true;
		this.overlayX = 0f;
		this.indeterminateWidth = (int)Math.round(getWidth()*0.1f);
	}
	public void setOverlayName(String overlayName) {
		overlay.setTextureName(overlayName);
	}
	@Override
	public void setColorName(String value) {
		super.setColorName(value);
		overlay.setColorName(value);
	}
	public void setProgress(float progress) {
		if (progress > 1) {
			this.progress = 1f;
		} else if (progress < 0) {
			this.progress = 0;
		} else {
			this.progress = progress;
		}
		this.indeterminate = false;
		overlay.setWidth((int)Math.round(getWidth()*progress));
	}
	public float getProgress() {
		return progress;
	}
	public void setIndeterminate() {
		this.indeterminate = true;
		this.overlayX = 0f;
		this.advancing = true;
		this.indeterminateWidth = (int)Math.round(getWidth()*0.1f);
		overlay.setWidth(indeterminateWidth);
		overlay.setX(0);
	}
	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (isViewable() && indeterminate) {
			if (advancing) {
				this.overlayX = overlayX + (tpf * 120);
				int nx = (int)overlayX;
				if (nx > (getWidth() - indeterminateWidth)) {
					nx = (getWidth() - indeterminateWidth);
					this.advancing = false;
				}
				overlay.setX(nx);
			} else {
				this.overlayX = overlayX - (tpf * 120);
				int nx = (int)overlayX;
				if (nx < 0) {
					nx = 0;
					this.advancing = true;
				}
				overlay.setX(nx);
			}
		}
	}
}
