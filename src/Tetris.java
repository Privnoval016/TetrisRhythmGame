import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * The Tetris class plays a game of Tetris, along with multiple additional features.
 *
 * Features:
 * Hard Drop (press space)
 * Block Hold (press C to toggle hold)
 * Wall Kick on Rotate
 * Block Shadows
 * Preview of Upcoming Pieces
 * T-Spin (Single and Triple Variants
 * Loss Detection
 * Background "Music"
 * Sound Effects (press M to toggle mute)
 * Score/Level System with Speed Up
 * Custom Block Animations
 *
 *
 * @author Pranav Sukesh
 * @version 3/13/2023
 */
public class Tetris implements ArrowListener
{
    private MyBoundedGrid<Block> grid;
    private BlockDisplay display;
    private Tetrad fallingTetrad;
    private int score, level;
    private Queue<Tetrad> nextTetrads;
    private ArrayList<Integer> randInts;
    private Tetrad heldTetrad;
    private boolean canHold, canShift, isSpaceClicked, mute, anim;



    /**
     * The main method for the class. It constructs a new Tetris game and begins gameplay.
     */
    public static void main(String[] args)
    {
        Tetris game = new Tetris();
        game.play();
    }

    /**
     * Constructs a new Tetris game. It begins the background music and  sets up the grid, queue,
     * and spawns the first block.
     */
    public Tetris()
    {
        try
        {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream
                    (new File("src/Audio/doodoodoodoo.wav"));
            Clip bgmusic = AudioSystem.getClip();
            bgmusic.open(inputStream);
            bgmusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
        catch(Exception e)
        {
            //do nothing
        }

        score = 0;
        level = 1;
        canHold = true;
        isSpaceClicked = false;
        canShift = true;
        anim = true;
        mute = false;
        grid = new MyBoundedGrid<Block>(20, 17);
        display = new BlockDisplay(grid);
        display.setTitle("Score: " + score + " Level: " + level + "");
        display.showBlocks();
        display.setArrowListener(this);

        for (int i = 0; i < grid.getNumRows(); i++)
        {
            Block wallBlock = new Block();
            wallBlock.setColor(Color.WHITE);
            wallBlock.putSelfInGrid(grid, new Location(i, 10));
        }

        for (int i = 0; i < 6; i++)
        {
            Block wallBlock = new Block();
            wallBlock.setColor(Color.WHITE);
            wallBlock.putSelfInGrid(grid, new Location(13, i+11));
        }

        nextTetrads = new ArrayDeque<Tetrad>();
        randInts = new ArrayList<Integer>();


        for (int i = 0; i < 7; i++)
        {
            randInts.add(i);
        }

        for (int i = 0; i < 4; i++)
        {
            int randInt = (int) (Math.random() * randInts.size());

            nextTetrads.add(new Tetrad(grid, new Location((3*i)+2, 13),
                    randInts.remove(randInt), this));
        }

        int randInt = (int) (Math.random() * randInts.size());

        fallingTetrad = new Tetrad(grid, new Location(1, 4), randInts.remove(randInt), this);
        fallingTetrad.updateShadow();
    }

    /**
     * Returns true if the sound effects should be muted, false otherwise.
     * @return  true if the sound effects should be muted, false otherwise.
     */
    public boolean getMute()
    {
        return mute;
    }

    /**
     * Returns true if animations should be active, false otherwise.
     * @return  true if animations should be active, false otherwise.
     */
    public boolean getAnim()
    {
        return anim;
    }

    /**
     * Toggles whether animations play or not (to lower lag).
     */
    public void haltAnims()
    {
        anim = !anim;
        if (!anim)
        {
            trailCleanUp();
        }
    }
    /**
     * Mutes the sound effects that play when a tetrad is dropped or a row is cleared.
     */
    public void muteSFX()
    {
        mute = !mute;
    }
    /**
     * Moves the falling tetrad left if possible. It also redraws the display.
     */
    public void moveLeft()
    {
        if (canShift)
        {
            fallingTetrad.translate(0, -1);
            display.showBlocks();
        }
    }

    /**
     * Moves the falling tetrad right if possible. It also redraws the display.
     */
    public void moveRight()
    {
        if (canShift)
        {
            fallingTetrad.translate(0, 1);
            display.showBlocks();
        }
    }

    /**
     * Moves the falling tetrad down if possible. It also redraws the display.
     */
    public void moveDown()
    {
        if (canShift)
        {
            fallingTetrad.translate(1, 0);
            score++;
            display.showBlocks();
        }
    }

    /**
     * Rotates the falling tetrad clockwise if possible. It also redraws the display.
     */
    public void rotateCW()
    {
        if (canShift)
        {
            fallingTetrad.rotate();
            display.showBlocks();
        }
    }

    /**
     * Rotates the falling tetrad clockwise if possible. It also redraws the display.
     */
    public void rotateCCW()
    {
        for (int i = 0; i < 3; i++)
            rotateCW();
    }

    /**
     * Moves the falling tetrad down until it cannot move any further. It also redraws the
     * display, updates the score, and activates the special animation.
     */
    public void hardDrop()
    {
        try
        {
            if (fallingTetrad != null && fallingTetrad.getCenterLocation().getCol() < 10)
            {
                score += (fallingTetrad.moveToBottom(display))*2;
                canHold = false;
                isSpaceClicked = true;

                display.showBlocks();

            }
        }
        catch (Exception e)
        {
            isSpaceClicked = false;
            hardDrop();
        }
    }

    /**
     * Swaps the falling tetrad with the held tetrad. If there is no held tetrad, it puts the
     * falling tetrad in the hold spot and generates a new tetrad at the top of the main grid.
     * It also redraws the display.
     */
    public void hold()
    {
        if (canHold)
        {
            if (heldTetrad == null)
            {
                heldTetrad = fallingTetrad;
                heldTetrad.translate(16 - heldTetrad.getCenterLocation().getRow(),
                        13 - heldTetrad.getCenterLocation().getCol());

                for (int i = 0; i < 4; i++)
                {
                    heldTetrad.removeShadows();
                }
                updateQueue();
                canHold = false;
            }
            else
            {
                canHold = !fallingTetrad.swapWithHeld(heldTetrad);
                canShift = !canHold;
            }
        }
        display.showBlocks();

    }

    /**
     * The method checks if the row is completed. A row is completed if all the blocks in the
     * row are not null.
     * @param row   The row to check
     * @return      True if the row is completed, false otherwise
     */
    private boolean isCompletedRow(int row)
    {
        for (int i = 0; i < 10; i++)
        {
            Block b = grid.get(new Location(row, i));
            if (b == null)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the given row from the grid. It does this by removing each block in the row from
     * the grid and moving each block above the row down one row.
     * @param row   The row to remove
     */
    private void clearRow(int row)
    {
        for (int i = 0; i < 10; i++)
        {
            grid.remove(new Location(row, i));
        }
        for (int i = row - 1; i >= 0; i--)
        {
            for (int j = 0; j < 10; j++)
            {
                Block b = grid.get(new Location(i, j));
                if (b != null)
                {
                    grid.remove(new Location(i, j));
                    grid.put(new Location(i + 1, j), b);
                }
            }
        }
    }

    /**
     * The method removes all the completed rows from the grid byt first checking if a row is
     * completed, then removing the row and shifting the blocks above it down if so. It also
     * plays the clearing sound effect.
     *
     * @return  The number of rows removed
     */
    private int clearCompletedRows()
    {
        int rowsRemoved = 0;
        for (int i = 0; i < grid.getNumRows(); i++)
        {
            if (isCompletedRow(i))
            {
                clearRow(i);
                rowsRemoved++;
                i--;

                if (!mute)
                    (new ClearThread()).start();
            }
        }

        return rowsRemoved;
    }


    /**
     * The method takes the tetrad at the front of the queue and makes it the falling tetrad.
     * It then adds a new tetrad to the back of the queue and moves all the other tetrads up
     * 2 rows.
     */
    private void updateQueue()
    {
        canHold = true;
        fallingTetrad = nextTetrads.remove();
        fallingTetrad.translate(-1, -9);
        Queue<Tetrad> temp = new ArrayDeque<Tetrad>();
        for (int i = 0; i < 3; i++)
        {
            temp.add(nextTetrads.remove());
        }

        for (int i = 0; i < 3; i++)
        {
            Tetrad t = temp.remove();
            t.translate(-3, 0);
            nextTetrads.add(t);
        }

        if (randInts.size() == 0)
        {
            for (int i = 0; i < 7; i++)
            {
                randInts.add(i);
            }
        }

        int randInt = (int) (Math.random() * randInts.size());
        nextTetrads.add(new Tetrad(grid, new Location(11, 13), randInts.remove(randInt), this));
        canShift = true;

    }

    /**
     * The method removes any extraneous shadows on the board by checking each tile in the grid,
     * and removing any blocks that are a shadow.
     */
    private void shadowCleanUp()
    {
        for (int i = 0; i < grid.getNumRows(); i++)
        {
            for (int j = 0; j < grid.getNumCols(); j++)
            {
                Block b = grid.get(new Location(i, j));
                if (b != null && b.getColor().equals(Tetrad.SHADOW))
                    b.removeSelfFromGrid();
            }
        }
    }

    /**
     * Removes all occurrences of a trail from the game board.
     */
    private void trailCleanUp()
    {
        for (int i = 0; i < grid.getNumRows(); i++)
        {
            for (int j = 0; j < grid.getNumCols(); j++)
            {
                Block b = grid.get(new Location(i, j));
                if (b != null && b.getColor().equals(Tetrad.TRAIL))
                    b.removeSelfFromGrid();
            }
        }
    }


    /**
     * The main gameplay loop of Tetris. It runs at 30 fps, and after a certain number of frames,
     * the program shifts the current tetrad down a row and clears any completed rows. Every
     * frame, the program checks to update other processes that need to run at a faster rate.
     */
    public void play()
    {
        int fps = 60;
        int waitTime = fps;
        int dropTimer = 0;
        while (true)
        {
            try
            {
                Thread.sleep(1000/fps);

                dropTimer++;

                level = (score / 3000) + 1;
                if (level < 16)
                    waitTime = (int) ((60*Math.exp(0.2*(1-level))/2));
                else
                    waitTime = 3;


                shadowCleanUp();

                if (fallingTetrad != null && fallingTetrad.getCenterLocation().getCol() > 10)
                {
                    fallingTetrad.removeShadows();
                    Tetrad temp = fallingTetrad;
                    fallingTetrad = heldTetrad;
                    heldTetrad = temp;

                    while (heldTetrad.getDirection() != Location.NORTH)
                    {
                        heldTetrad.rotate();
                    }
                }

                if (heldTetrad != null && heldTetrad.getCenterLocation().getCol() < 10)
                {
                    fallingTetrad.removeShadows();
                    Tetrad temp = fallingTetrad;
                    fallingTetrad = heldTetrad;
                    heldTetrad = temp;

                    while (heldTetrad.getDirection() != Location.NORTH)
                    {
                        heldTetrad.rotate();
                    }

                    heldTetrad.translate(18 - heldTetrad.getCenterLocation().getRow(),
                            13 - heldTetrad.getCenterLocation().getCol());

                }


                if (fallingTetrad != null && !fallingTetrad.canMoveDown(1))
                {
                    if (isSpaceClicked)
                    {
                        isSpaceClicked = false;
                        canShift = false;
                        dropTimer = waitTime;
                    }



                }


                if (dropTimer == waitTime)
                {
                    canShift = true;

                    if (fallingTetrad != null && !fallingTetrad.translate(1, 0))
                    {

                        canHold = false;

                        if (isGameOver())
                        {
                            System.err.println("rip bozo");
                            System.exit(0);
                        }

                        trailCleanUp();

                        int rowsRemoved = clearCompletedRows();


                        if (rowsRemoved == 1)
                        {
                            score += 100*level;
                        }
                        else if (rowsRemoved == 2)
                        {
                            score += 300*level;
                        }
                        else if (rowsRemoved == 3)
                        {
                            score += 500*level;
                        }
                        else if (rowsRemoved == 4)
                        {
                            score += 1000*level;
                        }

                        updateQueue();

                        canHold = true;
                    }

                    dropTimer = 0;
                }

                display.setTitle("Score: " + score + " Level: " + level + "");
                fallingTetrad.updateShadow();
                display.showBlocks();
            }
            catch (Exception e)
            {
                //do nothing
            }


        }
    }


    /**
     * Checks if the game is over. The game is over if there is a block at the location (1,4)
     * (the spawn location of the next tetrad).
     * @return  True if the game is over, false otherwise
     */
    private boolean isGameOver()
    {
        for (int i = 0; i <= 1; i++)
        {
            for (int j = 3; j <= 6; j++)
            {
                Block b = grid.get(new Location(i, j));
                if (b != null && !b.getColor().equals(Tetrad.TRAIL)
                        && !b.getColor().equals(Tetrad.SHADOW))
                    return true;
            }
        }
        return false;
    }

    /**
     * The method plays a sound effect after a row is cleared.
     */
    private void playClear()
    {
        try
        {

            AudioInputStream dream = AudioSystem.getAudioInputStream
                    (new File("src/Audio/OHDREAMMMM.wav"));
            Clip clear = AudioSystem.getClip();
            clear.open(dream);
            clear.start();
        }
        catch (Exception e)
        {
            //do nothing
        }
    }

    /**
     * A ClearThread is a Thread that plays the sound effect for clearing a row.
     * @author  Pranav Sukesh
     * @version 3/16/2023
     */
    private class ClearThread extends Thread
    {
        @Override
        public void run()
        {
            playClear();
        }
    }



}
