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
package com.stp.games.jme.client;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.app.Application;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture2D;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
// Internal Dependencies
import com.stp.games.jme.hud.HudContainer;
import com.stp.games.jme.hud.HudComponent;
import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.hud.HudButton;
import com.stp.games.jme.hud.HudLabel;
import com.stp.games.jme.hud.HudWindow;

/** @author Paul Collins
 *  @version v1.0 ~ 09/27/2017
 *  HISTORY: Version 1.0 Created hud component ClientMenu ~ 09/27/2017
 */
public class ClientMenu extends HudContainer {
	protected ClientScreen screen;
	protected HudManager hud;
	protected HudWindow messageWindow;
	protected HudContainer messagePanel;
	protected HudLabel messageLabel;
	protected HudButton messageButton;
	protected HudContainer contextMenu;
	protected float scrollSpeed = 16f;

	public ClientMenu(String name, ClientScreen screen) {
		super(name, 0, 0);
		this.screen = screen;
		this.hud = HudManager.getInstance();
		setAbsolute();
		setVisible(false);

		messageLabel = new HudLabel("Message", name + "-message-label", hud.getStyle("WhiteTextMedium"), 0, 0);
		messageLabel.setHorizontalAlignment(CENTERED, 0);
		messageLabel.setAutoWidth(true);

		messageButton = new HudButton("Ok", name + "-message-button", hud.getStyle("Button"), 0, 0);
		messageButton.setHorizontalAlignment(CENTERED, 0);

		messagePanel = new HudContainer(name + "-message-panel", hud.getStyle("Container"), 1, 1);
		messagePanel.add(messageLabel);
		messagePanel.add(messageButton);
		messagePanel.setLayoutMode(VERTICAL_LIST);
		
		messageWindow = new HudWindow(name + "-message-window", "Message", hud.getStyle("Window"), 256, 256);
		messageWindow.disableExitButton();
		messageWindow.setCentered();
		messageWindow.setVisible(false);
		messageWindow.add(messagePanel);
		messageWindow.getContentPane().add(messagePanel);
		add(1000, messageWindow);
		
		contextMenu = new HudContainer(name + "-context-menu", hud.getStyle("Button"), 160, 1);
		contextMenu.setRenderBackground(false);
		contextMenu.setLayoutMode(VERTICAL_LIST);
		contextMenu.setAbsolute();
		contextMenu.setPopup(true);
		contextMenu.setVisible(false);
		add(contextMenu);
	}
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		super.doLayout(parentWidth, parentHeight);
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (eventCode == CLICK_EVENT) {
			if (comp.equals(messageButton)) {
				messageWindow.zoomFadeOut(4f);
			}
			if (comp.getParent().equals(contextMenu)) {
				contextMenu.setVisible(false);
				screen.receiveInput("context:" + comp.getTextValue(), true, 0f);
			}
		}
	}
	@Override
	public HudComponent doScroll(int x, int y, int axis, float value) {
		return super.doScroll(x, y, axis, value*scrollSpeed);
	}
	public void showMessage(String message) {
		if (isViewable()) {
			messageLabel.setTextValue(message);
			messagePanel.setWidth(Math.max(256, messageLabel.getWidth()));
			messageWindow.pack();
			messageWindow.zoomFadeIn(10f);
		}
	}
	public void showContextMenu(int eventX, int eventY, String... options) {
		if (isViewable()) {
			contextMenu.removeAll();
			contextMenu.setSize(160, contextMenu.getStyle().getHeight() * options.length);
			for (int i = 0; i < options.length; i++) {
				HudButton menuItem = new HudButton(options[i], name + "-context-" + i, contextMenu.getStyle(), 160, 0);
				menuItem.setHorizontalAlignment(CENTERED, 0);
				contextMenu.add(menuItem);
			}
			contextMenu.setLocation(eventX, eventY);
			contextMenu.setVisible(true);
		}
	}
	public void hideContextMenu() {
		contextMenu.setVisible(false);
	}
}