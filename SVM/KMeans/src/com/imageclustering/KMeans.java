package com.imageclustering;

/*** Author :Vibhav Gogate , Tanvi Patil
 The University of Texas at Dallas
 *****/

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

public class KMeans {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("Usage: Kmeans <input-image> <k> <output-image>");
			return;
		}
		try {
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			int k = Integer.parseInt(args[1]);
			BufferedImage kmeansJpg = kmeans_helper(originalImage, k);
			ImageIO.write(kmeansJpg, "jpg", new File(args[2]));

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static BufferedImage kmeans_helper(BufferedImage originalImage,
			int k) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(w, h,
				originalImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(originalImage, 0, 0, w, h, null);
		// Read rgb values from the image
		int[] rgb = new int[w * h];
		int count = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				rgb[count++] = kmeansImage.getRGB(i, j);
			}
		}
		// Call kmeans algorithm: update the rgb values
		kmeans(rgb, k);

		// Write the new rgb values to the image
		count = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				kmeansImage.setRGB(i, j, rgb[count++]);
			}
		}
		return kmeansImage;
	}

	private static void kmeans(int[] rgb, int k) {
		// Distinct colors in the image
		HashSet<Color> distinctColors = new HashSet<Color>();
		ArrayList<Color> distinctColorsList = null;
		// rgb array in Color datatype
		Color[] pixelArray = new Color[rgb.length];
		Color[] compressedColors = new Color[rgb.length];
		// HashMap of clusters. Stores cluster color to original color mapping
		HashMap<Color, ArrayList<Color>> clusters = new HashMap<Color, ArrayList<Color>>();

		for (int rgbArrayIterator = 0; rgbArrayIterator < rgb.length; rgbArrayIterator++) {
			pixelArray[rgbArrayIterator] = new Color(rgb[rgbArrayIterator]);
			if (!distinctColors.contains(pixelArray[rgbArrayIterator])) {
				distinctColors.add(pixelArray[rgbArrayIterator]);
			}
		}

//		System.out.println("Distinct colors length : " + distinctColors.size());
//		System.out.println("RGB length : " + rgb.length);
		
		distinctColorsList = new ArrayList<Color>(distinctColors);
		for (int kIterator = 0; kIterator < k; kIterator++) {
			Random r = new Random();
			int low = 0;
			int high = distinctColors.size();
			int randomNo = r.nextInt(high - low) + low;
			Color colorPicked = distinctColorsList.remove(randomNo);
			distinctColors.remove(colorPicked);
			clusters.put(colorPicked, new ArrayList<Color>());
//			System.out.println(colorPicked);
		}

		while (true) {

			// The color in the cluster closest to the color under consideration
			Color closestColor = null;
			// Distance between the color under consideration and cluster color
			int distance;
			// Minimum distance between the cluster color and color under
			// consideration
			int minDistance;
			// Distinct colors in the cluster
			Set<Color> clusterColorSet = clusters.keySet();
			// For all the colors in the image
			for (int pixelArrayIterator = 0; pixelArrayIterator < pixelArray.length; pixelArrayIterator++) {
				minDistance = Integer.MAX_VALUE;
				closestColor = null;
				// For all the cluster colors.
				for (Color clusterColor : clusterColorSet) {
					// calculate the distance between red, green and blue
					// component of cluster color and image color
					int redDist, greenDist, blueDist;
					redDist = (int) Math.pow(clusterColor.getRed()
							- pixelArray[pixelArrayIterator].getRed(), 2);
					greenDist = (int) Math.pow(clusterColor.getGreen()
							- pixelArray[pixelArrayIterator].getGreen(), 2);
					blueDist = (int) Math.pow(clusterColor.getBlue()
							- pixelArray[pixelArrayIterator].getBlue(), 2);
					distance = redDist + greenDist + blueDist;
					// If the distance between current cluster color and the
					// image color is less than the earlier calculated //
					// distances.
					if (distance < minDistance) {
						minDistance = distance;
						closestColor = clusterColor;
					}
				}
				// Add the image color to the cluster to which it is closest
				clusters.get(closestColor).add(pixelArray[pixelArrayIterator]);
				// Modify the new pixel array with the color closest to it.
				compressedColors[pixelArrayIterator] = closestColor;
			}
			
			int noOfChangedClusterColors = 0;
			ArrayList<Color> newClusterColors = new ArrayList<Color>();

			// Calculate the average color of every cluster
			for (Color clusterColor : clusterColorSet) {
				
				// The list of colors in the current cluster
				ArrayList<Color> clusterColorList = clusters.get(clusterColor);
				int redAverage = 0, blueAverage = 0, greenAverage = 0;
				for (Color colorInTheList : clusterColorList) {
					redAverage += colorInTheList.getRed();
					greenAverage += colorInTheList.getGreen();
					blueAverage += colorInTheList.getBlue();
				}
				int listLength = clusterColorList.size();
				Color averageColor = new Color(redAverage / listLength,
						greenAverage / listLength, blueAverage / listLength);
				newClusterColors.add(averageColor);
				
				// If the average color of the list is not same as the cluster
				// color.
				if (closestColor.getRGB() != averageColor.getRGB()) {
					noOfChangedClusterColors++;
				}
			}
			
//			System.out.println(noOfChangedClusterColors);
			if (noOfChangedClusterColors == 0) {
				break;
			}
			// for(Color colorToRemove : removeColors){
			// clusters.remove(colorToRemove); // }
			clusters = new HashMap<Color, ArrayList<Color>>();
			for (Color colorToAdd : newClusterColors) {
				clusters.put(colorToAdd, new ArrayList<Color>());
			}
		}
		for (int rgbIterator = 0; rgbIterator < rgb.length; rgbIterator++) {
			rgb[rgbIterator] = compressedColors[rgbIterator].getRGB();
		}
	}

}