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
import com.jme3.app.SimpleApplication;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.HostedConnection;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.controls.*;
import com.stp.games.jme.controls.GameControl.ObjectType;
import com.stp.games.jme.network.ControlSerializer;
import com.stp.games.jme.network.PlayerMessage;

public abstract class BaseApp extends SimpleApplication {
	public static final String BLANK_IP = "0.0.0.0";
	public static final int QUERY = 0;
	public static final int LOGIN = 1;
	public static final int INVALID = 2;
	public static final int VALIDATED = 3;
	public static final int KEY_CHECK = 4;
	public static final int KEY_VALID = 5;
	public static final int KEY_INVALID = 6;
	public static final int PLAY = 7;

	// Network message to handle basic queries uses an int as a command switch and a String as a parameter
	@Serializable
	public static class NetworkQuery extends AbstractMessage {
		public int command = -1;
		public String param = "";
		public NetworkQuery() {}
		public NetworkQuery(int command, String param) {
			this.command = command;
			this.param = param;
		}
	}
	// Network message for transferring player data
	@Serializable
	public static class AddPlayer extends AbstractMessage {
		public int id;
		public SentientControl control;
		public AddPlayer() {}
		public AddPlayer(int id, SentientControl control) {
			this.id = id;
			this.control = control;
		}
		public int getId() {
			return id;
		}
		public SentientControl getControl() {
			return control;
		}
		public void setControl(SentientControl control) {
			this.control = control;
		}
	}
	// Network message for removing a player
	@Serializable
	public static class RemovePlayer extends AbstractMessage {
		public int id;
		public RemovePlayer() {}
		public RemovePlayer(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
		}
	}
	public static class NetworkAction {
		public HostedConnection connection;
		public Client client;
		public Message message;
		public NetworkCommand command;
		public NetworkAction(HostedConnection connection, Message message, NetworkCommand command) {
			this.connection = connection;
			this.message = message;
			this.command = command;
			this.client = null;
		}
		public NetworkAction(Client client, Message message, NetworkCommand command) {
			this.connection = null;
			this.message = message;
			this.command = command;
			this.client = client;
		}
		public HostedConnection getConnection() {
			return connection;
		}
		public Client getClient() {
			return client;
		}
		public Message getMessage() {
			return message;
		}
		public NetworkCommand getCommand() {
			return command;
		}
	}

	public enum NetworkCommand {
		AddPlayer,
		RemovePlayer,
		PlayerLoaded,
		RequestScene,
		ActivateScene,
		MotionUpdate;
	}

	protected ConcurrentLinkedQueue<NetworkAction> networkQueue;
	protected ScheduledThreadPoolExecutor executor;
	protected final GameRegistry registry;

	protected String appName;
	protected int port;
	protected int netLimit;
	
	public BaseApp(String appName)	{
		registry = GameRegistry.getInstance();
		networkQueue = new ConcurrentLinkedQueue<NetworkAction>();
		executor = new ScheduledThreadPoolExecutor(4);
		this.appName = appName;
		this.netLimit = 100;
	}
	
	public void simpleInitApp()	{
		rootNode.detachAllChildren();

		registry.setAssetManager(assetManager);
		registerObjectTypes(registry);
		registerResources();
	}
	
	public void registerObjectTypes(GameRegistry r) {
		r.registerObjectType(ItemControl.ITEM_TYPE);
		r.registerObjectType(ToolControl.TOOL_TYPE);
		r.registerObjectType(ClothingControl.CLOTHING_TYPE);
		r.registerObjectType(ContainerControl.CONTAINER_TYPE);
		r.registerObjectType(CreatureControl.CREATURE_TYPE);
		r.registerObjectType(SentientControl.SENTIENT_TYPE);
		r.registerObjectType(PlantControl.PLANT_TYPE);
		r.registerObjectType(StructureControl.STRUCTURE_TYPE);
		r.registerObjectType(VehicleControl.VEHICLE_TYPE);
		r.registerObjectType(ResourceControl.RESOURCE_OBJ_TYPE);
	}
	
	public void registerResources() {
		
	}
	
	public Client getClient() {
		return null;
	}
	
	@Override
    public void destroy()	{
		executor.shutdown();
        super.destroy();       
    }
	
	@Override
	public void simpleUpdate(float tpf)	{
		processNetworkQueue();
	}
	
	protected void processNetworkQueue() {
		int count = 0;
		if (!networkQueue.isEmpty()) {
			Iterator<NetworkAction> i = networkQueue.iterator();
			while (i.hasNext() && count < netLimit) {
				if (processNetworkAction(i.next())) {
					i.remove();
				}
				count++;
			}
		}
	}
	protected abstract boolean processNetworkAction(NetworkAction netAction);
}