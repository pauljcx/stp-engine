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
// JME3 Dependcencies
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public class IsoSurface {

    private static boolean calculateNormalsNew = true;
    
    public static final int[] MS_CORNERS_FRONT = {7, 6, 2, 3};
    public static final int[] MS_CORNERS_BACK = {5, 4, 0, 1};
    public static final int[] MS_CORNERS_LEFT = {4, 7, 3, 0};
    public static final int[] MS_CORNERS_RIGHT = {6, 5, 1, 2};
    public static final int[] MS_CORNERS_TOP = {4, 5, 6, 7};
    public static final int[] MS_CORNERS_BOTTOM = {3, 2, 1, 0};
	
	public static final Vector3f[] LOD_SIZE = { new Vector3f(1f, 1f, 1f),
																		new Vector3f(2f, 2f, 2f),
																		new Vector3f(4f, 4f, 4f) };

    private IsoSurface() {
    }

	// Creates the voxel shape geometry using marching cubes algorithm
	public static Mesh generateVoxelMesh(VoxelShape source) {
		return generateVoxelMesh(source, new Vector3f(-source.getWidth()/2, -source.getHeight()/2, -source.getLength()/2), LOD_SIZE[0]);
	}
	// Creates the voxel shape geometry using marching cubes algorithm
	public static Mesh generateVoxelMesh(VoxelShape source, Vector3f loc, Vector3f lodSize) {
		MeshBuilder meshBuilder = new MeshBuilder();
        for (float i = -1f; i < source.getWidth(); i += lodSize.x) {
            for (float j = -1f; j < source.getHeight(); j += lodSize.y) {
                for (float k = -1f; k < source.getLength(); k += lodSize.z) {
				
                    float[] values = {
						source.getVoxel(i, j, k),
                        source.getVoxel(i + lodSize.x, j, k),
                        source.getVoxel(i + lodSize.x, j, k + lodSize.z),
                        source.getVoxel(i, j, k + lodSize.z),
                        source.getVoxel(i, j + lodSize.y, k),
                        source.getVoxel(i + lodSize.x, j + lodSize.y, k),
                        source.getVoxel(i + lodSize.x, j + lodSize.y, k + lodSize.z),
                        source.getVoxel(i, j + lodSize.y, k + lodSize.z)
					};

                    Vector3f[] locations = {
                        new Vector3f(loc.x + i + 0, loc.y + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + 0, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + 0, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + lodSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + lodSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + lodSize.y, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + lodSize.y, loc.z + k + lodSize.z)
                    };
					addMarchingCubesTriangles(source, locations, values, null, meshBuilder);
                }
            }
        }
		if (meshBuilder.countVertices() > 0) {
			return meshBuilder.generateMesh();
		}
		return null;
	}
    /**
     * Adds triangles to a MeshBuilder via Marching Cubes.
     *
     * @param corners The corners of the cube to triangulate via Marching Cubes.
     * @param volumeValues The cached volume values, one Vector4 consists of
     * gradient (x, y, z) and density (w). If 0 is given, it will be calculated.
     * @param mb The MeshBuilder to add the triangles to.
	 * @return true if new geometry was added, false otherwise
     */
    public static boolean addMarchingCubesTriangles(VoxelSource source, Vector3f[] corners, float volumeValue[], Vector3f volumeGradient[], MeshBuilder mb) {
        char cubeIndex = 0;
		float isoLevel = 0f;

        float[] values = new float[8];
        Vector3f[] gradients = new Vector3f[8];

        for (int i = 0; i < 8; i++) {
            if (volumeValue != null) {
                values[i] = volumeValue[i];
            } else {
                values[i] = source.getVoxel(corners[i]);
            }
            
            if(volumeGradient != null)
            {
				gradients[i] = volumeGradient[i];
            }else{
				gradients[i] = source.getGradient(corners[i], new Vector3f());
            }
            if (values[i] >= isoLevel) {
                cubeIndex |= 1 << i;
            }
        }

        int edge = edgeTable[cubeIndex];

        // Nothing intersects
        if (edge == 0) {
            return false;
        }

        Vector3f[] intersectionPoints = new Vector3f[12];
        Vector3f[] intersectionNormals = new Vector3f[12];
         
         for(int i=0;i<12;i++)
              intersectionNormals[i] = new Vector3f();

        /* Find the vertices where the surface intersects the cube */
        if ((edge & 1) != 0) {
            intersectionPoints[0] = interpolate(corners[0], corners[1], values[0], values[1], gradients[0], gradients[1], intersectionNormals[0], isoLevel);
        }
        if ((edge & 2) != 0) {
            intersectionPoints[1] = interpolate(corners[1], corners[2], values[1], values[2], gradients[1], gradients[2], intersectionNormals[1], isoLevel);
        }
        if ((edge & 4) != 0) {
            intersectionPoints[2] = interpolate(corners[2], corners[3], values[2], values[3], gradients[2], gradients[3], intersectionNormals[2], isoLevel);
        }
        if ((edge & 8) != 0) {
            intersectionPoints[3] = interpolate(corners[3], corners[0], values[3], values[0], gradients[3], gradients[0], intersectionNormals[3], isoLevel);
        }
        if ((edge & 16) != 0) {
            intersectionPoints[4] = interpolate(corners[4], corners[5], values[4], values[5], gradients[4], gradients[5], intersectionNormals[4], isoLevel);
        }
        if ((edge & 32) != 0) {
            intersectionPoints[5] = interpolate(corners[5], corners[6], values[5], values[6], gradients[5], gradients[6], intersectionNormals[5], isoLevel);
        }
        if ((edge & 64) != 0) {
            intersectionPoints[6] = interpolate(corners[6], corners[7], values[6], values[7], gradients[6], gradients[7], intersectionNormals[6], isoLevel);
        }
        if ((edge & 128) != 0) {
            intersectionPoints[7] = interpolate(corners[7], corners[4], values[7], values[4], gradients[7], gradients[4], intersectionNormals[7], isoLevel);
        }
        if ((edge & 256) != 0) {
            intersectionPoints[8] = interpolate(corners[0], corners[4], values[0], values[4], gradients[0], gradients[4], intersectionNormals[8], isoLevel);
        }
        if ((edge & 512) != 0) {
            intersectionPoints[9] = interpolate(corners[1], corners[5], values[1], values[5], gradients[1], gradients[5], intersectionNormals[9], isoLevel);
        }
        if ((edge & 1024) != 0) {
            intersectionPoints[10] = interpolate(corners[2], corners[6], values[2], values[6], gradients[2], gradients[6], intersectionNormals[10], isoLevel);
        }
        if ((edge & 2048) != 0) {
            intersectionPoints[11] = interpolate(corners[3], corners[7], values[3], values[7], gradients[3], gradients[7], intersectionNormals[11], isoLevel);
        }

        // Create the triangles according to the table.
        for (int i = 0; triTable[cubeIndex][i] != -1; i += 3) {

            if(calculateNormalsNew) {
				mb.addVertex(intersectionPoints[triTable[cubeIndex][i]], source.getGradient(intersectionPoints[triTable[cubeIndex][i]], new Vector3f()));
                mb.addVertex(intersectionPoints[triTable[cubeIndex][i + 1]], source.getGradient(intersectionPoints[triTable[cubeIndex][i + 1]], new Vector3f()));
                mb.addVertex(intersectionPoints[triTable[cubeIndex][i + 2]], source.getGradient(intersectionPoints[triTable[cubeIndex][i + 2]], new Vector3f()));
            } else {
                //mb.addVertex(intersectionPoints[triTable[cubeIndex][i]], intersectionNormals[triTable[cubeIndex][i]]);
               // mb.addVertex(intersectionPoints[triTable[cubeIndex][i+1]], intersectionNormals[triTable[cubeIndex][i+1]]);
                //mb.addVertex(intersectionPoints[triTable[cubeIndex][i+2]], intersectionNormals[triTable[cubeIndex][i+2]]);
				Vector3f tangent = new Vector3f(intersectionPoints[triTable[cubeIndex][i+1]]);
				tangent.subtractLocal(intersectionPoints[triTable[cubeIndex][i]]);
				Vector3f binormal = new Vector3f(intersectionPoints[triTable[cubeIndex][i+2]]);
				binormal.subtractLocal(intersectionPoints[triTable[cubeIndex][i]]);
				Vector3f fNormal = tangent.cross(binormal);
				
				mb.addVertex(intersectionPoints[triTable[cubeIndex][i]], fNormal);
                mb.addVertex(intersectionPoints[triTable[cubeIndex][i+1]], fNormal);
                mb.addVertex(intersectionPoints[triTable[cubeIndex][i+2]], fNormal);
            }
        }
		return true;
    }

    /**
     * Adds triangles to a MeshBuilder via Marching Squares.
     *
     * @param corners The corners of the cube where one side is to be
     * triangulated.
     * @param volumeValues The cached volume values, one Vector4 consists of
     * gradient (x, y, z) and density (w). If 0 is given, it will be calculated.
     * @param indices The four corners of the cube (== one side) to triangulate.
     * @param maxDistance The maximum distance to the surface where triangles
     * are generated.
     * @param mb The MeshBuilder to add the triangles to.
     */
    public static void addMarchingSquaresTriangles(VoxelSource source, Vector3f corners[], float volumeValues[],Vector3f volumeGradient[], int indices[], float maxDistance, MeshBuilder mb) {
        char squareIndex = 0;
		float isoLevel = 0f;
        float values[] = new float[4];
        Vector3f gradients[] = new Vector3f[4];

        for (int i = 0; i < 4; ++i) {
            if (volumeValues != null) {
                values[i] = volumeValues[indices[i]];
            } else {
                values[i] = source.getVoxel(corners[indices[i]]);
            }
            
            if(volumeGradient != null)
            {
                gradients[i] = volumeGradient[indices[i]];
            }else{
                gradients[i] = source.getGradient(corners[indices[i]], new Vector3f());
            }
                
            if (values[i] >= isoLevel) {
                squareIndex |= 1 << i;
            }
        }
        
        // Don't generate triangles if we are completely inside and far enough away from the surface
        if (squareIndex == 15 && values[0] >= maxDistance && values[1] >= maxDistance && values[2] >= maxDistance && values[3] >= maxDistance) {
            return;
        }
        
        int edge = msEdges[squareIndex];
        
        Vector3f[] intersectionPoints = new Vector3f[8];
        Vector3f[] intersectionNormals = new Vector3f[8];
        
        intersectionPoints[0] = corners[indices[0]];
        intersectionPoints[2] = corners[indices[1]];
        intersectionPoints[4] = corners[indices[2]];
        intersectionPoints[6] = corners[indices[3]];
        
       for(int i = 0; i < 8; i++) {
              intersectionNormals[i] = new Vector3f();      
       }
         
         if(calculateNormalsNew==false)
         {
             for(int i=0;i<8;i+=2)
             {
				source.getGradient(intersectionPoints[i],  intersectionNormals[i]);
				intersectionNormals[i].mult(source.getVoxel(intersectionPoints[i]) + 1.0f);
             }
         }
        
        if ((edge & 1) != 0) {
            intersectionPoints[1] = interpolate(corners[indices[0]], corners[indices[1]], values[0], values[1], gradients[0], gradients[1], intersectionNormals[1], isoLevel);
        }
        if ((edge & 2) != 0) {
            intersectionPoints[3] = interpolate(corners[indices[1]], corners[indices[2]], values[1], values[2], gradients[1], gradients[2], intersectionNormals[3], isoLevel);
        }
        if ((edge & 4) != 0) {
            intersectionPoints[5] = interpolate(corners[indices[2]], corners[indices[3]], values[2], values[3], gradients[2], gradients[3], intersectionNormals[5], isoLevel);
        }
        if ((edge & 8) != 0) {
            intersectionPoints[7] = interpolate(corners[indices[3]], corners[indices[0]], values[3], values[0], gradients[3], gradients[0], intersectionNormals[7], isoLevel);
        }

       for (int i = 0; triTable[squareIndex][i] != -1; i += 3) { //Why Marching Cubes Table here?

           if(calculateNormalsNew)
           {
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i]], source.getGradient(intersectionPoints[msTriangles[squareIndex][i]], new Vector3f()));
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i + 1]], source.getGradient(intersectionPoints[msTriangles[squareIndex][i + 1]], new Vector3f()));
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i + 2]], source.getGradient(intersectionPoints[msTriangles[squareIndex][i + 2]], new Vector3f()));
           } else {
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i]], intersectionNormals[msTriangles[squareIndex][i]]);
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i + 1]], intersectionNormals[msTriangles[squareIndex][i + 1]]);
                mb.addVertex(intersectionPoints[msTriangles[squareIndex][i + 2]], intersectionNormals[msTriangles[squareIndex][i + 2]]);
           }
        }
    }

    private static Vector3f interpolate(Vector3f p1, Vector3f p2, float valp1, float valp2, float isoLevel) {

        float mu;
        Vector3f p = new Vector3f();
        if (Math.abs(isoLevel - valp1) < 1.192092896e-07F) {
            return (p1);
        }
        if (Math.abs(isoLevel- valp2) < 1.192092896e-07F) {
            return (p2);
        }
        if (Math.abs(valp1 - valp2) < 1.192092896e-07F) {
            return (p1);
        }
        mu = (isoLevel - valp1) / (valp2 - valp1);

        p.x = p1.x + mu * (p2.x - p1.x);
        p.y = p1.y + mu * (p2.y - p1.y);
        p.z = p1.z + mu * (p2.z - p1.z);
        return (p);
    }
    
     private static Vector3f interpolate(Vector3f v0, Vector3f v1, float val0, float val1, Vector3f gradient0, Vector3f gradient1, Vector3f normal, float isoLevel) {
		 if (Math.abs(val0 - isoLevel) <= 1.192092896e-07F) {
			normal.x = gradient0.x;
			normal.y = gradient0.y;
			normal.z = gradient0.z;
			return (v0);
		 }
		 if (Math.abs(val1 - isoLevel) <= 1.192092896e-07F) {
			normal.x = gradient1.x;
			normal.y = gradient1.y;
			normal.z = gradient1.z;
			return (v1);
		 }
		 if (Math.abs(val1 - val0) <= 1.192092896e-07F) {
			normal.x = gradient0.x;
			normal.y = gradient0.y;
			normal.z = gradient0.z;
			return (v0);
		 }
		 float mu = (isoLevel - val0) / (val1 - val0);

		 normal.x = gradient0.x + mu * (gradient1.x - gradient0.x);
		 normal.y = gradient0.y + mu * (gradient1.y - gradient0.y);
		 normal.z = gradient0.z + mu * (gradient1.z - gradient0.z);

		normal.normalizeLocal();
		Vector3f p = new Vector3f();
		
		 p.x = v0.x + mu * (v1.x - v0.x);
		 p.y = v0.y + mu * (v1.y - v0.y);
		 p.z = v0.z + mu * (v1.z - v0.z);
		 return (p);
     }
	/* public static final int[] cube_edges = new int[24];
	 public static final int[] edge_table = new int[256];
	 static {
		 //Initialize the cube_edges table
		// This is just the vertex number of each cube
		int k = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 1; j <= 4; j <<= 1) {
				int p = i^j;
				if(i <= p) {
					cube_edges[k++] = i;
					cube_edges[k++] = p;
				}
			}
		}
		//  This is a 2^(cube configuration) ->  2^(edge configuration) map
		//  There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for(int i = 0; i < 256; i++) {
			int em = 0;
			for(int j = 0; j < 24; j+=2) {
				int a = (i & (1<<cube_edges[j]));
				int b = (i & (1<<cube_edges[j+1]));
				em |= (a != b) ? (1 << (j >> 1)) : 0;
			}
			edge_table[i] = em;
		}
	 }*/
	 //Internal buffer, this may get resized at run time
	/*int[] buffer = new int[4096];

	public static void function(data, dims) {
  
  var vertices = []
    , faces = []
    , n = 0
    , x = new Int32Array(3)
    , R = new Int32Array([1, (dims[0]+1), (dims[0]+1)*(dims[1]+1)])
    , grid = new Float32Array(8)
    , buf_no = 1;
   
  //Resize buffer if necessary 
  if(R[2] * 2 > buffer.length) {
    buffer = new Int32Array(R[2] * 2);
  }
  
  //March over the voxel grid
  for(x[2]=0; x[2]<dims[2]-1; ++x[2], n+=dims[0], buf_no ^= 1, R[2]=-R[2]) {
  
    //m is the pointer into the buffer we are going to use.  
    //This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
    //The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
    var m = 1 + (dims[0]+1) * (1 + buf_no * (dims[1]+1));*/
	 
