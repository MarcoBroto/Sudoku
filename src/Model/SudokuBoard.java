/**
 *
 */

package Model;

import Homework.HW_1.JavaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.InputMismatchException;

/**
 * @author Marco Soto
 *
 */
public class SudokuBoard extends Board {

    public final int SIZE;
    private Cell[] fixedNumbers;
    public int numbersAdded;

    public SudokuBoard(int boardSize) {
        super(isPerfectSquare(boardSize));
        this.SIZE = boardSize;
    }

    public int getSize() {
        if (super.height != super.width) throw new IllegalArgumentException();
        return super.height;
    }

    private static int isPerfectSquare(int size) {
        if (Math.sqrt(size) != (int)Math.sqrt(size))
            throw new IllegalArgumentException("Size parameter is not perfect square.");
        else
            return size;
    }

    private boolean checkRowForNumber(int number, int row, int column) {
        for (int i = 0; i < this.cells[row].length; i++)
            if (i != column && this.cells[row][i] == number) {
                System.out.printf("COL: %d\n",i+1); // (Debug)
                System.out.println("!!!This number is already in the row!!!");
                return true;
            }
        return false;
    }

    private boolean checkColumnForNumber(int number, int row, int column) {
        for (int i = 0; i < this.cells.length; i++)
            if (i != row && this.cells[i][column] == number) {
                System.out.printf("ROW: %d\n",i+1); // (Debug)
                System.out.println("!!!This number is already in the column!!!");
                return true;
            }
        return false;
    }

    private boolean checkSubSquareForNumber(int number, int row, int column) {
        int size = this.cells.length;
        int squareSize = (int)Math.sqrt(size);
        for (int i = 0; i < squareSize; i++) {
            for (int j = 0; j < squareSize; j++) {
                int rowIndex = (2*(row/squareSize)) + ((row + i) % squareSize);
                int colIndex = (2*(column/squareSize)) + ((column + i) % squareSize);
                if (rowIndex == row && colIndex == column) continue; //Ignore square being compared to
                else if (this.cells[rowIndex][colIndex] == number) {
                    System.out.printf("Index: (%d, %d)\nNumber: %d\n", rowIndex+1,colIndex+1,number);
                    System.out.println("!!!This number already exists in the subsquare!!!");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean insertNumber(int number, int row, int column) {
        if ((row > this.SIZE || row < 1) || (column > this.SIZE || column < 1)) {
            System.out.printf("Row: %d, Column: %d\n", row, column); // (Debug)
            throw new IllegalArgumentException();
        }
        row--; column--; // Normalize coordinates
        if (checkColumnForNumber(number,row,column) ||
                checkRowForNumber(number,row,column) ||
                checkSubSquareForNumber(number,row,column)) {
            return false;
        }
        int cellNum = this.cells[row][column];
        this.cells[row][column] = number;
        if (cellNum != 0) return false;
        this.numbersAdded++;
        return true;
    }

    public boolean removeNumber(int row, int column) {
        if ((row > this.SIZE || row < 1) || (column > this.SIZE || column < 1)) throw new IllegalArgumentException();
        row--; column--; // Normalize coordinates
        int number = this.cells[row][column];
        if (number < 1 || number > this.SIZE) return false;
        this.cells[row][column] = 0;
        this.numbersAdded--;
        return true;
    }

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

            for (int j = 0; j < this.cells[i].length; j++) {
                int number = this.cells[i][j];
                if (checkColumnForNumber(number, i, j) ||
                        checkRowForNumber(number, i, j) ||
                        checkSubSquareForNumber(number, i, j)) {
                    return false;
                }
            }
        }

        return true;
    }

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

    public static SudokuBoard generateRandomBoard(int boardSize, int difficulty) {
        SudokuBoard SB = new SudokuBoard(boardSize);

        String response = JavaClient.generateResponse(boardSize, difficulty);
        JSONTokener t = new JSONTokener(response);
        JSONObject json = new JSONObject(t);
        if (!json.getBoolean("response")) throw new InputMismatchException();
        int size = json.getInt("size");
        if (size != boardSize) throw new InputMismatchException();
        JSONArray squares = json.getJSONArray("squares");
        SB.fixedNumbers = new Cell[squares.length()];
        //System.out.println(squares); // (debug)

        for (int i = 0; i < squares.length(); i++) {
            JSONObject cell = squares.getJSONObject(i);
            System.out.println(cell); // (debug)
            int x = cell.getInt("x")+1;
            int y = cell.getInt("y")+1;
            int number = cell.getInt("value");
            SB.fixedNumbers[i] = new Cell(x,y);
            SB.insertNumber(number,x,y);
        }
        return SB;
    }

    public boolean canAlterNumber(int row, int column) {
        for (int i = 0; i < this.fixedNumbers.length; i++)
            if (row == this.fixedNumbers[i].row && column == this.fixedNumbers[i].column) return false;
        return true;
    }

    static private class Cell {
        final int row;
        final int column;

        Cell(int row, int column) {
           this.row = row;
           this.column = column;
           //System.out.printf("[Cell: (%d,%d)]\n",row, column); // (Debug)
        }
    }
}
