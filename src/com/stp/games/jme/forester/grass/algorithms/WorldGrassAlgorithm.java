package com.stp.games.jme.forester.grass.algorithms;

import com.stp.games.jme.forester.paging.interfaces.Page;
import com.stp.games.jme.forester.grass.GrassPage;
import com.stp.games.jme.forester.image.DensityMap;
import com.stp.games.jme.forester.util.FastRandom;
import com.stp.games.jme.forester.RectBounds;
import com.stp.games.jme.forester.grass.GrassLayer;
import com.stp.games.jme.terrain.World;

public class WorldGrassAlgorithm implements GrassPlantingAlgorithm {
	private World world;
	public WorldGrassAlgorithm(World world) {
		this.world = world;
	}
    /**
     * This should be an algorithm for generating grass. It should always
     * return the number of grass patches that was generated.
     * 
     * @param page The grass page.
     * @param layer The grasslayer.
     * @param densityMap A density map (or null).
     * @param grassData The array for storing grass data.
     * @param grassCount The initial number of grass patches.
     * @return The number of grass patches after running the algorithm.
     */
	 
    public int generateGrassData(Page page, GrassLayer layer, DensityMap densityMap, float[] grassData, int grassCount) {
		RectBounds bounds = page.getBounds();
        //Populating the array of locations (and also getting the total amount of quads).
        FastRandom rand = new FastRandom();
        float width = bounds.getWidth();		
        //Iterator
        int iIt = 0;

        for (int i = 0; i < grassCount; i++) {
            float x = rand.unitRandom() * (bounds.getWidth() - 0.01f);
            float z = rand.unitRandom() * (bounds.getWidth() - 0.01f);
			if (world.getHeight(x + bounds.getxMin(), z + bounds.getzMin()) < 2) {
				continue;
			}			

            if (rand.unitRandom()*5 < 1) {
                grassData[iIt++] = x + bounds.getxMin(); // the x world coord
                grassData[iIt++] = z + bounds.getzMin(); // the x world coord
                grassData[iIt++] = rand.unitRandom(); // the grass scale
                //-pi/2 -> pi/2
                grassData[iIt++] = (-0.5f + rand.unitRandom())*3.141593f; // the grass rotation
            }
        }
        //The iterator divided by four is the grass-count.
        return iIt/4;
	}
}