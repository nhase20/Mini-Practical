<<<<<<< HEAD
package Visualisation;

import Storage.DataManager;
import Storage.Date;
import Storage.Features;
import Storage.FreshnessLvl;
import Storage.Product;
import Storage.Shelf;
import Storage.TreeGraph;
import DataCalculations.FreshCalculator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * The main GUI controller for the supermarket inventory visualization system.
 * Handles user interactions, image loading, and coordinates between visualization components.
 */
public class GuiHandler extends Application {
    private TreeGraph productGraph;
    private GraphVisualizer graphVisualizer;
    private Shelf currentShelf;
    private Product selectedProduct;
    
    private ImageView shelfImageView;
    private Label statusLabel;
    private Rectangle selectionRect;
    private double startX, startY;
    
    private Menu fileMenu;
	private Stage mainStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.productGraph = new TreeGraph(new HashMap<>());
        this.graphVisualizer = new GraphVisualizer(productGraph);
        this.mainStage = primaryStage;
        primaryStage.setTitle("Supermarket Inventory Visualizer");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        //Menu Bar
        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu("File");
        MenuItem loadImageItem = new MenuItem("Load Shelf Image");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(loadImageItem, new SeparatorMenuItem(), exitItem);
        
        Menu viewMenu = new Menu("View");
        MenuItem showGraphItem = new MenuItem("Show Product Graph");
        viewMenu.getItems().add(showGraphItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        root.setTop(menuBar);
        
        // Image View
        shelfImageView = new ImageView();
        shelfImageView.setPreserveRatio(true);
        shelfImageView.setSmooth(true);
        shelfImageView.setCache(true);
        
        ScrollPane imageScrollPane = new ScrollPane(shelfImageView);
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setFitToHeight(true);
        root.setCenter(imageScrollPane);
        
        // Status Bar
        statusLabel = new Label("Ready");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);
        
        // Event Handlers
        loadImageItem.setOnAction(e -> loadShelfImage(primaryStage));
        
        showGraphItem.setOnAction(e -> {
            if (productGraph.getAllProducts().isEmpty()) {
                showAlert("No Products", "Please load data first", Alert.AlertType.WARNING);
            } else {
                graphVisualizer.displayGraph();
            }
        });
        exitItem.setOnAction(e -> Platform.exit());
        
