/**
 * Provides board model to store cell information for any board game.
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

	/**
	 * Used for rectangular boards.
	 * @param width		Desired width of board.
	 * @param height	Desired height of board.
	 */
	public Board(int width, int height) {
		this.cells = new int[width][height];
		this.width = width;
		this.height = height;
	}

	/**
	 * Used for square boards.
	 * @param size	Desired side length of board (all side).
	 */
	public Board(int size) {
		this.cells = new int[size][size];
		this.height = this.width = size;
	}

	/**
	 * Getter method for requested cell location.
	 * @param row	row index in cell table
	 * @param col	column index in cell table
	 * @return	Value of cell in given row and column (i.e. cells[row][column])
	 */
	public int getCell(int row, int col) {
		return this.cells[row][col];
	}
}
