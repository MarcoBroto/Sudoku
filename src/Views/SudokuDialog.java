package Views;

import sample.BoardPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import Model.*;


/**
 * A dialog template for playing simple Sudoku games.
 * You need to write code for three callback methods:
 * newClicked(int), numberClicked(int) and boardClicked(int,int).
 *
 * @author Yoonsik Cheon
 */
@SuppressWarnings("serial")
public class SudokuDialog extends JFrame {

    /** Default dimension of the dialog. */
    private final static Dimension DEFAULT_SIZE = new Dimension(310, 430);

    private final static String IMAGE_DIR = "/image/";

    /** Sudoku board. */
    private SudokuBoard board;

    /** Special panel to display a Sudoku board. */
    private BoardPanel boardPanel;

    private JPanel buttons;

    private boolean insertState = false;
    private boolean deleteState = false;
    private int savedNum = 0;

    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");

    /** Create a new dialog. */
    public SudokuDialog() {
    	this(DEFAULT_SIZE,9);
    }
    
    /** Create a new dialog of the given screen dimension. */
    public SudokuDialog(Dimension dim, int size) {
        super("Sudoku");
//        setSize(dim);
        this.setSize(new Dimension(450,470));
        board = SudokuBoard.generateRandomBoard(size, 2);
        boardPanel = new BoardPanel(board, this::boardClicked);
        configureUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        //setResizable(false);
    }

    /**
     * Callback to be invoked when a square of the board is clicked.
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     */
    private void boardClicked(int x, int y) {
        // WRITE YOUR CODE HERE ...
        //
        if (!this.board.canAlterNumber(y, x)) {
            showMessage(String.format("Can't Alter Number at row: %d col %d", y, x));
            System.out.printf("Can't Alter Number at Row: %d Col: %d\n", y, x);
            return;
        }
        if (insertState) {
            this.insertState = false;
            System.out.printf("Number %d will be inserted at row: %d col %d\n", savedNum, y, x);

            if (this.board.insertNumber(savedNum, y, x)) {
                showMessage(String.format("Inserted %d at row: %d col %d", savedNum, y, x));
                System.out.printf("Number %d finished inserting at row: %d col %d\n", savedNum, y, x);
                this.repaint();
            }
            else
                showMessage(String.format("Error: Number Conflict"));

            if (this.board.numbersAdded == Math.pow(this.board.getSize(),2) && this.board.validateBoard()) {
                showMessage(String.format("Congratulations! You Solved This Puzzle!"));
                JPanel wind = new JPanel();
                wind.add(new JLabel("Congratulations! You Solved This Puzzle!"));
                wind.setVisible(true);
            }
        }
        else if (deleteState) {
            this.deleteState = false;
            showMessage(String.format("Deleted number at row: %d col %d", y, x));
            this.board.removeNumber(y, x);
            this.repaint();
        }
        else {
            showMessage(String.format("Board clicked: x = %d, y = %d", x, y));
        }
    }
    
    /**
     * Callback to be invoked when a number button is clicked.
     * @param number Clicked number (1-9), or 0 for "X".
     */
    private void numberClicked(int number) {
        // WRITE YOUR CODE HERE ...
        //
        if (number == 0) {
            showMessage("Press a square to delete a number");
            System.out.println("Entered delete mode");
            this.deleteState = true;
            this.insertState = false;
        }
        else {
            showMessage("Number clicked: " + number);
            System.out.printf("\nSaved number %d\n", number);
            this.savedNum = number;
            this.insertState = true;
            this.deleteState = false;
        }
    }
    
    /**
     * Callback to be invoked when a new button is clicked.
     * If the current game is over, start a new game of the given size;
     * otherwise, prompt the user for a confirmation and then proceed
     * accordingly.
     * @param size Requested puzzle size, either 4 or 9.
     */
    private void newClicked(int size) {
        // WRITE YOUR CODE HERE ...
        //
        showMessage("New clicked: " + size);
        new SudokuDialog(DEFAULT_SIZE, size);
        this.setVisible(false);
        System.gc();
    }

    /**
     * Display the given string in the message bar.
     * @param msg Message to be displayed.
     */
    private void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configure the UI. */
    private void configureUI() {
        setLayout(new BorderLayout());
        
        JPanel buttons = makeControlPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        add(buttons, BorderLayout.NORTH);
        
        JPanel boardP = new JPanel();
        boardP.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        boardP.setLayout(new GridLayout(1,1));
        boardP.add(boardPanel);
        this.add(boardP, BorderLayout.CENTER);

        JPanel numberButtons = new JPanel();
        numberButtons.setLayout(new BoxLayout(numberButtons, BoxLayout.PAGE_AXIS));
        numberButtons.setBorder(BorderFactory.createEmptyBorder(10,0,0,19));
        int maxNumber = board.getSize() + 1;
        for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            button.setFocusPainted(false);
            button.setMargin(new Insets(0,2,0,2));
            button.addActionListener(e -> numberClicked(number));
            numberButtons.add(button);
        }
        numberButtons.setAlignmentX(LEFT_ALIGNMENT);
        this.add(numberButtons, BorderLayout.EAST);
        
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        add(msgBar, BorderLayout.SOUTH);
    }
      
    /** Create a control panel consisting of new and number buttons. */
    private JPanel makeControlPanel() {
    	JPanel newButtons = new JPanel(new FlowLayout());
    	JButton[] sizeButtons = {new JButton("New 4x4"), new JButton("New 9x9")};
        for (JButton button: sizeButtons) {
        	button.setFocusPainted(false);
            button.addActionListener(e -> {
                newClicked(e.getSource() == sizeButtons[0] ? 4 : 9);
            });
            newButtons.add(button);
    	}
    	newButtons.setAlignmentX(LEFT_ALIGNMENT);

    	JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(newButtons);
        return content;
    }

    /** Create an image icon from the given image file. */
    private ImageIcon createImageIcon(String filename) {
        URL imageUrl = getClass().getResource(IMAGE_DIR + filename);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl);
        }
        return null;
    }

    public static void main(String[] args) {
        new SudokuDialog();
    }
}
