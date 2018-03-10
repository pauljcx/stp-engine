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
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.math.Vector3f;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.stp.games.jme.controls.CreatureControl;

public class CharacterAction extends MoveAction implements AnimEventListener
{
	protected CreatureControl creature;
	private BetterCharacterControl control;
	private AnimControl animator;
	private AnimChannel channel;
	private boolean hasStrafe;
	
	public CharacterAction() {
		this.hasStrafe = false;
	}
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (spatial != null) {
			creature = spatial.getControl(CreatureControl.class);
			if (creature != null) {
				this.speed = creature.getWalkSpeed();
				this.control = creature.getCharacterControl();
				this.animator = creature.getAnimator();
				if (control != null) {
					System.out.println("BCC: " + control.isEnabled());
				}
			} if (animator != null) {
				hasStrafe = animator.getAnim("StrafeRight") != null;
				animator.addListener(this);
				channel = animator.createChannel();
				channel.setAnim("Idle", 0.50f);
				channel.setLoopMode(LoopMode.DontLoop);
				channel.setSpeed(1f);
			} else {
				System.out.println("Animator Null");
			}
		}
	}
	public void setCharacterControl(BetterCharacterControl control) {
		this.control = control;
	}
	@Override
	public boolean setCommandState(String command, boolean state) {
		if (command.equals("idle")) {
			channel.setAnim("Idle", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
			return false;
		}
		if (command.equals("chop")) {
			if (channel != null)  {
				if (state) {
					if (!channel.getAnimationName().equals("Chopping")) {
						channel.setAnim("Chopping", 0.5f);
						channel.setLoopMode(LoopMode.Loop);
					}
				} else {
					if (channel.getAnimationName().equals("Chopping")) {
						channel.setAnim("Idle", 0.50f);
						channel.setLoopMode(LoopMode.DontLoop);
						channel.setSpeed(1f);
					}
				}
			}
			return false;
		}
		return super.setCommandState(command, state);
	}
	@Override
	public void axisChanged(int axis) {
	}
	@Override
	public void initialize() {
		if (channel != null && !channel.getAnimationName().equals("Walking")) {
			channel.setAnim("Walking", 0.5f);
			channel.setLoopMode(LoopMode.Loop);
		}
	}
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Jumping")) {
			channel.setAnim("Idle", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
	}
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {}
	@Override
	public void jump() {
		if (control != null) {
			control.jump();
		}
		if (channel != null) {
			channel.setAnim("Jumping", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
		}
	}
	@Override
	public void updateSpatialDirection() {
		if (!Float.isNaN(targetCoords.x)) {
			creature.lookAt(targetCoords);
		}
	}
	@Override
	protected void setMovement(float tpf) {
		if (channel != null && Float.isNaN(targetCoords.x)) {
			if (lastAxis == X_AXIS && hasStrafe) {
				String strafe = (panning <	0) ? "StrafeRight" : "StrafeLeft";
				if (!channel.getAnimationName().equals(strafe)) {
					channel.setAnim(strafe, 0.5f);
					channel.setLoopMode(LoopMode.Loop);
					System.out.println("Strafe: " + strafe);
				}
			} else {
				if (!channel.getAnimationName().equals("Walking")) {
					channel.setAnim("Walking", 0.5f);
					channel.setLoopMode(LoopMode.Loop);
					System.out.println("Walking");
				}
			}
		}
		if (control != null) {
			updateDirection();
			direction.multLocal(speed);
			control.setWalkDirection(direction);
			return;
		}
		super.setMovement(tpf);
	}
	@Override
	public boolean stop() {
		if (channel != null) {
			channel.setAnim("Idle", 0.1f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
		if (control != null) {
			control.setWalkDirection(Vector3f.ZERO);
			control.setViewDirection(direction);
		}
		return super.stop();
	}
}