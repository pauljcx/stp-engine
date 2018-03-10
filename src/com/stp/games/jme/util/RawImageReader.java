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
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import com.stp.util.JavaIO;

public final class RawImageReader {

	public static Image getImageData(String fileName) {
		try {
			InputStream inputStream = JavaIO.getInputStream(fileName);
			if (inputStream != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = reader.readLine();
				int width = 0;
				int height = 0;
				int offset = 0;
				int byteCount = 0;
				ByteOrder bOrder = ByteOrder.LITTLE_ENDIAN;
				while (line != null) {
					String[] parts = line.trim().toLowerCase().split("\\s+");
					if (parts.length > 1) {
						if (parts[0].equals("width")) {
							width = Integer.valueOf(parts[1]);
						} else if (parts[0].equals("height")) {
							height = Integer.valueOf(parts[1]);
						} else if (parts[0].equals("offset")) {
							offset = Integer.valueOf(parts[1]);
						} else if (parts[0].equals("data-length")) {
							byteCount = Integer.valueOf(parts[1]);
						} else if (parts[0].equals("byte-order")) {
							bOrder = parts[1].equals("little-endian") ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
						}
						System.out.println(parts[0] + "=" + parts[1]);
					}
					line = reader.readLine();
				}
				inputStream.close();
				reader.close();
				
				int bufferSize = width * height * byteCount;
				if (bufferSize > 0) {
					inputStream = JavaIO.getInputStream(fileName.replace(".rpl", ".raw"));
					if (inputStream != null) {
						ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
						buffer.order(bOrder);
						byte[] data = new byte[1024];
						int count = inputStream.read(data);
						while (count >= 0) {
							buffer.put(data, 0, count);
							count = inputStream.read(data);
						}
						inputStream.close();
						buffer.flip();
						return new Image(Image.Format.Depth16, width, height, buffer, ColorSpace.Linear);
					}
				}
			}
		} catch (Exception ex) {}
		return null;
	}
}