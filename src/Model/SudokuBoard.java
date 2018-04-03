
package Model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import External.JavaClient;

import java.util.*;

/**
 * @author Marco Soto
 *
 */
public class SudokuBoard extends Board {

    private final int SIZE; // Side length of board
    private ArrayList<Cell> fixedNumbers = new ArrayList<>(); // Stores the unalterable cell locations received from json web service.
    public int numbersAdded; // Stores the total number of numbers entered into the board; game finishes when numbersAdded = (board length)^2

    public SudokuBoard(int boardSize) {
        super(isPerfectSquare(boardSize));
        this.SIZE = boardSize;
    }

    /**
     * Getter for the side length of the sudoku board.
     * @return  Sudoku board side length.
     */
    public int getSize() {
        if (super.height != super.width) throw new IllegalArgumentException("Invalid Board Dimensions");
        return super.height;
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
        for (int i = 0; i < this.cells[row].length; i++)
            if (i != column && this.cells[row][i] == number) return true;
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
        for (int i = 0; i < this.cells.length; i++)
            if (i != row && this.cells[i][column] == number) return true;
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
                else if (this.cells[rowIndex][colIndex] == number) return true;
            }
        }
        return false;
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
        //row--; column--; // Normalize coordinates, use only if coordinates are not already normalized to 0 start index
        if (isInColumn(number,row,column) ||
                isInRow(number,row,column) ||
                isInSubsquare(number,row,column)) {
            return false;
        }
        int cellNum = this.cells[row][column];
        this.cells[row][column] = number;
        if (cellNum != 0) return true; // Number replaces existing number
        this.numbersAdded++; // Increments only when number is inserted into empty cell.
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
        //row--; column--; // Normalize coordinates, use only if coordinates are not already normalized to 0 start index
        int number = this.cells[row][column];
        if (number < 1 || number > this.SIZE) return false; // Return false if cell is empty or number does not belong.
        this.cells[row][column] = 0;
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
            if (dim != this.cells[i].length) {
                System.out.println("Board dimensions are incompatible.");
                return false;
            }

            for (int j = 0; j < dim; j++) {
                int number = this.cells[i][j];
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

                if (this.cells[i][j] == 0) System.out.print("   ");
                else System.out.printf(" %d ", this.cells[i][j]); //Print digit if present
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
            System.out.println(response);
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
     * //TODO: Documentation
     * @param boardSize
     * @return
     */
    public static SudokuBoard randPopulateBoard(int boardSize) {
        java.util.Random rand = new Random();
        SudokuBoard board = new SudokuBoard(boardSize);
        for (int i = 0; i < 20; i++) {
            int row = rand.nextInt(boardSize);
            int col = rand.nextInt(boardSize);
            int num = rand.nextInt(boardSize+1);
            if (board.insertNumber(num, row, col) && board.canAlterNumber(row, col))
                board.fixedNumbers.add(new Cell(row,col));
        }
        return board;
    }

    /**
     * //TODO: Documentation
     */
    public static SudokuBoard generateRandomBoard(int boardSize) {
        //TODO: Implement random board generator algorithm
        return new SudokuBoard(boardSize);
    }

    /**
     * @author Marco Soto
     * //TODO: Documentation
     * @return Returns true if the board is solvable and inserts solved board numbers, and false if the board is not solvable.
     */
    public boolean solveBoard() {
        //TODO: Optimize sudoku board solver
        System.out.println("\nSolving Board");
        if (!this.validateBoard()) {
            System.out.println("Solver Error(1): Board is not solvable.");
            return false;
        }
        int numCells = (int)Math.pow(this.getSize(), 2);
        LinkedList[][] grid = generatePossibleNumberGrid();
        printNumberGrid(grid);
        Stack<Cell> cellAltered = new Stack<>();
        int startNum = 1;
        while (this.numbersAdded < numCells) { //TODO: Remove this loop (redundant)
            for (int i = 0; i < this.getSize() && this.numbersAdded < numCells; i++) {
                for (int j = 0; j < this.getSize() && this.numbersAdded < numCells; j++) {
                    if (!this.canAlterNumber(i,j)) continue;
                    boolean success = false;
                    for (int ins = startNum; ins <= this.getSize(); ins++) {
                        //System.out.println("Inserting " + ins + " at (" + i + ", " + j + ")");
                        if (this.insertNumber(ins,i,j)) {
                            //System.out.println("Insert Succeeded!");
                            cellAltered.push(new Cell(i,j));
                            startNum = 1;
                            success = true;
                            break;
                        }
                        //System.out.println("Insert Failed");
                    }
                    if (success) continue;
                    if (cellAltered.isEmpty()) {
                        System.out.println("Solver Error(2): Board is not solvable.");
                        return false;
                    }
                    Cell top = cellAltered.pop();
                    i = top.row;
                    j = top.column-1;
                    startNum = this.getCell(i,j+1)+1; // Start at next number
                    this.removeNumber(top.row,top.column);
                    //System.out.printf("\tStack popped, back to (%d, %d)\n", i, j+1);
                    if (cellAltered.isEmpty()) System.out.println("\t\tStack is empty, at (" + i + ", " + j+1 + ")");
                }
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
     * //TODO: Documentation
     * @return
     */
    public LinkedList[][] generatePossibleNumberGrid() {
        LinkedList[][] grid = new LinkedList[this.getSize()][this.getSize()];
        for (int i = 0; i < this.getSize(); i++) {
            for (int j = 0; j < this.getSize(); j++) {
                grid[i][j] = new LinkedList<>();
                if (!this.canAlterNumber(i,j)) continue;
                for (int num = 1; num <= this.getSize(); num++) {
                    if (!isInRow(num,i,j) && !isInColumn(num,i,j) && !isInSubsquare(num,i,j)) grid[i][j].addLast(num);
                }
            }
        }
        return grid;
    }

    /**
     * //TODO: Documentation
     * @param grid
     */
    private static void printNumberGrid(LinkedList[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print("[ ");
                while (!grid[i][j].isEmpty())
                    System.out.print(grid[i][j].removeFirst() + ", ");
                System.out.print("], ");
            }
            System.out.println();
        }
    }

    /**
     * TODO: Documentation
     */
    public void clearBoard() {
        System.out.println("Clearing Board");
        for (int i = 0; i < this.getSize(); i++) {
            for (int j = 0; j < this.getSize(); j++)
                this.removeNumber(i,j);
        }
    }
}
/**
 * @author Marco Soto
 * Used privately only for the purpose of storing locations of fixed numbers provided by the json web service.
 */
class Cell {
    final int row;
    final int column;

    Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
