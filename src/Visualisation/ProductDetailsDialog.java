package Visualisation;

import Storage.Product;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
/**
 * A dialog window for displaying detailed information about a product
 * and its similar products.
 */
public class ProductDetailsDialog {
	 
    /**
     * Shows a modal dialog with product details.
     * @param product The product to display
     * @param similarProducts List of similar products
     */
    public static void show(Product product, List<Product> similarProducts) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Product Details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Product details
        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(product.getName()), 1, 0);
        
        grid.add(new Label("Shelf ID:"), 0, 1);
        grid.add(new Label(product.getShelfID()), 1, 1);
        
        grid.add(new Label("Freshness:"), 0, 2);
        grid.add(new Label(product.getFresh().toString()), 1, 2);
        
        // Similar products
        grid.add(new Label("Similar Products:"), 0, 3);
        
        ListView<String> similarList = new ListView<>();
        for (Product similar : similarProducts) {
            similarList.getItems().add(String.format("%s (%s)", 
                similar.getName(), similar.getFresh()));
        }
        grid.add(similarList, 0, 4, 2, 1);
        
        Scene scene = new Scene(grid, 400, 300);
        dialog.setScene(scene);
        dialog.show();
    }
}