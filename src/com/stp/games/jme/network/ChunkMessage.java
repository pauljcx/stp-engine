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
package com.stp.games.jme.network;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.stp.games.jme.terrain.ChunkControl;

public class ChunkMessage {
	@Serializable
	public static class ActivateChunk extends AbstractMessage {
		public long version;
		public int volume;
		public int x;
		public int y;
		public int z;
		public ActivateChunk() {}
		public ActivateChunk(ChunkControl chunk) {
			this.version = chunk.getVersion();
			this.x = chunk.getX();
			this.y = chunk.getY();
			this.z = chunk.getZ();
		}
		public long getVersion() {
			return version;
		}
		public int getVolume() {
			return volume;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public int getZ() {
			return z;
		}
	}
	@Serializable
	public static class UpdateChunk extends AbstractMessage {
		public ChunkControl chunk;
		public UpdateChunk() {}
		public UpdateChunk(ChunkControl chunk) {
			this.chunk = chunk;
		}
		public ChunkControl getChunk() {
			return chunk;
		}
	}
}