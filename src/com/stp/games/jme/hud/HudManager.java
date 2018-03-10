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
import com.jme3.post.SceneProcessor;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.MatParamTexture;
import com.jme3.texture.Texture2D;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.shape.Quad;
import com.jme3.system.JmeSystem;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.ColorRGBA;
import com.jme3.font.Rectangle;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.SoftTextDialogInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.SoftTextDialogInputListener;
// Java Dependencies
import java.io.File;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
// Internal Dependencies
import com.stp.util.JavaIO;
import com.stp.util.XMLFileUtility;

/** @author Paul Collins
 *  @version v1.0 ~ 03/21/2015
 *  HISTORY: Version 1.0 Created control HudManager ~ 03/21/2015
 */
public class HudManager implements SceneProcessor,SceneGraphVisitor,AnalogListener,SoftTextDialogInputListener {
	protected static HudManager instance;
	
	public static final String PRIMARY_CLICK = "PrimaryClick";
	public static final String SECONDARY_CLICK = "SecondaryClick";
	public static final String OBSCURED_SAMPLE = "************************************************************";
	
	public static HudFont DEFAULT_FONT;

	protected RenderManager renderManager;
	protected AssetManager assetManager;
	protected InputManager inputManager;
	protected RenderState renderState;
	protected Renderer renderer;
	protected ViewPort vp;
	protected int width;
	protected int height;
	protected boolean initialized;
	protected Material colorMaterial;
	protected Material textureColorMaterial;
	protected Material highlightColorMaterial;
	protected Material vertexColorMaterial;
	protected Material shapeColorMaterial;
	protected ColorRGBA renderColor;
	private Matrix4f tempMat = new Matrix4f();
	private final Quad quad = new Quad(1, -1, true);
    private final Geometry quadGeom = new Geometry("gui-quad", quad);
	private VertexBuffer quadDefaultTC = quad.getBuffer(Type.TexCoord);
    private VertexBuffer quadModTC = quadDefaultTC.clone();
	private Mesh borderMesh;
	private Geometry borderGeom;
	protected HashMap<String, HudStyle> styles = new HashMap<String, HudStyle>();
	protected final ArrayList<HudFont> fonts = new ArrayList<HudFont>();
	protected final ArrayList<HudAudio> sounds = new ArrayList<HudAudio>();
	private HashMap<CachedTextKey, BitmapText> textCacheLastFrame = new HashMap<CachedTextKey, BitmapText>();
    private HashMap<CachedTextKey, BitmapText> textCacheCurrentFrame = new HashMap<CachedTextKey, BitmapText>();
	private final ArrayList<HudCursor> cursorList = new ArrayList<HudCursor>();
	private final ArrayList<HudComponent> popups = new ArrayList<HudComponent>();
	protected final ExecutorService executor = Executors.newSingleThreadExecutor();
	private ActionListener listener;
	
	// Mouse Variables
	private volatile Vector2f mouse = new Vector2f();
	private final Vector2f mouseBoundMin = new Vector2f();
	private final Vector2f mouseBoundMax = new Vector2f();
	private final Vector2f innerBoundMin = new Vector2f();
	private final Vector2f innerBoundMax = new Vector2f();
	private final Vector2f screenPosition = new Vector2f();

	protected float boundEdgeWidth = 24f;
	protected float mouseMoveSpeed = 272f;
	protected boolean invertYaxis = true;
    protected boolean invertXaxis = false;
	protected boolean mouseContained = true;
	
	protected HudContainer root;
	protected HudComponent softTextRecipient;
	protected HudComponent lastClickTarget;
	protected HudCursor cursor;
	protected HudCursor defaultCursor;
	protected HudDragItem dragItem;
	protected HudLoadingOverlay loadingScreen;
	protected boolean dragInitiated;
	protected boolean softKeysEnabled;

	private static class CachedTextKey {
		BitmapFont font;
		String text;
		public CachedTextKey(BitmapFont font, String text) {
			this.font = font;
			this.text = text;
		}
		@Override
		public boolean equals(Object other) {
			CachedTextKey otherKey = (CachedTextKey) other;
			return font.equals(otherKey.font) && text.equals(otherKey.text);
		}
		@Override
		public int hashCode() {
			int hash = 5;
			hash = 53 * hash + font.hashCode();
			hash = 53 * hash + text.hashCode();
			return hash;
		}
	}

