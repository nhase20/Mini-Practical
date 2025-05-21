package Visualisation;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * JavaFx panel class for image handling
 */
public class ImgPanel extends Pane{
	private ImageView ImgView;
    private Rectangle selection;
    private double startX,startY;

    public ImgPanel() {
    	setPrefSize(300, 300);//setting panel size
        setStyle("-fx-background-color: lightgray;");

        ImgView = new ImageView();//used to display image
        ImgView.setPreserveRatio(true);//ensuring product images avoiding stretching or squashing
        ImgView.setSmooth(true);//when the image is resized,it improves visual quality
        ImgView.setCache(true);//improve performance during animations or frequent redraws

        //binding to parent size
        ImgView.fitWidthProperty().bind(widthProperty());
        ImgView.fitHeightProperty().bind(heightProperty());

        getChildren().add(ImgView);

        //creating the selection rectangle we use to zoom 
        selection = new Rectangle();
        selection.setStroke(Color.RED);
        selection.setStrokeWidth(1);
        selection.setFill(Color.TRANSPARENT);
        selection.setVisible(false);
        getChildren().add(selection);

        // Mouse pressed - start drawing on image
        setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
            selection.setX(startX);
            selection.setY(startY);
            selection.setWidth(0);
            selection.setHeight(0);
            selection.setVisible(true);
        });

        // Mouse dragged - update selection size
        setOnMouseDragged(e -> {
            double width = Math.abs(e.getX() - startX);
            double height = Math.abs(e.getY() - startY);
            selection.setX(Math.min(startX, e.getX()));
            selection.setY(Math.min(startY, e.getY()));
            selection.setWidth(width);
            selection.setHeight(height);
        });

        // Mouse released - finalize selection
        setOnMouseReleased(MouseEvent::consume); //you can handle further actions here
    }

    /**
     * Sets new image
     * @param img
     */
    public void setImage(Image img) {
    	ImgView.setImage(img);
        ImgView.setPreserveRatio(true);
        ImgView.setSmooth(true);
        ImgView.setCache(true);
    }

    /**
     * Method  converts a mouse-drawn rectangle (on the image) into a rectangle in image pixel coordinates
     * @return
     */
    public Rectangle getSelection() {
    	//if the user hasn’t drawn a selection, or if no image is loaded, there’s nothing to crop
    	if (!selection.isVisible() || ImgView.getImage() == null) {
    		return null;
    	}

        Image img = ImgView.getImage();
        //getting the real pixel size of the image
        double Iwidth = img.getWidth();
        double Iheight = img.getHeight();
        //getting the size of the ImageView
        double Vwidth = ImgView.getBoundsInParent().getWidth();
        double Vheight = ImgView.getBoundsInParent().getHeight();
        //determining ratios of the image and the panel
        double imgRatio = Iwidth / Iheight;
        double viewRatio = Vwidth / Vheight;

        //calculating how many pixels of the panel the image actually takes up so it doesn't fill the ImaeView
        double displWidth = Vwidth;
        double displHeight = Vheight;
        if (imgRatio > viewRatio) {
            displHeight = Vwidth / imgRatio;
        } else {
        	displWidth = Vheight * imgRatio;
        }
        //setting for padding
        double offX = (Vwidth-displWidth) / 2;
        double offY = (Vheight-displHeight) / 2;
        //how many actual image pixels correspond to 1 on-screen pixel (scaling)
        double ratioX = Iwidth/displWidth;
        double ratioY = Iheight/displHeight;
        //adjusting coordinates 
        double selX = selection.getX()-offX;
        double selY = selection.getY()-offY;
        //converting from on-screen selection rectangle to actual image pixels
        int imgX = (int) Math.max(0,(selX*ratioX));
        int imgY = (int) Math.max(0,(selY*ratioY));
        int imgW = (int) Math.min((Iwidth-imgX),selection.getWidth()*ratioX);
        int imgH = (int) Math.min(Iheight-imgY,selection.getHeight()*ratioY);

        return new Rectangle(imgX,imgY,imgW,imgH);
    }

     /**
      * Just a utility method that hides the red selection rectangle the user draws over the image
      */
    public void clearSelection() {
    	selection.setVisible(false);
    }
    
}
