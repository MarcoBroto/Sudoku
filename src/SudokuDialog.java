/************************************************************************
 * SudokuDialog.java                                                    *
 * @author Created by Yoonsik Cheon                                     *
 * @author Modified by Marco Soto                                       *
 * Sudoku game developed for UTEP CS 3331 Advanced OOP                  *
 *                                                                      *
 * This file provides class for implementing Sudoku game start point    *
 * and user interface. Requires sudoku board models defined in          *
 * Model package.                                                       *
 ************************************************************************/

import Model.SudokuBoard;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


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
    private final static Dimension DEFAULT_SIZE = new Dimension(650, 650);

    /** Special panel to display a Sudoku board. Holds SudokuBoard object used for game model */
    private BoardPanel boardPanel;  // BoardPanel instance that also holds the game's SudokuBoard object.

    private int boardSize = 9;

    /**  Stores playable boards read from read file method called in main  */
    private ArrayList<SudokuBoard>[] playableBoardsLists;

    /**  State memory for user interface & game functionality */
    private boolean insertState = false;
    private boolean deleteState = false;

    private int savedNum = 0; // Number saved for board insertion

    /**  User interface buttons stored for easy access   */
    private ArrayList<JButton> numberButtons = new ArrayList<>();
    private ArrayList<JButton> toolbarButtons = new ArrayList<>();

    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");

    /** SudokuDialog default constructor and helper constuctor  */
    public SudokuDialog(ArrayList[] boardLists) {
        this(DEFAULT_SIZE,9,boardLists);
    }
    
    /**
     * @author Modified by Marco Soto
     * Create a new dialog of the given screen dimension.
     */
    public SudokuDialog(Dimension dim, int size, ArrayList[] boardLists) {
        super("Sudoku");
        URL url = this.getClass().getResource("/Assets/sudoku.png");
        this.playableBoardsLists = boardLists;
        try {
            BufferedImage img = ImageIO.read(url);
            this.setIconImage(img);
        }
        catch (Exception ex) {
            System.out.println("Set icon image failed");
        }
        setSize(dim);
        boardSize = size;
        java.util.Random rand = new java.util.Random();
        this.msgBar.setText("Welcome To Sudoku!");
        boardPanel = new BoardPanel(this.getBoardAtIndex(rand.nextInt(playableBoardsLists[0].size())), this::boardClicked);

        configureUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }

    /**
     * @author Marco Soto
     * Callback to be invoked when a square of the board is clicked.
     *
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     */
    private void boardClicked(int x, int y) {
        this.msgBar.setForeground(Color.BLACK);
        if (!this.boardPanel.getBoard().canAlterNumber(y, x)) { // Number conflicts with fixed number
            this.msgBar.setForeground(Color.RED);
            showMessage(String.format("Can't Alter Number at row: %d col %d", y+1, x+1));
            return;
        }
        if (insertState) {
            insertState = false;
            SudokuBoard board = this.boardPanel.getBoard();
            int numberReplaced = board.getCell(y,x);
            if (board.insertNumber(savedNum, y, x)) { // Number was inserted successfully
                showMessage(String.format("Inserted %d at row: %d col %d", savedNum, y+1, x+1));
                if (board.getNumberOccurrence(savedNum) == this.boardSize)
                    this.numberButtons.get(savedNum-1).setVisible(false);
                board.rememberMove(savedNum, numberReplaced, y, x);
                this.repaint();
            }
            else { // Number did conflict with another in the board
                this.msgBar.setForeground(Color.RED);
                showMessage(String.format("There Is A Number Conflict!"));
            }
            if (this.boardPanel.getBoard().getNumbersAdded() == Math.pow(this.boardPanel.getBoard().getSize(),2) && this.boardPanel.getBoard().validateBoard()) { // Puzzle solved action
                showMessage(String.format("Congratulations! You Solved This Puzzle!"));
                showCongratWindow();
            }
        }
        else if (deleteState) {
            this.deleteState = false;
            showMessage(String.format("Deleted number at row: %d col %d", y+1, x+1));
            SudokuBoard board = this.boardPanel.getBoard();
            if (board.getCell(y,x) > 0) this.numberButtons.get(board.getCell(y,x)-1).setVisible(true);
            board.rememberMove(0, board.getCell(y,x), y, x);
            board.removeNumber(y, x);
            this.repaint();
        }
        else {
            showMessage(String.format("Board clicked: x = %d, y = %d", x+1, y+1));
        }
    }
    
    /**
     * @author Modified by Marco Soto
     * Callback to be invoked when a number button is clicked.
     *
     * @param number Clicked number (1-9), or 0 for "X".
     */
    private void numberClicked(int number) {
        this.msgBar.setForeground(Color.BLACK);
        if (number == 0) {
            showMessage("Press a square to delete a number");
            this.deleteState = true;
            this.insertState = false;
        }
        else {
            showMessage("Number clicked: " + number);
            this.savedNum = number;
            this.insertState = true;
            this.deleteState = false;
        }
    }
    
    /**
     * @author Modified by Marco Soto
     * Callback to be invoked when a new button is clicked.
     * If the current game is over, start a new game of the given size;
     * otherwise, prompt the user for a confirmation and then proceed
     * accordingly.
     *
     * @param size Requested puzzle size, either 4 or 9.
     */
    private void newClicked(int size) {
        this.msgBar.setForeground(Color.BLACK);
        System.out.println("Creating new " + size + "x" + size + " game");
        this.boardPanel.getBoard().clearBoard();
        boardSize = size;
        java.util.Random rand = new java.util.Random();
        this.boardPanel.showPossibleNumber(false);
        insertState = deleteState = false;
        toolbarButtons.get(3).setIcon(new ImageIcon(createImageIcon("help.png").getImage().getScaledInstance( 40, 40,  java.awt.Image.SCALE_SMOOTH ))); // Reset Image Icon

        if (this.boardPanel.getBoard().getSize() == size) { // Repaints new board without creating new dialog
            this.boardPanel.setBoard(getBoardAtIndex(rand.nextInt(playableBoardsLists[0].size())));
            this.showMessage("New clicked: " + size);
            repaint();
        }
        else { // Opens new dialog box with new board dimension
            SudokuDialog newView = new SudokuDialog(DEFAULT_SIZE, size, this.playableBoardsLists);
            this.showMessage("New clicked: " + size);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
        System.gc();
    }

    /**
     * Display the given string in the message bar.
     * @param msg Message to be displayed.
     */
    private void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configures game UI. */
    private void configureUI() {
        setLayout(new BorderLayout());
        
        JPanel buttons = makeControlPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        this.add(buttons, BorderLayout.NORTH);
        
        JPanel boardP = new JPanel();
        boardP.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        boardP.setLayout(new GridLayout(1,1));
        boardP.add(boardPanel);
        this.add(boardP, BorderLayout.CENTER);

        JPanel numButtons = new JPanel();
        numButtons.setLayout(new BoxLayout(numButtons, BoxLayout.PAGE_AXIS));
        numButtons.setBorder(BorderFactory.createEmptyBorder(10,0,0,19));
        int maxNumber = boardPanel.getBoard().getSize() + 1;
        for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            if (i < maxNumber) this.numberButtons.add(button);
            button.setFocusPainted(false);
            button.setMargin(new Insets(0,2,0,2));
            button.addActionListener(e -> numberClicked(number));
            numButtons.add(button);
        }
        numButtons.setAlignmentX(LEFT_ALIGNMENT);
        this.add(numButtons, BorderLayout.EAST);
        
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        add(msgBar, BorderLayout.SOUTH);
    }

    /**
     * @author Modified by Marco Soto
     * Create a control panel consisting of new and number buttons.
     */
    private JPanel makeControlPanel() {
        /*--------------------------------------------------------------------------------*/
        /*  Menu Bar and Menu Items */
        JMenuBar menuBar = new JMenuBar();
        JMenu menu1 = new JMenu("Game");
        JMenu menu2 = new JMenu("Options");

        /*  Menu Mnemonics  */
        menu1.setMnemonic('g');
        menu2.setMnemonic('o');

        JMenuItem[] menu1Items = {
                new JMenuItem("New 9x9 Board"),
                new JMenuItem("New 4x4 Board")
        };
        JMenuItem[] menu2Items = {
                new JMenuItem("Check For Valid Solution"),
                new JMenuItem("Solve Puzzle"),
                new JMenuItem("Clear Board"),
                new JMenuItem("Toggle Valid Number Grid")
        };
        for (JMenuItem item: menu1Items) menu1.add(item);
        for (JMenuItem item: menu2Items) menu2.add(item);
        menuBar.add(menu1);
        menuBar.add(menu2);
        this.setJMenuBar(menuBar);

        /*--------------------------------------------------------------------------------*/
        /*  Menu Accelerators    */
        menu1.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, Event.CTRL_MASK));
        menu1.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Event.CTRL_MASK));

        menu2.getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
        menu2.getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        menu2.getItem(2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
        menu2.getItem(3).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));

        /*--------------------------------------------------------------------------------*/
        /*  Toolbar Buttons */
    	JPanel newButtons = new JPanel(new FlowLayout());
        JToolBar toolbar = new JToolBar("Options");
        JButton[] tbButtons = new JButton[4];
        ImageIcon[] tbIcons = { // Create Button Icons
                createImageIcon("checkmark.png"),
                createImageIcon("key.png"),
                createImageIcon("remove.png"),
                createImageIcon("help.png")
        };
        String[] tooltipText = {
                "Check if Solution Exists",
                "Solve Puzzle",
                "Clear Board",
                "Toggle Valid Number Grid"
        };

        for (int i = 0; i < tbButtons.length; i++) {
            tbIcons[i] = new ImageIcon(tbIcons[i].getImage().getScaledInstance( 40, 40,  java.awt.Image.SCALE_SMOOTH )); // Resize Icons
            tbButtons[i] = new JButton(tbIcons[i]); // Create toolbar buttons with images
            tbButtons[i].setToolTipText(tooltipText[i]); // Set toolbar button tool tip
        }
        toolbar.setFloatable(false);
        newButtons.add(toolbar);

        /*--------------------------------------------------------------------------------*/
        /*  Size buttons    */
        JButton[] sizeButtons = {new JButton("New 4x4"), new JButton("New 9x9")};

        /*--------------------------------------------------------------------------------*/
        /*  Action Listener implementation used for menu1Items and new board buttons. Performs equivalent functionality  */
        ActionListener menu1_newboard_Listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newClicked((e.getSource() == sizeButtons[0] || e.getSource() == menu1Items[1]) ? 4 : 9 );
            }
        };

        /*--------------------------------------------------------------------------------*/
        /*  Action Listener implementation used for menu2Items and toolbar items. Performs equivalent functionality   */
        ActionListener menu2_toolbar_Listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msgBar.setForeground(Color.BLUE);
                if (e.getSource().equals(tbButtons[1]) || e.getSource().equals(menu2Items[1])) { // Solve button
                    msgBar.setText("Solving Puzzle");
                    if (boardPanel.getBoard().solveBoard()) {
                        msgBar.setText("Puzzle Solved");
                        for (JButton numB: numberButtons) numB.setVisible(false);
                        repaint();
                    }
                    else
                        msgBar.setText("No Solution Found");
                }
                else if (e.getSource().equals(tbButtons[2]) || e.getSource().equals(menu2Items[2])) { // Clear button
                    msgBar.setText("Cleared Board");
                    boardPanel.getBoard().clearBoard();
                    for (JButton numB: numberButtons) numB.setVisible(true);
                    boardPanel.repaint();
                }
                else if (e.getSource().equals(tbButtons[3]) || e.getSource().equals(menu2Items[3])) { // Toggle button
                    msgBar.setText("Toggled Valid Number Grid");
                    boardPanel.showPossibleNumber(!boardPanel.showingPossibleNumbers());
                    if (boardPanel.showingPossibleNumbers()) {
                        ImageIcon greyedImg = new ImageIcon(GrayFilter.createDisabledImage(tbIcons[3].getImage()));
                        tbButtons[3].setIcon(greyedImg);
                    }
                    else tbButtons[3].setIcon(tbIcons[3]);
                    boardPanel.repaint();
                }
                else {
                    if (SudokuBoard.copy(boardPanel.getBoard()).solveBoard()) { // Check for solution button
                        System.out.println("Checking Board");
                        showMessage("A Valid Solution Exists");
                    }
                    else
                        showMessage("No Valid Solution Exists");
                }
            }
        };

        /*--------------------------------------------------------------------------------*/
        /*  Action Listener additions for menu1 items and new board size buttons  */
        for (JButton button: sizeButtons) { // New size buttons
            button.setFocusPainted(false);
            button.addActionListener(menu1_newboard_Listener);
            newButtons.add(button);
        }
        newButtons.setAlignmentX(LEFT_ALIGNMENT);

        for (JMenuItem i: menu1Items) i.addActionListener(menu1_newboard_Listener); // Menu1 items

        /*--------------------------------------------------------------------------------*/
        /*  Action Listener additions for menu2 items and toolbar buttons  */
        for (JMenuItem i: menu2Items) { // Menu2 Items
            i.addActionListener(menu2_toolbar_Listener);
        }

        for (JButton b: tbButtons) { // Toolbar buttons
            b.addActionListener(menu2_toolbar_Listener); // Add action listeners
            toolbar.add(b);
            toolbarButtons.add(b);
        }

        /*--------------------------------------------------------------------------------*/
        /*  Undo and Redo button action listener implementation and addition    */
        JButton undoButton = new JButton();
        JButton redoButton = new JButton();
        undoButton.setToolTipText("Undo Move");
        redoButton.setToolTipText("Redo Move");

        ImageIcon undoImage = new ImageIcon(createImageIcon("undo.jpg").getImage().getScaledInstance( 40, 40,  java.awt.Image.SCALE_SMOOTH )); // Resize Images
        ImageIcon redoImage = new ImageIcon(createImageIcon("redo.jpg").getImage().getScaledInstance( 40, 40,  java.awt.Image.SCALE_SMOOTH )); // Resize Images
        undoButton.setIcon(undoImage);
        redoButton.setIcon(redoImage);

        undoButton.addActionListener(e -> {
            SudokuBoard board = this.boardPanel.getBoard();
            board.undoMove();
            msgBar.setText("Undo Move");
            repaint();
        });

        redoButton.addActionListener(e -> {
            SudokuBoard board = this.boardPanel.getBoard();
            board.redoMove();
            msgBar.setText("Redo Move");
            repaint();
        });

        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.setAlignmentX(LEFT_ALIGNMENT);

        /*--------------------------------------------------------------------------------*/
        /*    UI Configuration finishers and additions    */
    	JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(newButtons);
        return content;
    }

    /**
     * @author  Marco Soto
     * Used to create dialog window to congratulate the player once the user finishes solving the given puzzle.
     *
     * @return  Window that congratulates the user when the puzzle is solved.
     */
    public static JFrame showCongratWindow() {
        JFrame congrat_window = new JFrame("Congratulations");
        congrat_window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        congrat_window.setSize(new Dimension(300,100));
        congrat_window.setLocationRelativeTo(null);
        congrat_window.setVisible(true);
        congrat_window.setResizable(false);
        JLabel congrats = new JLabel("Congratulations! You Solved This Puzzle!");
        congrats.setBorder(BorderFactory.createEmptyBorder(20,10,0,20));
        congrat_window.add(congrats);
        return congrat_window;
    }

    /** Create an image icon from the given image file. */
    private ImageIcon createImageIcon(String filename) {
        URL imageUrl = getClass().getResource("/Assets/" + filename);
        if (imageUrl != null)
            return new ImageIcon(imageUrl);
        return null;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     * @param index
     * @return
     */
    private SudokuBoard getBoardAtIndex(int index) {
        ArrayList<SudokuBoard> list;
        switch (this.boardSize) {
            case 9:
                list = playableBoardsLists[1];
                break;
            case 4:
                list = playableBoardsLists[0];
                break;
            default:
                list = playableBoardsLists[1];
        }
        return list.get(index);
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     */
    public static void main(String[] args) {
        ArrayList[] boardLists = {
                SudokuBoard.readBoardListFile(new java.io.File("src/Assets/boardList_4.txt"),4),
                SudokuBoard.readBoardListFile(new java.io.File("src/Assets/boardList_9.txt"),9)
        };
        for (ArrayList list: boardLists) Collections.shuffle(list);
        new SudokuDialog(boardLists);
    }
}
