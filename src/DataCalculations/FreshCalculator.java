<<<<<<< HEAD
package DataCalculations;

import Storage.FreshnessLvl;
import java.time.LocalDate;

/**
 * Calculates and assigns freshness levels based on calculations from expiry date
 * updates product statuses(FRESH ,....)
 */

public class FreshCalculator {
    public static FreshnessLvl calculate(LocalDate currentDate, LocalDate expiryDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(currentDate, expiryDate);
        
        if (daysBetween <= 0) {
            return FreshnessLvl.EXPIRED;
        } else if (daysBetween <= 3) {
            return FreshnessLvl.ROTATE;
        } else {
            return FreshnessLvl.FRESH;
        }
    }
}
=======
package DataCalculations;
import java.util.ArrayList;
import java.util.List;

import Storage.*;

public class FreshCalculator {

	/**
     * Assigns freshness level based on expiry date
     * @param expiryDate The date the product will expire
     * @return FreshnessLevel
     */
    public static FreshnessLvl getFreshnessLevel(Date currDate,Date exprDate){
    	if (currDate == null || exprDate == null) {
            return FreshnessLvl.EXPIRED; // or handle differently
        }
    	int daysLeft = Date.ExpiryDate(currDate,exprDate);
        if (daysLeft<0) {
            return FreshnessLvl.EXPIRED;
        }
        if (daysLeft<=3) {
            return FreshnessLvl.ROTATE;
        }
        if(daysLeft>=365) {
        	return FreshnessLvl.SHELF_STABLE;
        }
        return FreshnessLvl.FRESH;
    }

    /**
     * Filters a list of products based on a desired freshness level
     * @param products The full list
     * @param desired FreshnessLevel to filter by
     * @return List<Product>
     */
    public static List<Product> filterByFreshness(List<Product> products,FreshnessLvl lvl) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getFresh() == lvl) {
                result.add(p);
            }
        }
        return result;
    }
    /**
     * Updates the freshness status of each product in the list
     * @param products List of products to update
     */
    public static void updateFreshnessStatus(List<Product> products,Date today) {
        for (Product p : products) {
            FreshnessLvl level = getFreshnessLevel(today,p.getExpiryDate());
            p.setFresh(level);
        }
    }
    /**
     * Returns products that should be rotated (soon to expire)
     */
    public static List<Product> getRotatableProducts(List<Product> products) {
        List<Product> rotatables = new ArrayList<>();
        for (Product p : products) {
            if (p.getFresh() == FreshnessLvl.ROTATE) {
                rotatables.add(p);
            }
        }
        return rotatables;
    }

    /**
     * Returns products that are already expired
     */
    public static List<Product> getExpiredProducts(List<Product> products) {
        List<Product> expired = new ArrayList<>();
        for (Product p : products) {
            if (p.getFresh() == FreshnessLvl.EXPIRED) {
                expired.add(p);
            }
        }
        return expired;
    }
}
>>>>>>> d9a7cb6 (final changes)
