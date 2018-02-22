package ViewControllers;

import oracle.jvm.hotspot.jfr.JFR;

import javax.swing.*;

public class MenuViewController {

    public MenuViewController() {
        displayMenu();
    }

    public static void main(String[] args) {
        new MenuViewController();
    }

    static void displayMenu() {
        JFrame menuFrame = new JFrame("Sudoku");
        JPanel menu = new JPanel();
        JLabel title = new JLabel("Welcome to Sudoku!");
        JLabel prompt = new JLabel("Select the board size you wish to play");
        JButton select_4 = new JButton("4 by 4");
        JButton select_9 = new JButton("9 by 9");

        menuFrame.setVisible(true);
        menuFrame.setSize(550,650);
        menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuFrame.add(menu);


        menu.add(title);
        menu.add(prompt);
        menu.add(select_4);
        menu.add(select_9);
    }
}
