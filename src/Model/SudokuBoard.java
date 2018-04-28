/************************************************************************
 * SudokuBoard.java                                                     *
 * @author Created and Modified by Marco Soto                           *
 * Sudoku game developed for UTEP CS 3331 Advanced OOP                  *
 *                                                                      *
 * This file provides the core model for the sudoku game application    *
 * developed for CS 3331. Includes all functionality for running a      *
 * standalone sudoku game. Requires Board.java superclass file.         *
 ************************************************************************/

package Model;

import External.JavaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @author Marco Soto
 * //TODO: Documentation
 */
public class SudokuBoard extends Board {

    private final int SIZE; // Side length of board
    private final int SUBSQUARE_SIZE;
    private ArrayList<Cell> fixedNumbers = new ArrayList<>(); // Stores the unalterable cell locations received from json web service.
    private int numbersAdded; // Stores the total number of numbers entered into the board; game finishes when numbersAdded = (board length)^2
    private LinkedList<Integer>[][] possibleNumbers = null;
    private Stack<Move> undoList = new Stack<>();
    private Stack<Move> redoList = new Stack<>();
    private int[] numberOccurrences;

    public SudokuBoard(int boardSize) {
        super(isPerfectSquare(boardSize));
        this.SIZE = boardSize;
        this.SUBSQUARE_SIZE = (int)Math.sqrt(SIZE);
        this.numberOccurrences = new int[SIZE+1];
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * Getter for the side length of the sudoku board.
     * @return  Sudoku board side length.
     */
    public int getSize() {
        if (super.getHeight() != super.getWidth()) throw new IllegalArgumentException("Invalid Board Dimensions");
        return super.getHeight();
    }

    /**
     * @author
     * //TODO: Documentation
     *
     * Getter for array containing the occurrences of each number in the board.
     * @return  Array containing number occurrences
     */
    public int getNumbersAdded() { return this.numbersAdded; };

    /**
     * @author Marco Soto
     * //TODO: Documenation
     *
     * @param n
     * @return
     */
    public int getNumberOccurrence(int n) {
        if (n < 1 || n > SIZE) throw new IllegalArgumentException();
        return this.numberOccurrences[n];
    }

    /**
     * @author Marco Soto
     * Used only in the SudokuBoard constructor method to ensure the given dimensions provide a perfect square for the board.
     *
     * @param size  Side length to be tested.
     * @return  Returns size of board to be passed intro constructor if the given dimensions are a perfect square, otherwise an exception is thrown.
     */
    private static int isPerfectSquare(int size) {
        if (Math.sqrt(size) != (int)Math.sqrt(size))
            throw new IllegalArgumentException("Board dimensions are not perfect square.");
        else
            return size;
    }

    /**
     * @author Marco Soto
     * Receives a cell and number and checks if that number exists in that row. Used to validate number inputs in Sudoku board.
     *
     * @param number    Number searched for.
     * @param row       Row the number will belong to and to be searched.
     * @param column    Column the number will belong to.
     * @return      True if the number already exists in the row, false otherwise.
     */
    private boolean isInRow(int number, int row, int column) {
        for (int i = 0; i < SIZE; i++)
            if (i != column && this.getCell(row,i) == number) return true;
        return false;
    }

    /**
     * @author Marco Soto
     * Receives a cell and number and checks if that number exists in that row. Used to validate number inputs in Sudoku board.
     *
     * @param number    Number searched for.
     * @param row       Row the number will belong to.
     * @param column    Column the number will belong to and to be searched.
     * @return      True if the number already exists in the column, false otherwise.
     */
    private boolean isInColumn(int number, int row, int column) {
        for (int i = 0; i < SIZE; i++)
            if (i != row && this.getCell(i,column) == number) return true;
        return false;
    }

    /**
     * @author Marco Soto
     * Receives a cell and number and checks if that number exists in the same subsquare it belongs to on the board.
     * Searched indices will be saved in 'rowIndex' and 'colIndex' within the code.
     *
     * @param number    Number searched for.
     * @param row       Row the number will belong to.
     * @param column    Column the number will belong to.
     * @return      True if the number already exists in the subsquare, false otherwise.
     */
    private boolean isInSubsquare(int number, int row, int column) {
        int size = this.getSize();
        int squareSize = (int)Math.sqrt(size);
        for (int i = 0; i < squareSize; i++) {
            for (int j = 0; j < squareSize; j++) {
                int rowIndex = squareSize * (row/squareSize) + (i%squareSize);
                int colIndex = squareSize * (column/squareSize) + (j%squareSize);
                if (rowIndex == row && colIndex == column) continue; // Ignore square being compared to
                else if (this.getCell(rowIndex,colIndex) == number) return true;
            }
        }
        return false;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param number
     * @param row
     * @param column
     * @return
     */
    private boolean isValidInsert(int number, int row, int column) {
        if (!canAlterNumber(row,column) || isInColumn(number,row,column) || isInRow(number,row,column) || isInSubsquare(number,row,column))
            return false;
        else
            return true;
    }

    /**
     * @author Marco Soto
     * Inserts number into board so long as the number belongs to the set of viable numbers and does not conflict with
     * the row, column, or subsquare numbers. Handles total numbers inserted.
     *
     * @param number    Number being inserted into sudoku board.
     * @param row   Row insertion index.
     * @param column Column insertion index.
     * @return  True if the number passes criteria and is inserted, false otherwise.
     */
    public boolean insertNumber(int number, int row, int column) {
        if ((row > this.SIZE-1 || row < 0) || (column > this.SIZE-1 || column < 0))
            throw new IllegalArgumentException();
        if (number < 1) return false;
        if (!isValidInsert(number,row,column)) return false;
        int cellNum = this.getCell(row,column);
        this.setCell(number, row, column);
        this.numberOccurrences[number]++; // Increase inserted number occurrence
        if (cellNum != 0) {
            this.numberOccurrences[cellNum]--; // Decrease replaced number occurrence
            return true; // Number replaces existing number, resulting in successful (true) insert; do not increment numbersAdded
        }
        this.numbersAdded++; // Increments only when number is inserted into empty cell.
        return true;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param number
     * @param row
     * @param column
     * @return
     */
    public boolean insertFixedNumber(int number, int row, int column) {
        if ((row > this.SIZE-1 || row < 0) || (column > this.SIZE-1 || column < 0))
            throw new IllegalArgumentException();
        if (!isValidInsert(number,row,column)) return false;
        this.setCell(number,row,column);
        this.numberOccurrences[number]++;
        fixedNumbers.add(new Cell(row,column));
        this.numbersAdded++;
        return true;
    }

    /**
     * @author Marco Soto
     * Removes a number at the desired cell so long as the number exists.
     *
     * @param row   Row index
     * @param column Column index
     * @return  True if the number is successfully removed, false otherwise.
     */
    public boolean removeNumber(int row, int column) {
        if ((row > this.SIZE-1 || row < 0) || (column > this.SIZE-1 || column < 0)) throw new IllegalArgumentException();
        if (!canAlterNumber(row,column)) return false;
        int cellNum = this.getCell(row,column);
        if (cellNum < 1 || cellNum > this.SIZE) return false; // Return false if cell is empty or number does not belong.
        this.numberOccurrences[cellNum]--; // Decrease removed number occurrence
        this.setCell(0,row,column); // Reset cell value
        this.numbersAdded--; // Decrements only if an existing number was actually removed.
        return true;
    }

    /**
     * @author Marco Soto
     * Receives a SudokuBoard object and determines if the given board is valid Sudoku board.
     *
     * @return  True if the board does not have any conflicting numbers and satisfies the criteria of a sudoku board.
     */
    public boolean validateBoard() {
        int dim = this.SIZE;
        if (Math.sqrt(dim) != (int)Math.sqrt(dim)) {
            System.out.println("Board dimensions are incompatible.");
            return false;
        }

        for (int i = 0; i < dim; i++) {
            if (dim != SIZE) {
                System.out.println("Board dimensions are incompatible.");
                return false;
            }

            for (int j = 0; j < dim; j++) {
                int number = this.getCell(i,j);
                if (this.getCell(i,j) == 0) continue;
                if (isInColumn(number, i, j) ||
                        isInRow(number, i, j) ||
                        isInSubsquare(number, i, j)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     */
    public void printBoard() {

        // Print board contents
        System.out.println();
        for (int i = 0; i < this.getSize(); i++) {
            // Print top border
            for (int j = 0; j < this.getSize(); j++) {
                if (i != 0 && i % Math.sqrt(this.getSize()) == 0) System.out.print("****"); //Print subsquare border
                else if (j != 0 && j % Math.sqrt(this.getSize()) == 0) System.out.print("*---"); //Print border adjacent to subsquare wall
                else System.out.print("+---"); //Print regular border
            }
            if (i != 0 && i % Math.sqrt(this.getSize()) == 0) System.out.println("*"); //For subsquare border
            else System.out.println("+");

            for (int j = 0; j < this.getSize(); j++) {
                if (j != 0 && j % Math.sqrt(this.getSize()) == 0 ) System.out.print("*"); //Print subsquare wall
                else System.out.print("|"); //Print regular wall

                if (this.getCell(i,j) == 0) System.out.print("   ");
                else System.out.printf(" %d ", this.getCell(i,j)); //Print digit if present
            }
            System.out.print("|");
            System.out.println();
        }
        // Print bottom border
        for (int i = 0; i < this.getSize(); i++) {
            if (i != 0 && i % Math.sqrt(this.getSize()) == 0) System.out.print("*---"); //Print subsquare border
            else System.out.print("+---"); //Print regular border
        }
        System.out.println("+\n");
    }

    /**
     * @author Marco Soto
     * Used in conjunction with json webservice to provide starting Sudoku board by saving locations of starting numbers.
     * NOTE: Requires internet connection
     *
     * @param boardSize     Desired sudoku board size
     * @param difficulty    Puzzle difficulty.
     * @return  New SudokuBoard object with number locations saved in 'fixedNumbers' and numbers inserted into the
     * object's cells property.
     */
    public static SudokuBoard generateRandomBoardWithWebService(int boardSize, int difficulty) {
        SudokuBoard SB = new SudokuBoard(boardSize);

        String response = JavaClient.generateResponse(boardSize, difficulty);
        JSONTokener t = new JSONTokener(response);
        JSONObject json = new JSONObject(t);
        if (!json.getBoolean("response")) {
            System.out.println("Web Service JSON response: " + response);
            throw new InputMismatchException("Sudoku web service request failed.");
        }
        int size = json.getInt("size");
        if (size != boardSize) throw new InputMismatchException("Board size does not match");
        JSONArray squares = json.getJSONArray("squares");

        for (int i = 0; i < squares.length(); i++) {
            JSONObject cell = squares.getJSONObject(i);
            int x = cell.getInt("x");
            int y = cell.getInt("y");
            int number = cell.getInt("value");
            SB.insertNumber(number,x,y);
            SB.fixedNumbers.add(new Cell(x,y));
        }
        return SB;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @return Returns true if the board is solvable and inserts solved board numbers, and false if the board is not solvable.
     */
    public boolean solveBoard() {
        System.out.println("Solving Board");
        if (!this.validateBoard()) {
            System.out.println("Solver Error(1): Board is not solvable.");
            return false;
        }
        int numCells = this.getSize()*this.getSize();
        this.clearBoard();
        if (possibleNumbers == null) possibleNumbers = generatePossibleNumberGrid();
        Stack<Cell> cellAltered = new Stack<>();
        Stack<Integer> startNumStack = new Stack<>();
        int startIndex = 0;
        startNumStack.push(startIndex);
        for (int i = 0; i < this.getSize() && this.numbersAdded < numCells; i++) {
            for (int j = 0; j < this.getSize() && this.numbersAdded < numCells; j++) {
                if (!this.canAlterNumber(i,j)) continue;
                boolean success = false;
                LinkedList<Integer> possibleNumbersCell = possibleNumbers[i][j];
                for (int insert = startIndex; insert < possibleNumbersCell.size(); insert++) {
                    if (this.insertNumber(possibleNumbersCell.get(insert),i,j)) {
                        cellAltered.push(new Cell(i,j));
                        startNumStack.push(insert);
                        startIndex = 0;
                        success = true;
                        break;
                    }
                }
                if (success) continue;
                if (cellAltered.isEmpty()) {
                    System.out.println("Solver Error(2): Board is not solvable.");
                    return false;
                }
                Cell top = cellAltered.pop();
                i = top.row;
                j = top.column;
                startIndex = startNumStack.pop()+1; // Start at next number
                this.removeNumber(i,j);
                j--;
            }
        }
        System.out.println("Puzzle Solved");
        return true;
    }

    /**
     * @author Marco Soto
     * Determines if the given location belongs to the set of unalterable cells by searching the locations of
     * unalterable cells in the sudoku board stored in 'fixedNumbers'.
     *
     * @param row   Row index of location.
     * @param column    Column index of location.
     * @return  True if the number does not belong to the fixed number set, false otherwise.
     */
    public boolean canAlterNumber(int row, int column) {
        for (Cell fixedNumber: fixedNumbers) {
            if (row == fixedNumber.row && column == fixedNumber.column) return false;
        }
        return true;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @return
     */
    public LinkedList<Integer>[][] generatePossibleNumberGrid() {
        LinkedList[][] grid = new LinkedList[this.getSize()][this.getSize()];
        for (int i = 0; i < this.getSize(); i++) {
            for (int j = 0; j < this.getSize(); j++) {
                grid[i][j] = new LinkedList<Integer>();
                if (!this.canAlterNumber(i,j)) continue;
                for (int num = 1; num <= this.getSize(); num++) {
                    if (!isInRow(num,i,j) && !isInColumn(num,i,j) && !isInSubsquare(num,i,j)) grid[i][j].addLast(num);
                }
            }
        }
        return grid;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     * //TODO: Test
     *
     * @param num
     * @param row
     * @param col
     */
    public void addToPossibleNumberGrid(int num, int row, int col) {
        if (possibleNumbers == null) throw new IllegalArgumentException("Grid has not been initialized.");
        LinkedList<Integer> cell;
        for (int i = 0; i < this.getSize(); i++) {
            if (isValidInsert(num, row, col)) {
                cell = possibleNumbers[i][col]; // Add to row
                int index = 0;
                for (Integer n = cell.get(index); index < cell.size(); index++)
                    if (n > num) cell.add(index,num);
                cell = possibleNumbers[row][i]; // Add to column
                index = 0;
                for (Integer n = cell.get(index); index < cell.size(); index++)
                    if (n > num) cell.add(index,num);
                for (int j = 0; j < SUBSQUARE_SIZE; j++) {
                    cell = possibleNumbers[j*(row/SUBSQUARE_SIZE)][(i/col)+(i%SUBSQUARE_SIZE)]; // Add to subsquare
                    index = 0;
                    for (Integer n = cell.get(index); index < cell.size(); index++)
                        if (n > num) cell.add(index,num);
                }
            }
        }
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     * //TODO: Test
     *
     * @param num
     * @param row
     * @param col
     */
    public void removeFromPossibleNumberGrid(int num, int row, int col) {
        if (possibleNumbers == null) throw new IllegalArgumentException("Grid has not been initialized.");
        for (int i = 0; i < this.getSize(); i++) {
            possibleNumbers[i][col].remove(num); // Remove from row
            possibleNumbers[row][i].remove(num); // Remove from column
            for (int j = 0; j < SUBSQUARE_SIZE; j++) possibleNumbers[j*(row/SUBSQUARE_SIZE)][(i/col)+(i%SUBSQUARE_SIZE)].remove(num); // Remove from subsquare
        }
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param grid
     */
    private static void printNumberGrid(LinkedList[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print("[ ");
                for (int k = 0; k < grid[i][j].size(); k++) {
                    System.out.print(grid[i][j].get(k) + " ");
                }

                System.out.print("], ");
            }
            System.out.println();
        }
    }

    /**
     * @author Marco Soto
     * TODO: Documentation
     */
    public void clearBoard() {
        System.out.println("Clearing Board");
        for (int i = 0; i < this.getSize(); i++) {
            for (int j = 0; j < this.getSize(); j++)
                this.removeNumber(i, j);
        }
        undoList = new Stack<>();
        redoList = new Stack<>();
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param fName
     * @param SB
     */
    public static void writeBoardToFile(SudokuBoard SB, File fName) {
        try {
            FileWriter writer = new FileWriter(fName,true);
            for (int i = 0; i < SB.getSize(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < SB.getSize(); j++) {
                    row.append(SB.getCell(i,j));
                    row.append(" ");
                }
                row.append("\n");
                writer.write(row.toString());
            }
            writer.close();
        }
        catch (java.io.IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param boardFile
     * @return
     */
    public static ArrayList<SudokuBoard> readBoardListFile(File boardFile, int boardSize) {
        ArrayList<SudokuBoard> readBoards = new ArrayList<>();
        try {
            java.util.Scanner input = new java.util.Scanner(boardFile);
            while (input.hasNext()) {
                SudokuBoard board = new SudokuBoard(boardSize);
                for (int i = 0; i < boardSize; i++) {
                    String row = input.nextLine();
                    char[] row_chars = row.toCharArray();
                    int column = 0;
                    for (char row_char : row_chars) {
                        if (row_char == ' ') continue;
                        int num = row_char - '0';
                        board.insertFixedNumber(num, i, column);
                        column++;
                    }
                }
                readBoards.add(board);
            }
            input.close();
        }
        catch (java.io.FileNotFoundException ex) {
            System.out.println(ex);
        }
        return readBoards;
    }

    /**
     * @author Marco Soto
     * //TODO: DOcumentation
     *
     * @param SB
     * @return
     */
    public static SudokuBoard copy(SudokuBoard SB) {
        System.out.println("Copying Board");
        SudokuBoard copy = new SudokuBoard(SB.getSize());
        copy.redoList = (Stack)SB.redoList.clone();
        copy.undoList = (Stack)SB.undoList.clone();
        for (Cell c: SB.fixedNumbers) copy.insertFixedNumber(SB.getCell(c.row,c.column),c.row,c.column);
        for (int i = 0; i < SB.getSize(); i++) {
            for (int j = 0; j < SB.getSize(); j++)
                copy.insertNumber(SB.getCell(i,j),i,j);
        }
        return copy;
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     * @return
     */
    public void redoMove() {
        if (redoList.isEmpty()) return;
        Move redo = redoList.pop();
        Cell pos = redo.position;
        if (redo.numberInserted == 0) removeNumber(pos.row, pos.column);
        else insertNumber(redo.numberInserted, pos.row, pos.column);
        undoList.push(redo);
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @return
     */
    public void undoMove() {
        if (undoList.isEmpty()) return;
        Move undo = undoList.pop();
        Cell pos = undo.position;
        if (undo.numberReplaced == 0) removeNumber(pos.row, pos.column);
        else insertNumber(undo.numberReplaced, pos.row, pos.column);
        redoList.push(undo);
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     *
     * @param inserted
     * @param replaced
     * @param row
     * @param column
     */
    public void rememberMove(int inserted, int replaced, int row, int column) {
        Move move = new Move(inserted, new Cell(row,column), replaced);
        undoList.push(move);
        redoList = new Stack<>();
    }
}

/**
 * @author Marco Soto
 * //TODO: Documentation
 */
class Cell {
    final int row;
    final int column;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }
}

/**
 * @author Marco Soto
 * //TODO: Documentation
 */
class Move {
    int numberInserted;
    int numberReplaced;
    Cell position;

    Move(int number, Cell position, int numberReplaced) {
        this.numberInserted = number;
        this.position = position;
        this.numberReplaced = numberReplaced;
    }
}