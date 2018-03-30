

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import Model.*;


/**
 * A special panel class to display a Sudoku board modeled by the
 * @author Yoonsik Cheon
 */
@SuppressWarnings("serial")
public class BoardPanel extends JPanel {
    
	public interface ClickListener {
		
		/** Callback to notify clicking of a square. 
		 * 
		 * @param x 0-based column index of the clicked square
		 * @param y 0-based row index of the clicked square
		 */
		void clicked(int x, int y);
	}
	
    /** Background color of the board. */
	private static final Color boardColor = new Color(247, 223, 150);

    /** Board to be displayed. */
    private SudokuBoard board;

    /** Width and height of a square in pixels. */
    private int squareSize;

    /** Create a new board panel to display the given board. */
    public BoardPanel(SudokuBoard board, ClickListener listener) {
        this.setMinimumSize(new Dimension(500,400));
        this.setPreferredSize(new Dimension(500,400));
        this.board = board;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	int xy = locateSquare(e.getX(), e.getY());
            	if (xy >= 0) {
            		listener.clicked(xy / 100, xy % 100);
            	}
            }
        });
    }

    /** Set the board to be displayed. */
    public void setBoard(SudokuBoard board) {
    	this.board = board;
    }

    public SudokuBoard getBoard() { return this.board; }

    /**
     * Given a screen coordinate, return the indexes of the corresponding square
     * or -1 if there is no square.
     * The indexes are encoded and returned as x*100 + y, 
     * where x and y are 0-based column/row indexes.
     */
    private int locateSquare(int x, int y) {
    	if (x < 0 || x > board.getSize() * squareSize
    			|| y < 0 || y > board.getSize() * squareSize) {
    		return -1;
    	}
    	int xx = x / squareSize;
    	int yy = y / squareSize;
    	return xx * 100 + yy;
    }

    /** Draw the associated board. */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // determine the square size
        Dimension dim = this.getSize();
        squareSize = Math.min(dim.width, dim.height) / board.getSize();

        // draw background

        final Color oldColor = g.getColor();
        g.setColor(boardColor);
        g.fillRect(0, 0, squareSize * board.getSize(), squareSize * board.getSize());

        // WRITE YOUR CODE HERE ...
        // i.e., draw grid and squares.

        g.setColor(Color.BLACK);

        // Draw border
        int sideLength = squareSize * board.getSize();
        g.drawLine(0,0, sideLength, 0);
        g.drawLine(0, 0,0, sideLength);
        g.drawLine(sideLength, 0, sideLength, sideLength);
        g.drawLine(0, sideLength, sideLength, sideLength);

        // Draw Grid
        for (int i = 0; i < board.getSize(); i++) {
            int pos = i*squareSize;
            g.drawLine(0, pos, squareSize * board.getSize(), pos);
            g.drawLine(pos, 0, pos, squareSize * board.getSize());
        }

        // Draw thick grid
        int sqrt_size = (int)Math.sqrt(this.board.getSize());
        for (int i = 1; i < sqrt_size; i++) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(4));
            int pos = (i*squareSize)*sqrt_size;
            g2.drawLine(pos, 0, pos, squareSize * board.getSize());
            g2.drawLine(0, pos, squareSize * board.getSize(), pos);
        }

        g.setFont(new Font("Helvetica", Font.BOLD, 16));
        for (int i = 0; i < this.board.getSize(); i++) {
            for (int j = 0; j < this.board.getSize(); j++) {
                g.setFont(new Font("default", Font.BOLD, 16));
                String val = Integer.toString(this.board.getCell(i,j));
                int half = squareSize/2;
                if (this.board.getCell(i,j) != 0) {
                    if (!this.board.canAlterNumber(i,j)) {
                        g.setColor(Color.BLUE);
                    }
                    else {
                        g.setColor(Color.BLACK);
                    }
                    int strWidth = g.getFontMetrics().stringWidth("0");
                    g.drawString(val, squareSize*j+half-strWidth/2+1, squareSize*i+half+strWidth/2+1);
                }
            }
        }

    }
}
