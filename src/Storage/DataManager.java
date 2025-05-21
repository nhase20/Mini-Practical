package Storage;
import java.io.*;

/**
 * A class used to store and retrieve data, not needed but we can do it if we want to
 * Load/save products and shelf data
 * Handle storage in memory or file
 * 
 */

public class DataManager {
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
}

