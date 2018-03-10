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
package com.stp.games.jme.actions;
// JME3 Dependencies
import com.jme3.math.Vector3f;
import com.jme3.cinematic.MotionPath;
// Java Dependencies
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Collection;
// Internal Dependencies
import com.stp.games.jme.terrain.World;
import com.stp.games.jme.terrain.Volume;
import com.stp.games.jme.terrain.ChunkControl;
import com.stp.games.jme.controls.Schematic;
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.Waypoint;
import com.stp.games.jme.controls.CreatureControl;
import com.stp.games.jme.controls.SentientControl;
import com.stp.games.jme.controls.ContainerControl;

/**
 * The TaskAI class contains useful algorithms for handling and creating AI game tasks
 *
 */
public class TaskAI {
	protected ScheduledThreadPoolExecutor executor;
	
	public TaskAI() {
		this.executor = new ScheduledThreadPoolExecutor(8);
	}
	// To be overriden in subclasses
	public void sentientLoaded(SentientControl sentient) {
	}
	// To be overriden in subclasses
	public void creatureLoaded(CreatureControl creature) {
	}
	// To be overriden in subclasses
	public int handleTask(ActionTask task, CreatureControl creature, GameAction.Param params, float tpf) {
		return 1;
	}
	public void destroy() {
		executor.shutdown();
	}
	public Future<ArrayList<ContainerControl>> getAvailableResources(final World world, final Vector3f target, final String buildName) {
		return executor.submit(new Callable<ArrayList<ContainerControl>>() {
			public ArrayList<ContainerControl> call() throws Exception {
				Schematic schema = world.getSchematic(buildName);
				if (schema != null) {
					ChunkControl chunk = world.getVolume().getChunk(target);
					if (chunk != null) {
						final ArrayList<ContainerControl> sources = new ArrayList<ContainerControl>();
						for (GameControl o : chunk.getObjects()) {
							if (o.isType(ContainerControl.CONTAINER_TYPE)) {
								for (Schematic.ResourceParam param : schema.getRequiredResources()) {
									if (((ContainerControl)o).hasResources(param)) {
										sources.add((ContainerControl)o);
										System.out.println("Sources0");
									}
								}
							}
						}
						System.out.println("Sources1");
						if (sources.size() == schema.getRequiredResources().size()) {
							return sources;
						}
					}
				}
				return null;
			}
		});
	}
	/* This is an implementation of the A* algorithm used to determine the shortest path from point A to B within
	 * a given volume avoiding any obsticals. The algorithm is run on a seperate thread and the result can be obtained
	 * through the returned Future object.
	 */
	public Future<MotionPath> getPath(final Volume volume, final Vector3f source, final Vector3f target) {
		return executor.submit(new Callable<MotionPath>() {
			public MotionPath call() throws Exception {
				long startTime = System.currentTimeMillis();
				MotionPath path = new MotionPath();
				ArrayList<PathNode> openList= new ArrayList<PathNode>();
				ArrayList<PathNode> closedList = new ArrayList<PathNode>();
				PathNode startNode = new PathNode(source);
				PathNode endNode = new PathNode(target);
				if (!volume.isPassable(endNode.getLocation())) {
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							if (x == 0 && y == 0) {
								continue;
							}
							PathNode neighbor = new PathNode((int)endNode.getX() + x, (int)endNode.getY() + y);
							if (volume.isPassable(neighbor.getLocation())) {
								neighbor.set(PathNode.getDistance(startNode, neighbor), PathNode.getDistance(neighbor, endNode));
								neighbor.setParent(endNode);
								closedList.add(neighbor);
							}
						}
					}
					endNode = closedList.get(0);
					for (PathNode node : closedList) {
						if (node.getG() < endNode.getG()) {
							endNode = node;
						}
					}
					closedList.clear();
				}
				startNode.set(0, PathNode.getDistance(startNode, endNode));		
				endNode.set(PathNode.getDistance(startNode, endNode), 0);
				openList.add(startNode);
				//System.out.println("Start Node: " + startNode);
				//System.out.println("End Node: " + endNode);
				
				while (openList.size() > 0) {
					PathNode current = openList.get(0);
					for (PathNode node : openList) {
						if (node.getF() < current.getF() || (node.getF() == current.getF() && node.getH() < current.getH())) {
							current = node;
						}
					}
					//System.out.println("Current Node: " + current + " | " + openList.size());
					openList.remove(current);
					closedList.add(current);
					if (current.equals(endNode)) {
						while (!current.equals(startNode)) {
							//System.out.println("Path Node: " + current);
							path.addWayPoint(current.getLocation());
							current = current.getParent();
						}
						double calcTime = (System.currentTimeMillis() - startTime)/1000.0;
						//System.out.println("Path Length: " + path.getNbWayPoints() + " | " + calcTime + "s");
						return path;
					}
					// Add Neighbors
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							if (x == 0 && y == 0) {
								continue;
							}
							PathNode neighbor = new PathNode((int)current.getX() + x, (int)current.getY() + y);
							// Check if the neighbor has already been evaluated and if it is passable
							if (!closedList.contains(neighbor) && volume.isPassable(neighbor.getLocation())) {
								neighbor.set(current.getG() + PathNode.getDistance(current, neighbor), PathNode.getDistance(neighbor, endNode));
								neighbor.setParent(current);
								int index = openList.indexOf(neighbor);
								if (index < 0) {
									openList.add(neighbor);
									//System.out.println("Neighbor Node: " + neighbor);
								} else if (neighbor.getG() < openList.get(index).getG()) {
									openList.get(index).setG(neighbor.getG());
								}
							}
						}
					}
				}
				return path;
			}
		});
	}
}