        // Image Click Handler
        shelfImageView.setOnMouseClicked(e -> {
            if (currentShelf != null && !currentShelf.getProductList().isEmpty()) {
                int randomIndex = (int)(Math.random() * currentShelf.getProductList().size());
                selectedProduct = currentShelf.getProductList().get(randomIndex);
                statusLabel.setText("Selected: " + selectedProduct.getName());
                
                List<Product> similarProducts = productGraph.findSimilarProducts(selectedProduct, 3);
                ProductDetailsDialog.show(selectedProduct, similarProducts);
            }
        });
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        setupDataPersistence();
        setupImageCropping();
    }
    /**
     * functionality for adding shelf image 
     * @param primaryStage - the stage used for showing all layout elements
     */
    private void loadShelfImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Shelf Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Image shelfImage = new Image(selectedFile.toURI().toString());
                shelfImageView.setImage(shelfImage);
                
                currentShelf = new Shelf("Shelf-" + selectedFile.getName(), shelfImage);
                statusLabel.setText("Loaded: " + selectedFile.getName());
                
                addProductsToGraph(currentShelf.getProductList());
                
            } catch (Exception ex) {
                statusLabel.setText("Error loading image: " + ex.getMessage());
                showAlert("Error", "Error loading image", Alert.AlertType.ERROR);
            }
        }
    }
    
    
    /**
     * helper method for setting up image cropping
     */
    private void setupImageCropping() {
        shelfImageView.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
            selectionRect = new Rectangle(startX, startY, 0, 0);
            selectionRect.setStroke(Color.BLUE);
            selectionRect.setStrokeWidth(2);
            selectionRect.setFill(Color.color(0, 0, 1, 0.1));
            Pane imagePane = (Pane) shelfImageView.getParent();
            imagePane.getChildren().add(selectionRect);
        });

        shelfImageView.setOnMouseDragged(e -> {
            if (selectionRect != null) {
                double width = e.getX() - startX;
                double height = e.getY() - startY;
                selectionRect.setWidth(Math.abs(width));
                selectionRect.setHeight(Math.abs(height));
                selectionRect.setX(width < 0 ? e.getX() : startX);
                selectionRect.setY(height < 0 ? e.getY() : startY);
            }
        });

        shelfImageView.setOnMouseReleased(e -> {
            if (selectionRect != null) {
                // Get cropped image
                Image cropped = ImageProcessor.cropImage(
                    shelfImageView.getImage(),
                    (int) selectionRect.getX(),
                    (int) selectionRect.getY(),
                    (int) selectionRect.getWidth(),
                    (int) selectionRect.getHeight()
                );
                
                // Create new product from cropped region
                createProductFromCrop(cropped);
                
                // Remove selection rectangle
                Pane imagePane = (Pane) shelfImageView.getParent();
                imagePane.getChildren().remove(selectionRect);
            }
        });
    }
    
    /**
     * helper method fr updating product fresshnes
     */
    private void updateProductFreshness() {
        if (currentShelf == null) return;
        
        LocalDate today = LocalDate.now();
        for (Product product : currentShelf.getProductList()) {
            Date expiry = product.getExpiryDate();
            LocalDate expiryDate = LocalDate.of(
                expiry.getYear(), 
                expiry.getMonth(), 
                expiry.getDay()
            );
            
            FreshnessLvl freshness = FreshCalculator.calculate(today, expiryDate);
            product.setFresh(freshness);
        }
        
        if (graphVisualizer != null) {
            graphVisualizer.displayGraph();
        }
    }
    
    /**
     * method for creating product from crop
     * @param cropped - the cropped image
     */

    private void createProductFromCrop(Image cropped) {
        // Show dialog to enter product details
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Product");
        dialog.setHeaderText("Enter product details");
        dialog.setContentText("Product Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            DatePicker expiryPicker = new DatePicker();
            expiryPicker.setPromptText("Expiry Date");
            
            GridPane grid = new GridPane();
            grid.add(new Label("Expiry Date:"), 0, 0);
            grid.add(expiryPicker, 1, 0);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Product");
            alert.setHeaderText("Set expiry date for " + name);
            alert.getDialogPane().setContent(grid);
            
            Optional<ButtonType> expiryResult = alert.showAndWait();
            expiryResult.ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Features features = ImageProcessor.extractFeatures(cropped);
                    Date expiryDate = new Date(
                        expiryPicker.getValue().getYear(),
                        expiryPicker.getValue().getMonthValue(),
                        expiryPicker.getValue().getDayOfMonth()
                    );
                    
                    Product newProduct = new Product(
                        name, 
                        features, 
                        currentShelf, 
                        cropped, 
                        expiryDate, 
                        FreshnessLvl.FRESH
                    );
                    
                    currentShelf.addProductList(newProduct);
                    productGraph.addProduct(newProduct);
                }
            });
        });
    }
    
    
    /**
     * method for setting up data persistence
     */
    private void setupDataPersistence() {
        // Add to menu
        MenuItem saveItem = new MenuItem("Save Data");
        MenuItem loadItem = new MenuItem("Load Data");
        fileMenu.getItems().addAll(new SeparatorMenuItem(), saveItem, loadItem);
        
        saveItem.setOnAction(e -> saveData());
        loadItem.setOnAction(e -> loadData());
    }

    private void saveData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shelf Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        File file = fileChooser.showSaveDialog(mainStage);
        
        if (file != null) {
            try {
                DataManager.saveShelf(currentShelf, file.getAbsolutePath());
                statusLabel.setText("Data saved to " + file.getName());
            } catch (Exception ex) {
                showAlert("Save Error", "Failed to save data: " + ex.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        }
    }

    private void loadData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Shelf Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        File file = fileChooser.showOpenDialog(mainStage);
        
        if (file != null) {
            try {
                currentShelf = DataManager.loadShelf(file.getAbsolutePath());
                shelfImageView.setImage(currentShelf.getShelfImage());
                addProductsToGraph(currentShelf.getProductList());
                statusLabel.setText("Data loaded from " + file.getName());
            } catch (Exception ex) {
                showAlert("Load Error", "Failed to load data: " + ex.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        }
    }

    // Update DataManager.java
    public static void saveShelf(Shelf shelf, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(filePath))) {
            oos.writeObject(shelf);
        }
    }

    public static Shelf loadShelf(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(filePath))) {
            return (Shelf) ois.readObject();
        }
    }
    
    private void addProductsToGraph(List<Product> products) {
        productGraph = new TreeGraph(new HashMap<>());
        graphVisualizer = new GraphVisualizer(productGraph);
        
        for (Product product : products) {
            productGraph.addProduct(product);
        }
        
        for (int i = 0; i < products.size(); i++) {
            for (int j = i + 1; j < products.size(); j++) {
                Product p1 = products.get(i);
                Product p2 = products.get(j);
                
                if (p1.getName().equals(p2.getName())) {
                    productGraph.addEdge(p1, p2);
                }
            }
        }
        
        statusLabel.setText("Added " + products.size() + " products to graph");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void launchApp() {
        launch();
    }
=======
package Visualisation;

import DataCalculations.FreshCalculator;
import DataCalculations.GroupClust;
import Memory.ManageData;
import Storage.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GuiHandler extends Application {
	private ComboBox<Product> productSelector;
    private ImgPanel imagePanel;
    private ManageData dataManager;
    private TextField nameField, shelfField, EyearField, EmonthField, EdayField, TyearField, TmonthField, TdayField;
    private Image loadedImage,originalImage;
    private String ImgPath;
    private Date today;
    private GroupClust Gclust;

    @Override
    public void start(Stage primaryStage) {
        dataManager = new ManageData();
        primaryStage.setTitle("Supermarket Product Manager");
        primaryStage.setScene(new Scene(createContent(),1000,500));
        primaryStage.show();
    }

    private BorderPane createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(createInputPanel());
        root.setCenter(imagePanel = new ImgPanel());
        return root;
    }

    private GridPane createInputPanel() {
    	//creating panel 
        GridPane inputPanel = new GridPane();
        inputPanel.setPadding(new Insets(10));
        inputPanel.setHgap(10);
        inputPanel.setVgap(10);
        
        //fields for input
        nameField = new TextField();
        shelfField = new TextField();
        TyearField = new TextField();
        TmonthField = new TextField();
        TdayField = new TextField();
        EyearField = new TextField();
        EmonthField = new TextField();
        EdayField = new TextField();
        
        //new dropdown for selecting products
        productSelector = new ComboBox<>();
        productSelector.setPromptText("Select a product");
        dataManager.getAllProducts().forEach(productSelector.getItems()::add);
        productSelector.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        productSelector.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);  // show prompt text here
                } else {
                    setText(item.getName());
                }
            }
        });
        
        //buttons to press for action
        Button loadImageBtn = new Button("Load Image");
        Button cropBtn = new Button("Crop & Analyze");
        Button removeExpiredBtn = new Button("Delete Expired Products");
        Button addBtn = new Button("Add Product");
        Button showGraphBtn = new Button("Show Graph");
        Button saveBtn = new Button("Save Data");
        Button loadBtn = new Button("Load Data");
        Button viewFreshnessGridBtn = new Button("View Freshness");
        Button rotationBtn = new Button("Rotation"); 
        Button resetImageBtn = new Button("Reset Image");
        Button findSimilarBtn = new Button("Find Similar Products");
        
        //actions of each button
        loadImageBtn.setOnAction(e -> loadImage());
        cropBtn.setOnAction(e -> cropImage());
        addBtn.setOnAction(e -> addProduct());
        showGraphBtn.setOnAction(e -> showGraph());
        saveBtn.setOnAction(e -> saveData());
        loadBtn.setOnAction(e -> loadData());
        viewFreshnessGridBtn.setOnAction(e -> showFreshnessGrid());
        resetImageBtn.setOnAction(e -> resetImage());
        rotationBtn.setOnAction(e -> showRotationRecommendations());
        findSimilarBtn.setOnAction(e -> showSimilarProducts(productSelector.getValue()));
        removeExpiredBtn.setOnAction(e -> removeExpiredProducts());
        
        //input being taken in
        inputPanel.add(new Label("Product Name:"),0,0);
        inputPanel.add(nameField,1,0);
        inputPanel.add(new Label("Shelf ID:"),0,1);
        inputPanel.add(shelfField,1,1);
        inputPanel.add(new Label("Today Date(YYYY MM DD):"),0,2);
        inputPanel.add(TyearField,1,2);
        inputPanel.add(TmonthField,2,2);
        inputPanel.add(TdayField,3,2);
        inputPanel.add(new Label("Expiry Date(YYYY MM DD):"),0,3);
        inputPanel.add(EyearField,1,3);
        inputPanel.add(EmonthField,2,3);
        inputPanel.add(EdayField,3,3);
        inputPanel.add(addBtn,0,4);
        inputPanel.add(cropBtn,1,4);
        inputPanel.add(loadImageBtn,2,4);
        inputPanel.add(showGraphBtn,0,5);
        inputPanel.add(saveBtn,1,5);
        inputPanel.add(loadBtn,2,5);
        inputPanel.add(viewFreshnessGridBtn,0,6);
        inputPanel.add(rotationBtn,1,6);
        inputPanel.add(resetImageBtn,2,6);
        inputPanel.add(findSimilarBtn,0,7);
        inputPanel.add(removeExpiredBtn, 1, 7);
        
        return inputPanel;
    }

    /**
     * Resets image after zooming
     */
    private void resetImage() {
        if (originalImage != null) {
            loadedImage = new WritableImage(originalImage.getPixelReader(),
            		(int)originalImage.getWidth(),(int)originalImage.getHeight());
            imagePanel.setImage(loadedImage);
        } else {
            showError("No image to reset.");
        }
    }

    /**
     * Loads an image from user's files
     */
    private void loadImage() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            ImgPath = file.getAbsolutePath();
            try {
                originalImage = new Image(file.toURI().toString());
                loadedImage = new WritableImage(originalImage.getPixelReader(),(int)originalImage.getWidth(),(int)originalImage.getHeight());
                imagePanel.setImage(loadedImage);
            } catch (Exception ex) {
                showError("Failed to load image.");
            }
        }
    }

    /**
     * Crops image and captures dimensions/size of image
     */
    private void cropImage() {
        try {
            Rectangle r = imagePanel.getSelection();
            if (r == null || r.getWidth() == 0 || r.getHeight() == 0) {
                showError("Please draw a region to crop.");
                return;
            }
            Image cropped = ImageProcessor.cropImage(loadedImage,(int)r.getX(),(int)r.getY(),(int)r.getWidth(),(int)r.getHeight());
            loadedImage = cropped;
            imagePanel.setImage(cropped);
        } catch (Exception ex) {
            showError("Invalid crop parameters.");
        }
    }

    /**
     * adds a product to list of products in shelves
     */
    private void addProduct() {
        try {
        	if (nameField.getText().isEmpty()||shelfField.getText().isEmpty()) {
                showError("Name and Shelf ID are required.");
                return;
            }
            String name = nameField.getText();
            String shelf = shelfField.getText();
            int tyear = Integer.parseInt(TyearField.getText());
            int tmonth = Integer.parseInt(TmonthField.getText());
            int tday = Integer.parseInt(TdayField.getText());
            int eyear = Integer.parseInt(EyearField.getText());
            int emonth = Integer.parseInt(EmonthField.getText());
            int eday = Integer.parseInt(EdayField.getText());
            Date expiry = new Date(eyear, emonth, eday);
            today = new Date(tyear, tmonth, tday);
            Features feats = ImageProcessor.extractFeatures(loadedImage);
            List<Product> similar = dataManager.findSimilarTo(new Product(name,feats,new Shelf(shelf,null),null,null,null));
            if (!similar.isEmpty()) {
                Product suggested = similar.get(0);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Suggested match: " + suggested.getName() + ". Accept?");
                if (alert.showAndWait().get() == ButtonType.YES) {
                    // Create a temporary product for similarity comparison
                    Product tempProduct = new Product(name, feats, new Shelf(shelf,loadedImage),loadedImage,expiry, 
                                                     FreshCalculator.getFreshnessLevel(today,expiry));
                    tempProduct.setImagePath(ImgPath);
                    // Add similarity edge
                    dataManager.addSimilarity(tempProduct, suggested);
                    // Update suggested product’s metadata
                    suggested.setFresh(FreshCalculator.getFreshnessLevel(today,expiry));
                    showMessage("Product matched and similarity updated: " + suggested.getName());
                    return;
                }
            }
            Product product = new Product(name,feats,new Shelf(shelf,loadedImage),loadedImage,expiry,FreshCalculator.getFreshnessLevel(today,expiry));
            product.setImagePath(ImgPath);
            dataManager.addProduct(product);
            productSelector.getItems().setAll(dataManager.getAllProducts());
            productSelector.setValue(product);
            showMessage("Product added.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to add product.");
        }
    }

    /**
     * Shows node/edge graph
     */
    private void showGraph() {
    	Gclust = new GroupClust(5,dataManager.getAllProducts());
        Stage graphStage = new Stage();
        graphStage.setTitle("Graph");
        Map<Product, Integer> clusterMap = new HashMap<>();
        for (Map.Entry<Integer, List<Product>> entry : Gclust.getClusters().entrySet()) {
            for (Product p : entry.getValue()) {
                clusterMap.put(p,entry.getKey());
            }
        }
        GraphVisualizer visual = new GraphVisualizer(dataManager.getGraph(),clusterMap);
        ScrollPane scrollPane = new ScrollPane(visual);
        scrollPane.setPrefSize(800, 600);
        Scene scene = new Scene(scrollPane);
        graphStage.setScene(scene);
        graphStage.setWidth(850);   // You can adjust as needed
        graphStage.setHeight(650);  // You can adjust as needed
        graphStage.setResizable(true); // Optional, allows user resizing
        graphStage.centerOnScreen();   // Optional, centers on screen
        graphStage.show();
    }

    /**
     * Saves the list of products and shelves if needed for use at a later stage
     */
    private void saveData() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try {
                dataManager.saveToCSV(file.getPath());
                showMessage("Data saved.");
            } catch (IOException ex) {
                showError("Failed to save.");
            }
        }
    }

    /**
     * loads saved data that can be reused
     */
    private void loadData() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                dataManager.loadFromCSV(file.getPath());
                productSelector.getItems().setAll(dataManager.getAllProducts());
                showMessage("Data loaded.");
            } catch (IOException ex) {
                showError("Failed to load.");
            }
        }
    }

    /**
     * Utility that displays message given
     * @param msg
     */
    private void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Utility shows error given
     */
    private void showError(String err) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(err);
        alert.showAndWait();
    }

    /**
     * Shows which products need to be changed 
     */
    private void showFreshnessGrid() {
    	
        if (today == null) {
            showError("Please add at least one product so the system can track today's date.");
            return;
        }
        
      //makingake sure freshness status is updated before grouping
        FreshCalculator.updateFreshnessStatus(dataManager.getAllProducts(), today);


        Map<FreshnessLvl, List<Product>> groups = dataManager.groupByFreshness();
        for (FreshnessLvl lvl : groups.keySet()) {
            groups.get(lvl).sort((p1, p2) -> {
                int d1 = Date.ExpiryDate(today,p1.getExpiryDate());
                int d2 = Date.ExpiryDate(today,p2.getExpiryDate());
                return Integer.compare(d1,d2);
            });
        }

        GridPane gridPanel = new GridPane();
        gridPanel.setHgap(10);
        gridPanel.setVgap(5);
        gridPanel.setPadding(new Insets(10));

        int col = 0;
        for (FreshnessLvl lvl : FreshnessLvl.values()) {
            Label header = new Label(lvl.name());
            header.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-background-color:lightgray;-fx-padding: 5;");
            header.setAlignment(Pos.CENTER);
            gridPanel.add(header,col++, 0);
        }

        for (int row = 1; row <= 5; row++) {
            col = 0;
            for (FreshnessLvl lvl : FreshnessLvl.values()) {
                List<Product> list = groups.getOrDefault(lvl,new ArrayList<>());
                if (row - 1 < list.size()) {
                    Product p = list.get(row - 1);
                    HBox cell = new HBox(5);
                    ImageView imgView = new ImageView(p.getImg());
                    imgView.setFitWidth(50);
                    imgView.setFitHeight(50);
                    Label productLabel = new Label(p.getName() + " (" + Date.ExpiryDate(today,p.getExpiryDate()) + " days)");
                    productLabel.setAlignment(Pos.CENTER);
                    switch (lvl) {
                        case FRESH: productLabel.setTextFill(Color.GREEN); break;
                        case ROTATE: productLabel.setTextFill(Color.ORANGE); break;
                        case SHELF_STABLE: productLabel.setTextFill(Color.BLUE); break;
                        case EXPIRED: productLabel.setTextFill(Color.RED); break;
                    }
                    cell.getChildren().addAll(imgView,productLabel);
                    gridPanel.add(cell,col++,row);
                } else {
                    gridPanel.add(new Label(""),col++,row);
                }
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Top 5 Products by Freshness Level");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(gridPanel);
        alert.showAndWait();
    }
    
    /**
     * Recommends which products to change
     */
    private void showRotationRecommendations() {
        List<Product> rotatables = FreshCalculator.getRotatableProducts(dataManager.getAllProducts());
        List<Product> expired = FreshCalculator.getExpiredProducts(dataManager.getAllProducts());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rotation Recommendations");
        StringBuilder sb = new StringBuilder();
        sb.append("Rotate Soon:\n");
        rotatables.forEach(p -> sb.append(p.getName()).append(" (").append(Date.ExpiryDate(today, p.getExpiryDate())).append(" days)\n"));
        sb.append("\nExpired:\n");
        expired.forEach(p -> sb.append(p.getName()).append("\n"));
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
    private void showSimilarProducts(Product selected) {
    	System.out.println("Selected product: " + selected.getName());
        if (selected == null){
            showError("Please select a product.");
            return;
        }
        List<Product> similar = dataManager.findSimilarTo(selected);
        if (similar.isEmpty()) {
            showMessage("No similar products found.");
            return;
        }

        // Calculate optimal k for display
        int optimalK = DataCalculations.KNN.findOptimalK(dataManager.getGraph().getNeighbors(selected), 
                                                        Math.min(10, dataManager.getGraph().getNeighbors(selected).size()));

        VBox resultPanel = new VBox(10);
        resultPanel.setPadding(new Insets(10));
        resultPanel.getChildren().add(new Label("Similar Products (Optimal k = " + optimalK + "):"));

        for (Product p : similar) {
            HBox productBox = new HBox(10);
            ImageView imgView = new ImageView(p.getImg());
            imgView.setFitWidth(50);
            imgView.setFitHeight(50);
            double distance = DataCalculations.KNN.computeDistance(selected, p);
            Label info = new Label(String.format("%s (Distance: %.2f, Shelf: %s)", 
                                                p.getName(), distance, p.getShelfID()));
            productBox.getChildren().addAll(imgView, info);
            resultPanel.getChildren().add(productBox);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Similar Products");
        alert.setHeaderText("Products similar to " + selected.getName());
        alert.getDialogPane().setContent(resultPanel);
        alert.showAndWait();
    }
    
    /**
     * Method to remove expired products from store.
     * 
     */
    private void removeExpiredProducts() {
        if (today == null) {
            showError("Please enter today's date before performing this action.");
            return;
        }

       //making sure all products have updated freshness
        FreshCalculator.updateFreshnessStatus(dataManager.getAllProducts(), today);
        List<Product> expired = FreshCalculator.getExpiredProducts(dataManager.getAllProducts());
        if (expired.isEmpty()) {
            showMessage("No expired products found.");
            return;
        }

        // Confirm before removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Remove Expired Products");
        confirm.setContentText("Are you sure you want to remove " + expired.size() + " expired products?");
        if (confirm.showAndWait().get() != ButtonType.OK) {
            return;
        }

        // Remove from data
        expired.forEach(dataManager::removeProduct);
        productSelector.getItems().setAll(dataManager.getAllProducts());
        showMessage(expired.size() + " expired products removed.");
    }
>>>>>>> d9a7cb6 (final changes)
}