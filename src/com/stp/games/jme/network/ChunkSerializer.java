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
import com.jme3.network.serializing.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.stp.games.jme.terrain.ChunkControl;

@SuppressWarnings("unchecked")
public class ChunkSerializer extends Serializer {
	public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
		try {
			ChunkControl chunk = (ChunkControl)c.newInstance();

			// Read all recieved bytes into an array
			byte[] byteArray = new byte[data.remaining()];
			data.get(byteArray);

			// Decompress the received data using a buffered stream
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(byteArray));
			BufferedInputStream bis = new BufferedInputStream(gis);
			chunk.read(bis);
			bis.close();
			gis.close();

			return (T)chunk;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new IOException(ex.toString());
		}
	}
	public void writeObject(ByteBuffer buffer, Object object) throws IOException {
		ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(byteArrayOutput);

		((ChunkControl)object).write(gos);
		gos.finish();
		gos.flush();
		gos.close();

		buffer.put(byteArrayOutput.toByteArray());
	}
}