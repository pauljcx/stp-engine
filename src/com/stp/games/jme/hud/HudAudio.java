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
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Filter;
import com.jme3.math.Vector3f;

/** @author Paul Collins
 *  @version v1.0 ~ 11/08/2017
 *  HISTORY: Version 1.0 Created the HudAudio object to store audio information for playing sound effects for the GUI ~ 11/08/2017
 */
public class HudAudio implements AudioSource {
	protected String path = "";
	protected AudioData data;
	protected float volume = 1;
    protected float pitch = 1;
    protected float timeOffset = 0;
	protected volatile AudioSource.Status status = AudioSource.Status.Stopped;
    protected volatile int channel = -1;
	
	public HudAudio(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setAudioData(AudioData data) {
		this.data = data;
	}
	public AudioData getAudioData() {
		return data;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HudAudio) {
			return ((HudAudio)obj).getPath().equals(path);
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return path;
	}
	public int getChannel() {
		return channel;
	}
	public Vector3f getDirection() {
		return Vector3f.UNIT_Y;
	}
	public Filter getDryFilter() {
		return null;
	}
	public float getInnerAngle() {
		return 360f;
	}
	public float getMaxDistance() {
		return 200f;
	}
	public float getOuterAngle() {
		return 360f;
	} 
	public float getPitch() {
		return pitch;
	}
	public float getPlaybackTime() {
		return 0;
	}
	public Vector3f getPosition() {
		return Vector3f.ZERO;
	}
	public float getRefDistance() {
		return 10f;
	}
	public Filter getReverbFilter() {
		return null;
	}
	public AudioSource.Status getStatus() {
		return status;
	}
	public float getTimeOffset() {
		return timeOffset;
	}
	public Vector3f getVelocity() {
		return Vector3f.ZERO;
	}
	public float getVolume() {
		return volume;
	} 
	public boolean isDirectional() {
		return false;
	}
	public boolean isLooping() {
		return false;
	}
	public boolean isPositional() {
		return false;
	}
	public boolean isReverbEnabled() {
		return false;
	}
	public void setChannel(int channel) {
		if (status != AudioSource.Status.Stopped) {
			throw new IllegalStateException("Can only set source id when stopped");
		}
		this.channel = channel;
	}
	 public final void setStatus(AudioSource.Status status) {
        this.status = status;
    }
}