/**
 * 
 */
package Model;

/**
 * @author Marco Soto
 *
 */
public class Board {
	final int width;
	final int height;
	int[][] cells;
	
	public Board(int width, int height) {
		this.cells = new int[width][height];
		this.width = width;
		this.height = height;
	}

	public Board(int size) {
		this.cells = new int[size][size];
		this.height = this.width = size;
	}
}
