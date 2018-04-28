/************************************************************************
 * SudokuGenerator.java                                                 *
 * @author Created by Marco Soto                                        *
 * Sudoku game developed for UTEP CS 3331 Advanced OOP                  *
 *                                                                      *
 * This file provides a crude sudoku board generator for the use by the *
 * sudoku game application from CS 3331. Develops random boards and     *
 * verifies their solution and develops new puzzle combinations by      *
 * translating and rotating the given puzzle. Once the desired puzzles  *
 * are generated, the contents of each board are written to a file and  *
 * saved. This program is too time consuming for practical production   *
 * applications so the program is run seperately and the file outputted *
 * is included in the production applications required asset files.     *
 ************************************************************************/

package Model;

import java.io.File;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Random;

public class SudokuGenerator {
    /**
     * //TODO: Documentation
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Running Board Generator Program");
        generateRandomBoardsFile(4);
    }

    /**
     * //TODO: Documentation
     * @param boardSize
     * @return
     */
    public static void generateRandomBoardsFile(int boardSize) {
        System.out.println("--------------------------------------------------\n" +
                "Generating Random Boards File for boards of size " + boardSize +
                "\n--------------------------------------------------");
        String fileName = "/Users/msoto/Documents/workspace/Sudoku/src/Assets/boardList_" + boardSize + ".txt";
        File fName = new java.io.File(fileName);
        int boardsWritten = 0;
        int iteration = 0;
        int limit = 700;
        while (boardsWritten < limit) {
            System.out.println("Iteration: " + iteration);
            System.out.println("Boards Added: " + boardsWritten);
            SudokuBoard SB = populateRandomBoard(boardSize);
            if (SB.solveBoard()) {
                SB.clearBoard();
                LinkedList<SudokuBoard> boardCombinations = generateBoardCombinations(SB);
                for (SudokuBoard i: boardCombinations) {
                    SudokuBoard x = SudokuBoard.copy(i);
                    if (x.solveBoard()){
                        SudokuBoard.writeBoardToFile(i,fName);
                        boardsWritten++;
                    }
                    if (boardsWritten == limit) return;
                }
            }
            iteration++;
        }
    }

    /**
     * //TODO: Documentation
     * @param boardSize
     * @return
     */
    public static SudokuBoard populateRandomBoard(int boardSize) {
        java.util.Random rand = new Random();
        SudokuBoard board = new SudokuBoard(boardSize);
        int hintsAdded = 0;
        int minHints = (boardSize == 9) ? 17: 5;
        int maxHints = (boardSize == 9) ? 25: 7;
        int hints = rand.nextInt(maxHints-minHints+1)+minHints;
        while (hintsAdded < hints) {
            int row = rand.nextInt(boardSize);
            int col = rand.nextInt(boardSize);
            int num = rand.nextInt(boardSize)+1;
            if (board.insertFixedNumber(num, row, col)) hintsAdded++;
        }
        return board;
    }

    /**
     * //TODO: Documentation
     * @param SB
     * @return
     */
    public static LinkedList<SudokuBoard> generateBoardCombinations(SudokuBoard SB) {
        LinkedList<SudokuBoard> combinations = new LinkedList<>();
        int subSquareSize = (int)Math.sqrt(SB.getSize());
        SudokuBoard newBoard = SB;
        for (int k = 0; k <= 4; k++) {
            switch (k) {
                case 1:
                    newBoard = rotateBoardRight(SB);
                    break;
                case 2:
                    newBoard = rotateBoardLeft(SB);
                    break;
                case 3:
                    newBoard = flipBoardHorizontally(SB);
                    break;
                case 4:
                    newBoard = flipBoardVertically(SB);
                    break;
            }
            combinations.add(newBoard);
            for (int i = 1; i <= subSquareSize; i++) {
                for (int j = 1; j <= subSquareSize; j++) {
                    if (i != j && i < j) {
                        newBoard = swapSquareRow(SB,i,j);
                        combinations.add(newBoard);
                        newBoard = swapSquareColumn(SB,i,j);
                        combinations.add(newBoard);
                    }
                }
            }
        }
        return combinations;
    }

    /**
     * //TODO: Documenation
     * @param SB
     * @return
     */
    public static SudokuBoard rotateBoardLeft(SudokuBoard SB) {
        SudokuBoard rotatedBoard = new SudokuBoard(SB.getSize());
        for (int j = 0; j < SB.getSize(); j++) {
            int ind = SB.getSize()-1-j;
            for (int i = 0; i < SB.getSize(); i++) {
                int num = SB.getCell(i,j);
                rotatedBoard.insertNumber(num, ind, i);
            }
        }
        return rotatedBoard;
    }

    /**
     * //TODO: Documentation
     * @param SB
     * @return
     */
    public static SudokuBoard rotateBoardRight(SudokuBoard SB) {
        SudokuBoard rotatedBoard = new SudokuBoard(SB.getSize());
        for (int i = 0; i < SB.getSize(); i++) {
            int ind = SB.getSize()-1-i;
            for (int j = 0; j < SB.getSize(); j++) {
                int num = SB.getCell(i,j);
                rotatedBoard.insertNumber(num, j, ind);
            }
        }
        return rotatedBoard;
    }

