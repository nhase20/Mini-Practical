<<<<<<< HEAD
package Storage;
import  DataCalculations.KNN;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class stores graph of products using Maps to link them
 */
public class TreeGraph {
	private Map<Product, List<Product>> adjacencyList; // based on similarity
	
	public TreeGraph(Map<Product, List<Product>> adjacencyList) {
		super();
		this.adjacencyList = adjacencyList;
		
	}
	/**
	 * Method adds a product node to the graph
	 * @param product
	 */
    public void addProduct(Product product) {
    	//uses the equals() and hashCode() methods of the Product class
    	//to determine if a product already exists in the Map
        adjacencyList.putIfAbsent(product, new ArrayList<>());
    }

    /**
     * Adds a connection between two products if they are similar(eg. Milk1 --> Milk2)
     * @param p1
     * @param p2
     */
    public void addEdge(Product p1, Product p2) {
    	//Adds products if they are not already on the Map
        if (!adjacencyList.containsKey(p1)) {
        	addProduct(p1);
        }
        if (!adjacencyList.containsKey(p2)) {
        	addProduct(p2);
        }
        //Make an edge for each node to each other to show they are similar
        adjacencyList.get(p1).add(p2);
        adjacencyList.get(p2).add(p1); // undirected edge
    }

    /**
     * Get all connected products/neighbors of given product
     * @param product
     * @return List<Product> 
     */
    public List<Product> getNeighbors(Product product) {
    	//get me the products connected to the given product 
    	//if they exist otherwise give me an empty list
    	List<Product> products =  new ArrayList<>();
    	if(adjacencyList.containsKey(product)) {
    		products = adjacencyList.get(product);
    	}
        return products ;
    }

    /**
     * Get all products in an unordered set 
     * @return Set<Product>
     */
    public Set<Product> getAllProducts() {
    	Set<Product> setProd = adjacencyList.keySet();
    	//if we want to use a list instead for simplicity instead
    	//List<Product> listProd = new ArrayList<>(setProd); 
        return setProd;
    }

    /**
     * Find similar products based on freshness or features using KNN
     * @param product
     * @param k
     * @return List<Product>
     */
    public List<Product> findSimilarProducts(Product product, int k) {
    	List<Product> neighbors = getNeighbors(product);//retrieving all directly related neighbors of product
    	if (neighbors.isEmpty()) {
    		 return new ArrayList<>();
    	}
    	// Find best k using neighbors instead of allProducts
        int optimalK = KNN.findOptimalK(neighbors, Math.min(10, neighbors.size()));
        neighbors.sort(Comparator.comparingDouble(p -> KNN.computeDistance(product, p)));//arranging them in  in ascending order of distance from the given product by difference
        List<Product> output = neighbors.subList(0, Math.min(optimalK, neighbors.size()));
        return output;
    }

    // Check if the graph contains a product
    public boolean contains(Product product) {
        return adjacencyList.containsKey(product);
    }

    // Print graph for visualization/debug
    public void printGraph() {
        for (Map.Entry<Product, List<Product>> entry : adjacencyList.entrySet()) {
        	Product key = entry.getKey();
        	List<Product> neighbors = entry.getValue();//neighbors of product
        	
        	System.out.print(key.getName() + " --> [");
        	for (int i = 0; i < neighbors.size(); i++) {
        	    System.out.print(neighbors.get(i).getName());
        	    //adding a comma after each product excluding the last one
        	    if (i < neighbors.size() - 1) {
        	        System.out.print(", ");
        	    }
        	}
        	System.out.println("]");
        }
    }
}
=======
package Storage;
import  DataCalculations.KNN;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class stores graph of products
 * Class abstracts products as nodes and similarities as edges
 */
public class TreeGraph {
	private Map<Product,List<Product>> adjList; // based on similarity
	
	public TreeGraph(Map<Product,List<Product>> adjacencyList) {
		super();
		this.adjList = adjacencyList;
		
	}
	/**
	 * Method adds a product node to the graph
	 * @param product
	 */
    public void addProduct(Product product) {
    	//uses the equals() and hashCode() methods of the Product class
    	//to determine if a product already exists in the Map
    	adjList.putIfAbsent(product, new ArrayList<>());
    }

    /**
     * Adds a connection between two products if they are similar(eg. Milk1 --> Milk2)
     * @param p1
     * @param p2
     */
    public void addEdge(Product p1,Product p2) {
    	//Adds products if they are not already on the Map
        if (!adjList.containsKey(p1)) {
        	addProduct(p1);
        }
        if (!adjList.containsKey(p2)) {
        	addProduct(p2);
        }
        //Make an edge for each node to each other to show they are similar
        adjList.get(p1).add(p2);
        adjList.get(p2).add(p1); //undirected edge
    }

    /**
     * Get all connected products/neighbors of given product
     * @param product
     * @return List<Product> 
     */
    public List<Product> getNeighbors(Product product) {
    	//get me the products connected to the given product 
    	//if they exist otherwise give me an empty list
    	List<Product> products =  new ArrayList<>();
    	if(adjList.containsKey(product)) {
    		products = adjList.get(product);
    	}
        return products ;
    }

    /**
     * Get all products in an unordered set 
     * @return Set<Product>
     */
    public Set<Product> getAllProducts() {
    	Set<Product> setProd = adjList.keySet();
    	//if we want to use a list instead for simplicity instead
    	//List<Product> listProd = new ArrayList<>(setProd); 
        return setProd;
    }

    /**
     * Find similar products based on freshness or features using KNN
     * @param product
     * @param k
     * @return List<Product>
     */
    public List<Product> findSimilarProducts(Product product) {
    	List<Product> candidates = new ArrayList<>(getAllProducts());
    	candidates.remove(product);//retrieving all directly related neighbors of product
    	if (candidates.isEmpty()) {
    		 return new ArrayList<>();
    	}
    	// Find best k using neighbors instead of allProducts
        int optimalK = KNN.findOptimalK(candidates, Math.min(10,candidates.size()));
        //arranging them in  in ascending order of distance from the given product by difference
        Map<Product, Double> distanceMap = new HashMap<>();
        for (Product p : candidates) {
            distanceMap.put(p, KNN.computeDistance(product, p));
        }
        candidates.sort(Comparator.comparingDouble(distanceMap::get));
        List<Product> output = candidates.subList(0, Math.min(optimalK,candidates.size()));
        //adding an edge for each product and the products similar to it.
        for(Product p:output) {
        	addEdge(product, p);
        }
        return output;
    }

    /**
     * Check if the graph contains a product
     * @param product
     * @return boolean
     */
    public boolean contains(Product product) {
        return adjList.containsKey(product);
    }
    
    /**
     * Removes a product from the TreeGraph
     * @param p
     */
    public void removeProduct(Product p) {
    	adjList.remove(p);
    	adjList.values().forEach(list -> list.remove(p));
    }
    
    /**
     * Method classifies a product based on similarities with other products
     * @param features
     * @return
     */
    public Product classifyProduct(Features features) {
        List<Product> similar = findSimilarProducts(new Product("",features,null,null,null,null));//creating a dummy product with features that will be compared to
        return similar.isEmpty() ? null : similar.get(0);//if there is a similar product return it
    }
    
    /**
     * Automatically connects a product to its most similar products based on KNN
     * @param product
     */
    public void connectSimilarProducts(List<Product> product) {
        for(Product p:product) {
        	findSimilarProducts(p);
        }
    }

}
>>>>>>> d9a7cb6 (final changes)
