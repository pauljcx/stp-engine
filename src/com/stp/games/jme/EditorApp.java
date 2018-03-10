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
package com.stp.games.jme;
// JME3 Dependencies
import com.jme3.app.SimpleApplication;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.system.AppSettings;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.math.ColorRGBA;
import com.jme3.input.MouseInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
// Java Dependencies
import java.io.File;
// Internal Dependencies
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.controls.*;
import com.stp.games.jme.hud.*;

public class EditorApp extends SimpleApplication implements ActionListener {
	protected GameRegistry registry;
	protected HudManager hud;
	protected HudEditorApp editor;
	
	public EditorApp() {
	}

	@Override
	public void simpleInitApp() {
		/* Clear JME Defaults */
		setDisplayStatView(false);
		setDisplayFps(true);
		flyCam.unregisterInput();
		stateManager.detach(stateManager.getState(FlyCamAppState.class));
		stateManager.detach(stateManager.getState(DebugKeysAppState.class));
		guiNode.detachAllChildren();
		rootNode.detachAllChildren();
		
		registry = GameRegistry.getInstance();
		registry.setAssetManager(assetManager);
		//registry.setSaveFile(new File("schemas.zip"));
		
		registerObjectTypes(registry);
		registerResources();

		setupInputs();
		setupGUI();
		
		editor = new HudEditorApp("editor", this);
		hud.add(editor);
	}
	
	@Override
	public void simpleUpdate(float tpf) {
	}
	
	private void setupInputs() {
		inputManager.clearMappings();

		inputManager.addMapping("quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addMapping("LeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, "LeftClick", "RightClick", "quit", "test");
		inputManager.setCursorVisible(false);
	}
	
	private void setupGUI() {
		hud = HudManager.createInstance(assetManager, "Font/open-sans-40.fnt");
		hud.setMouseSpeed(300f);
		hud.registerCursor("select", "Interface/cursor10.png", 32, 5, 4);
		hud.registerCursor("unit", "Interface/cursor11.png", 32, 15, 15);
		hud.registerCursor("grab", "Interface/cursor02.png", 32, 7, 0);
		hud.registerWithInput(inputManager);
		//HudManager.putFontStyle("HudLabel", new HudManager.FontStyle(assetManager.loadFont("Font/open-sans-40.fnt"), 16f, new ColorRGBA(0, 0, 0, 1)));
		//HudManager.putFontStyle("HudButton", new HudManager.FontStyle(assetManager.loadFont("Font/open-sans-40.fnt"), 16f, new ColorRGBA(0, 0, 0, 1)));
		getGuiViewPort().addProcessor(hud);
	}
	
	public void registerObjectTypes(GameRegistry r) {
	}
	
	public void registerResources() {
		
	}
	
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("quit")) {
			//registry.save();
			stop();
			return;
		}
		// Check Hud first to see if it consumes the input event
		if (hud.receiveInput(name, isPressed, tpf)) {
			return;
		}
	}
	
	@Override
	public void destroy()	{
		super.destroy();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		EditorApp instance = new EditorApp();
		instance.setShowSettings(false);
		
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1600, 900);
		settings.setBitsPerPixel(24);
		settings.setFullscreen(false);
		settings.setVSync(true);
		settings.setFrameRate(120);
		settings.setTitle("EditorApp");
		
		instance.setSettings(settings);
		instance.setPauseOnLostFocus(false);
		try {
			instance.start();
		} catch (Exception ex) {
			System.exit(0);
		}
	}
}