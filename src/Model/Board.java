/****************************************************************************
 * Board.java																*
 * @author Created and Modified by Marco Soto								*
 * Sudoku game developed for UTEP CS 3331 Advanced OOP						*
 *																			*
 * This file provides a barebones class model for board games and			*
 * applications. Originally designed for sudoku game board and applciation.	*
 * Extended by SudokuBoard class defined in SudokuBoard.java.				*
 ****************************************************************************/

package Model;

/**
 * @author Marco Soto
 * //TODO: Documentation
 */
public class Board {

	private final int width;
	private final int height;

	/**	Stores data points of board	*/
	private int[][] cells;

	/**
	 * @author Marco Soto
	 * Constructor used for rectangular boards.
	 *
	 * @param width		Desired width of board.
	 * @param height	Desired height of board.
	 */
	public Board(int width, int height) {
		this.cells = new int[width][height];
		this.width = width;
		this.height = height;
	}

	/**
	 * @author Marco Soto
	 * Constructor used only for square boards.
	 *
	 * @param size	Desired side lengths of board (all sides).
	 */
	public Board(int size) {
		this.cells = new int[size][size];
		this.height = this.width = size;
	}

	/**
	 * @author Marco Soto
	 * Getter method for requested cell location.
	 *
	 * @param row	row index in cell table
	 * @param col	column index in cell table
	 * @return	Value of cell in given row and column (i.e. cells[row][column])
	 */
	public int getCell(int row, int col) {
		return this.cells[row][col];
	}

	/**
	 * @author Marco Soto
	 * //TODO: Documentation
	 *
	 * @param number
	 * @param row
	 * @param col
	 */
	public void setCell(int number, int row, int col) {
		this.cells[row][col] = number;
	}

	/**	Gets height dimension of board	*/
	public int getHeight() { return this.height; }

	/**	Gets width dimension of board	*/
	public int getWidth() { return this.width; }
}
