<<<<<<< HEAD
package Visualisation;

import Storage.Features;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class ImageProcessor {
	/**
     * Extracts visual features from a product image.
     * @param image The product image to analyze
     * @return Features object containing color histogram, average gray value, and dimensions
     * @throws IllegalArgumentException if the image is null
     */
    public static Features extractFeatures(Image image) {
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int pixelCount = width * height;
        
        double totalGray = 0;
        double[] rgbHistogram = new double[12]; // 4 bins for each R, G, B
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                
                // Grayscale
                double gray = 0.299 * color.getRed() * 255 + 
                             0.587 * color.getGreen() * 255 + 
                             0.114 * color.getBlue() * 255;
                totalGray += gray;
                
                // Histogram
                int rBin = (int) (color.getRed() * 3);     // 0-3
                int gBin = (int) (color.getGreen() * 3);   // 0-3
                int bBin = (int) (color.getBlue() * 3);    // 0-3
                rgbHistogram[rBin]++;
                rgbHistogram[4 + gBin]++;
                rgbHistogram[8 + bBin]++;
            }
        }
        
        // Normalize histogram
        for (int i = 0; i < rgbHistogram.length; i++) {
            rgbHistogram[i] /= pixelCount;
        }
        
        double avgGray = totalGray / pixelCount;
        return new Features(avgGray, rgbHistogram, width, height);
    }
    
    /**
     * Crops a rectangular region from an image.
     * @param original The source image to crop from
     * @param x The x-coordinate of the top-left corner
     * @param y The y-coordinate of the top-left corner
     * @param width The width of the crop region
     * @param height The height of the crop region
     * @return The cropped image
     * @throws IllegalArgumentException if crop dimensions are invalid
     */
    public static WritableImage cropImage(Image original, int x, int y, int width, int height) {
        validateCropDimensions(original, x, y, width, height);
        
        WritableImage cropped = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = cropped.getPixelWriter();
        
        for (int cropY = 0; cropY < height; cropY++) {
            for (int cropX = 0; cropX < width; cropX++) {
                writer.setColor(cropX, cropY, 
                    reader.getColor(x + cropX, y + cropY));
            }
        }
        return cropped;
    }
    
    
    private static void validateCropDimensions(Image image, int x, int y, int w, int h) {
        if (x < 0 || y < 0 || w <= 0 || h <= 0 ||
            x + w > image.getWidth() || y + h > image.getHeight()) {
            throw new IllegalArgumentException(
                String.format("Invalid crop dimensions: x=%d, y=%d, w=%d, h=%d", x, y, w, h));
        }
    }
    
    
    private static void updateHistogram(double[] histogram, Color color) {
        int rBin = (int) (color.getRed() * 3);
        int gBin = (int) (color.getGreen() * 3) + 4;
        int bBin = (int) (color.getBlue() * 3) + 8;
        
        histogram[rBin]++;
        histogram[gBin]++;
        histogram[bBin]++;
    }

    private static double calculateLuminance(Color color) {
        return 0.299 * color.getRed() * 255 + 
               0.587 * color.getGreen() * 255 + 
               0.114 * color.getBlue() * 255;
    }

    private static void normalizeHistogram(double[] histogram, int pixelCount) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= pixelCount;
        }
    }

    
    
}
=======
package Visualisation;
import Storage.Features;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import java.io.IOException;
/**
 * Just read an image and handles image loading and feature extraction
 * Make sure GUI has input fields for the values of crop and a "Crop" button.
 */
public class ImageProcessor {
	//All images will be resized to this scale to help improve feature extraction
	public static final int RESIZED_WIDTH = 100;
	public static final int RESIZED_HEIGHT = 100;
	
	/**
	 * Crops the given image to a specified region.
	 * @param img The original BufferedImage
	 * @param x The x coordinate of the top-left corner
	 * @param y The y coordinate of the top-left corner
	 * @param width The width of the cropped area
	 * @param height The height of the cropped area
	 * @return Cropped BufferedImage
	 */
	public static Image cropImage(Image img,int x,int y,int width,int height) {
		// Ensure crop box is within bounds
        int maxX = Math.min(x+width,(int) img.getWidth());
        int maxY = Math.min(y+height,(int) img.getHeight());
        int safeWidth = maxX - x;
        int safeHeight = maxY - y;

        PixelReader reader = img.getPixelReader();
        WritableImage cropped = new WritableImage(safeWidth,safeHeight);
        PixelWriter writer = cropped.getPixelWriter();

        for (int i = 0; i<safeWidth; i++) {
            for (int j = 0; j<safeHeight; j++) {
                writer.setColor(i,j,reader.getColor(x+i,(y+j)));
            }
        }
        return cropped;
	}
	/**
     * Loads an image from file path and extracts its features.
     * @param imagePath path to the image file
     * @return Features object containing image properties
     * @throws IOException if image cannot be read
     */
    public static Features extractFeatures(Image img) throws IOException {
        //resize to fixed dimensions
        img = resizeImage(img);
        
        int width =(int) img.getWidth();
        int height =(int) img.getHeight();
        double avgGray = 0;
        double[] rgbHist =new double[256]; // simplistic grayscale histogram

        long TGray = 0;
        int[] hist = new int[256];
        PixelReader reader = img.getPixelReader();
      
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
            	javafx.scene.paint.Color color = reader.getColor(x, y);
                // Getting each color component (0-1 range, convert to 0-255)
                int red = (int) (color.getRed() * 255);
                int green = (int) (color.getGreen() * 255);
                int blue = (int) (color.getBlue() * 255);

                int gray = (red+green+blue) / 3; // Average color
                hist[gray]++; // Forming color histogram
                TGray += gray;
            }
        }
        avgGray = TGray/(double)(width*height);
        //Normalize histogram to get frequency distribution
        for (int i = 0; i<256; i++) {
        	rgbHist[i] = hist[i]/(double)(width*height);
        }
        Features feats = new Features(avgGray,rgbHist,width,height); 
        return feats;
    }
    /**
     * Resizes an image to the specified width and height
     * @param originalImage
     * @return
     */
    public static Image resizeImage(Image Img) {
    	int OWidth = (int)Img.getWidth();
        int OHeight = (int)Img.getHeight();
        WritableImage resizedImage = new WritableImage(RESIZED_WIDTH,RESIZED_HEIGHT);
        PixelReader reader = Img.getPixelReader();
        PixelWriter writer = resizedImage.getPixelWriter();

        for (int y = 0; y<RESIZED_HEIGHT; y++) {
            for (int x = 0; x<RESIZED_WIDTH; x++) {
                // Calculate the corresponding source coordinates
                int srcX = (int)((x/(double)RESIZED_WIDTH)*OWidth);
                int srcY = (int)((y/(double)RESIZED_HEIGHT)*OHeight);
                writer.setColor(x,y,reader.getColor(srcX,srcY));
            }
        }
        return resizedImage;
    }
    
}
>>>>>>> d9a7cb6 (final changes)
