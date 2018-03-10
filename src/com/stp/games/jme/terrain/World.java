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
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.binary.ByteUtils;
import com.jme3.light.PointLight;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.FastMath;  
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.util.SkyFactory;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.water.WaterFilter;
import com.jme3.post.filters.BloomFilter;
// Java Dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;

import com.stp.games.jme.forester.paging.interfaces.TileLoader;
import com.stp.games.jme.forester.grass.GrassLoader;
import com.stp.games.jme.forester.grass.GrassLayer;
import com.stp.games.jme.forester.grass.algorithms.WorldGrassAlgorithm;
import com.stp.games.jme.forester.grass.datagrids.MapGrid;
import com.stp.games.jme.forester.image.FormatReader.Channel;
import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.controls.*;
import com.stp.games.jme.actions.*;
import com.stp.games.jme.util.TimeOfDay;
import com.stp.games.jme.GameRegistry;

public class World extends AbstractAppState {
	private static final long HOUR = 3600000L;
	private static final long MINUTE = 600000L;
	private static AtomicLong counter = new AtomicLong(0);
	
	protected static long time = 28800000L;
	protected final ArrayList<ChunkListener> chunkListeners = new ArrayList<ChunkListener>();
	protected final ConcurrentLinkedQueue<CreatureControl> creatureQueue =  new ConcurrentLinkedQueue<CreatureControl>();
	protected final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
	protected final HashMap<String, Task> tasks = new HashMap<String, Task>();
	protected final ArrayList<TileLoader> tileLoaders = new ArrayList<TileLoader>();	
	protected final Node worldNode = new Node("world");
	protected final Node playerNode = new Node();
	protected final Node creatureNode = new Node();
	protected final GameRegistry registry = GameRegistry.getInstance();
	
	protected SimpleApplication app;
	protected AssetManager assetManager;
	protected Volume volume;
	protected PhysicsSpace physicsWorld;
	protected Material material;
	protected Player player;
	protected Controller viewControl;
	protected TaskAI ai;
	protected boolean registered;
	protected File saveFile;
	
	protected Future loadResult;
	protected Vector3f dimensions;
	protected Vector3f radius;
	protected Vector3f currentChunk;
	
	protected HudManager hud;

	protected Vector3f sunDirection;
	protected ColorRGBA sunColor;
	protected DirectionalLight sun;
	protected AmbientLight ambient;
	//protected SkyControl skyControl;
	protected TimeOfDay timeOfDay;
	protected double msph;
	protected float dayTime;
	protected float colorFactor;
	
