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
package com.stp.games.jme.util;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.Animation;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.AnimControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.FastMath;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class BvhImporter {
	private static class Joint {
		int channels = 0;
		Quaternion[] rotations;
		Vector3f[] translations;
		float[] times;
		public Joint(int channels) {
			this.channels = channels;
		}
	}
	public static AnimControl importAnimation(String fileName, String animName, boolean yUp, float scale, Vector3f offset, AnimControl control) throws IOException {
		ArrayList<Bone> boneList = new ArrayList<Bone>();
		ArrayList<Joint> jointList = new ArrayList<Joint>();
		Bone rootBone = null;
		Bone activeBone = null;
		boolean isEnd =  false;
		boolean beginMotion = false;
		int frames = 0;
		int currentFrame = 0;
		float frameTime = 0;
		float currentTime = 0;
		Quaternion xRot = new Quaternion().fromAngles(0, 0, FastMath.PI/2);
		
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line = in.readLine();
		while (line != null) {
			String text = line.trim();
			if (text.toLowerCase().startsWith("root")) {
				rootBone = new Bone(text.substring(5).trim());
				boneList.add(rootBone);
				activeBone = rootBone;
			} else if (text.toLowerCase().startsWith("joint")) {
				Bone joint = new Bone(text.substring(5).trim());
				boneList.add(joint);
				if (activeBone != null) {
					activeBone.addChild(joint);
				}
				activeBone = joint;
			} else if (text.toLowerCase().startsWith("offset")) {
				String[] parts = text.split("\\s+");
				if (parts.length == 4) {
					activeBone.setUserControl(true);
					Vector3f temp = new Vector3f(Float.valueOf(parts[1]), Float.valueOf(parts[2]), Float.valueOf(parts[3]));
					if (yUp) {
						temp.set(Float.valueOf(parts[1]), Float.valueOf(parts[3]), Float.valueOf(parts[2]));
					}
					temp.multLocal(scale);
					temp.add(offset);
					activeBone.setUserTransforms(temp, Quaternion.IDENTITY, Vector3f.UNIT_XYZ);
					activeBone.setUserControl(false); 
				}
			} else if (text.toLowerCase().startsWith("channels")) {
				String[] parts = text.split("\\s+");
				if (parts.length > 2) {
					jointList.add(new Joint(Integer.valueOf(parts[1])));
				}
				//TODO
			} else if (text.toLowerCase().startsWith("end")) {
				isEnd = true;
			} else if (text.startsWith("}")) {
				if (!isEnd) {
					if (activeBone != null) {
						activeBone = activeBone.getParent();
					}
				}
				isEnd = false;
			} else if (text.toLowerCase().startsWith("motion")) {
				beginMotion = true;
			} else if (text.toLowerCase().startsWith("frames")) {
				String[] parts = text.split("\\s+");
				if (parts.length > 1) {
					frames = Integer.valueOf(parts[1]);
				}
				if (frames > 0) {
					for (Joint j : jointList) {
						j.times = new float[frames];
						j.rotations = new Quaternion[frames];
						j.translations = new Vector3f[frames];
					}
				}
			} else if (text.toLowerCase().startsWith("frame time")) {
				String[] parts = text.split("\\s+");
				if (parts.length > 2) {
					frameTime = Float.valueOf(parts[2]);
				}
			} else {
				if (beginMotion && frames > 0) {
					int channelIndex = 0;
					String[] parts = text.split("\\s+");
					for (Joint j : jointList) {
						if (j.channels > 3) {
							if (yUp) {
								j.translations[currentFrame] = new Vector3f(Float.valueOf(parts[channelIndex]),
																									Float.valueOf(parts[channelIndex+2]),
																									Float.valueOf(parts[channelIndex+1]));
							} else {
								j.translations[currentFrame] = new Vector3f(Float.valueOf(parts[channelIndex]),
																									Float.valueOf(parts[channelIndex+1]),
																									Float.valueOf(parts[channelIndex+2]));
							}
							j.translations[currentFrame].multLocal(scale);
							channelIndex+=3;
						} else {
							j.translations[currentFrame] = new Vector3f();
						}
						if (j.channels > 0) {
							if (yUp) {
								j.rotations[currentFrame] = new Quaternion().fromAngles(Float.valueOf(parts[channelIndex+2])*FastMath.DEG_TO_RAD,
																															Float.valueOf(parts[channelIndex+1])*FastMath.DEG_TO_RAD,
																															Float.valueOf(parts[channelIndex])*FastMath.DEG_TO_RAD);
							} else {
								j.rotations[currentFrame] = new Quaternion().fromAngles(Float.valueOf(parts[channelIndex])*FastMath.DEG_TO_RAD,
																															Float.valueOf(parts[channelIndex+1])*FastMath.DEG_TO_RAD,
																															Float.valueOf(parts[channelIndex+2])*FastMath.DEG_TO_RAD);
							}
							j.times[currentFrame] =  currentTime;
							channelIndex+=3;
						}
					}
					currentTime = currentTime + frameTime;
					currentFrame++;
				}
			}
			line = in.readLine();
		}
		if (control == null) {
			control = new AnimControl(new Skeleton(boneList.toArray(new Bone[boneList.size()])));
        }
		Skeleton skeleton = control.getSkeleton();
		System.out.println("Imported Bones: " + boneList.size() + " of " + jointList.size());
		for (int b = 0; b < skeleton.getBoneCount(); b++) {
			System.out.println("|" + skeleton.getBone(b).getName() + "|");
		}
		Animation animation = new Animation(animName, currentTime);
		for (int j = 0; j < jointList.size(); j++) {
			String boneName = boneList.get(j).getName().replace(':', '_');
			int boneIndex = skeleton.getBoneIndex(boneName);
			System.out.println("BoneTrack |" + boneName + "|" + boneIndex + "|" + jointList.get(j).rotations.length);
			animation.addTrack(new BoneTrack(boneIndex, jointList.get(j).times, jointList.get(j).translations, jointList.get(j).rotations));
		}
		
		control.addAnim(animation);
		return control;
	}
}