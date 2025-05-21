<<<<<<< HEAD
package Storage;
import java.time.LocalDate;	
import java.time.temporal.ChronoUnit;
/**
 * Class to Just to keep track of Date
 */

public class Date {
	private int year;
	private int month;
	private int day;
	
	public Date(int year, int month, int day) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
	}
	/**
	 * Calculating days between two dates
	 * Assuming the first date is today and the second is the date it expires 
	 * @param D1
	 * @param D2
	 * @return
	 */
	public static int ExpiryDate(Date D1,Date D2) {
		LocalDate today = LocalDate.of(D1.getYear(),D1.getMonth(),D1.getDay());
		LocalDate expiry = LocalDate.of(D2.getYear(),D2.getMonth(),D2.getDay());
		int days = (int)ChronoUnit.DAYS.between(today, expiry);
		return days;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}
}
=======
package Storage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/**
 * Class to Just to keep track of Date of products
 */

public class Date {
	private int year;
	private int month;
	private int day;
	
	public Date(int year, int month, int day) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
	}
	/**
	 * Calculating days between two dates
	 * Assuming the first date is today and the second is the date it expires 
	 * @param D1
	 * @param D2
	 * @return
	 */
	public static int ExpiryDate(Date D1,Date D2) {
		LocalDate today = LocalDate.of(D1.getYear(),D1.getMonth(),D1.getDay());
		LocalDate expiry = LocalDate.of(D2.getYear(),D2.getMonth(),D2.getDay());
		int days = (int)ChronoUnit.DAYS.between(today, expiry);
		return days;
	}

	//Getters and Setters
	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}
	/**
	 * Method to check if this date is after the other date
	 * @param other
	 * @return boolean
	 */
	public boolean isAfter(Date other) {
	        if (this.year != other.year) {
	            return this.year > other.year;
	        }
	        if (this.month != other.month) {
	            return this.month > other.month;
	        }
	        return this.day > other.day;
	}

	 /**
		 * Method to check if this date is before the other date
		 * @param other
		 * @return boolean
		 */
	public boolean isBefore(Date other) {
	        if (this.year != other.year) {
	            return this.year < other.year;
	        }
	        if (this.month != other.month) {
	            return this.month < other.month;
	        }
	        return this.day < other.day;
	}

	/**
	 * Method to check if this date is the same the other date
	 * @param other
	 * @return boolean
	 */
	public boolean isEqual(Date other) {
	      return this.year == other.year &&
	             this.month == other.month &&
	             this.day == other.day;
	}
	   
}
>>>>>>> d9a7cb6 (final changes)