	public World(AssetManager assetManager, HudManager hud, File saveFile, int dimX, int dimY, int dimZ) {
		this.saveFile = saveFile;
		this.hud = hud;
		this.registered = false;
		this.dimensions = new Vector3f(dimX, dimY, dimZ);
		this.radius = new Vector3f((int)(dimensions.x/2), (int)(dimensions.y/2), (int)(dimensions.z/2));
		this.currentChunk = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
		playerNode.setShadowMode(ShadowMode.Cast);
		creatureNode.setShadowMode(ShadowMode.Cast);
		this.assetManager = assetManager;
		//registry.setSaveFile(new File("schemas.zip"));
		registry.setAssetManager(assetManager);
		
		// Ambient light setup
		ambient = new AmbientLight();
		ambient.setColor(new ColorRGBA(0.2f, 0.2f, 0.25f, 1f));
		
		// Setup sun light
		sunDirection = new Vector3f(-0.7f,-0.5f,0.9f).normalizeLocal();
		sunColor = new ColorRGBA(1f, 1f, 1f, 1f);
		sun = new DirectionalLight();
		sun.setColor(sunColor);
		sun.setDirection(sunDirection);
		
		// Setup sky
		//Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/FullskiesSunset0068.dds", false);
        //sky.setLocalScale(300);
		
		msph = 1.0;
		
		worldNode.attachChild(playerNode);
		worldNode.attachChild(creatureNode);
		worldNode.addLight(ambient);
		worldNode.addLight(sun);
		//worldNode.attachChild(sky);
	}
	public Node getNode() {
		return worldNode;
	}
	public DirectionalLight getSun() {
		return sun;
	}
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app); 
		this.app = (SimpleApplication)app;
		if (!isRegistered()) {
			registerAssets(app.getAssetManager());
			viewControl = new Controller(app.getCamera(), new Vector3f(0, 2f, 0), this);
			viewControl.setRotationSpeed(0.75f);
			viewControl.registerWithInput(app.getInputManager());
			viewControl.setDragToRotate(false);
			viewControl.vRotateCamera(0.5f);
			//viewControl.setDragToRotate(true);
			if (player != null) {
				//viewControl.setMovable(player.getSelectedUnit());
			}
			//hud.setViewControl(viewControl);
			
			/*this.skyControl = new SkyControl(assetManager, app.getCamera(), 0.9f, false, true);
			skyControl.getSunAndStars().setHour(12f);
			skyControl.getSunAndStars().setSolarLongitude(Calendar.FEBRUARY, 10);
			//sc.getSunAndStars().setObserverLatitude(37.4046f * FastMath.DEG_TO_RAD);
		
			skyControl.getSunAndStars().setObserverLatitude(-0.5f);
			//sc.setCloudiness(1f);
			//sc.setCloudYOffset(0.4f);
			worldNode.addControl(skyControl);
			skyControl.setEnabled(true);*/
		}
		// Attach world to the scene graph
		this.app.getRootNode().attachChild(worldNode);
		
		/*Updater updater = skyControl.getUpdater();
		updater.addViewPort(app.getViewPort());
		updater.setAmbientLight(ambient);
		updater.setMainLight(sun);
        updater.setBloomEnabled(true);
        updater.setShadowFiltersEnabled(true);*/
		
		// Detect if the bullet physics system is available
		BulletAppState bullet = app.getStateManager().getState(BulletAppState.class);
		if (bullet != null) {
			physicsWorld = bullet.getPhysicsSpace();
		}

		/* Drop shadows */
        final int SHADOWMAP_SIZE=4096;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
		dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsr.setLight(sun);
		//updater.addShadowRenderer(dlsr);
        app.getViewPort().addProcessor(dlsr);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
		dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsf.setLight(sun);
        dlsf.setEnabled(true);
		//updater.addShadowFilter(dlsf);

		WaterFilter water = new WaterFilter(worldNode, sun.getDirection());
		water.setWaterHeight(0);
		water.setUseFoam(true);
		water.setSpeed(0.35f); // 1f default alt 0.2f
		water.setShininess(0.3f); //0.7f default
		water.setUseCaustics(true);
		water.setUseRipples(true); // default true
		water.setWaterTransparency(0.1f); //0.1f default alt 0.6f
		water.setRefractionConstant(0.1f);
		water.setRefractionStrength(0); // 0 default alt 0.3f
		water.setWaveScale(0.001f); //0.005f default alt 0.1f
		water.setMaxAmplitude(1.4f); // 1f default alt 0.5f
		water.setFoamExistence(new Vector3f(0.1f,1f,1.0f));
		water.setShoreHardness(1f); // default 0.1f
		
		BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setBlurScale(2.5f);
        bloom.setExposurePower(1f);
        //updater.addBloomFilter(bloom);
		
		FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fog.setFogDistance(192f);
        fog.setFogDensity(1.3f);
		
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
		fpp.addFilter(water);
		fpp.addFilter(bloom);
		//fpp.addFilter(fog);
        app.getViewPort().addProcessor(fpp);
		
		timeOfDay = new TimeOfDay(14f);
        app.getStateManager().attach(timeOfDay);
        timeOfDay.setRate(100f);
	} 
	@Override
	public void cleanup() {
		super.cleanup();
		if (isSavable()) {
			//volume.saveVolume();
			//player.save();
		}
		volume.clearVolume();
		this.app.getRootNode().detachChild(worldNode);
	}
	// Note that update is only called while the state is both attached and enabled
	@Override
	public void update(float tpf) {
		float hour = timeOfDay.getHour();
		//skyControl.getSunAndStars().setHour(hour);
		float lightRatio = hour < 12 ? hour : 24f - hour;
		lightRatio = Math.min(Math.max(lightRatio, 3f), 8f)/8f; // 0.375 to 1.0
		lightRatio = lightRatio*lightRatio; // 0.1406f to 1f
		//skyControl.getUpdater().setAmbientMultiplier(lightRatio*2f);
		//skyControl.getUpdater().setMainMultiplier(lightRatio*2f);
       
		// Update the game time
		//time = time + (long)(tpf*msph*HOUR);
		// Calculate the current hour in game time
		/*long hr = (time/HOUR)%24;
		long min = (time/MINUTE)%1440;
		float seconds = time / 1000.0f;
		dayTime = min/1440.0f;
		
		// Updates the sun direction based on the current time of day
		sunDirection.set((float)Math.sin(dayTime*3.14*2), -(float)Math.cos(dayTime*3.14*2), 0);
		sunDirection.normalizeLocal();
		sun.setDirection(sunDirection);
		
		// Sets the daylight color
		colorFactor = 1;*/
		
		/*if (hr < 6 || hr > 20) {
			colorFactor = 0;
		} else if (hr >= 6 && hr < 8) {
			colorFactor = (dayTime-0.25f)*10;
		} else if (hr > 18 && hr <= 20) {
			colorFactor = 1.0f-((dayTime-0.75f)*10);
		}*/
		/*if (dayTime > 0.30f && dayTime < 0.70f) { colorFactor = 0f; }
		if (dayTime > 0.20f && dayTime <= 0.30f) { colorFactor = 1.0f-((dayTime-0.20f)*10); }
		if (dayTime >= 0.70f && dayTime < 0.80f) { colorFactor = (dayTime-0.70f)*10; }
		sunColor.set(colorFactor, colorFactor, colorFactor, 1f);
		sun.setColor(sunColor);*/

		// Wait for all chunks to finish loading before doing additional updates
		if (loadResult != null) {
			if (loadResult.isDone()) {
				loadResult = null;
				loadComplete();
			}
			return;
		}
		// Deactivate all chunks if there are no players
		if (player == null) {
			// Mark all chunks inactive and remove them
			volume.markInactive();
			volume.moveInactive();
			return;
		}
		// Update any chunks that need it on a separate execution thread
		updateChunks();
		
		// Optimization for client, with only one player no need to update unless their location changed
		if (true) {//recalculateLocation(player.getSelectedUnit())) {
			// Mark all chunks inactive, any that are still inactive after the update will be removed
			volume.markInactive();
		
			// Activate all chunks within the radius of each player
			int loadCount = 0;//activateChunks(player.getSelectedUnit());

			// Move all chunks still marked as inactive to the inactive list
			volume.moveInactive();
			// Load any chunks that need it on a separate execution thread, return to wait for chunks to finish loadiing
			if (loadCount > 0) {
				System.out.println("Load Started: " + loadCount);
				loadResult = executor.submit(loadChunks);
				return;
			}
		}
		if (!creatureQueue.isEmpty()) {
			Iterator<CreatureControl> i = creatureQueue.iterator();
			while (i.hasNext()) {
				CreatureControl creature = i.next();
				if (creature.isActive()) {
					creatureNode.detachChild(creature.getSpatial());
					removePhysicsObject(creature.getPhysicsControl()); 
				} else {
					creatureNode.attachChild(creature.getSpatial());
					creature.setActive(true);
					addPhysicsObject(creature.getPhysicsControl()); 
				}
				i.remove();
			}
		}
		// Update Forester Tile Paging
		/*for(TileLoader loader: tileLoaders){
            loader.update(tpf);
        }*/
	}
	public void loadComplete() {
	}
	public void registerAssets(AssetManager assetManager) {
		this.ai = new TaskAI();
		this.volume = new Volume();
		worldNode.attachChild(volume.getNode());
		this.registered = true;
	}
	// Checks to see if the world assets have already been registered
	public boolean isRegistered() {
		return registered;
	}
	// Gets the volume object for this world
	public Volume getVolume() {
		return volume;
	}
	// Gets the root node of the application
	public Node getRootNode() {
		return app.getRootNode();
	}
	public HudManager getHudManager() {
		return hud;
	}
	public AssetManager getAssetManager() {
		return assetManager;
	}
	public TaskAI getTaskAI() {
		return ai;
	}
	// Adds a player to the world so that chunks can be loaded according to their location
	public void setPlayer(Player player) {
		this.player = player;
		/*for (CreatureControl c : player.getUnits()) {
			playerNode.attachChild(c.getSpatial());
			//ai.sentientLoaded(c);
		}*/
		if (viewControl != null) {
			//viewControl.setMovable(player.getSelectedUnit());
		}
	}
	public void addCreature(CreatureControl creature) {
		if (inRenderArea(creature) && creature.hasSpatial()) {
			creatureQueue.offer(creature);
		}
		ai.creatureLoaded(creature);
	}
	public void setSelectedUnit(CreatureControl creature) {
		if (viewControl != null) {
			//viewControl.setMovable(player.setSelectedUnit(creature));
		}
	}
	public CreatureControl getSelectedUnit() {
		return null; //player.getSelectedUnit();
	}
	public Player getPlayer() {
		return player;
	}
	protected void addMoveControl(CreatureControl creature) {
		//creature.setMoveControl(new CharacterAction(this));
	}
	// Checks whether the player has crossed a chunk boundary and updates their location accordingly
	protected boolean recalculateLocation(CreatureControl creature) {
		// Calculate local group location by dividing the local coordinates by the group size
		Vector3f coords = creature.getWorldTranslation();
		int nx = Math.round(coords.x/volume.getChunkSizeX());
		int ny = Math.round(coords.y/volume.getChunkSizeY());
		int nz = Math.round(coords.z/volume.getChunkSizeZ());
		return creature.setChunkLocation(nx, ny, nz);
	}
	// Check if a given object is in the rendering area
	public boolean inRenderArea(GameControl object) {
		String chunkName = object.getChunkName();
		// If the object is not in a chunk then do a boundary test
		if (chunkName.length() == 0) {
			Vector3f test = object.getWorldTranslation();
			Vector3f center = viewControl.getLocation();
			float w = dimensions.x*volume.getChunkSizeX();
			float h = dimensions.z*volume.getChunkSizeZ();
			float x = center.x - (w/2);
			float z = center.z - (h/2);
			// Test lower boundary
			if (test.x < x || test.z < z) {
				return false;
			}
			w += x;
			h += z;
			// Test upper boundary
			return ((w < x || w > test.x) && (h < z || h > test.z));
		}
		// If the object is in a chunk determine if the chunk is loaded and currently active
		ChunkControl chunk = volume.getChunkByName(chunkName);
		if (chunk != null) {
			return chunk.isActive();
		}
		return false;
	}
	// Generates new tile data for the chunk
	// TODO: implement terrain generation
	public void generateChunk(ChunkControl chunk) {
		chunk.setLoaded(true);
	}
	public Schematic getSchematic(String name) {
		return registry.get(name);
	}
	// Creates a new object from the schematic that matches the specified name
	public GameControl createControl(String name) {
		/*Schematic schema = registry.get(name);
		if (schema != null) {
			return schema.createControl();
		}
		return null;*/
		return registry.createObject(name);
	}
	// Creates a new object from the schematic that matches the specified name
	public GameControl createObject(String name) {
		return registry.createObject(name);
	}
	// Creates a new object from the schematic that matches the specified name
	public ItemControl createItem(String name) {
		GameControl control = registry.createObject(name);
		if (control instanceof ItemControl) {
			control.setUniqueId(counter.getAndIncrement());
			return (ItemControl)control;
		}
		return ItemControl.NONE;
	}
	public CreatureControl createCreature(String name, float x, float y, float z) {
		GameControl control = registry.createObject(name);
		if (control instanceof CreatureControl) {
			CreatureControl creature = (CreatureControl)control;
			creature.setUniqueId(counter.getAndIncrement());
			creature.setActionControl(new GameAction(creature, this, ai));
			addMoveControl(creature);
			//creature.setEnablePhysics(false);
			creature.setStoredLocation(x, y, z);
			//System.out.println("Created Creature: " + creature + " | " + creature .getTranslation());
			return creature;
		}
		return null;
	}
	public SentientControl createCharacter(String name, String firstName, String lastName, float x, float y, float z) {
		GameControl control = registry.createObject(name);
		if (control instanceof SentientControl) {
			SentientControl sentient = (SentientControl)control;
			sentient.setUniqueId(counter.getAndIncrement());	
			sentient.setActionControl(new GameAction(sentient, this, ai));
			//addMoveControl(sentient);
			//sentient.setMoveControl(new CharacterAction(this));
			//sentient.setEnablePhysics(false);
			sentient.setStoredLocation(x, y, z);
			sentient.setFirstName(firstName);
			sentient.setLastName(lastName);
			addPhysicsObject(sentient.getPhysicsControl()); 
			return sentient;
		}
		return null;
	}
	// Searches for objects within a given radius of the player that allow the specified action
	public GameControl getActionableObject(int radius, String action) {
		return null;//getActionableObject(player.getSelectedUnit().getWorldTranslation(), radius, action);
	}
	// Searches for objects within a given radius that allow the specified action
	public GameControl getActionableObject(Vector3f location, int radius, String action) {
		for (int x = -radius; x < radius; x++) {
			for (int y = -radius; y < radius; y++){
				for (int z = -radius; z < radius; z++) {
					GameControl result = volume.getObject(location.x + x, location.y + y, location.z + z);
					if (result != null && result.matchAction(action)) {
						return result;
					}
				}
			}
		}
		return null;
	}
	public boolean receiveInput(String name, boolean state, float tpf) {
		return viewControl.receiveInput(name, state, tpf);
	}
	// Activates all chunks within a given radius of the player
	protected int activateChunks(CreatureControl creature)	{
		int unloaded = 0;
		Vector3f temp = new Vector3f();
		for (int x = 0; x < dimensions.getX(); x++) {
			for (int y = 0; y < dimensions.getY(); y++) {
				for (int z = 0; z < dimensions.getZ(); z++) {
					temp.set(creature.getChunkX() - radius.getX() + x, creature.getChunkY() - radius.getY() + y, creature.getChunkZ() - radius.getZ() + z);
					// Count the number of chunks that need to be loaded
					ChunkControl chunk = volume.activateChunk(temp);
					if (!chunk.isLoaded()) {
						unloaded++;
					}
				}
			}
		}
		return unloaded;
	}
	public void requestObjectUpdate(String chunkName) {
		ChunkControl chunk = volume.getChunkByName(chunkName);
		if (chunk != null) {
			chunk.setObjectUpdateNeeded(true);
		}
	}
	public void requestObjectUpdate(GameControl object) {
		ChunkControl chunk = volume.getChunkByName(object.getChunkName());
		if (chunk != null) {
			chunk.setObjectUpdateNeeded(true);
		}
	}
	public ChunkControl getChunk(String name) {
		return volume.getChunkByName(name);
	}
	public float getHeight(float gx, float gz) {
		return volume.getHeight(gx, 0, gz);
	}
	public float getHeight(Vector3f globalLocation) {
		return volume.getHeight(globalLocation);
	}
	public ArrayList<ChunkControl> getActiveChunks() {
		ArrayList<ChunkControl> chunks = volume.getActiveChunks(false);
		if (player != null) {
			for (ChunkControl chunk : chunks) {
				//chunk.setDistance(player.getSelectedUnit().getChunkLocation());
				chunk.setDistance(app.getCamera().getLocation());
			}
			Collections.sort(chunks);
		}
		return chunks;
	}
	public boolean isSavable()	{
		return saveFile != null;
	}
	public File getSaveFile()	{
		return saveFile;
	}
	public void saveChunks(ArrayList<ChunkControl> chunks)	{
		if (saveFile == null || chunks.size() == 0) {
			return;
		}
		ZipInputStream zin = null;
		ZipOutputStream out = null;
		try {
			// get a temp file
			File tempFile = File.createTempFile(saveFile.getName(), null);
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();
			
			boolean saveExists = saveFile.exists();
			if (saveExists) {
				boolean renameOk = saveFile.renameTo(tempFile);
				if (!renameOk) {
					throw new RuntimeException("could not rename the file " + saveFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
				}
			}

			FileOutputStream dest = new FileOutputStream(saveFile);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			out.putNextEntry(new ZipEntry("LastId"));
			out.write(ByteUtils.convertToBytes(counter.longValue()));

			for (ChunkControl chunk : chunks) {
				ZipEntry entry = new ZipEntry(chunk.getName());
				out.putNextEntry(entry);
				chunk.write(out);
			}
			
			if (saveExists) {
				byte[] buf = new byte[4096 * 1024];
				zin = new ZipInputStream(new FileInputStream(tempFile));
				ZipEntry entry = zin.getNextEntry();
				while (entry != null) {
					try {
						// Add ZIP entry to output stream.
						out.putNextEntry(new ZipEntry(entry.getName()));
						// Transfer bytes from the ZIP file to the output file
						int len;
						while ((len = zin.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
					} catch (Exception ex) {}
					entry = zin.getNextEntry();
				}
				zin.close();
				tempFile.delete();
			}
			out.closeEntry();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				out.close();
				zin.close();
			} catch (Exception ex) {}
		}
	}
	public void load() {
		if (saveFile == null || !saveFile.exists()) {
			return;
		}
		ZipFile zip = null;
		try {
			zip = new ZipFile(saveFile);
			ZipEntry entry = zip.getEntry("LastId");
			if (entry != null) {
				counter.set(ByteUtils.readLong(zip.getInputStream(entry)));
			}
			zip.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try { zip.close(); }
			catch (Exception ex) {}
		}
	}
	public boolean loadChunk(ChunkControl chunk) {
		if (saveFile == null || !saveFile.exists()) {
			return false;
		}
		ZipFile zip = null;
		try {
			zip = new ZipFile(saveFile);
			ZipEntry entry = zip.getEntry(chunk.getName());
			if (entry != null) {
				chunk.read(zip.getInputStream(entry));
				System.out.println("Chunk Loaded: " + chunk);
				chunk.setLoaded(true);
			}
			zip.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try { zip.close(); }
			catch (Exception ex) {}
		}
		return chunk.isLoaded();
	}
	// Task to load chunks on a separate thread and returns the number loaded
	private Callable<Integer> loadChunks = new Callable<Integer>() {
		public Integer call() throws Exception {
			int count = 0;
			long startTime = System.currentTimeMillis();
			// Check all active chunks to see if any need loading
			for (ChunkControl chunk : volume.getActiveChunks(false)) {
				if (!chunk.isLoaded()) {
					// Loads the chunk from file or generates a new one if a file does exist
					if (!volume.loadChunk(chunk)) {
						try {
							generateChunk(chunk);
							volume.addLoadedChunk(chunk);
						} catch (Exception ex) { ex.printStackTrace(); }
					}
					volume.beforeActivate(chunk);
					fireChunkLoaded(chunk);
					count++;
				}
			}
			double buildTime = (System.currentTimeMillis() - startTime)/1000.0;
			System.out.println("Volume Load Complete: " + count + " chunks " + buildTime + "s");
			return count;
		}
	};
	private int updateChunks() {
		int mCount = 0;
		int oCount = 0;
		// Check all active chunks to see if any need updating
		for (final ChunkControl chunk : getActiveChunks()) {
			chunk.updateView(app.getCamera().getLocation());
			if (chunk.isMeshUpdateNeeded() && !chunk.isRestricted()) {
				chunk.setMeshUpdateNeeded(false);
				// Submit update tasks to the executor pool to be updated on separate threads
				executor.execute(new Runnable() {
					public void run() {
						// Rebuild the chunks mesh
						final Mesh mesh = volume.buildMesh(chunk);
						//System.out.println("Chunk updated: " + chunk);
						// Update the geometry back on the main thread
						app.enqueue(new Callable<ChunkControl>() {
							public ChunkControl call() {
								chunk.updateMesh(mesh, material);
								// Update and/or construct the underlying physics object if enabled
								if (isPhysicsEnabled()) {
									volume.initializePhysics(chunk);
								}
								return chunk;
							}
						});
					}
				});
				mCount++;
			}
			if (chunk.isObjectUpdateNeeded()) {
				chunk.setObjectUpdateNeeded(false);
				// Submit update tasks to the executor pool to be updated on separate threads
				executor.execute(new Runnable() {
					public void run() {
						// Initialize objects
						/*for (GameControl object : chunk.getObjects()) {
							if (!object.hasSpatial()) {
								object.initialize(app.getAssetManager());
							}
						}*/
						//System.out.println("Objects updated: " + chunk);
						// Update the geometry back on the main thread
						app.enqueue(new Callable<ChunkControl>() {
							public ChunkControl call() {
								//chunk.updateObjects();
								return chunk;
							}
						});
					}
				});
				oCount++;
			}
		}
		if (oCount > 0) {
			System.out.println(oCount + " chunk object updates - " + executor.getTaskCount());
		}
		if (mCount > 0) {
			System.out.println(mCount + " chunks submitted for update - " + executor.getTaskCount());
		}
		return mCount;
	}
	public void addItem(ItemControl item) {
		worldNode.attachChild(item.getSpatial());
		addPhysicsObject(item.getPhysicsControl());
	}
	public void removeItem(ItemControl item) {
		worldNode.detachChild(item.getSpatial());
		removePhysicsObject(item.getPhysicsControl());
	}
	public void addPhysicsObject(PhysicsControl obj) {
		if (isPhysicsEnabled()) {
			if (obj != null) {
				physicsWorld.add(obj);
			}
		}
	}
	public void removePhysicsObject(PhysicsControl obj) {
		if (isPhysicsEnabled()) {
			if (obj != null) {
				physicsWorld.remove(obj);
			}
		}
	}
	public void destroy() {
		executor.shutdown();
		ai.destroy();
	}
	// Add the specified listener to start receiving notifications
	public void addChunkListener(ChunkListener listener) {
        chunkListeners.add(listener);
    }
	// Removes the specified chunk listener from receiving notifications
    public void removeChunkListener(ChunkListener listener) {
        chunkListeners.remove(listener);
    }
	public void fireChunkActivated(ChunkControl chunk) {
		for (ChunkListener listener : chunkListeners) {
			listener.onChunkActivated(chunk);
		}
	}
	public void fireChunkDeactivated(ChunkControl chunk) {
		for (ChunkListener listener : chunkListeners) {
			listener.onChunkDeactivated(chunk);
		}
	}
	public void fireChunkLoaded(ChunkControl chunk) {
		for (ChunkListener listener : chunkListeners) {
			listener.onChunkLoaded(chunk);
		}
	}
	public void setViewControlTarget(GameControl object, float threshold) {
		viewControl.setTargetCoords(getWorldCoordinates(object), threshold);
	}
	public Controller getViewControl() {
		return viewControl;
	}
	public Vector3f getWorldCoordinates(GameControl object) {
		/*ChunkControl chunk = volume.getChunkByName(object.getChunkName());
		if (chunk != null) {
			return chunk.getStartLocation().add(object.getTranslation());
		}*/
		return object.getWorldTranslation();
	}
	public GameControl getTargetControl(Vector2f click2d) {
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector3f click3d = app.getCamera().getWorldCoordinates(click2d, 0f);
		Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all nodes in results list.
		worldNode.collideWith(ray, results);
		if (results.size() > 0) {
			GameControl control = null;
			// Iterate over all collisions to find the first GameControl target
			for (Iterator<CollisionResult> collisions = results.iterator(); collisions.hasNext();) {
				Spatial object = collisions.next().getGeometry();
				control = object.getControl(GameControl.class);
				while (object.getParent() != null && control == null) {
					object = object.getParent();
					control = object.getControl(GameControl.class);
				}
				if (control != null) {
					return control;
				}
			}
		}
		return null;
	}
	public CollisionResult getCollisionResult(Vector2f click2d) {	
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector3f click3d = app.getCamera().getWorldCoordinates(click2d, 0f);
		Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all nodes in results list.
		worldNode.collideWith(ray, results);
		if (results.size() > 0) {
			return results.getClosestCollision();
		}
		return null;
	}
	/**
	 * Returns the CollisionResult containing a list of all chunks at where a user clicked on the screen
	 *
	 * @param screenCoords a 2d vector containing click location in screen coordinates
	 * @return a CollisionResult object containing a list of any chunks at the clicked location
	 */
	public CollisionResult getChunkCollision(Vector2f screenCoords) {	
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector3f click3d = app.getCamera().getWorldCoordinates(screenCoords, 0f);
		Vector3f dir = app.getCamera().getWorldCoordinates(screenCoords, 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all active chunks.
		for (ChunkControl chunk : getActiveChunks()) {
			int count = chunk.collideWith(ray, results);
			if (count > 0) {
				return results.getClosestCollision();
			}
		}
		return null;
	}
	public void registerTask(String name, Task task) {
		tasks.put(name, task);
	}
	public Task getTask(String actionName) {
		return tasks.get(actionName);
	}
	public Collection<Task> getTaskList() {
		return tasks.values();
	}
	public boolean isPhysicsEnabled() {
		return physicsWorld != null;
	}
}