<<<<<<< HEAD
package Visualisation;

import Storage.Product;
import Storage.TreeGraph;
import Storage.FreshnessLvl;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

/**
 * Visualizes product relationships as an interactive graph using JavaFX.
 * Nodes represent products with color-coding based on freshness levels,
 * and edges represent similarity relationships between products.
 */
public class GraphVisualizer {
    
    private static final double NODE_RADIUS = 20;
    private static final double SPACING = 100;
    
    private final TreeGraph productGraph;
    private final Map<Product, Circle> nodeMap;
    private Pane graphPane;
    private Stage stage;

    /**
     * Constructs a new GraphVisualizer for the specified product graph.
     * @param productGraph The tree graph containing product relationships to visualize
     */
    public GraphVisualizer(TreeGraph productGraph) {
        this.productGraph = productGraph;
        this.nodeMap = new HashMap<>();
    }

    /**
     * Displays the product relationship graph in a new window.
     * The graph layout follows a circular pattern with products of the same
     * type clustered together.
     */
    public void displayGraph() {
        initializeGraphWindow();
        drawGraph();
        stage.show();
    }

    /**
     * Highlights similar products when a node is clicked.
     * @param product The product to highlight and show details for
     */
    public void highlightSimilarProducts(Product product) {
        resetNodeStyles();
        
        Circle selectedNode = nodeMap.get(product);
        if (selectedNode != null) {
            selectedNode.setStroke(Color.BLUE);
            selectedNode.setStrokeWidth(3);
        }
        
        highlightSimilarNodes(product);
        showProductDetails(product);
    }

    private void initializeGraphWindow() {
        stage = new Stage();
        stage.setTitle("Product Similarity Graph");
        
        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);
        
        Scene scene = new Scene(graphPane);
        stage.setScene(scene);
    }

    private void drawGraph() {
        graphPane.getChildren().clear();
        nodeMap.clear();
        
        int productCount = productGraph.getAllProducts().size();
        int index = 0;
        
        for (Product product : productGraph.getAllProducts()) {
            double angle = 2 * Math.PI * index / productCount;
            double x = 400 + 250 * Math.cos(angle);
            double y = 300 + 250 * Math.sin(angle);
            
            createProductNode(product, x, y);
            createSimilarityEdges(product);
            index++;
        }
    }

    private void createProductNode(Product product, double x, double y) {
        Circle node = new Circle(x, y, NODE_RADIUS);
        node.setFill(getFreshnessColor(product.getFresh()));
        node.setStroke(Color.BLACK);
        
        node.setOnMouseEntered(e -> node.setStroke(Color.BLUE));
        node.setOnMouseExited(e -> node.setStroke(Color.BLACK));
        node.setOnMouseClicked(e -> highlightSimilarProducts(product));
        
        Text label = new Text(x - NODE_RADIUS, y - NODE_RADIUS - 5, product.getName());
        
        graphPane.getChildren().addAll(node, label);
        nodeMap.put(product, node);
    }

    private void createSimilarityEdges(Product product) {
        for (Product neighbor : productGraph.getNeighbors(product)) {
            if (nodeMap.containsKey(neighbor)) {
                Line edge = createEdge(nodeMap.get(product), nodeMap.get(neighbor));
                graphPane.getChildren().add(edge);
                edge.toBack();
            }
        }
    }

    private Line createEdge(Circle source, Circle target) {
        Line edge = new Line(
            source.getCenterX(), source.getCenterY(),
            target.getCenterX(), target.getCenterY()
        );
        edge.setStroke(Color.LIGHTGRAY);
        return edge;
    }

    private void resetNodeStyles() {
        nodeMap.values().forEach(node -> {
            node.setStroke(Color.BLACK);
            node.setStrokeWidth(1);
        });
    }

    private void highlightSimilarNodes(Product product) {
        for (Product similar : productGraph.findSimilarProducts(product, 3)) {
            Circle node = nodeMap.get(similar);
            if (node != null) {
                node.setStroke(Color.PURPLE);
                node.setStrokeWidth(2);
            }
        }
    }

    private void showProductDetails(Product product) {
        ProductDetailsDialog.show(product, 
            productGraph.findSimilarProducts(product, 3));
    }

    private Color getFreshnessColor(FreshnessLvl freshness) {
        return switch (freshness) {
            case FRESH -> Color.GREEN;
            case ROTATE -> Color.ORANGE;
            case EXPIRED -> Color.RED;
        };
    }
=======
package Visualisation;

import Storage.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataCalculations.KNN;

public class GraphVisualizer extends Pane {
	
    private final TreeGraph graph;
    private final Map<Product, Point> positions;
    private final Map<Product, Integer> productClusterMap;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private final Canvas canvas;

    /**
     * Displays TreeGraph visually where Products are nodes and similar products are connected with lines
     * @param graph
     * @param clusterMap
     */
    public GraphVisualizer(TreeGraph graph,Map<Product,Integer> ClustMap) {
        this.graph = graph;
        this.positions = generateHierarchicalShelfLayout(graph,WIDTH,150,150); //calculating where each product will go
        this.productClusterMap = ClustMap;

        //initialize canvas
        int TWidth = Math.max(WIDTH,graph.getAllProducts().size()*200);
        int THeight = Math.max(HEIGHT,graph.getAllProducts().size()*100);
        canvas = new Canvas(TWidth,THeight);
        this.setPrefSize(TWidth,THeight);
        this.getChildren().add(canvas);

        //draw the graph
        drawGraph(canvas.getGraphicsContext2D());
    }

