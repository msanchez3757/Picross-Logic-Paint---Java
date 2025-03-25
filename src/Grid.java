import java.util.Arrays;
import java.util.Random;

public class Grid {

    //difficulty from 0 to 3 shown below
    //5x5, 10x10, 15x15, 20x20
    private int size;
    private boolean[][] solutionGrid;
    private boolean[][] playerGrid;
    private int[][] rowClues;
    private  int[][] colClues;

    public Grid(int size){
        this.size = size;
        solutionGrid = new boolean[size][size];
        playerGrid = new boolean[size][size];
        rowClues = new int[size][];
        colClues = new int[size][];
    }

    //preset 5x5 grid for testing
    public void presetGrid(){
        //hardcoded 5x5 grid for testing
        //based on miku's hairpin 39393939
        /*
                4 1 5 2/1 4
            1/2|-----------
            1/3|
          1 1 1|
          1 1 1|
              4|
         */
        solutionGrid = new boolean[][] {
                {true, false, true, true, false},
                {true, false, true, true, true},
                {true, false, true, false, true},
                {true, false, true, false, true},
                {false, true, true, true, true}
        };
        generateClues();
    }

    //generate a random grid - each cell being a 50% of being filled in
    public void generateRandomPuzzle(){
        Random rand = new Random();

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                solutionGrid[i][j] = rand.nextDouble() < 0.5; //gives cells a 50% for being filled
            }
        }
        generateClues();
    }

    public void generateRandomPuzzleImproved(){
        solutionGrid = new boolean[size][size];
        playerGrid = new boolean[size][size];

        Random rand = new Random();

        //temp empty grid
        boolean[][] tempGrid = new boolean[size][size];

        //generate random structured pattern
        for (int row = 0; row < size; row++){
            if (rand.nextDouble() < 0.6){ //60% for rows
                int start = rand.nextInt(size / 2); //rand start
                int length = rand.nextInt(size / 2); //rand end
                for (int i = start; i < Math.min(size, start + length); i++){
                    tempGrid[row][i] = true;
                }
            }
        }

        //ensure Cols remain solvable
        for(int col = 0; col < size; col++){
            int filledCount = 0;
            for(int row = 0; row < size; row++){
                if(tempGrid[row][col]){
                    filledCount++;
                }
            }
            if (filledCount == 0 && rand.nextDouble() < 0.5){ //add cols if needed
                int start = rand.nextInt(size);
                int length = rand.nextInt(size / 2) + 1;
                for(int i = start; i < Math.min(size, size + length); i++){
                    tempGrid[i][col] = true;
                }
            }
        }

        solutionGrid = tempGrid;
        generateClues();
        if(!isSolvable()) {
            generateRandomPuzzleImproved();
        }
    }

    private boolean isSolvable(){
        boolean[][] tempGrid = new boolean[size][size];

        //attempt to 'solve' the puzzle using the generated clues
        for(int i = 0; i < size; i++){
            int[] rowClue = getRowClue(i);
            int[] colClue = getColumnClue(i);

            if(rowClue.length == 0 || colClue.length == 0){
                return false; //row or col has no clues
            }

            //invalid clues
            for(int num : rowClue){
                if (num > size){
                    return false;
                }
            }
            for(int num : colClue){
                if (num > size){
                    return false;
                }
            }
        }
        return true;
    }

    //generates the clues of the grid
    //not to be confused with hints
    //this generates the numbers on top and side of the puzzle used to solve it
    private void generateClues() {
        rowClues = calculateClues(solutionGrid);
        //to generate col - we can transpose the grid to flip row and cols
        colClues = calculateClues(transposeGrid(solutionGrid));
    }

    //parse through the solution grid and count the filled/trues
    //generate an int[][] of the clues
    private int[][] calculateClues(boolean[][] grid) {
        int[][] clues = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            int count = 0;
            StringBuilder clue = new StringBuilder();
            for (boolean cell : grid[i]) {
                if (cell) {
                    count++;
                } else if (count > 0) {
                    clue.append(count).append(" ");
                    count = 0;
                }
            }
            if (count > 0) {
                clue.append(count);
            }
            String[] parts = clue.toString().trim().split(" ");
            clues[i] = parts.length > 0 && !parts[0].isEmpty()
                    ? Arrays.stream(parts).mapToInt(Integer::parseInt).toArray()
                    : new int[]{0}; // Default to [0] if no clues
        }
        return clues;
    }

    private boolean[][] transposeGrid(boolean[][] grid) {
        boolean[][] transposed = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                transposed[j][i] = grid[i][j];
            }
        }
        return transposed;
    }

    public void toggleCell(int row, int col){
        playerGrid[row][col] = !playerGrid[row][col];
    }

    public boolean isSolved(){
        return Arrays.deepEquals(solutionGrid, playerGrid);
    }

    public int getSize() {
        return size;
    }

    public boolean[][] getPlayerGrid() {
        return playerGrid;
    }

    public int[] getRowClue(int row) {
        return rowClues[row];
    }

    public int[] getColumnClue(int col) {
        return colClues[col];
    }

    public void clearPlayerGrid() {
        playerGrid = new boolean[size][size];
    }

    public boolean[][] getSolutionGrid() {
        return solutionGrid;
    }
}
