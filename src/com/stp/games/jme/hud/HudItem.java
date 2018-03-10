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
import com.jme3.math.ColorRGBA;
import com.jme3.font.BitmapFont;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.math.Vector3f;
import com.jme3.material.Material;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
// Internal Dependencies
import com.stp.games.jme.controls.ItemControl;
import com.stp.games.jme.controls.ItemHolder;

/** @author Paul Collins
 *  @version v1.0 ~ 03/26/2015
 *  HISTORY: Version 1.0 Created control HudItem ~ 03/26/2015
 */
public class HudItem extends HudComponent {
	protected ItemControl item;
	protected ItemHolder holder;
	//protected Geometry shape;
	
	public HudItem(String name, Texture2D texture,  BitmapFont font,  ItemControl item, ItemHolder holder) {
		super (name, item.getSize().getWidth() * 48, item.getSize().getHeight() * 48);
		this.item = item;
		this.holder = holder;
		setDraggable(false);
		if (font != null) {
			//this.text = new HudText(font, ""+item.getQuantity(), new ColorRGBA(1f, 1f, 1f, 1f));
		}
		/*if (item != null) {
			Spatial obj = item.getSpatial();
			if (obj instanceof Geometry) {
				shape = ((Geometry)obj).clone(true);
				DirectionalLight light = new DirectionalLight();
				light.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
				light.setDirection(new Vector3f(2, 0, -10).normalizeLocal());				
				shape.addLight(light);
				shape.updateGeometricState();
			}
		}*/
	}
	/*public void setItem(ItemControl item, ItemHolder holder, AssetManager assetManager) {
		this.item = item;
		this.holder = holder;
		if (item != null  && !item.isNone()) {
			setVisible(true);
			setDraggable(true);
			setTexture((Texture2D)assetManager.loadTexture(item.getIcon()));
			setBackground(new ColorRGBA(1f, 1f, 1f, 1f);
			setSize(item.getSize().getWidth() * 48, item.getSize().getHeight() * 48);
		}
	}*/
	public ItemControl getItem() {
		return item;
	}
	public ItemHolder getHolder() {
		return holder;
	}
	public void setHolder(ItemHolder holder) {
		this.holder = holder;
	}
	public void swap(ItemHolder holderNew, int address) {
		holder.removeItem(item);
		holderNew.addItem(address, item);
		this.holder = holderNew;
	}
	public boolean hasText() {
		return false; //(text != null && item.getQuantity() > 0);
	}
	public void setText(String input) {
		/*if (text != null) {
			text.setText(input);
		}*/
	}
	/*@Override
	public Geometry getGeometry() {
		return shape;
	}*/
}