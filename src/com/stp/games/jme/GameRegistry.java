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
import com.jme3.asset.AssetManager;
// Java Dependencies
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.ArrayList;
// Internal Dependencies
import com.stp.util.JavaIO;
import com.stp.util.XMLFileUtility;
import com.stp.games.jme.controls.Schematic;
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.GameControl.ObjectType;
import com.stp.games.jme.forester.grass.GrassLayer;

public class GameRegistry {
	private final ArrayList<Schematic> schematics = new ArrayList<Schematic>();
	//private final ArrayList<Category> resources = new ArrayList<Category>();
	private final ArrayList<ObjectType> types = new ArrayList<ObjectType>();
	private final ArrayList<GrassLayer> grassLayers = new ArrayList<GrassLayer>();
	
	private static final String ZERO_FILL = "00000000";
	private static GameRegistry instance;
	private AssetManager assetManager;
	private String lastId = "0";
	
	// Disable external instantiation for singleton class
	private GameRegistry() {
	}
	// Entry point to retrieve the single instance of this object
	public static GameRegistry getInstance() {
		if (instance == null)	{
			instance = new GameRegistry();
		}
		return instance;
	}
	// Set the asset manager used to locate assets for loading
	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}
	
	// Register a new object type to be used in the game
	public ObjectType registerObjectType(ObjectType type) {
		int index = types.indexOf(type);
		if (index < 0) {
			types.add(type);
			return type;
		} else {
			return types.get(index);
		}
	}
	// Get the list of all objects to iterate through them
	public ArrayList<ObjectType> getObjectTypes() {
		return types;
	}
	
	// Find an object type by it's name
	public static ObjectType findObjectType(String text) {
		for (ObjectType t : getInstance().getObjectTypes()) {
			if (t.matches(text)) {
				return t;
			}
		}
		return GameControl.GENERIC_TYPE;
	}
	
	// Register a new grass layer to be used by the chunk paging system
	public GrassLayer registerGrassLayer(GrassLayer layer) {
		int index = grassLayers.indexOf(layer);
		if (index < 0) {
			grassLayers.add(layer);
			return layer;
		} else {
			return grassLayers.get(index);
		}
	}
	
	// Gets a grass layer it's id or return null if no matching grass layer was found
	public GrassLayer getGrassLayer(int layerId) {
		for (GrassLayer layer : grassLayers) {
			if (layer.getLayerId() == layerId) {
				return layer;
			}
		}
		return null;
	}
	// Get a list of all registered grass layers
	public ArrayList<GrassLayer> getGrassLayers() {
		return grassLayers;
	}
	
	// Find a grass layer by it's name
	public static GrassLayer findGrassLayer(String text) {
		for (GrassLayer layer : getInstance().getGrassLayers()) {
			if (layer.getName().equals(text)) {
				return layer;
			}
		}
		return null;
	}
	public <T extends GameControl> T createObject(String name, java.lang.Class<T> controlType) {
		GameControl control = createObject(name);
		if (control != null && controlType.isInstance(control)) {
			return controlType.cast(control);
		} else {
			return null;
		}
	}
	
	// Creates a new object from the Schematic that matches the given name
	public GameControl createObject(String name) {
		if (assetManager != null) {
			Schematic schema = get(name);
			if (schema != null) {
				return schema.createNew(assetManager);
			}
		}
		return null;
	}
	public GameControl createObject(long id) {
		if (assetManager != null) {
			Schematic schema = get(id);
			if (schema != null) {
				return schema.createNew(assetManager);
			}
		}
		return null;
	}
	public GameControl createObject(Schematic schema) {
		return createObject(schema.getId());
	}
	public GameControl initializeControl(GameControl control, long id) {
		if (assetManager != null && !control.hasSpatial()) {
			Schematic schema = get(id);
			if (schema != null) {
				schema.initControl(assetManager, control);
			}
		}
		return control;
	}
	public void add(Schematic schema) {
		if (schema.getId() != 0) {
			Schematic found = get(schema.getId());
			if (found != null) {
				return;
			}
		}
		try {
			schema.setId(getNextId());
			schematics.add(schema);
		} catch (Exception ex) {}
	}
	public void remove(int index)	{
		if (index > 0 && index < schematics.size()) {
			schematics.remove(index);
		}
	}
	public long getNextId() throws Exception {
		int lastIndex = Integer.parseInt(lastId, 36);
		String nextId = Integer.toString(lastIndex+1, 36);
		this.lastId = nextId;
		String uid = ZERO_FILL.substring(0, 8-nextId.length()) + nextId;
		return ByteBuffer.wrap(uid.getBytes("UTF8")).getLong();
	}
	public static String stringValue(long value) throws Exception {
		return new String(((ByteBuffer)ByteBuffer.wrap(new byte[8]).putLong(value).flip()).array(), "UTF8");
	}
	public static long decodeBase36(String value) {
		try {
			return ByteBuffer.wrap(value.getBytes("UTF8")).getLong();
		} catch (Exception ex) {
			return 0L;
		}
	}
	public ArrayList<Schematic> getLoadedSchematics()	{
		return schematics;
	}
	public ArrayList<Schematic> getSchematics(Schematic.Category category) {
		ArrayList<Schematic> results = new ArrayList<Schematic>();
		for (Schematic s : schematics) {
			if (s.getCategory() == category) {
				results.add(s);
			}
		}
		return results;
	}
	public Schematic[] get(String[] names)	{
		Schematic[] results = new Schematic[names.length];
		for (int r = 0; r < names.length; r++) {
			results[r] = get(names[r]);
		}
		return results;
	}
	public Schematic get(String name) {
		for (Schematic s : schematics) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}
	public Schematic[] get(Long[] idList) {
		Schematic[] results = new Schematic[idList.length];
		for (int r = 0; r < idList.length; r++) {
			results[r] = get(idList[r]);
		}
		return results;
	}
	public Schematic get(long id) {
		for (Schematic s : schematics) {
			if (s.getId() == id) {
				return s;
			}
		}
		return null;
	}
	public boolean saveXML(File sFile) {
		if (schematics.size() > 0) {
			return JavaIO.saveXML(sFile, schematics.toArray(new Schematic[schematics.size()]));
		} else {
			return false;
		}
	}
	public boolean loadXML(InputStream in) {
		try {
			Schematic[] inData = (Schematic[])XMLFileUtility.readXMLObjects(in);
			schematics.clear();
			for (Schematic s : inData) {
				schematics.add(s);
				//System.out.println("Loaded: " + s.getName());
			}
			// Read and store the id of the last Schematic loaded
			if (inData.length > 0) {
				this.lastId = stringValue(inData[inData.length-1].getId());
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}