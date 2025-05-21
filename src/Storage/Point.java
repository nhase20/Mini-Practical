package Storage;
/**
 * Class is used to contain point format of x & y axis of products/shelfs
 */
public class Point {
	int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //Getters and Setters
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