	private HudManager(AssetManager assetManager, String fontString) {
		this.assetManager = assetManager;
		
		// Load Defaults
		DEFAULT_FONT = getFont(fontString);
		//HudDropList.DEFAULT_TEXTURE = loadTexture2D("Interface/Defaults/droplist.png");	
		HudLabel.DEFAULT_LABEL_STYLE.setFont(DEFAULT_FONT, 32f, new ColorRGBA(0, 0, 0, 1), BitmapFont.Align.Left, BitmapFont.VAlign.Top, LineWrapMode.NoWrap);
		HudLabel.DEFAULT_LABEL_STYLE.setSize(0, 32, 0, 0);		
		HudButton.TEXT_BUTTON_STYLE.setBackground(null, new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(0.8f, 0.8f, 0.8f, 1f));
		HudButton.TEXT_BUTTON_STYLE.setFont(DEFAULT_FONT, 32f, new ColorRGBA(0, 0, 0, 1), BitmapFont.Align.Left, BitmapFont.VAlign.Top, LineWrapMode.NoWrap);
		HudButton.TEXT_BUTTON_STYLE.setBorder(new ColorRGBA(0, 0, 0, 1f), 1);
		HudButton.TEXT_BUTTON_STYLE.setSize(0, 32, 0, 0);		
		HudButton.IMAGE_BUTTON_STYLE.setBackground(null, new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(0.8f, 0.8f, 0.8f, 1f));		
		HudWindow.DEFAULT_WINDOW_STYLE.setBackground(null, new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(1f, 1f, 1f, 1f));
		HudWindow.DEFAULT_WINDOW_STYLE.setBorder(new ColorRGBA(0, 0, 0, 1f), 1);		
		HudTextField.DEFAULT_TEXT_FIELD_STYLE.setBackground(null, new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(1.0f, 0.97647f, 0.7098f, 1.0f));
		HudTextField.DEFAULT_TEXT_FIELD_STYLE.setFont(DEFAULT_FONT, 32f, new ColorRGBA(0, 0, 0, 1), BitmapFont.Align.Left, BitmapFont.VAlign.Top, LineWrapMode.NoWrap);
		HudTextField.DEFAULT_TEXT_FIELD_STYLE.setBorder(new ColorRGBA(0, 0, 0, 1f), 1);
		HudTextField.DEFAULT_TEXT_FIELD_STYLE.setSize(0, 32, 0, 0);		
		HudScrollBar.DEFAULT_SCROLL_BAR_STYLE.setBackground(loadTexture2D("Interface/Defaults/scroll_arrows.png"), new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(1f, 1f, 1f, 1f));		
		HudTextArea.DEFAULT_TEXT_AREA_STYLE.setFont(DEFAULT_FONT, 32f, new ColorRGBA(0, 0, 0, 1), BitmapFont.Align.Left, BitmapFont.VAlign.Top, LineWrapMode.Word);		
		HudCheckbox.DEFAULT_CHECK_BOX_STYLE.setBackground(loadTexture2D("Interface/Defaults/checkbox_radial.png"), new ColorRGBA(1f, 1f, 1f, 1f), new ColorRGBA(1f, 1f, 1f, 1f));

		borderMesh = new Mesh();
		borderMesh.setBuffer(Type.Position, 3, new float[]{0, 0, 0, 1, 0, 0, 1, -1, 0, 0, -1, 0});
		borderMesh.setBuffer(Type.Normal, 3, new float[]{0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1});
		borderMesh.setBuffer(Type.Index, 2, new short[] {0, 1, 1, 2, 2, 3, 3, 0});
		borderMesh.setMode(Mesh.Mode.LineStrip);
		borderGeom = new Geometry("gui-border", borderMesh);
		
        // Material with a single color (no texture or vertex color)
		renderColor = new ColorRGBA(1, 1, 1, 1);
        colorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // Material with a texture and a color (no vertex color)
        textureColorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		
		highlightColorMaterial = new Material(assetManager, "MatDefs/Gui.j3md");
        
        // Material with vertex color, used for gradients (no texture)
        vertexColorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        vertexColorMaterial.setBoolean("VertexColor", true);
		
		shapeColorMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		shapeColorMaterial.setColor("GlowColor",ColorRGBA.White);
		
		root = new HudContainer("hud-root", 1, 1);
		root.setAbsolute();
		
		cursor = new HudCursor("default-cursor", loadTexture2D("Interface/Cursors/cursor_arrow.png"), 32, 4, 3);
		cursor.setZ(1000);
		cursor.setLocation((int)mouse.x, (int)mouse.y);
		//root.add(cursor);
		
		defaultCursor = new HudCursor("default-cursor", loadTexture2D("Interface/Cursors/cursor_arrow.png"), 32, 4, 3);
		addCursor(defaultCursor);
		setCursor(defaultCursor);
		
		dragItem = new HudDragItem("hud-drag-item");
		//root.add(dragItem);
		dragItem.setZ(999);
		dragInitiated = false;

        // Shared render state for all materials
		renderState = new RenderState();
        renderState.setDepthTest(false);
        renderState.setDepthWrite(false);
		renderState.setBlendMode(RenderState.BlendMode.Alpha);
		renderState.setFaceCullMode(RenderState.FaceCullMode.Back);

		softKeysEnabled = false;
		initialized = false;
	}
	public static HudManager createInstance(AssetManager assetManager, String fontString) {
		if (instance == null) {
			instance = new HudManager(assetManager, fontString);
		}
		return instance;
	}
	public static HudManager getInstance() {
		return instance;
	}
	public Texture2D loadTexture2D(String path) {
		return (Texture2D)assetManager.loadTexture(path);
	}
	public BitmapFont loadFont(String path) {
		return assetManager.loadFont(path);
	}
	public void addCursor(HudCursor hudCursor) {
		cursorList.add(hudCursor);
	}
	public void registerCursor(String name, String texture, int size, int x, int y) {
		addCursor(new HudCursor(name, (Texture2D)assetManager.loadTexture(texture), size, x, y));
	}
	public HudCursor getCursor(String name) {
		for (HudCursor c : cursorList) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return defaultCursor;
	}
	public void setCursor(String name) {
		if (!hasMouseFocus()) {
			setCursor(getCursor(name));
		}
	}
	public void setDefaultCursor() {
		setCursor(defaultCursor);
	}
	public void setMouseSpeed(float speed) {
		this.mouseMoveSpeed = speed;
	}
	public void setCursor(HudCursor hudCursor) {
		if (cursor != null) {
			if (cursor.getName().equals(hudCursor.getName())) {
				return;
			}
			cursor.set(hudCursor);
			cursor.setZ(1000);
			cursor.setLocation((int)mouse.x, (int)mouse.y);
			cursor.setVisible(true);
		}
	}
	public void showCursor() {
		cursor.setVisible(true);
	}
	public void hideCursor() {
		cursor.setVisible(false);
	}
	public HudFont getDefaultFont() {
		return DEFAULT_FONT;
	}
	public HudFont getFont(String path) {
		for (HudFont font : fonts) {
			if (font.getPath().equals(path)) {
				return font;
			}
		}
		BitmapFont loadedFont = assetManager.loadFont(path);
		if (loadedFont != null) {
			HudFont newFont = new HudFont(path);
			newFont.setFont(loadedFont);
			fonts.add(newFont);
			return newFont;
		} else {
			return null;
		}
	}
	public HudAudio getAudio(String path) {
		for (HudAudio audio : sounds) {
			if (audio.getPath().equals(path)) {
				return audio;
			}
		}
		AudioData audioData = assetManager.loadAudio(path);
		if (audioData != null) {
			HudAudio newAudio = new HudAudio(path);
			newAudio.setAudioData(audioData);
			sounds.add(newAudio);
			return newAudio;
		} else {
			return null;
		}
	}
	public void addStyle(HudStyle style) {
		styles.put(style.getName(), style);
	}
	public HudStyle getStyle(String key) {
		HudStyle style = styles.get(key);
		return (style != null) ? style : styles.get("default");
	}
	public static Texture2D getTexture(String path) {
		return (Texture2D)instance.getAssetManager().loadTexture(path);
	}
	public AssetManager getAssetManager() {
		return assetManager;
	}
	public InputManager getInputManager() {
		return inputManager;
	}
	public ExecutorService getExecutor() {
		return executor;
	}
	public RenderManager getRenderManager() {
		return renderManager;
	}
	public HudComponent getFocusComponent() {
		return root.getFocusComponent();
	}
	public void setLoadingScreen(HudLoadingOverlay loadingScreen) {
		this.loadingScreen = loadingScreen;
		root.add(9999999, loadingScreen);
	}
	public HudLoadingOverlay getLoadingScreen() {
		return loadingScreen;
	}
	public void startLoading(float progress, String message) {
		if (loadingScreen != null) {
			if (progress < 0) {
				loadingScreen.setIndeterminate();
			} else {
				loadingScreen.setProgress(progress);
			}
			loadingScreen.setLoadingMessage(message);
			loadingScreen.setLoading(true);
			hideCursor();
		}
	}
	public void stopLoading() {
		if (loadingScreen != null) {
			loadingScreen.setLoading(false);
			showCursor();
		}
	}
	public void setSoftKeysEnabled(boolean softKeysEnabled) {
		this.softKeysEnabled = softKeysEnabled;
	}
	public void requestSoftTextDialog(int id, String title, String initialValue, HudComponent recipient) {
		this.softTextRecipient = recipient;
		SoftTextDialogInput softTextInput = JmeSystem.getSoftTextDialogInput();
		if (softTextInput != null) {
			softTextInput.requestDialog(id, title, initialValue, this);
		}
	}
	public void onSoftText(int action, String text) {
		if (action == SoftTextDialogInputListener.COMPLETE && softTextRecipient != null) {
			softTextRecipient.setTextValue(text);
		}
	}
	public boolean hasMouseFocus() {
		if (dragItem.isVisible()) {
			return true;
		}
		return root.hasMouseFocus((int)mouse.x, (int)mouse.y);
		/*for (HudComponent c : root.getChildren()) {
			if (c.isVisible() && c.contains() {
				return true;
			}
		}
		return false;*/
	}

