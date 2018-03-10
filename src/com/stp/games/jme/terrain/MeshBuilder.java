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
package com.stp.games.jme.terrain;
// JME3 Dependencies
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
// Java Dependencies
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MeshBuilder {

    private HashMap<Vector3f, Integer> indexMap;
    private LinkedList<Vector3f> verticesPosition;
    private LinkedList<Vector3f> verticesNormal;
    private LinkedList<Integer> indices;

    public MeshBuilder() {
        indexMap = new HashMap<Vector3f, Integer>();
        verticesPosition = new LinkedList<Vector3f>();
        verticesNormal = new LinkedList<Vector3f>();
        indices = new LinkedList<Integer>();
    }

    public void addVertex(Vector3f position, Vector3f normal) {
        int i = 0;
        Integer index = indexMap.get(position);

        if (index == null) {
            i = verticesPosition.size();
            indexMap.put(position, i);
            verticesPosition.add(position);
            verticesNormal.add(normal);

            // Update bounding box
        } else {
            i = index;
        }

        indices.add(i);
    }

    public Mesh generateMesh() {
        Mesh mesh = new Mesh();

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verticesPosition.toArray(new Vector3f[0])));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(verticesNormal.toArray(new Vector3f[0])));
        // mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texcoords.toArray(new Vector2f[0])));

       // mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(toIntArray(indices)));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, listToBuffer(indices));

        mesh.updateBound();
        
        //Clear All
        indexMap.clear();
        verticesPosition.clear();
        verticesNormal.clear();
        indices.clear();
		
		//TangentBinormalGenerator.generate(mesh);

        return mesh;
    }
    
    public IntBuffer listToBuffer(List<Integer> list)
    {
        IntBuffer buff = BufferUtils.createIntBuffer(list.size());
        buff.clear();
        for (Integer e : list) {
            buff.put(e.intValue());
        }
        buff.flip();
        return buff;
    }

    int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e.intValue();
        }
        return ret;
    }
    
    public int countVertices()
    {
        return verticesPosition.size();
    }
}
