/************************************************************************
 * BoardPanel.java                                                      *
 * @author Created by Yoonsik Cheon                                     *
 * @author Modified by Marco Soto                                       *
 * Sudoku game developed for UTEP CS 3331 Advanced OOP                  *
 *                                                                      *
 * This file provides the embedded user interface of the sudoku board   *
 * component in the sudoku game application.
 ************************************************************************/

import Model.SudokuBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;

/**
 * A special panel class to display a Sudoku board modeled by the SudokuBoard.java class
 * @author Created by Yoonsik Cheon
 * @author Modified by Marco Soto
 */
@SuppressWarnings("serial")
public class BoardPanel extends JPanel implements MouseMotionListener {

    /** Background color of the board. */
    private static final Color boardColor = new Color(247, 223, 150);

    /** Board to be displayed. */
    private SudokuBoard board;

    /** Width and height of a square in pixels. */
    private int squareSize;

    /** Condition which determines whether this instance's paint method draws possible valid number grid    */
    private boolean showPossibleNumbers = false;

    /** Create a new board panel to display the given board. */
    public BoardPanel(SudokuBoard board, ClickListener listener) {
        Dimension DEFAULT_SIZE = new Dimension(500,400);
        this.setMinimumSize(DEFAULT_SIZE);
        this.setPreferredSize(DEFAULT_SIZE);
        this.setSize(DEFAULT_SIZE);
        this.board = board;
        this.squareSize = Math.min(DEFAULT_SIZE.width, DEFAULT_SIZE.height) / board.getSize();
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int xy = locateSquare(e.getX(), e.getY());
                if (xy >= 0) {
                    listener.clicked(xy / 100, xy % 100);
                }
            }
        });
        addMouseMotionListener(this);
    }

    /** Set this object's SudokuBoard object instance which will be displayed. */
    public void setBoard(SudokuBoard board) {
        this.board = board;
    }

    /** Gets this object's SudokuBoard instance */
    public SudokuBoard getBoard() { return this.board; }

    /** Getter for showing possible number grid boolean */
    public boolean showingPossibleNumbers() { return showPossibleNumbers; }

    /** Setter for showing possible number grid boolean */
    public void showPossibleNumber(boolean show) { showPossibleNumbers = show; }

    @Override
    public void mouseDragged(MouseEvent e) { }

    /** Repaints board when mouse is detected on board; used for changing square color mouse hovers over    */
    @Override
    public void mouseMoved(MouseEvent e) { repaint(); }

    /**
     * @author Modified by Marco Soto
     * Draws the associated board.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // determine the square size
        Dimension dim = this.getSize();
        squareSize = Math.min(dim.width, dim.height) / board.getSize();
        int midpoint = squareSize/2;

        /*--------------------------------------------------------------------------------*/
        // Draw background
        g.setColor(boardColor);
        g.fillRect(0, 0, squareSize * board.getSize(), squareSize * board.getSize());
        g.setColor(Color.BLACK);

        /*--------------------------------------------------------------------------------*/
        // Draw Mouse Hover Square Position
        Point p = getMousePosition();
        if (p != null) {
            int square = locateSquare((int)p.getX(),(int)p.getY());
            if (square >= 0) {
                int row = square/100;
                int col = square%100;
                g.setColor(Color.GREEN);
                g.fillRect(squareSize*row, squareSize*col, row+squareSize - row, col+squareSize - col); // Subtracts pixels based on the number of lines occurring before cell
            }
        }

        /*--------------------------------------------------------------------------------*/
        // Draw Border
        int sideLength = squareSize * board.getSize();
        g.setColor(Color.BLACK);
        g.drawLine(0,0, sideLength, 0);
        g.drawLine(0, 0,0, sideLength);
        g.drawLine(sideLength, 0, sideLength, sideLength);
        g.drawLine(0, sideLength, sideLength, sideLength);

        /*--------------------------------------------------------------------------------*/
        // Draw Grid
        for (int i = 0; i < board.getSize(); i++) {
            int pos = i*squareSize;
            g.drawLine(0, pos, squareSize * board.getSize(), pos); //Draw Horizontal Line
            g.drawLine(pos, 0, pos, squareSize * board.getSize());
        }

        // Draw thick grid
        int sqrt_size = (int)Math.sqrt(this.board.getSize());
        for (int i = 1; i < sqrt_size; i++) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(4));
            int pos = (i*squareSize)*sqrt_size;
            g2.drawLine(0, pos, squareSize * board.getSize()-2, pos); //Draw Horizontal Line
            g2.drawLine(pos, 0, pos, squareSize * board.getSize()); //Draw Vertical Line
        }

        /*--------------------------------------------------------------------------------*/
        /*  Draw Model.Cell Numbers   */
        g.setFont(new Font("Helvetica", Font.BOLD, 18));
        for (int i = 0; i < this.board.getSize(); i++) {
            for (int j = 0; j < this.board.getSize(); j++) {
                String val = Integer.toString(this.board.getCell(i,j));
                if (this.board.getCell(i,j) != 0) {
                    if (!this.board.canAlterNumber(i,j)) {
                        g.setColor(Color.BLUE);
                    }
                    else {
                        g.setColor(Color.BLACK);
                    }
                    int strWidth = g.getFontMetrics().stringWidth("0");
                    g.drawString(val, squareSize*j+midpoint-strWidth/2+1, squareSize*i+midpoint+strWidth/2+1);
                }
            }
        }

        /*--------------------------------------------------------------------------------*/
        /*  Draw Possible Number Grid */
        g.setFont(new Font("default", Font.BOLD, 11));
        g.setColor(Color.MAGENTA);
        if (showPossibleNumbers) {
            //TODO: Optimize possible number grid retrieval by implementing in place updates rather than repeated generation.
            LinkedList<Integer>[][] grid = this.board.generatePossibleNumberGrid();
            for (int i = 0; i < this.board.getSize(); i++) {
                for (int j = 0; j < this.board.getSize(); j++) {
                    if (this.board.getCell(i,j) != 0) continue;
                    LinkedList<Integer> cell = grid[i][j];
                    for (int k = 0; k < cell.size(); k++) {
                        //TODO: Fix Drawing Possible Number (spacing)
                        String val = Integer.toString(cell.get(k));
                        int strWidth = g.getFontMetrics().stringWidth(val);
                        int xPos = squareSize*j + ( strWidth * (k % 3) + strWidth ) ;
                        int yPos = squareSize*i + ( strWidth * (k / 3) + strWidth+2 ) + strWidth;
                        g.drawString(val,xPos,yPos);
                    }
                }
            }
        }
    }

    public interface ClickListener {
		
		/** Callback to notify clicking of a square. 
		 * 
		 * @param x 0-based column index of the clicked square
		 * @param y 0-based row index of the clicked square
		 */
		void clicked(int x, int y);
	}

    /**
     * Given a screen coordinate, return the indexes of the corresponding square
     * or -1 if there is no square.
     * The indexes are encoded and returned as x*100 + y, 
     * where x and y are 0-based column/row indexes.
     */
    private int locateSquare(int x, int y) {
    	if (x < 0 || x >= board.getSize() * squareSize
    			|| y < 0 || y >= board.getSize() * squareSize) {
    		return -1;
    	}
    	int xx = x / squareSize;
    	int yy = y / squareSize;
    	return xx * 100 + yy;
    }
}
