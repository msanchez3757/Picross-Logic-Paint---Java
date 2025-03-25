import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
    private Grid grid;
    private JButton[][] buttons;
    private JLabel[] rowClues;
    private JLabel[] colClues;
    private int size;
    private JLabel[][] colClueLabels;
    private JButton resetButton;
    private JComboBox<String> difficultySelector;
    private int mistakes = 0;
    private JLabel mistakeLabel;
    private int elapsedSeconds = 0;
    private Timer timer;
    private JLabel timerLabel;


    public GUI(int size){
        this.size = size;
        initializeGame(size);
    }

    private void startTimer(){
        elapsedSeconds = 0;
        timerLabel.setText("Time: 0s");
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Time: " + elapsedSeconds + "s");
        });
        timer.start();
    }

    private void stopTimer(){
        if (timer != null){
            timer.stop();
        }
    }

    private void initializeGame(int newSize){
        this.size = newSize;
        //stopTimer();
        //startTimer();
        mistakes = 0;

        grid = new Grid(size);
        //grid.presetGrid(); //test grid
        grid.generateRandomPuzzleImproved();
        buttons = new JButton[size][size];
        rowClues = new JLabel[size];
        colClues = new JLabel[size];

        getContentPane().removeAll(); // Clear previous UI components
        setLayout(new BorderLayout());

        setTitle("Logic Paint Puzzle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        // Give 2 rows for the Col label
        int clueRows = 2; // Adjust for bigger column clue display

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for Col
        JPanel colCluePanel = new JPanel(new GridLayout(clueRows + 1, size + 1)); // +1 for top-left spacer
        colClueLabels = new JLabel[clueRows][size];

        // **EMPTY LABEL FOR TOPLEFT IN ORDER TO FIX SHIFT**
        colCluePanel.add(new JLabel(""));

        // Add Column Clues
        for (int col = 0; col < size; col++) {
            colCluePanel.add(new JLabel("", SwingConstants.CENTER)); // Empty row for spacing
        }

        for (int row = 0; row < clueRows; row++) {
            colCluePanel.add(new JLabel("")); // Empty for row clue alignment
            for (int col = 0; col < size; col++) {
                colClueLabels[row][col] = new JLabel("", SwingConstants.CENTER);
                colCluePanel.add(colClueLabels[row][col]);
            }
        }

        mainPanel.add(colCluePanel, BorderLayout.NORTH);

        //Rows + Grid panel

        JPanel rowGridPanel = new JPanel(new GridLayout(size, size + 1));
        for (int row = 0; row < size; row++) {
            rowClues[row] = new JLabel(formatClue(grid.getRowClue(row)), SwingConstants.RIGHT);
            rowClues[row].setPreferredSize(new Dimension(40, 30)); // Ensure width
            rowGridPanel.add(rowClues[row]);

            for (int col = 0; col < size; col++) {
                buttons[row][col] = new JButton();
                //set every other row to be light blue and off white
                if (col % 2 == 0) {
                    buttons[row][col].setBackground(new Color(200, 220, 255)); // Light blue
                } else {
                    buttons[row][col].setBackground(new Color(240, 245, 255)); // Off-white
                }
                buttons[row][col].addActionListener(new ButtonClickListener(row, col));
                rowGridPanel.add(buttons[row][col]);
            }
        }
        mainPanel.add(rowGridPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        //Timer label
        //timerLabel = new JLabel("Time: 0s");

        //mistakes label
        mistakeLabel = new JLabel("Mistakes: 0");
        JPanel statusPanel = new JPanel();

        // **Difficulty Dropdown**
        String[] difficultyOptions = {"Easy (5x5)", "Medium (10x10)", "Hard (15x15)", "Very Hard (20x20)"};
        difficultySelector = new JComboBox<>(difficultyOptions);
        difficultySelector.addActionListener(e -> changeGridSize());

        //Buttons
        JPanel buttonPanel = new JPanel();
        //New Grid Button
        JButton newGridButton = new JButton("New Grid");
        newGridButton.addActionListener(e -> {
            generateNewPuzzle();
            updateRowClues();
        });
        //Reset/clear button
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetGrid());

        //Button panels
        //statusPanel.add(timerLabel);
        statusPanel.add(mistakeLabel);
        buttonPanel.add(newGridButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(difficultySelector);
        add(statusPanel,BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);


        updateColClues(); // align clues labels properly - stacked

        pack();
        setSize(700,700);
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void changeGridSize(){
        String selectedDifficulty = (String) difficultySelector.getSelectedItem();
        int newSize = switch (selectedDifficulty) {
            case "Easy (5x5)" -> 5;
            case "Medium (10x10)" -> 10;
            case "Hard (15x15)" -> 15;
            case "Very Hard (20x20)" -> 20;
            default -> 5;
        };
        
        //FOR UPDATING DROPBOX SELECTION TO MATCH CURRENT SIZE
        // Prevent unnecessary UI reset if size hasn’t changed
        if (newSize == size) return;

        // **Remove ActionListener Before Resetting UI**
        ActionListener[] listeners = difficultySelector.getActionListeners();
        for (ActionListener listener : listeners) {
            difficultySelector.removeActionListener(listener);
        }

        // **Reinitialize the game with the new grid size**
        initializeGame(newSize);

        // **Restore Dropdown Selection**
        difficultySelector.setSelectedItem(selectedDifficulty);

        // **Reattach ActionListener to Avoid Infinite Loop**
        difficultySelector.addActionListener(e -> changeGridSize());
    }

    private void generateNewPuzzle(){
        grid.generateRandomPuzzleImproved();
        grid.clearPlayerGrid();
        mistakes = 0;
        mistakeLabel.setText("Mistakes: 0");
        updateColClues();
        updateRowClues();
        updateGridDisplay();
    }

    private void updateRowClues(){
        for (int row = 0; row < size; row++) {
            rowClues[row].setText(formatClue(grid.getRowClue(row))); // Refresh row clues
        }
    }

    //updates the col clues to be stacked
    private void updateColClues() {
        for (int col = 0; col < size; col++) {
            int[] clueNumbers = grid.getColumnClue(col);
            String[] clues = formatClue(clueNumbers).split(" ");
            int rowOffset = colClueLabels.length - clues.length;
            for (int row = 0; row < colClueLabels.length; row++) {
                colClueLabels[row][col].setText(row >= rowOffset ? clues[row - rowOffset] : "");
            }
        }
    }

    //take array of clues and return a clean string
    private String formatClue(int[] clues) {
        if (clues.length == 0){
            return " ";
        }
        StringBuilder sb = new StringBuilder();
        for (int num : clues) {
            sb.append(num).append(" ");
        }
        return sb.toString().trim();
    }

    private class ButtonClickListener implements ActionListener {

        private int row, col;

        public ButtonClickListener(int row, int col){
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            grid.toggleCell(row, col);
            if (grid.getPlayerGrid()[row][col] != grid.getSolutionGrid()[row][col]) {
                mistakes++; // Increase mistake count
                mistakeLabel.setText("Mistakes: " + mistakes); // Update UI
            }
            updateGridDisplay();
            if (grid.isSolved()) {
                JOptionPane.showMessageDialog(null, "Solved with " + mistakes + " mistakes!");
            }
        }
    }

    private void updateGridDisplay() {
        boolean isPuzzleSolved = grid.isSolved(); // Check if the puzzle is solved

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                boolean isFilled = grid.getPlayerGrid()[row][col];
                boolean shouldBeFilled = grid.getSolutionGrid()[row][col];

                if (isPuzzleSolved) {
                    // If the puzzle is solved, ensure all correct cells are displayed properly
                    if (shouldBeFilled) {
                        buttons[row][col].setBackground(Color.BLACK);
                    } else {
                        buttons[row][col].setBackground((col % 2 == 0) ? new Color(200, 220, 255) : new Color(240, 245, 255));
                    }
                    buttons[row][col].setText(""); // Remove any "X" on solved puzzle
                } else {
                    // Normal game behavior
                    if (isFilled) {
                        if (isFilled == shouldBeFilled) {
                            buttons[row][col].setBackground(Color.BLACK);
                            buttons[row][col].setText(""); // Correct selection
                        } else {
                            buttons[row][col].setBackground(Color.GRAY); // Incorrect selection
                            buttons[row][col].setText("X"); // Show "X" for mistakes
                            buttons[row][col].setForeground(Color.WHITE);
                        }
                    } else {
                        // Reset incorrect cells when unselected
                        buttons[row][col].setBackground((col % 2 == 0) ? new Color(200, 220, 255) : new Color(240, 245, 255));
                        buttons[row][col].setText(""); // Remove "X"
                    }
                }
            }
        }
    }

    //clear the grid
    private void resetGrid(){
        grid.clearPlayerGrid();
        updateGridDisplay();
    }
}