    /**
     * Draws everything
     * Edge thickness depends on similarity
     * Nodes are drawn at their (x, y) position
     * @param gc
     */
    private void drawGraph(GraphicsContext gc) {
        gc.setLineWidth(1); //setting the line width
        gc.setStroke(Color.GRAY); //setting color of line

        //track already drawn edges
        List<String> drawnEdges = new ArrayList<>();
        
        //drawing edges (shelf → product,product → similar products)
        for (Product p : positions.keySet()) {
            Point pos1 = positions.get(p); //getting first position (shelf)

            //shelf nodes connect to its products
            if(p.getFeatures() == null && p.getImg() == null){ //this is a dummy shelf node
                for (Product product : graph.getAllProducts()) {
                    if (product.getShelfID().equals(p.getName())) { //checking which products belong to this shelf
                        Point pos2 = positions.get(product);
                        gc.strokeLine(pos1.getX(),pos1.getY(),pos2.getX(),pos2.getY()); //drawing edge line
                    }
                }
            } else { //product nodes connect to similar products
            	//real product: draw edges to similar products (once)
                for (Product neighbor : graph.getNeighbors(p)) {
                    if (!positions.containsKey(neighbor)) continue;

                    String edgeKey = getEdgeKey(p,neighbor);
                    if (!drawnEdges.contains(edgeKey)) {
                        Point pos2 = positions.get(neighbor);
                        gc.strokeLine(pos1.getX(),pos1.getY(),pos2.getX(),pos2.getY());
                        drawnEdges.add(edgeKey);
                    }
                }
            }
        }

        //draw nodes (shelves,products,similar products)
        int radius = 30;
        Color[] ClustColors = {Color.CYAN,Color.MAGENTA,Color.ORANGE,Color.GREEN,Color.PINK,Color.LIGHTGRAY};

        for (Product p : positions.keySet()) {
            Point point = positions.get(p);
            int clusterId = productClusterMap.getOrDefault(p,0); //default to cluster 0
            Color nodeColor = ClustColors[clusterId%ClustColors.length];

            gc.setFill(nodeColor);//fill color based on cluster
            gc.fillOval(point.getX()-radius/2.0,point.getY() - radius/2.0,radius,radius);

            gc.setStroke(Color.BLACK);//border
            gc.strokeOval(point.getX() - radius / 2.0, point.getY() - radius / 2.0,radius,radius);

            gc.setFill(Color.BLACK);//text color
            gc.fillText(p.getName(), point.getX() - radius / 2.0, point.getY() - radius);
        }
    }

    /**
     * Generates a similarity-based layout using a simple force-directed algorithm.
     * @param graph
     * @param panelWidth
     * @param verticalSpacing
     * @param horizontalSpacing
     * @return Map<Product, Point>
     */
    private Map<Product, Point> generateHierarchicalShelfLayout(TreeGraph graph,int PanWidth,int VertSpacing,int HorizSpacing) {
        Map<Product, Point> layout = new HashMap<>();
        Map<String, List<Product>> shelves = new HashMap<>();

        //grouping products by shelf ID
        for(Product product : graph.getAllProducts()){
            shelves.computeIfAbsent(product.getShelfID(), k -> new ArrayList<>()).add(product);
        }

        int x = 50; //start drawing from the left
        int yShelf = 50; // first row for shelves

        int count = 0;
        //layout each shelf and its tree
        for (String shelfID : shelves.keySet()) {
        	count++;
            List<Product> shelfProducts = shelves.get(shelfID);

            //creating a dummy shelf node 
            Product shelfDummy = new Product(shelfID,null,null,null,null,null); //only used for display
            Point shelfPos = new Point(x,yShelf);
            layout.put(shelfDummy,shelfPos); //placed at the top of the diagram
            List<Product> BelowSimmilar = new ArrayList<>();
            int xProduct = x;
            int yProduct = yShelf+VertSpacing; //position each product below the shelf node
            //going through each product in a shelf
            for (Product product : shelfProducts) {
            	//Skipping arrangement of products which are similar since they have been arranged
            	for(Product BS:BelowSimmilar) {
            		if(BS.equals(product)) {
            			continue;
            		}
            	}
                //storing the position for each product under the shelf
                Point productPos = new Point(xProduct,yProduct);
                layout.put(product, productPos);
                //layout similar products under each product
                List<Product> similars = graph.getNeighbors(product);
                int xSim = xProduct - ((similars.size()-1)*HorizSpacing/2); //calculating starting x-position for similar products
                int ySim = yProduct + VertSpacing; //go down a depth
                //placing similar products underneath each product
                for (Product sim : similars) {
                	double distance = KNN.computeDistance(product, sim);
                	if (distance <= 2){
                            layout.put(sim,new Point(xSim,ySim));
                            xSim += HorizSpacing;
                            BelowSimmilar.add(sim);    
                	}
                }
                xProduct += HorizSpacing; //spacing to the next product
            }
            x +=shelfProducts.size()*HorizSpacing+100; //move to next shelf group
        }
        return layout;
    }
    
    private String getEdgeKey(Product a, Product b) {
        String name1 = a.getName();
        String name2 = b.getName();
        return name1.compareTo(name2) < 0 ? name1 + "-" + name2 : name2 + "-" + name1;
    }
>>>>>>> d9a7cb6 (final changes)
}