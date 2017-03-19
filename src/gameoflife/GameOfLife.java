
package gameoflife;

import java.io.*;
import java.util.Scanner;
import java.awt.*; //needed for graphics
import javax.swing.*; //needed for graphics
import static javax.swing.JFrame.EXIT_ON_CLOSE; //needed for graphics
        
public class GameOfLife extends JFrame{

    int numGenerations = 500; //Number of total generations
    int currGeneration = 1; //Current generation
    int startOfGrid = 0; //Defines the start of the grid (starting coordinate)
    Color aliveColor = Color.YELLOW; //Colour of an alive cell
    Color deadColor = Color.BLUE; //Colour of a dead cell
    
    String fileName = "Initial cells.txt"; //File to load first generation from

    int width = 900; //Width of the window in pixels
    int height = 900; //Height of the window in pixels
    int borderWidth = 100; //The width of the border around the main grid

    int numCellsX = 50; //Width of grid in cells
    int numCellsY = 50; //Height of grid in cells
    
    //Used to store boolean values indicating whether a celll is dead or alive
    boolean alive[][] = new boolean[numCellsY][numCellsX];
    //Used to update values for the next generation without messing up the current values
    boolean aliveNext[][] = new boolean[numCellsY][numCellsX];
    
    int cellSizeX = (width - 2 * borderWidth) / numCellsX; //The width of the cell
    int cellSizeY = (height - 2 * borderWidth) / numCellsY; //The height of the cell
    
    int labelX = width / 2; //X coordinate of the generation counter message 
    int labelY = borderWidth; //Y coordinate of the generation counter message 
    
