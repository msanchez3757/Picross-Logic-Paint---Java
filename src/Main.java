import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI(5)); // Start a 5x5 Picross game
    }
}