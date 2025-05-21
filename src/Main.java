<<<<<<< HEAD
import Storage.TreeGraph;
import Visualisation.GuiHandler;

/**
 * Entry point of application
 */
public class Main {
	/**
	 * main method
	 * @param args - command line arguments 
	 */
    public static void main(String[] args) {
        // Initialize with some sample data
        TreeGraph productGraph = new TreeGraph(new java.util.HashMap<>());
        
        // Launch the JavaFX application
        GuiHandler.launchApp();
    }
}
=======
import Visualisation.GuiHandler;
import javafx.application.Application;
public class Main {

	public static void main(String[] args) {
		// Launch the JavaFX application
        Application.launch(GuiHandler.class, args);
	}

}
>>>>>>> d9a7cb6 (final changes)