    static int FPSMin = 1; //Minimum frames per second
    static int FPSMax = 10; //Maximum frames per second
    static int FPSInit = 5; //Initial frames per second
    //JSlider used to control the frames per second
    static JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, FPSMin, FPSMax, FPSInit);
    
    //METHODS
    //Plants the first generation of cells
    public void plantFirstGeneration() throws IOException {
        //Ensure all cells are set to dead/false
        makeEveryoneDead();
        //Plant first generation from file
        plantFromFile(fileName);
        
        //Or plant first generation using defined functions to create patterns
        plantBlock (20, 20, 5, 20);
        //plantGlider(1, 2, 4);
        plantGlider(10, 2, 4);
        //plantGlider(5, 20, 4);
        //plantGlider(1, 26, 3);
    }

    
    //Sets all cells to dead
    public void makeEveryoneDead() {
        for (int i = 0; i < numCellsX; i++) {
            for (int j = 0; j < numCellsY; j++) {
                alive[i][j] = false;
            }
        }
    }

    
    //Reads the first generation's alive cells from a file
    public void plantFromFile(String fileName) throws IOException {
        //Used to read from the desired file
        FileReader f = new FileReader(fileName);
        Scanner s = new Scanner(f);

        int x, y;
        
        //Format of file: "x-coord y-coord"
        //Loops throught the entire file
        while (s.hasNext()) {
            x = s.nextInt();
            y = s.nextInt();
            
            //Set the desired cell to be alive
            alive[y][x] = true;
        }
    }

    
    //Plants a solid rectangle of alive cells. Would be used in place of or in addition to plantFromFile()
    public void plantBlock(int startX, int startY, int numColumns, int numRows) {
        //Define the end column and row (find out which rows/columns must be traversed)
        int endCol = Math.min(startX + numColumns, numCellsX);
        int endRow = Math.min(startY + numRows, numCellsY);
        
        //Set all the cells in the rectangular region to be alive
        for (int i = startY; i < endRow; i++) {
            for (int j = startX; j < endCol; j++) {
                alive[i][j] = true;
            }
        }
    }

    
    //Plants a "glider" group, which is a cluster of living cells that migrates across the grid from 1 generation to the next
    //Direction can be "NE" (1), "NW" (2), "SW" (3), or "SE" (4)
    public void plantGlider(int startX, int startY, int direction) { 
        //Cells that are always needed to make a glider (direction does not affect these cells)
        alive[startY][startX] = true;
        alive[startY + 1][startX] = true;
        alive[startY + 2][startX] = true;
        
        //This switch decides which direction the glider will go, according to the argument given
        switch (direction) {
            //Northeast
            case 1: 
            {
                alive[startY][startX - 1] = true;
                alive[startY + 1][startX - 2] = true;
                break;
            }
            //Northwest
            case 2: 
            {
                alive[startY][startX + 1] = true;
                alive[startY + 1][startX + 2] = true;
                break;
            }
            //Southwest
            case 3: 
            {
                alive[startY + 2][startX + 1] = true;
                alive[startY + 1][startX + 2] = true;
                break;
            }
            //Southeast
            case 4: 
            {
                alive[startY + 2][startX - 1] = true;
                alive[startY + 1][startX - 2] = true;
                break;
            }
        }
    }

    
    //Applies the rules of The Game of Life to set the true-false values of the aliveNext[][] array,
    //based on the current values in the alive[][] array
    public void computeNextGeneration() {
        //Loop through all cells and update them in the aliveNext array
        for (int y = 0; y < numCellsY; y++){
            for (int x= 0; x < numCellsX; x++){
                //Find the number of living neighbors around the current cell
                int numNeighbors = countLivingNeighbors(x, y);
                //Apply rules for currently living cells
                if (alive[y][x]) {
                    //If it's too lonely or too crowded the cell will die
                    if (numNeighbors <= 1 || numNeighbors >= 4){
                        aliveNext[y][x] = false;
                    }
                    //If it has 2 or 3 neighbors, the cell stays alive
                    else{ 
                        aliveNext[y][x] = true;
                    }
                }
                //If the current cell has exactly 3 living neighbors, it will be
                //born in the next generation
                else if (numNeighbors == 3){
                    aliveNext[y][x] = true;
                }
                //Otherwise, make sure the cell stays dead
                else{
                    aliveNext[y][x] = false;
                }
            }
        }
    }

    
    //Overwrites the current generation's 2-D array with the values from the next generation's 2-D array
    public void plantNextGeneration() {
        //Loop through all of the 1D arrays within aliveNext (which is a 2D array)
        for (int y = 0; y < numCellsX; y++) {
            //Copy the 1D array from the "aliveNext" 2D array into the "alive" 2D array
            System.arraycopy(aliveNext[y], 0, alive[y], 0, numCellsY);
        }
    }

    
    //Counts the number of living cells adjacent to cell (x, y)
    public int countLivingNeighbors(int x, int y) {
        //Define the bounding area of the search (the row/columns directly surrounding the cell)
        //Ensures that there is not a ArrayIndexOutOfBoundsException due to negative indices
        //startOfGrid is set to 0 (the first index/coordinate of the grid)
        int rowStart  = Math.max(y - 1, startOfGrid);
        int rowFinish = Math.min(y + 1, alive.length - 1);
        int colStart  = Math.max(x - 1, startOfGrid);
        int colFinish = Math.min(x + 1, alive.length - 1);
        
        //Initialize a count variable to store the amount of living neighbors
        int count = 0;
        
        //Loop through the desired rows
        for (int curRow = rowStart; curRow <= rowFinish; curRow++) {
            //Loop through the desired columns
            for (int curCol = colStart; curCol <= colFinish; curCol++) {
                //If the current neighbor found is alive AND it is not the cell itself (that we are checking)
                if (alive[curRow][curCol] && !(curRow == y && curCol == x)){
                    //Increment the count of living neighbors
                    count++;
                }
            }
        }
        return count;
    }

    
    //Makes the pause between generations
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } 
        catch (Exception e) {}
    }

    
    //Displays the statistics at the top of the screen
    void drawLabel(Graphics g, int state) {
        g.setColor(Color.black);
        //Make sure the border is filled with black rectangles
        g.fillRect(0, 0, width, borderWidth);
        g.fillRect(0, 0, borderWidth, height-borderWidth);
        g.fillRect(width-borderWidth, 0, borderWidth, height-borderWidth);
        g.fillRect(0,height-borderWidth, width, borderWidth-60);
        //Display the text for statistics and FPS
        g.setColor(Color.yellow);
        g.drawString("Generation: " + state, labelX, labelY);
        g.drawString("Frames Per Second", width/2, height-borderWidth + (borderWidth-60)/2);
    }
    //Draws the JSlider at the bottom of the window
    void drawSlider(){
        //Set JSlider attributes
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setMajorTickSpacing(4);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        
        //Add JSlider to the main window (JFrame)
        add(framesPerSecond, BorderLayout.SOUTH);
    }

    
    //Draws the current generation of living cells on the screen
    @Override
    public void paint( Graphics g ) {
        int x, y, i, j;
        
        //Offset x and y by the border width
        x = borderWidth;
        y = borderWidth;
        
        //Draw slider and label
        drawSlider();
        drawLabel(g, currGeneration);
        
        //Go through the alive array and update all cells accordingly
        for (i = 0; i < numCellsX; i++) {
            for (j = 0; j < numCellsY; j++) {
                //Set colour according to the boolean value in the array
                if (alive[i][j] == true) {
                    g.setColor(aliveColor);
                }
                else {
                    g.setColor(deadColor);
                }
                
                //Fill in the cell with the desired colour and draw its border
                g.fillRect(x, y, cellSizeX, cellSizeY);
                
                //Draw the border around the cell
                g.setColor(Color.black);
                g.drawRect(x, y, cellSizeX, cellSizeY);

                //Increment x (move on to the next cell's x coordinate)
                x += cellSizeX;   
            }
            //Reset x to the start of the row
            x = borderWidth;
            //Increment y (the coordinate of the next row)
            y += cellSizeY;
        }
        //Increment the generation number
        currGeneration++;
    }


    //Sets up the JFrame screen
    public void initializeWindow() {
        //Set JFrame attributes
        setTitle("Game of Life Simulator");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.black);
        
        //Draw FPS slider
        drawSlider(); 
        setVisible(true); //Calls paint() for the first time
    }
    
    //Main algorithm
    public static void main(String args[]) throws IOException {
        //Instantiate the class
        GameOfLife currGame = new GameOfLife();

        currGame.initializeWindow(); //Initialize the window
        currGame.plantFirstGeneration(); //Sets the initial generation of living cells, either by reading from a file or creating them algorithmically
        
        //Loop for the desired number of generations
        for (int i = 1; i <= currGame.numGenerations; i++) {
            //Sleep for animation effect
            GameOfLife.sleep(1000/framesPerSecond.getValue());
            //Calculate the boolean values of the next generation
            currGame.computeNextGeneration();
            //Replace the current generation with the next generation's boolean values
            currGame.plantNextGeneration();
            //Repaint the screen to show the next generation to the user
            currGame.repaint();
        }
        
    }   
} //End of GameOfLife class
