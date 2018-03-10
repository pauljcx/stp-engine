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
package com.stp.games.jme.terrain;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.scene.shape.Sphere;

// Java Dependencies
// Internal Dependencies
import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.hud.HudContainer;
import com.stp.games.jme.hud.HudWindow;

public class RegionMapping implements SceneProcessor {
	protected ViewPort view;
	protected HudManager hud;
	protected Node node;
	protected Texture2D texture;
	protected boolean initialized;
	protected float delta;
	protected HudContainer gui;
	protected HudWindow window;
	protected int width;
	protected int height;
	
	public RegionMapping(RenderManager renderManager) {
		hud = HudManager.getInstance();
		AssetManager assetManager = hud.getAssetManager();

		this.width = 1024;
		this.height = 1024;
		
		Camera camera = new Camera(width, height);

        view = renderManager.createPreView("Offscreen View", camera);
		view.setClearFlags(true, true, true);
        view.setBackgroundColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));

        // Create offscreen framebuffer
        FrameBuffer frameBuffer = new FrameBuffer(width, height, 1);

        // Setup camera
		/*camera.setAxes(new Vector3f(1, 0, 0), Vector3f.UNIT_Y, new Vector3f(0, 0, 1f));
		camera.setLocation(new Vector3f(0, 0, 10));
		camera.setParallelProjection(true);
		camera.setFrustum(-10, 100, -50, 50, -50, 50);
		camera.update();*/
		
		 // Setup framebuffer's texture
		texture = new Texture2D(width, height, Format.RGBA8);
        texture.setMinFilter(Texture.MinFilter.Trilinear);
        texture.setMagFilter(Texture.MagFilter.Bilinear);

        // Setup framebuffer to use texture
        frameBuffer.setDepthBuffer(Format.Depth);
        frameBuffer.setColorTexture(texture);
        
        // Set viewport to render to offscreen framebuffer
        view.setOutputFrameBuffer(frameBuffer);
		
		// Setup framebuffer's scene
        /*Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", new ColorRGBA(0.0f, 0.0f, 1.0f, 1.0f));
        Geometry shape = new Geometry("Sphere", new Sphere(16, 16, 128f));
        shape.setMaterial(material);
		node = new Node("Mapping Node");
		node.setQueueBucket(Bucket.Gui);
		node.attachChild(shape);
		node.setLocalTranslation(512f, 512f, 0);
		node.updateGeometricState();*/
		
		// Setup GUI
		gui = new HudContainer("offscreen-gui", 1, 1);
		gui.setAbsolute();
		
		/*window = new HudWindow("gui-window", hud.loadTexture2D("Interface/border96.png"), 512, 256);
		window.createNinePartTexture(32);
		window.createExitButton(hud.loadTexture2D("Interface/exit_button.png"), 32, 32, 2, 2);
		window.setCentered();
		gui.add(window);*/

        // Attach the scene to the viewport to be rendered
       // view.attachScene(node);
	   view.addProcessor(this);
	   view.setEnabled(true);
	}
	public void update() {
		gui.doLayout(width, height);
	}
	public Texture2D getTexture() {
		return texture;
	}
	public void setEnabled(boolean enable) {
		view.setEnabled(enable);
	}
	// Called when the SP is removed from the RM.
	public void cleanup() {
		initialized = false;
	}
	// Called in the render thread to initialize the scene processor.
	public void initialize(RenderManager rm, ViewPort vp) {
		this.width = vp.getCamera().getWidth();
		this.height = vp.getCamera().getHeight();
		gui.setSize(width, height);
		gui.doLayout(width, height);
		initialized = true;
	}
	public boolean isInitialized() {
		return initialized;
	}
	// Called after a frame has been rendered and the queue flushed.
	public void postFrame(FrameBuffer out) {
		System.out.println("Post Frame: " + view.getName());
		view.setEnabled(false);
	}
	// Called after the scene graph has been queued, but before it is flushed.
	public void postQueue(RenderQueue rq) {
		hud.renderForView(view, gui);
	}
	// Called before a frame
	public void preFrame(float tpf) {
		gui.update(tpf);
	}
	// Called when the resolution of the viewport has been changed.
	public void reshape(ViewPort vp, int w, int h) {
		this.width = w;
		this.height = h;
		gui.setSize(width, height);
		gui.doLayout(width, height);
	}
}