/*
     //var SurfaceNets = (function() {
	public static void addSurfaceNetsTriangles() {

		//Precompute edge table, like Paul Bourke does.
		// This saves a bit of time when computing the centroid of each boundary cell
		//var cube_edges = new Int32Array(24)
		//, edge_table = new Int32Array(256);
(function() {

  //Initialize the cube_edges table
  // This is just the vertex number of each cube
  var k = 0;
  for(var i=0; i<8; ++i) {
    for(var j=1; j<=4; j<<=1) {
      var p = i^j;
      if(i <= p) {
        cube_edges[k++] = i;
        cube_edges[k++] = p;
      }
    }
  }

  //Initialize the intersection table.
  //  This is a 2^(cube configuration) ->  2^(edge configuration) map
  //  There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
  for(var i=0; i<256; ++i) {
    var em = 0;
    for(var j=0; j<24; j+=2) {
      var a = !!(i & (1<<cube_edges[j]))
        , b = !!(i & (1<<cube_edges[j+1]));
      em |= a !== b ? (1 << (j >> 1)) : 0;
    }
    edge_table[i] = em;
  }
})();

//Internal buffer, this may get resized at run time
var buffer = new Int32Array(4096);

return function(data, dims) {
  
  var vertices = []
    , faces = []
    , n = 0
    , x = new Int32Array(3)
    , R = new Int32Array([1, (dims[0]+1), (dims[0]+1)*(dims[1]+1)])
    , grid = new Float32Array(8)
    , buf_no = 1;
   
  //Resize buffer if necessary 
  if(R[2] * 2 > buffer.length) {
    buffer = new Int32Array(R[2] * 2);
  }
  
  //March over the voxel grid
  for(x[2]=0; x[2]<dims[2]-1; ++x[2], n+=dims[0], buf_no ^= 1, R[2]=-R[2]) {
  
    //m is the pointer into the buffer we are going to use.  
    //This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
    //The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
    var m = 1 + (dims[0]+1) * (1 + buf_no * (dims[1]+1));
    
    for(x[1]=0; x[1]<dims[1]-1; ++x[1], ++n, m+=2)
    for(x[0]=0; x[0]<dims[0]-1; ++x[0], ++n, ++m) {
    
      //Read in 8 field values around this vertex and store them in an array
      //Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
      var mask = 0, g = 0, idx = n;
      for(var k=0; k<2; ++k, idx += dims[0]*(dims[1]-2))
      for(var j=0; j<2; ++j, idx += dims[0]-2)      
      for(var i=0; i<2; ++i, ++g, ++idx) {
        var p = data[idx];
        grid[g] = p;
        mask |= (p < 0) ? (1<<g) : 0;
      }
      
      //Check for early termination if cell does not intersect boundary
      if(mask === 0 || mask === 0xff) {
        continue;
      }
      
      //Sum up edge intersections
      var edge_mask = edge_table[mask]
        , v = [0.0,0.0,0.0]
        , e_count = 0;
        
      //For every edge of the cube...
      for(var i=0; i<12; ++i) {
      
        //Use edge mask to check if it is crossed
        if(!(edge_mask & (1<<i))) {
          continue;
        }
        
        //If it did, increment number of edge crossings
        ++e_count;
        
        //Now find the point of intersection
        var e0 = cube_edges[ i<<1 ]       //Unpack vertices
          , e1 = cube_edges[(i<<1)+1]
          , g0 = grid[e0]                 //Unpack grid values
          , g1 = grid[e1]
          , t  = g0 - g1;                 //Compute point of intersection
        if(Math.abs(t) > 1e-6) {
          t = g0 / t;
        } else {
          continue;
        }
        
        //Interpolate vertices and add up intersections (this can be done without multiplying)
        for(var j=0, k=1; j<3; ++j, k<<=1) {
          var a = e0 & k
            , b = e1 & k;
          if(a !== b) {
            v[j] += a ? 1.0 - t : t;
          } else {
            v[j] += a ? 1.0 : 0;
          }
        }
      }
      
      //Now we just average the edge intersections and add them to coordinate
      var s = 1.0 / e_count;
      for(var i=0; i<3; ++i) {
        v[i] = x[i] + s * v[i];
      }
      
      //Add vertex to buffer, store pointer to vertex index in buffer
      buffer[m] = vertices.length;
      vertices.push(v);
      
      //Now we need to add faces together, to do this we just loop over 3 basis components
      for(var i=0; i<3; ++i) {
        //The first three entries of the edge_mask count the crossings along the edge
        if(!(edge_mask & (1<<i)) ) {
          continue;
        }
        
        // i = axes we are point along.  iu, iv = orthogonal axes
        var iu = (i+1)%3
          , iv = (i+2)%3;
          
        //If we are on a boundary, skip it
        if(x[iu] === 0 || x[iv] === 0) {
          continue;
        }
        
        //Otherwise, look up adjacent edges in buffer
        var du = R[iu]
          , dv = R[iv];
        
        //Remember to flip orientation depending on the sign of the corner.
        if(mask & 1) {
          faces.push([buffer[m], buffer[m-du], buffer[m-du-dv], buffer[m-dv]]);
        } else {
          faces.push([buffer[m], buffer[m-dv], buffer[m-du-dv], buffer[m-du]]);
        }
      }
    }
  }
  
  //All done!  Return the result
  return { vertices: vertices, faces: faces };
};
})();*/
	 
	 
    private static final int edgeTable[] = {
        0x000, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00, 
        0x190, 0x099, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c, 0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90, 
        0x230, 0x339, 0x033, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 
        0x3a0, 0x2a9, 0x1a3, 0x0aa, 0x7a6, 0x6af, 0x5a5, 0x4ac, 0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0, 
        0x460, 0x569, 0x663, 0x76a, 0x066, 0x16f, 0x265, 0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60, 
        0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0x0ff, 0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 
        0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x055, 0x15c, 0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 
        0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0x0cc, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 
        0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0x0cc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 
        0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x15c, 0x055, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650, 
        0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x2fc, 0x3f5, 0x0ff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0, 
        0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c, 0x265, 0x16f, 0x066, 0x76a, 0x663, 0x569, 0x460, 
        0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5, 0x6af, 0x7a6, 0x0aa, 0x1a3, 0x2a9, 0x3a0, 
        0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x033, 0x339, 0x230, 
        0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x099, 0x190, 
        0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c, 0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x000
    };
    private static final int triTable[][] = {
             {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
        {3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
        {3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
        {3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
        {9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
        {2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
        {8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
        {4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
        {3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
        {1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
        {4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
        {4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
        {5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
        {2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
        {9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
        {0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
        {2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
        {10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
        {5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
        {5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
        {9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
        {1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
        {10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
        {8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
        {2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
        {7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
        {2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
        {11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
        {5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
        {11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
        {11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
        {9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
        {2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
        {6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
        {3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
        {6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
        {10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
        {6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
        {8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
        {7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
        {3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
        {0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
        {9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
        {8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
        {5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
        {0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
        {6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
        {10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
        {10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
        {8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
        {1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
        {0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
        {10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
        {3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
        {6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
        {9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
        {8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
        {3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
        {6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
        {0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
        {10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
        {10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
        {2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
        {7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
        {7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
        {2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
        {1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
        {11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
        {8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
        {0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
        {7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
        {10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
        {2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
        {6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
        {7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
        {2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
        {10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
        {10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
        {0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
        {7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
        {6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
        {8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
        {9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
        {6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
        {4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
        {10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
        {8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
        {0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
        {1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
        {8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
        {10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
        {4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
        {10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
        {11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
        {9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
        {6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
        {7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
        {3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
        {7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
        {3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
        {6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
        {9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
        {1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
        {4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
        {7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
        {6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
        {3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
        {0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
        {6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
        {0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
        {11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
        {6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
        {5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
        {9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
        {1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
        {1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
        {10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
        {0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
        {5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
        {10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
        {11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
        {9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
        {7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
        {2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
        {8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
        {9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
        {9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
        {1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
        {9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
        {5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
        {0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
        {10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
        {2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
        {0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
        {0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
        {9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
        {5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
        {3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
        {5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
        {8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
        {0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
        {9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
        {1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
        {3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
        {4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
        {9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
        {11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
        {11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
        {2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
        {9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
        {3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
        {1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
        {4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
        {3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
        {0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
        {1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};
    
    
    private static final int msEdges[] =
    {
        0x0, // 0000 Nothing intersects
        0x9, // 0001 0
        0x3, // 0010 1
        0xA, // 0011 0 1
        0x6, // 0100 2
        0xF, // 0101 0 2
        0x5, // 0110 1 2
        0xC, // 0111 0 1 2
        0xC, // 1000 3
        0x5, // 1001 0 3
        0xF, // 1010 1 3
        0x6, // 1011 0 1 3
        0xA, // 1100 2 3
        0x3, // 1101 0 2 3
        0x9, // 1110 1 2 3
        0x0  // 1111 0 1 2 3
    };
    
    private static final int msTriangles[][] =
    {
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // Nothing intersects
        {7, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},    // 0
        {3, 2, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},    // 1
        {7, 2, 0, 7, 3, 2, -1, -1, -1, -1, -1, -1, -1},       // 0 1
        {5, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},    // 2
        {7, 1, 0, 3, 5, 4, -1, -1, -1, -1, -1, -1, -1},       // 0 2
        {1, 4, 2, 1, 5, 4, -1, -1, -1, -1, -1, -1, -1},       // 1 2
        {0, 7, 5, 0, 5, 4, 0, 4, 2, -1, -1, -1, -1},          // 0 1 2
        {7, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},    // 3
        {0, 5, 1, 0, 6, 5, -1, -1, -1, -1, -1, -1, -1},       // 0 3
        {1, 3, 2, 7, 6, 5, -1, -1, -1, -1, -1, -1, -1},       // 1 3
        {3, 6, 5, 3, 0, 6, 3, 2, 0, -1, -1, -1, -1},          // 0 1 3
        {7, 6, 4, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1},       // 2 3
        {3, 1, 0, 3, 0, 6, 3, 6, 4, -1, -1, -1, -1},          // 0 2 3
        {2, 1, 7, 2, 7, 6, 2, 6, 4, -1, -1, -1, -1},          // 1 2 3
        {6, 2, 0, 6, 4, 2, -1, -1, -1, -1, -1, -1, -1},       // 0 1 2 3
        {1, 0, 7, 1, 7, 5, 1, 5, 4, 1, 4, 3, -1},             // 0 2 alternative
        {1, 3, 2, 1, 5, 3, 1, 6, 5, 1, 7, 6, -1}              // 1 3 alternative
    };
}
