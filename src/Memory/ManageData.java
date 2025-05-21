package Memory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;

import Storage.*;
/**
 * Stores and retrieves data
 * Load/save products and shelf data
 */
public class ManageData {
	private List<Product> products;
    private TreeGraph PGraph;

    public ManageData() {
        this.products = new ArrayList<>();
        this.PGraph = new TreeGraph(new HashMap<>());
    }

    /**
     * Delegates adding a product to TreeGraph and list of products
     * @param product
     */
    public void addProduct(Product product) {
        products.add(product);
        PGraph.addProduct(product);
    }
    
    public void removeProduct(Product product) {
        products.remove(product);
        PGraph.removeProduct(product);
    }

    /**
     * Adds an edge between 2 similar products
     * @param p1
     * @param p2
     */
    public void addSimilarity(Product p1,Product p2) {
    	PGraph.addEdge(p1,p2);
    }

    //Getters and Setters
    public List<Product> getAllProducts() {
        return products;
    }

    public TreeGraph getGraph() {
        return PGraph;
    }

    /**
     * Clears all products in products list and TreeGraph to start over
     */
    public void clearAll() {
        products.clear();
        PGraph = new TreeGraph(new HashMap<>());
    }

    /**
     * Groups products by their shelfs(if similar)
     * @return Map<String,List<Product>> 
     */
    public Map<String,List<Product>> groupByShelf() {
        Map<String,List<Product>> shelfMap = new HashMap<>();//temporary shelf map 
        for (Product p : products) {     
            String key = p.getShelfID();//get the key based on the product's shelfID
            //if the map does not already contain a list for this key,then we create one
            if (!shelfMap.containsKey(key)) {
            	shelfMap.put(key, new ArrayList<>());
            }      
            shelfMap.get(key).add(p);//add the product to the corresponding list in the map
        }
        return shelfMap;
    }

    /**
     * Groups the products by the 4 freshness levels
     * @return
     */
    public Map<FreshnessLvl,List<Product>> groupByFreshness() {
        Map<FreshnessLvl,List<Product>> freshMap = new HashMap<>();
        for (Product p : products) {     
            FreshnessLvl key = p.getFresh();//get the key based on the product's freshness levol
            //if the map does not already contain a list for this key,then we create one
            if (!freshMap.containsKey(key)) {
                freshMap.put(key, new ArrayList<>());
            }      
            freshMap.get(key).add(p);// Add the product to the corresponding list in the map
        }
        return freshMap;
    }

    /**
     * Finds products similar to a particular product
     * @param target
     * @return
     */
    public List<Product> findSimilarTo(Product p) {
        return PGraph.findSimilarProducts(p);
    }

    /**
     * Saves products in List to a csv file for later use if needed
     * @param filePath
     * @throws IOException
     */
    public void saveToCSV(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Product p : products) {
                Features f = p.getFeatures();
                //writing all products variables to csv file
                writer.println(
                    p.getName()+","+
                    p.getShelfID()+","+
                    String.valueOf(p.getExpiryDate().getYear())+","+
                    String.valueOf(p.getExpiryDate().getMonth())+","+
                    String.valueOf(p.getExpiryDate().getDay())+","+
                    String.valueOf(f.getAvgGray())+","+
                    String.valueOf(f.getWidth())+","+
                    String.valueOf(f.getHeight())+","+
                    p.getImagePath()+ "," + 
                    p.getFresh().name()
                );
            }
        }
    }
 
    /**
     * Method used to read a csv file with products to reuse them
     * @param filePath
     * @throws IOException
     */
    public void loadFromCSV(String filePath) throws IOException {
        products.clear();//Clear the list of products in order to create space
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            //line by line read each product attribute in the csv file
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                String shelfID = parts[1];
                int year = Integer.parseInt(parts[2]);
                int month = Integer.parseInt(parts[3]);
                int day = Integer.parseInt(parts[4]);
                double avgGray = Double.parseDouble(parts[5]);
                int width = Integer.parseInt(parts[6]);
                int height = Integer.parseInt(parts[7]);
                String imagePath = parts[8];
                Features feats = new Features(avgGray,new double[256],width,height);
                FreshnessLvl freshness = FreshnessLvl.valueOf(parts[9]);
                Image img=null;//image usually null since we cannot save it to a csv file
                //but we can use the image path to find it again
                if(imagePath != null && !imagePath.trim().isEmpty()) {
                	try {
                		img = new Image(new File(imagePath).toURI().toString());//if we find image again the reread it and create an image
                	}catch(Exception e) {
                		System.err.println("Could not load image: " + imagePath + " but proceeding regardless.");
                		continue;
                	}
                }
                Shelf shelf = new Shelf(shelfID, img);
                Product product = new Product(name,feats,shelf,img,new Date(year,month,day),freshness);//create product 
                product.setImagePath(imagePath);
                addProduct(product);//add the product to the new list
            }
        }
    }
    
    
    
}