    /**
     * //TODO: Documentation
     * @param SB
     * @return
     */
    public static SudokuBoard flipBoardVertically(SudokuBoard SB) {
        SudokuBoard foldedBoard = new SudokuBoard(SB.getSize());
        int halfSize = SB.getSize()/2;
        int midCol = SB.getSize()-1-halfSize;
        for (int i = 0; i < SB.getSize(); i++) {
            foldedBoard.insertNumber(SB.getCell(i,midCol), i, midCol);
            for (int j = 0; j < halfSize; j++) {
                int leftNum = SB.getCell(i,j);
                int rightIndex = SB.getSize()-1-j;
                int rightNum = SB.getCell(i, rightIndex);
                foldedBoard.insertNumber(leftNum, i, rightIndex);
                foldedBoard.insertNumber(rightNum, i, j);
            }
        }
        return foldedBoard;
    }

    /**
     * //TODO: Documentation
     * @param SB
     * @return
     */
    public static SudokuBoard flipBoardHorizontally(SudokuBoard SB) {
        SudokuBoard foldedBoard = new SudokuBoard(SB.getSize());
        int halfSize = SB.getSize()/2;
        int midRow = SB.getSize()-1-halfSize;
        for (int j = 0; j < SB.getSize(); j++) {
            foldedBoard.insertNumber(SB.getCell(midRow,j), midRow, j);
            for (int i = 0; i < halfSize; i++) {
                int topNum = SB.getCell(i,j);
                int bottomIndex = SB.getSize()-1-i;
                int bottomNum = SB.getCell(bottomIndex, j);
                foldedBoard.insertNumber(topNum, bottomIndex, j);
                foldedBoard.insertNumber(bottomNum, i, j);
            }
        }
        return foldedBoard;
    }

    /**
     * //TODO: Documenation
     * @param row1
     * @param row2
     * @param SB
     * @return
     */
    public static SudokuBoard swapSquareRow(SudokuBoard SB, int row1, int row2) {
        int subsquareSize = (int)Math.sqrt(SB.getSize());
        if (row1 == row2 || row1 < 1 || row2 < 1 || row1 > subsquareSize || row2 > subsquareSize)
            throw new InputMismatchException("Invalid Subsquare Row Swap Inputs");
        row1--; row2--; // Normalize Row Inputs
        SudokuBoard swappedBoard = new SudokuBoard(SB.getSize());
        int firstIndex1 = row1*subsquareSize;
        int firstIndex2 = row2*subsquareSize;
        int lastIndex1 = firstIndex1+subsquareSize-1;
        int lastIndex2 = firstIndex2+subsquareSize-1;
        for (int i = 0; i < SB.getSize(); i++) {
            int row1_index = firstIndex1 + i;
            int row2_index = firstIndex2 + i;
            for (int j = 0; j < SB.getSize(); j++) {
                if (i < subsquareSize) {// Insert Swapped Numbers
                    int num1 = SB.getCell(row1_index, j);
                    int num2 = SB.getCell(row2_index, j);
                    swappedBoard.insertNumber(num1, row2_index, j);
                    swappedBoard.insertNumber(num2, row1_index, j);
                }
                if ((i < firstIndex1 || i > lastIndex1) && (i < firstIndex2 || i > lastIndex2)) {// Insert Remaining Numbers
                        swappedBoard.insertNumber(SB.getCell(i,j),i,j);
                }
            }
        }
        return swappedBoard;
    }

    /**
     * //TODO: Documentation
     * @param col1
     * @param col2
     * @param SB
     * @return
     */
    public static SudokuBoard swapSquareColumn(SudokuBoard SB, int col1, int col2) {
        int subsquareSize = (int)Math.sqrt(SB.getSize());
        if (col1 == col2 ||col1 < 1 || col2 < 1 || col1 > subsquareSize || col2 > subsquareSize)
            throw new InputMismatchException("Invalid Subsquare Column Swap Inputs");
        col1--; col2--; // Normalize Input Columns
        SudokuBoard swappedBoard = new SudokuBoard(SB.getSize());
        int firstIndex1 = col1*subsquareSize;
        int firstIndex2 = col2*subsquareSize;
        int lastIndex1 = firstIndex1+subsquareSize-1;
        int lastIndex2 = firstIndex2+subsquareSize-1;
        for (int j = 0; j < SB.getSize(); j++) {
            int col1_index = firstIndex1 + j;
            int col2_index = firstIndex2 + j;
            for (int i = 0; i < SB.getSize(); i++) {
                if (j < subsquareSize) { // Insert Swapped Numbers
                    int num1 = SB.getCell(i, col1_index);
                    int num2 = SB.getCell(i, col2_index);
                    swappedBoard.insertNumber(num1, i, col2_index);
                    swappedBoard.insertNumber(num2, i, col1_index);
                }
                if ((j < firstIndex1 || j > lastIndex1) && (j < firstIndex2 || j > lastIndex2)) // Insert Remaining Numbers
                    swappedBoard.insertNumber(SB.getCell(i,j),i,j);
            }
        }
        return swappedBoard;
    }
}