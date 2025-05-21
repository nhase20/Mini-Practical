<<<<<<< HEAD
package DataCalculations;
/**
 * Classifies food items based on freshness levels using clustering techniques to group them.
 * Basically just group items together using FreshCalculator according to their freshness level.
 * Use any data structure you want
 */
public class GroupClust {

}
=======
package DataCalculations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Storage.*;
/**
 * Classifies food items based on freshness levels using clustering techniques to group them.
 * Basically just group items together using FreshCalculator according to their freshness level.
 * Use any data structure you want
 */
public class GroupClust {
	private int k; // number of clusters
    private List<Product> products;
    private Map<Integer,List<Product>> clusts;

    public Map<Integer,List<Product>> getClusters() {
		return clusts;
	}

	/**
     * Constructor
     * @param k
     * @param products
     */
    public GroupClust(int k,List<Product> products) {
        this.k = k;
        this.products = products;
        this.clusts = new HashMap<>();
    }

    /**
     * Clusters products by finding their centroids the clusters them
     * @return Map<Integer, List<Product>>
     */
    public Map<Integer, List<Product>> runClustering() {
        //randomly pick initial cluster centroids
        List<Features> centroids = initializeCentroids();//gets the k centroids 

        boolean changed = true;
        while (changed) {//do this until centroids remain unchanged
            changed = false;
            Map<Integer, List<Product>> newClusters = new HashMap<>();
            //assigning each product to closest centroid
            for (Product p : products) {
                int closest = closestCentroid(p.getFeatures(),centroids);
                newClusters.computeIfAbsent(closest,x -> new ArrayList<>()).add(p);//adding the product as part of the list of closest centroid
            }
            //recalculating centroids
            List<Features> newCentroids = new ArrayList<>();
            for (int i = 0; i<k; i++) {
                List<Product> cluster = newClusters.getOrDefault(i,new ArrayList<>());
                newCentroids.add(averageFeatures(cluster));//getting average features of cluster
            }
            if(!centroids.equals(newCentroids)) {//comparing average features of cluster to cluster
                centroids = newCentroids;
                clusts = newClusters;
                changed = true;
            }
        }
        return clusts;
    }

    /**
     * Randomly choose k products' features as starting centroids
     * @return
     */
    private List<Features> initializeCentroids() {
        List<Features> centroids = new ArrayList<>();
        if (products.isEmpty()) {
            return centroids; // or throw an exception
        }
        Collections.shuffle(products);//randomize positions of each product
        //choosing 3 product features 
        for(int i = 0; i < k; i++) {
            centroids.add(products.get(i).getFeatures());
        }
        return centroids;
    }

    /**
     * Finds the closest centroid to a product
     * @param f
     * @param centroids
     * @return
     */
    private int closestCentroid(Features f,List<Features> centroids) {
        double minDist = Double.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < centroids.size(); i++) {
            double dist = KNN.computeDistance(f, centroids.get(i));
            if (dist < minDist) {
                minDist = dist;
                index = i;
            }
        }
        return index;
    }

    /**
     * a method that computes the average features from a group of products 
     * Going to be used to make assumptions about new products
     * @param cluster
     * @return
     */
    private Features averageFeatures(List<Product> cluster) {
        if (cluster.isEmpty()) return new Features(0, new double[256], 0, 0);

        double graySum = 0;
        int widthSum = 0, heightSum = 0;
        double[] histSum = new double[256];
        //adding each feature’s grayscale,width,height,and histogram into the totals
        for (Product p : cluster) {
            Features f = p.getFeatures();
            graySum += f.getAvgGray();
            widthSum += f.getWidth();
            heightSum += f.getHeight();
            double[] hist = f.getRgbHistogram();
            for (int i = 0; i<hist.length; i++) {
                histSum[i] += hist[i];
            }
        }
        int size = cluster.size();
        double[] avgHist = new double[256];
        for (int i = 0; i<256; i++) {
            avgHist[i] = (histSum[i]/size);//getting mean 
        }
        return new Features((graySum/size),avgHist,(widthSum/size),(heightSum/size));
    }
    
    /**
     * Method that returns a product which has the most similar products in store
     * @param graph
     * @param allProducts
     * @return
     */
    public Product getMostConnectedProduct(TreeGraph graph,List<Product> allProducts) {
        Product mostConnected = null;
        int maxConnections = -1;//exception made for the first product to be included even if it has 0 neighbors 
        //going through all products to find the product with most products it is similar to
        for (Product product : allProducts) {
            List<Product> neighbors = graph.getNeighbors(product);
            int connectionCount = neighbors.size();
            //compare which product is connected more
            if (connectionCount>maxConnections) {
                maxConnections = connectionCount;
                mostConnected = product;
            }
        }
        return mostConnected;
    }
    
    /**
     * Method groups products by their freshness level
     * @param allProducts
     * @return
     */
    public static Map<FreshnessLvl, List<Product>> groupProductsByFreshness(List<Product> allProducts) {
    	 Map<FreshnessLvl, List<Product>> grouped = new HashMap<>();

    	    //grouping products by freshness level
    	    for (Product product : allProducts) {
    	        FreshnessLvl level = product.getFresh();
    	        if (!grouped.containsKey(level)) {
    	            grouped.put(level,new ArrayList<>());
    	        }
    	        grouped.get(level).add(product);
    	    }

    	 //sorting each group
    	    for (Map.Entry<FreshnessLvl,List<Product>> entry : grouped.entrySet()) {
    	        List<Product> group = entry.getValue();
    	        FreshnessLvl level = entry.getKey();

    	        //sort by freshness level
    	        if (level == FreshnessLvl.EXPIRED || level == FreshnessLvl.ROTATE) {
    	            //most expired first (smallest expiryDate first)
    	            for (int i = 0; i<group.size() - 1; i++) {
    	                for (int j = i + 1; j<group.size(); j++) {
    	                    if (group.get(i).getExpiryDate().isAfter(group.get(j).getExpiryDate())) {
    	                        Product temp = group.get(i);
    	                        group.set(i,group.get(j));
    	                        group.set(j,temp);
    	                    }
    	                }
    	            }
    	        } else {
    	            //sort others by expiry date (longest shelf life first)
    	            for (int i = 0; i < group.size() - 1; i++) {
    	                for (int j = i + 1; j<group.size(); j++) {
    	                    if (group.get(i).getExpiryDate().isBefore(group.get(j).getExpiryDate())) {
    	                        Product temp = group.get(i);
    	                        group.set(i,group.get(j));
    	                        group.set(j,temp);
    	                    }
    	                }
    	            }
    	        }
    	    }
        return grouped;
    }

    /**
     * Find closest product from centroid
     * @param centroid
     * @param products
     * @return
     */
    public Product getClosestProductToCentroid(Features centroid,List<Product> products) {
        double minDistance = Double.MAX_VALUE;
        Product closest = null;
        for (Product p : products) {//For all find distance of features between each other until you find the 1 with smallest
            double dist = KNN.computeDistance(centroid,p.getFeatures());
            if (dist<minDistance) {
                minDistance = dist;
                closest = p;
            }
        }
        return closest;
    }

}
>>>>>>> d9a7cb6 (final changes)
