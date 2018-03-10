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
import com.jme3.scene.Spatial;
import com.jme3.bullet.control.VehicleControl;

public class VehicleAction extends MoveAction
{
	private VehicleControl control;
	
	public VehicleAction() {
		super();
		this.speed = 0.10f;
	}
	public void setSpatial(Spatial spatial)
	{
		super.setSpatial(spatial);
		if (spatial != null)
		{
			control = spatial.getControl(VehicleControl.class);
		}		
	}
	protected void setMovement(int moving, int panning, float tpf)
	{
		direction.set(-panning, 0, -moving);
		applyRotation(direction.normalizeLocal());
		direction.multLocal(speed*tpf);
		if (control != null)
		{
			if (panning != 0)
			{
				control.applyForce(new Vector3f(0, 1, 0), new Vector3f(panning, 0, 0));
			}
			else
			{
				control.applyImpulse(direction, Vector3f.ZERO);
			}
		}
		else
		{
			spatial.move(direction);
		}
	}
	@Override
	public void update(float tpf)
	{
		if (control != null)
		{
			control.applyCentralForce(control.getGravity().negate());
			//System.out.println("Processing");
		}
		super.update(tpf);
	}
}