<<<<<<< HEAD
package Storage;

	
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Represents one shelf image containing a lot of products
 */

public class Shelf implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5073673584629693524L;
	private String shelfId;
	private transient Image shelfImage;
	private List<Product> productList;
	
	public Shelf(String shelfId, Image shelfImage) {
		super();
		this.shelfId = shelfId;
		this.shelfImage = shelfImage;
		this.productList = new ArrayList<>();
	}
	
	public List<Product> getProductList() {
		return productList;
	}
	//add all the products found on image or shelf
	public void addProductList(Product product) {
		this.productList.add(product);
	}
	public String getShelfId() {
		return shelfId;
	}
	public Image getShelfImage() {
		return shelfImage;
	}

}
=======
package Storage;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

/**
 * Represents one shelf image
 */
public class Shelf {
	private String shelfId;
	private Image shelfImg;
	private List<Product> productList;
	
	public Shelf(String shelfId,Image shelfImage) {
		super();
		this.shelfId = shelfId;
		this.shelfImg = shelfImage;
		this.productList = new ArrayList<>();
	}
	/**
	 * Gives the list of products in this shelf
	 * @return
	 */
	public List<Product> getProductList() {
		
		return productList;
	}
	/**
	 * Adds all the products found on image or shelf
	 * @param product
	 */
	public void addProductList(Product product) {
		this.productList.add(product);
	}
	
	//Getters and setters
	public String getShelfId() {
		return shelfId;
	}
	public Image getShelfImage() {
		return shelfImg;
	}

}
>>>>>>> d9a7cb6 (final changes)
