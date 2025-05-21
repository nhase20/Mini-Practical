<<<<<<< HEAD
package DataCalculations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Storage.Features;
import Storage.Product;
/**
 * Utility class for KNN algorithms
 */
public class KNN {	
	/**
     * A helper method to find the average distance for a given k
     * Tests how well a certain value of k works for K-NN by calculating the average similarity distance across all products
     * @param allProducts
     * @param k
     * @return
     */
    public static double AvgDist(List<Product> allProducts, int k) {
        double totalDistance = 0;//the sum of distances between products and their neighbors
        int count = 0;//the total number of neighbor comparisons made
        
        //looping through each product we want to find neighbors for
        for (Product target : allProducts) {
            List<Product> neighbors = new ArrayList<>(allProducts);//making a copy of products
            neighbors.remove(target); //don't compare product with itself
            
            //it sorts the neighbors list in increasing order of similarity distance to target
            neighbors.sort(Comparator.comparingDouble((Product p)-> computeDistance(target, p)));//for every neighbor p, sort based on the output of computeDistance(target, p)
            List<Product> kNeighbors = neighbors.subList(0, Math.min(k, neighbors.size()));//chooses the nearest products to target if k is not greater than list of products/neighbors
            
            //sums up total distance of all neighbors to target and captures how many neighbors(loops) there are.
            for (Product neighbor : kNeighbors) {
                totalDistance += computeDistance(target, neighbor);
                count++;
            }
        }
        return totalDistance / count; //lower is better
    }
    
    /**
     * method to find the best k for our K-NN
     * @param allProducts
     * @param maxK
     * @return
     */
    public static int findOptimalK(List<Product> allProducts, int maxK) {
        double bestScore = Double.MAX_VALUE;//used to track the lowest average distance found so far
        int bestK = 1;//guess or random k

        //testing all reasonable values of k(from 1-10 usually)
        for (int k = 1; k <= maxK; k++) {
            double score = AvgDist(allProducts, k);
            System.out.println("k = " + k + ", avg distance = " + score);

            //after trying all values of k,choose the one that gave the lowest average distance
            if (score < bestScore) {
                bestScore = score;
                bestK = k;
            }   
        }
        return bestK;
    }
    
    /**
     * A helper method to define a  distance function between two Product objects using name, shelfID, and features
     * @param p1
     * @param p2
     * @return
     */
    public static double computeDistance(Product p1, Product p2) {
        double NameScore = 1;
        double histDiff = 0;
        double ShelfScore =  1;
        
        //if names or shelf IDs match then score = 0 (they're similar).
        if(p1.getName().equalsIgnoreCase(p2.getName())) {
        	NameScore = 0;
        }
        if(p1.getShelfID().equalsIgnoreCase(p2.getShelfID())) {
        	ShelfScore = 0 ;
        }
        //assigning features to local variables
        Features f1 = p1.getFeatures();
        Features f2 = p2.getFeatures();

        //euclidean distance between features
        double grayDiff = f1.getAvgGray() - f2.getAvgGray();
        double widthDiff = f1.getWidth() - f2.getWidth();
        double heightDiff = f1.getHeight() - f2.getHeight();
   
        double[] h1 = f1.getRgbHistogram();
        double[] h2 = f2.getRgbHistogram();
        for (int i = 0; i < h1.length; i++) {
            histDiff += h1[i] - h2[i];
        }
        double featureDistance = Math.sqrt(Math.pow(grayDiff ,2)+ Math.pow(widthDiff,2) + Math.pow(heightDiff,2) + Math.pow(histDiff, 2));
        // Total distance
        return NameScore+ShelfScore+featureDistance;
    }
    
    
    
}
=======
package DataCalculations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import Storage.Features;
import Storage.Product;
/**
 * Utility class for KNN algorithms
 */
public class KNN {	
	/**
     * A helper method to find the average distance for a given k
     * Tests how well a certain value of k works for K-NN by calculating the average similarity distance across all products
     * @param allProducts
     * @param k
     * @return
     */
    public static double AvgDist(List<Product> allProducts,int k) {
        double totalDistance = 0;//the sum of distances between products and their neighbors
        int count = 0;//the total number of neighbor comparisons made
        
        //looping through each product we want to find neighbors for
        for (Product target : allProducts) {
            List<Product> neighbors = new ArrayList<>(allProducts);//making a copy of products
            neighbors.remove(target); //don't compare product with itself
            
            //it sorts the neighbors list in increasing order of similarity distance to target
            neighbors.sort(Comparator.comparingDouble((Product p)-> computeDistance(target,p)));//for every neighbor p, sort based on the output of computeDistance(target, p)
            List<Product> kNeighbors = neighbors.subList(0,Math.min(k, neighbors.size()));//chooses the nearest products to target if k is not greater than list of products/neighbors
            
            //sums up total distance of all neighbors to target and captures how many neighbors(loops) there are.
            for (Product neighbor : kNeighbors) {
                totalDistance += computeDistance(target,neighbor);
                count++;
            }
        }
        return (totalDistance/count); //lower is better
    }
    
    /**
     * method to find the best k for our K-NN
     * @param allProducts
     * @param maxK
     * @return
     */
    public static int findOptimalK(List<Product> allProducts,int maxK) {
        double bestScore = Double.MAX_VALUE;//used to track the lowest average distance found so far
        int bestK = 1;//guess or random k

        //testing all reasonable values of k(from 1-10 usually)
        for (int k = 1; k<=maxK; k++) {
            double score = AvgDist(allProducts,k);

            //after trying all values of k,choose the one that gave the lowest average distance
            if (score<bestScore) {
                bestScore = score;
                bestK = k;
            }   
        }
        return bestK;
    }
    
    /**
     * A helper method to define a  distance function between two Product objects using name, shelfID, and features
     * @param p1
     * @param p2
     * @return
     */
    public static double computeDistance(Product p1,Product p2) {
        double NameScore =1;
        double ShelfScore =  1;       
        //if names or shelf IDs match then score = 0 (they're similar).
        if(p1.getName().equalsIgnoreCase(p2.getName())) {
        	NameScore = 0;
        	return NameScore;
        }
        if(p1.getShelfID().equalsIgnoreCase(p2.getShelfID())) {
        	ShelfScore = 0 ;
        }
        double featureDistance = computeDistance(p1.getFeatures(),p2.getFeatures());
        //total distance

        return NameScore+ShelfScore+featureDistance;
    }
  
  /**
   * compute Euclidean distance between two features using all possible feature attributes
   * @param f1
   * @param f2
   * @return
   */
    public static double computeDistance(Features f1,Features f2) {
        double grayDiff = f1.getAvgGray()-f2.getAvgGray();

        double histDiff = 0;
        double[] h1 = f1.getRgbHistogram();
        double[] h2 = f2.getRgbHistogram();
        for (int i = 0; i<h1.length; i++) {
            histDiff += Math.pow(h1[i]-h2[i],2);
        }
        return Math.sqrt(Math.pow(grayDiff,2) + histDiff);
    }
    
}
>>>>>>> d9a7cb6 (final changes)