	// Called when the SP is removed from the RM.
	public void cleanup() {
		initialized = false;
	}
	// Called in the render thread to initialize the scene processor.
	public void initialize(RenderManager rm, ViewPort vp) {
		this.renderManager = rm;
		this.renderer = rm.getRenderer();
		this.vp = vp;
		reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
		mouse.set(width/2, height/2);
		innerBoundMin.set(mouse.x-80, mouse.y-80);
		innerBoundMax.set(mouse.x + 80, mouse.y + 80);
		initialized = true;
	}
	public void update() {
		root.doLayout(width, height);
	}
	public boolean isInitialized() {
		return initialized;
	}
	public void renderForView(ViewPort view, HudComponent source) {
		renderManager.setCamera(view.getCamera(), true);

		popups.clear();
		render(source);
		// Render Popups
		Collections.sort(popups);
		for (HudComponent c : popups) {
			render(c);
		}

		HashMap<CachedTextKey, BitmapText> temp = textCacheLastFrame;
		textCacheLastFrame = textCacheCurrentFrame;
		textCacheCurrentFrame = temp;
		textCacheCurrentFrame.clear();
		renderManager.setForcedRenderState(null);

		renderManager.setCamera(view.getCamera(), false);
	}
	// Called after a frame has been rendered and the queue flushed.
	public void postFrame(FrameBuffer out) {
	}
	// Called after the scene graph has been queued, but before it is flushed.
	public void postQueue(RenderQueue rq) {
		renderManager.setCamera(vp.getCamera(), true);

		popups.clear();
		render(root);
		// Render Popups
		Collections.sort(popups);
		for (HudComponent c : popups) {
			render(c);
		}
		// Render Cursor
		render(dragItem);
		render(cursor);

		HashMap<CachedTextKey, BitmapText> temp = textCacheLastFrame;
		textCacheLastFrame = textCacheCurrentFrame;
		textCacheCurrentFrame = temp;
		textCacheCurrentFrame.clear();
		renderManager.setForcedRenderState(null);

		renderManager.setCamera(vp.getCamera(), false);
	}
	// Called before a frame
	public void preFrame(float tpf) {
		cursor.setLocation((int)mouse.x, (int)mouse.y);
		dragItem.setLocation((int)mouse.x, (int)mouse.y);
		root.update(tpf);
	}
	public void visit(Spatial spatial) {
		if (spatial instanceof Geometry) {
			Geometry shape = (Geometry)spatial;
			MatParamTexture paramTx = shape.getMaterial().getTextureParam("DiffuseMap");
			if (paramTx != null) {
				highlightColorMaterial.setTexture("Texture", paramTx.getTextureValue());
				highlightColorMaterial.render(shape, renderManager);
			}
		}
	}
	private void render(HudComponent component) {
		// Verify the component is visible and should be rendered
		if (component.isVisible()) {
			// Determine if the contents of the component needs to be cliped
			if (component.hasClip()) {
				HudComponent.ClipArea clip = component.getClip();
				renderer.setClipRect(clip.x, getScreenHeight() - clip.y, clip.w, clip.h);
			}
			// Get the positioning values for the component to be rendered
			tempMat.loadIdentity();
			tempMat.angleRotation(component.getRotationAngles());
			tempMat.setTranslation(component.getAbsoluteX(), getScreenHeight() - component.getAbsoluteY(), 0);
			tempMat.setScale(component.getScaledWidth(), component.getScaledHeight(), component.getScaledDepth());
			renderState.setLineWidth(Math.max(1, component.getBorderSize()));
			renderManager.setWorldMatrix(tempMat);
			renderManager.setForcedRenderState(renderState);

			// Render the component background if it has one
			if (component.hasBackground()) {
				Texture2D texture = component.getTexture();
				Spatial spatial = component.getSpatial();
				renderColor.set(component.getBackground());
				renderColor.a = renderColor.getAlpha() * component.getFadeAlpha();
				// Render the components 3D object if it has one
				if (spatial != null) {
					renderManager.setForcedRenderState(RenderState.DEFAULT);
					highlightColorMaterial.setColor("Color", renderColor);
					spatial.depthFirstTraversal(this);
				/*Geometry shape = component.getGeometry();
				if (shape != null) {
					renderManager.setForcedRenderState(null);
					Material mat = shape.getMaterial();
					MatParamTexture paramTx = shape.getMaterial().getTextureParam("DiffuseMap");
					if (paramTx != null) {
						//shapeColorMaterial.setTexture("DiffuseMap", paramTx.getTextureValue());
						//shapeColorMaterial.render(shape, renderManager);
						
						highlightColorMaterial.setColor("Color", component.getBackground());
						highlightColorMaterial.setTexture("Texture", paramTx.getTextureValue());
						highlightColorMaterial.render(shape, renderManager);
					}*/
					//colorMaterial.setColor("Color", component.getBackground());
					//colorMaterial.render(shape, renderManager);
					
				} else if (texture != null) {
					// Set the texture coordinates used for rendering this components texture
					FloatBuffer texCoords = (FloatBuffer)quadModTC.getData();
					texCoords.rewind();
					texCoords.put(component.getTxCoords());
					texCoords.flip();
					quadModTC.updateData(texCoords);
					quad.clearBuffer(Type.TexCoord);
					quad.setBuffer(quadModTC);
					
					if (component.hasHighlight()) {
						highlightColorMaterial.setColor("Color", renderColor);
						highlightColorMaterial.setTexture("Texture", texture);
						highlightColorMaterial.render(quadGeom, renderManager);
					} else {
						textureColorMaterial.setColor("Color", renderColor);
						textureColorMaterial.setTexture("ColorMap", texture);
						textureColorMaterial.render(quadGeom, renderManager);
					}
				} else {
					colorMaterial.setColor("Color", renderColor);
					colorMaterial.render(quadGeom, renderManager);
				}
			}
			// Render the component border if it has one
			if (component.hasBorder()) {
				renderColor.set(component.getBorderColor());
				renderColor.a = renderColor.getAlpha() * component.getFadeAlpha();
				colorMaterial.setColor("Color", renderColor);
				colorMaterial.render(borderGeom, renderManager);
			}
			String textValue = component.getTextValue();
			// Render the components text if it has any
			if (textValue.length() > 0 && component.hasText()) {
				// Determine if the text needs to be obscured
				if (component.isTextObscured()) {
					textValue = OBSCURED_SAMPLE.substring(0, textValue.length());
				}
				// Store text conent to be used in future frames while it remains visible
				CachedTextKey key = new CachedTextKey(component.getFont(), textValue);
				BitmapText text = textCacheLastFrame.get(key);
				if (text == null) {
					text = new BitmapText(component.getFont());
					text.setText(textValue);
					text.updateLogicalState(0);
				}
				textCacheCurrentFrame.put(key, text);
				
				tempMat.loadIdentity();
				tempMat.setTranslation(component.getAbsoluteX(), getScreenHeight() - component.getAbsoluteY(), 0);
				//tempMat.setScale(component.getWidth(), component.getHeight(), 0);

				renderManager.setWorldMatrix(tempMat);
				renderManager.setForcedRenderState(renderState);
				renderColor.set(component.getFontColor());
				renderColor.a = renderColor.getAlpha() * component.getFadeAlpha();
				
				text.setColor(renderColor);
				text.setBox(new Rectangle(5, 1, component.getScaledWidth()-10, component.getScaledHeight()-2));
				text.setSize(component.getScaledFontSize());
				text.setAlignment(component.getTextAlignment());
				text.setVerticalAlignment(component.getTextVerticalAlignment());
				text.setLineWrapMode(component.getLineWrapMode());
				text.updateLogicalState(0);
				text.render(renderManager, renderColor);
				component.setRenderedTextHeight((int)Math.ceil(text.getHeight()));
				//if (hudText.showCaret) {
					//colorMaterial.setColor("Color", component.getBackground());
					//colorMaterial.render(quadGeom, renderManager);
				//}
			}
			// Render Children
			if (component instanceof HudContainer) {
				for (HudComponent c : ((HudContainer)component).getRenderList()) {
					if (c.isPopup()) {
						popups.add(c);
					} else {
						render(c);
					}
				}
			}
			if (component.hasClip()) {
				renderer.clearClipRect();
			}
		}
	}
	public void add(HudComponent component) {
		root.add(component);
	}
	public HudContainer getRootContainer() {
		return root;
	}
	// When using a layered hud structure allows a single layer to be activated at a time
	public void activateExclusiveRootComponent(HudComponent component) {
		for (HudComponent c : root.getChildren()) {
			if (c.equals(component)) {
				c.setVisible(true);
			} else {
				c.setVisible(false);
			}
		}
	}
	public void show() {
		root.setVisible(true);
	}
	public void hide() {
		root.setVisible(false);
	}
	// Called when the resolution of the viewport has been changed.
	public void reshape(ViewPort vp, int w, int h) {
		this.width = w;
		this.height = h;
		mouseBoundMin.set(0, 0);
		mouseBoundMax.set(width, height);
	
		boundEdgeWidth = w*0.01f;
		float guiScale = h/768f;
		for (HudStyle style : styles.values()) {
			style.setScale(guiScale);
		}
		root.setSize(width, height);
		root.doLayout(width, height);
	}
	public int getScreenWidth() {
		return width;
	}
	public int getScreenHeight() {
		return height;
	}
	public boolean toggleMouseContained() {
		setMouseContained(!mouseContained);
		return mouseContained;
	}
	public void setMouseContained(boolean contained) {
		if (contained) {
			mouseBoundMin.set(innerBoundMin);
			mouseBoundMax.set(innerBoundMax);
		} else {
			mouseBoundMin.set(0, 0);
			mouseBoundMax.set(getScreenWidth(), getScreenHeight());
		}
		this.mouseContained = contained;
	}
	public float getMouseX() {
		return mouse.getX();
	}
	public float getMouseY() {
		return mouse.getY();
	}
	public float getMouseYInverted() {
		return getScreenHeight() - mouse.getY();
	}
	public Vector2f getMouseScreenPosition() {
		return screenPosition.set(getMouseX(), getMouseYInverted());
	}
	public Vector2f getMouseLocation() {
		return mouse;
	}
	public void setMouseLocation(float x, float y) {
		/*if (x > mouseBoundMax.x) {
			mouse.setX(mouseBoundMax.x);
		} else if (x < mouseBoundMin.x) {
			mouse.setX(mouseBoundMin.x);
		} else {*/
			mouse.setX(x);
		//}
		/*if (y > mouseBoundMax.y) {
			mouse.setY(mouseBoundMax.y);
		} else if (y < mouseBoundMin.y) {
			mouse.setY(mouseBoundMin.y);
		} else {*/
			mouse.setY(y);
		//}
	}
	public boolean receiveInput(String name, boolean pressed, float value) {
		if (name.equals(PRIMARY_CLICK)) {
			if (pressed) {
				root.clearFocus();
				// Check the last click target to ensure the event cycle was complete
				if (lastClickTarget != null) {
					lastClickTarget.doClick((int)mouse.x, (int)mouse.y, false);
					lastClickTarget = null;
				}
				for (HudComponent c : popups) {
					if (c.hasParent()) {
						lastClickTarget = c.getParent().doClick((int)mouse.x, (int)mouse.y, pressed);
					} else {
						lastClickTarget = c.doClick((int)mouse.x, (int)mouse.y, pressed);
					}
					if (lastClickTarget != null) {
						break;
					}
				}
				if (lastClickTarget == null) {
					lastClickTarget = root.doClick((int)mouse.x, (int)mouse.y, pressed);
				}
				//System.out.println("Click Result: " + result);
				if (lastClickTarget == null) {
					return hasMouseFocus();
				}
				// Check if the clicked component needs text input and soft keyboard input is needed
				if (lastClickTarget.needsKeyInput() && softKeysEnabled) {
					requestSoftTextDialog(SoftTextDialogInput.TEXT_ENTRY_DIALOG, lastClickTarget.getName(), lastClickTarget.getTextValue(), lastClickTarget);
				}
				if (!dragItem.isDragging() && lastClickTarget.dragEnabled()) {
					dragItem.startDrag(lastClickTarget);
					dragItem.doLayout(getScreenWidth(), getScreenHeight());
					dragInitiated = true;
					defaultCursor.setVisible(false);
					System.out.println("Drag Start: " + dragItem);
				}
				String hudName = lastClickTarget.getName();
				if (hudName == null) {
					return hasMouseFocus();
				}
				return true;
			} else {
				if (dragItem.isDragging()) {
					HudComponent target = root.getDropTarget((int)mouse.x, (int)mouse.y);
					if (target != null) {
						if (dragItem.transfer(target, (int)mouse.x, (int)mouse.y)) {
							defaultCursor.setVisible(true);
							return true;
						}
					}
					dragItem.returnItem();
					defaultCursor.setVisible(true);
					return true;
				}
				dragInitiated = false;
				if (lastClickTarget != null) {
					lastClickTarget.doClick((int)mouse.x, (int)mouse.y, false);
					lastClickTarget = null;
					return true;
				}
				return false;
			}
		}
		if (pressed) {
			return root.receiveInput(name, value);
		} else {
			return false;
		}
	}
	public void registerWithInput(InputManager inputManager) {
		this.inputManager = inputManager;
		inputManager.addListener(this, "MouseMotionXPos", "MouseMotionXNeg", "MouseMotionYPos", "MouseMotionYNeg");
	}
	public void onAnalog(String name, float value, float tpf) {
		float speed = (value*mouseMoveSpeed);
		 if (name.equals("MouseMotionXNeg")) {
			mouse.subtractLocal(speed, 0);
			if (mouse.x < (mouseBoundMin.x+boundEdgeWidth)) {
				mouse.setX(Math.max(mouse.x, mouseBoundMin.x));
			}
			return;
        }
		if (name.equals("MouseMotionXPos")) {
			mouse.addLocal(speed, 0);
			if (mouse.x > (mouseBoundMax.x-boundEdgeWidth)) {
				mouse.setX(Math.min(mouse.x, mouseBoundMax.x));
			}
			return;
        }
		if (name.equals("MouseMotionYPos")) {
			mouse.subtractLocal(0, speed);
			if (mouse.y < (mouseBoundMin.y+boundEdgeWidth)) {
				mouse.setY(Math.max(mouse.y, mouseBoundMin.y));
			}
			return;
        }
		if (name.equals("MouseMotionYNeg")) {
			mouse.addLocal(0, speed);
			if (mouse.y > (mouseBoundMax.y-boundEdgeWidth)) {
				mouse.setY(Math.min(mouse.y, mouseBoundMax.y));
			}
			return;
		}
	}
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	public void doAction(String name, boolean state, float value) {
		if (listener != null) {
			listener.onAction(name, state, value);
		}
	}
	public boolean saveStyles(File sFile) {
		if (styles.size() > 0) {
			return JavaIO.saveXML(sFile, styles.values().toArray(new HudStyle[styles.size()]));
		}
		return false;
	}
	public boolean loadStyles(InputStream in) {
		try {
			HudStyle[] inData = (HudStyle[])XMLFileUtility.readXMLObjects(in);
			for (HudStyle style : inData) {
				styles.put(style.getName(), style);
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	public void destroy() {
		executor.shutdown();
	}
	public void listAll(HudComponent component) {
		System.out.println(component + " w=" + component.getScaledWidth() + " h=" + component.getScaledHeight());
		if (component instanceof HudContainer) {
			for (HudComponent child : ((HudContainer)component).getRenderList()) {
				listAll(child);
			}
		}
	}
}