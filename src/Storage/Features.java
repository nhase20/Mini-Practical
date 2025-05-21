<<<<<<< HEAD
package Storage;

import java.util.Arrays;

/**
 * Class representing the cropped image's features such as average color(in greyscale),width etc...
 */
public class Features {
	private double avgGray;//average gray colour
	private double[] rgbHistogram;//colour spectrum for 
	private int width, height;
	
	public Features(double avgGray, double[] rgbHistogram, int width, int height) {
		super();
		this.avgGray = avgGray;
		this.rgbHistogram = rgbHistogram;
		this.width = width;
		this.height = height;
	}
	@Override
	public String toString() {
		return "Features - [avgGray =" + avgGray + "] [rgbHistogram =" +
				Arrays.toString(rgbHistogram) + "] [width =" + width
				+ "] [height =" + height + "]";
	}
	
	//Getters and Setters of the class variables
	public double getAvgGray() {
		return avgGray;
	}
	public double[] getRgbHistogram() {
		return rgbHistogram;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other) return true;
	    if(other == null || !(other instanceof Product)) {
			return false;
		}
	    Features that = (Features) other;
	    if( Double.compare(avgGray, that.avgGray) == 0 &&
	           width == that.width &&
	           height == that.height &&
	           Arrays.equals(rgbHistogram, that.rgbHistogram)) return true;
	    return false;
	}

	@Override
	public int hashCode() {
	    int result = Double.hashCode(avgGray);
	    result += Arrays.hashCode(rgbHistogram);
	    result += Integer.hashCode(width);
	    result += Integer.hashCode(height);
	    return result;
	}
	
}
=======
package Storage;

import java.util.Arrays;

/**
 * Class representing the cropped image's features such as average color(in greyscale),width etc...
 */
public class Features {
	private double avgGray;//average gray colour
	private double[] rgbHist;//colour spectrum for 
	private int width, height;
	
	public Features(double avgGray,double[] rgbHist,int width,int height) {
		super();
		this.avgGray = avgGray;
		//Checking size before assigning it
		if (rgbHist.length != 256) {
            throw new IllegalArgumentException("Histogram size must be " + 256);
        }
		this.rgbHist = rgbHist;
		this.width = width;
		this.height = height;
	}
	@Override
	public String toString() {
		return "Features - [avgGray =" + avgGray + "] [rgbHistogram =" +
				Arrays.toString(rgbHist) + "] [width =" + width
				+ "] [height =" + height + "]";
	}
	
	@Override
	/**
	 * Another equals overridden function to match features
	 * @return boolean
	 */
	public boolean equals(Object other) {
	    if (this == other) return true;
	    if(other == null || !(other instanceof Product)) {
			return false;
		}
	    Features that = (Features) other;
	    if( Double.compare(avgGray, that.avgGray) == 0 &&
	           width == that.width &&
	           height == that.height &&
	           Arrays.equals(rgbHist, that.rgbHist)) return true;
	    return false;
	}

	@Override
	/**
	 * Hash code function to make sure no product features are the same
	 */
	public int hashCode() {
		//created by summing up all the codes for avgGray,histogram,width and height to make it unique
	    int result = Double.hashCode(avgGray);
	    result += Arrays.hashCode(rgbHist);
	    result += Integer.hashCode(width);
	    result += Integer.hashCode(height);
	    return result;
	}
	
	//Getters and Setters of the class variables
		public double getAvgGray() {
			return avgGray;
		}
		public double[] getRgbHistogram() {
			return rgbHist;
		}
		public int getWidth() {
			return width;
		}
		public int getHeight() {
			return height;
		}
	
}
>>>>>>> d9a7cb6 (final changes)
