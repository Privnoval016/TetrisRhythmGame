import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A tetrad is a set of four blocks that act as the game pieces in Tetris. Each tetrad has a
 * specific shape and color. It can be shifted in any direction, rotated, dropped down as far as
 * possible, and be held. It also has a shadow that shows where the tetrad will land.
 *
 * @author Pranav Sukesh
 * @version 3/13/2023
 */
public class Tetrad
{
    public static final Color SHADOW = new Color(120, 120, 120, 120);
    public static final Color TRAIL = new Color(255, 255, 255, 120);
    private Block[] blocks;
    private MyBoundedGrid<Block> grid;
    private Color color;
    private Semaphore lock;
    private int direction;

    private Block[] shadowBlocks;
    private ArrayList<Block> trailBlocks;
    private Tetris game;


    /**
     * Constructs a new tetrad with the given blocks in the given grid. It is made in one of the
     * seven regular tetrad shapes, given by the user with each shape being a different color. It
     * appears in the top row of the grid, centered horizontally.
     * @param grid      The grid in which this tetrad is located
     */
    public Tetrad(MyBoundedGrid<Block> grid, Location center, int randShape, Tetris t)
    {
        lock = new Semaphore(1, true);
        game = t;
        direction = 0;
        blocks = new Block[4];
        trailBlocks = new ArrayList<Block>();
        for (int i = 0; i < 4; i++)
        {
            blocks[i] = new Block();
        }
        this.grid = grid;
        Color[] colors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.RED,  Color.BLUE,
                Color.GREEN, Color.ORANGE};

        color = new Color(colors[randShape].getRed(), colors[randShape].getGreen(),
                colors[randShape].getBlue(), 180);

        for (int i = 0; i < blocks.length; i++)
        {

            blocks[i].setColor(color);
        }


        /* I, T, O, Z, L, R, J */
        Location[][] shapes = {
                {new Location(center.getRow(), center.getCol()),
                        new Location(center.getRow(), center.getCol() - 1),
                        new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow(), center.getCol() + 2)},
                {center, new Location(center.getRow(), center.getCol() - 1),
                        new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow() - 1, center.getCol())},
                {center, new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow() - 1, center.getCol()),
                        new Location(center.getRow() - 1, center.getCol() + 1)},
                {center, new Location(center.getRow() - 1, center.getCol() - 1),
                        new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow() - 1, center.getCol())},
                {center, new Location(center.getRow(), center.getCol() - 1),
                        new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow() - 1, center.getCol() - 1)},
                {center, new Location(center.getRow(), center.getCol() - 1),
                        new Location(center.getRow() - 1, center.getCol()),
                        new Location(center.getRow() - 1, center.getCol() + 1)},
                {center, new Location(center.getRow(), center.getCol() - 1),
                        new Location(center.getRow(), center.getCol() + 1),
                        new Location(center.getRow() - 1, center.getCol() + 1)}
        };

        Location[] loc = shapes[randShape];

        shadowBlocks = new Block[4];
        for (int i = 0; i < 4; i++)
        {
            shadowBlocks[i] = new Block();
            shadowBlocks[i].setColor(SHADOW);
        }

        addToLocations(grid, loc);
    }


    /**
     * The method returns the direction of this tetrad.
     * @return  The direction of this tetrad
     */
    public int getDirection()
    {
        return direction;
    }

    /**
     * The method returns the locations of the blocks of this tetrad.
     * @return  The locations of the blocks of this tetrad
     */
    public Location[] getLocations()
    {
        Location[] locs = new Location[blocks.length];
        for (int i = 0; i < blocks.length; i++)
        {
            locs[i] = blocks[i].getLocation();
        }
        return locs;
    }

    public Location getCenterLocation()
    {
        return blocks[0].getLocation();
    }

    /**
     * The method adds the blocks of this tetrad to the grid at the given locations. It does this
     * by putting each block in the grid at the corresponding location in the array.
     *
     * @precondition    blocks are not in any grid, and locs.length == blocks.length
     * @postcondition   blocks are in the grid at the given locations
     * @param gr        The grid in which this tetrad is located
     * @param locs      The locations of the blocks of this tetrad
     */
    private void addToLocations(MyBoundedGrid<Block> gr, Location[] locs)
    {
        for (int i = 0; i < locs.length; i++)
        {
            blocks[i].putSelfInGrid(gr, locs[i]);
        }
    }

    /**
     * The method removes the blocks of this tetrad from the grid and returns the locations of
     * the blocks. It does this by removing each block from the grid and storing the location
     * of each block in an array.
     *
     * @return  The locations of the blocks of this tetrad
     */
    private Location[] removeBlocks()
    {
        Location[] oldLocs = new Location[blocks.length];
        for (int i = 0; i < blocks.length; i++)
        {
            oldLocs[i] = blocks[i].getLocation();
            blocks[i].removeSelfFromGrid();
        }
        return oldLocs;

    }

    /**
     * The method returns true if each of the locations given is valid and empty in the grid. It
     * does this by checking if each location is valid and if the grid contains a block at that
     * location.
     *
     * @param gr    The grid in which this tetrad is located
     * @param locs  The locations to check
     * @return      True if each of the locations is valid and empty in the grid, false otherwise
     */
    private boolean areEmpty(MyBoundedGrid<Block> gr, Location[] locs)
    {
        for (Location l: locs)
        {
            if (!gr.isValid(l) || !(gr.get(l) == null || gr.get(l).getColor().equals(SHADOW) ||
                    gr.get(l).getColor().equals(TRAIL)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * The method attempts to move this tetrad based on the given delta row and delta column.
     * If the move is possible, the tetrad is moved and true is returned. If the move is not
     * possible, the tetrad is not moved and false is returned. It does this by removing the
     * tetrad from the grid, calculating the new locations of the blocks, checking if the new
     * locations are valid and empty, and if so, adding the tetrad to the grid at the new locations.
     *
     * @param deltaRow  The change in row
     * @param deltaCol  The change in column
     * @return          True if the tetrad can be moved in the given direction, false otherwise
     */
    public boolean translate(int deltaRow, int deltaCol)
    {
        try
        {
            lock.acquire();
            Location[] oldLocs = removeBlocks();
            Location[] newLocs = shiftLocs(oldLocs, deltaRow, deltaCol);

            if (areEmpty(grid, newLocs))
            {
                addToLocations(grid ,newLocs);
                return true;
            }
            else
            {
                addToLocations(grid, oldLocs);
                return false;
            }
        }
        catch (InterruptedException e)
        {
            //did not modify the tetrad
            return false;
        }
        finally
        {
            lock.release();
        }


    }

    /**
     * The method attempts to rotate this tetrad clockwise. If the rotation is possible, the
     * tetrad is rotated and true is returned. If the rotation is not possible, the tetrad is
     * not rotated and false is returned. It does this by removing the tetrad from the grid,
     * calculating the new locations of the blocks, checking if the new locations are valid
     * and empty, and if so, adding the tetrad to the grid at the new locations. If the rotation
     * occurs next to a wall, it kicks off of the wall and is rotated a column to the left or right
     *
     * @return  True if the tetrad can be rotated, false otherwise
     */
    public boolean rotate()
    {
        try
        {
            lock.acquire();
            Color lightYellow = new Color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(),
                    Color.YELLOW.getBlue(), 180);
            Location[] oldLocs = removeBlocks();
            Location[] newLocs = new Location[oldLocs.length];
            Location center = oldLocs[0];
            for (int i = 0; i < oldLocs.length; i++)
            {

                newLocs[i] = new Location(center.getRow() - center.getCol() + oldLocs[i].getCol(),
                        center.getRow() + center.getCol() - oldLocs[i].getRow());
            }

            if (areEmpty(grid, newLocs) && !color.equals(lightYellow))
            {
                addToLocations(grid, newLocs);
                direction = (direction + 90) % 360;
                return true;
            }

            int[][] shifts = {{0, -1},
                    {0, 1},
                    {0, -2},
                    {0, 2},
                    {2, -1},
                    {2, 1},
                    {1, -1},
                    {1, -1}};

            for (int i = 0; i < shifts.length; i++)
            {
                Location[] shiftedLocs = shiftLocs(newLocs,shifts[i][0], shifts[i][1]);
                if (areEmpty(grid, shiftedLocs) && !color.equals(lightYellow))
                {
                    addToLocations(grid, shiftedLocs);
                    direction = (direction + 90) % 360;
                    return true;
                }
            }


            addToLocations(grid, oldLocs);
            return false;

        }
        catch (InterruptedException e)
        {
            //did not modify the tetrad
            return false;
        }
        finally
        {
            lock.release();
        }

    }

    /**
     * The method shifts the given locations by the given deltaRow and deltaCol.
     * @param oldLocs   The locations to be shifted
     * @param deltaRow  The change in row
     * @param deltaCol  The change in column
     * @return          A new array of locations that are shifted correctly
     */
    private Location[] shiftLocs(Location[] oldLocs, int deltaRow, int deltaCol)
    {
        Location[] shiftedLocs = new Location[oldLocs.length];
        for (int i = 0; i < oldLocs.length; i++)
        {
            shiftedLocs[i] =
                    new Location(oldLocs[i].getRow()+deltaRow, oldLocs[i].getCol()+deltaCol);
        }
        return shiftedLocs;
    }

    /**
     * The method moves the tetrad to the bottom of the grid. It does this by determining the
     * distance between the tetrad and its lowest possible position, then moving the tetrad there
     * It also plays a visual and sound effect.
     *
     * @return  The number of rows the tetrad moves down
     */
    public int moveToBottom(BlockDisplay display)
    {

        try
        {
            lock.acquire();
            MyBoundedGrid<Block> gr = blocks[0].getGrid();
            Location[] origLocs = removeBlocks();
            Location[] locs = new Location[origLocs.length];

            for (int i = 0; i < origLocs.length; i++)
            {
                locs[i] = new Location(origLocs[i].getRow(), origLocs[i].getCol());
            }


            int minRows = Integer.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                int dist = 0;
                Location temp = new Location(locs[i].getRow() + dist, locs[i].getCol());
                while (gr.isValid(temp) && areEmpty(gr, new Location[]{temp})) {
                    dist++;
                    temp = new Location(locs[i].getRow() + dist, locs[i].getCol());
                }
                if (dist < minRows) {
                    minRows = dist;
                }
            }

            for (int i = 0; i < 4; i++)
            {
                locs[i] =
                        new Location(locs[i].getRow() + minRows - 1, locs[i].getCol());
            }


            int min = 10;
            int max = 0;
            int row = 0;
            for (int i = 0; i < locs.length; i++)
            {
                Location l = locs[i];
                min = Math.min(l.getCol(), min);
                max = Math.max(l.getCol(), max);
                row = Math.max(l.getRow(), row);
            }

            dropEffect(min, max-min+1, row, display);

            addToLocations(gr, locs);

            return minRows - 1;

        }
        catch (InterruptedException e)
        {
            //did not modify the tetrad
            return 0;
        }
        finally
        {
            lock.release();
        }
    }

    /**
     * The method returns true if the tetrad can move down by the given deltaRow, and false
     * otherwise
     *
     * @param deltaRow  The change in row
     * @return          True if the tetrad can move down by the deltaRow, false otherwise
     */
    public boolean canMoveDown(int deltaRow)
    {
        try
        {
            lock.acquire();
            Location[] oldLocs = removeBlocks();
            Location[] newLocs = new Location[oldLocs.length];
            for (int i = 0; i < oldLocs.length; i++)
            {
                newLocs[i] = new Location(oldLocs[i].getRow() + deltaRow,
                        oldLocs[i].getCol());
            }

            if (areEmpty(grid, newLocs))
            {
                addToLocations(grid, oldLocs);
                return true;
            }
            else
            {
                addToLocations(grid, oldLocs);
                return false;
            }
        }
        catch (InterruptedException e)
        {
            //did not modify the tetrad
            return false;
        }
        finally
        {
            lock.release();
        }
    }


    /**
     * The method removes the shadow of this tetrad from the grid if the blocks are stored in a
     * grid. It returns the locations of the blocks that were removed.
     *
     * @return  The locations of the blocks that were removed
     */
    public Location[] removeShadows()
    {
        Location[] locs = new Location[4];
        for (int i = 0; i < 4; i++)
        {
            locs[i] = shadowBlocks[i].getLocation();
            shadowBlocks[i].removeSelfFromGrid();
        }

        return locs;

    }

    /**
     * The method adds the shadow of this tetrad to the grid at the given locations.
     *
     * @param gr    The grid to add the shadow to
     * @param locs  The locations to add the shadows at
     */
    public void addShadows(MyBoundedGrid<Block> gr, Location[] locs)
    {
        for (int i = 0; i < locs.length; i++)
        {
            shadowBlocks[i].putSelfInGrid(gr, locs[i]);
        }
    }


    /**
     * The method updates the shadow of this tetrad. It does this by removing the shadow from
     * the grid, removing the blocks from the grid, calculating the new locations of the shadow,
     * checking if the new locations are valid and empty, and if so, adding the shadow to the
     * grid at the new locations, and finally adding the blocks back to the grid.
     *
     * @return True if the shadow was updated, false otherwise
     */
    public boolean updateShadow()
    {
        try
        {
            lock.acquire();
            MyBoundedGrid<Block> gr = blocks[0].getGrid();
            removeShadows();
            Location[] shadowLocs = removeBlocks();
            Location[] origLocs = new Location[shadowLocs.length];
            for (int i = 0; i < shadowLocs.length; i++)
            {
                origLocs[i] = new Location(shadowLocs[i].getRow(), shadowLocs[i].getCol());
            }


            int minRows = Integer.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                int dist = 0;
                Location temp = new Location(origLocs[i].getRow() + dist, origLocs[i].getCol());
                while (gr.isValid(temp) && areEmpty(gr, new Location[]{temp})) {
                    dist++;
                    temp = new Location(origLocs[i].getRow() + dist, origLocs[i].getCol());
                }
                if (dist < minRows) {
                    minRows = dist;
                }
            }

            for (int i = 0; i < 4; i++)
            {
                shadowLocs[i] =
                        new Location(shadowLocs[i].getRow() + minRows - 1, shadowLocs[i].getCol());
            }
            addShadows(gr, shadowLocs);

            addToLocations(gr, origLocs);

            return true;


        }
        catch (InterruptedException e)
        {
            //did not modify the tetrad
            return false;
        }
        finally
        {
            lock.release();
        }
    }


    /**
     * The method moves this tetrad to the holding area and rotates it to face north. It also
     * moves the other tetrad to the top of the Tetris grid and rotates it to face north.
     * @param other The other tetrad
     * @return      True if the swap was successful, false otherwise
     */
    public boolean swapWithHeld(Tetrad other)
    {
        try {

            while (other.getDirection() != Location.NORTH) {
                other.rotate();
            }


            Location[] locs = getLocations();
            Location center = getCenterLocation();

            removeBlocks();

            while (getDirection() != Location.NORTH)
            {
                for (int i = 0; i < 4; i++)
                {
                    locs[i] = new Location(center.getRow() - center.getCol() + locs[i].getCol(),
                            center.getRow() + center.getCol() - locs[i].getRow());
                }
                direction = (direction + 90) % 360;
            }
            locs = shiftLocs(locs, 16 - center.getRow(),13 - center.getCol());

            other.translate(1 - other.getCenterLocation().getRow(),
                    4 - other.getCenterLocation().getCol());

            addToLocations(grid, locs);

            removeShadows();

            return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }

    /**
     * The method plays an effect for the dropped tetrad. The columns with this tetrad light up
     * temporarily, while the tetrad itself turns white temporarily.
     * while the
     * @param min       The leftmost column with this tetrad in it.
     * @param size      The horizontal length of the tetrad
     * @param row       The row that the bottom of this tetrad is in
     * @param display   The display to update
     */
    private void dropEffect(int min, int size, int row, BlockDisplay display)
    {

        if (!game.getMute())
        {
            try {
                AudioInputStream boom = AudioSystem.getAudioInputStream
                        (new File("src/Audio/vine-boom.wav"));
                Clip sfx1 = AudioSystem.getClip();
                sfx1.open(boom);
                sfx1.start();
            } catch (Exception e) {
                //do nothing
            }
        }

        if (game.getAnim())
        {
            for (int i = min; i < min + size; i++) {
                for (int j = 0; j < row; j++)
                    if (grid.isValid(new Location(j, i)) && grid.get(new Location(j, i)) == null) {
                        Block b = new Block();
                        b.setColor(TRAIL);
                        b.putSelfInGrid(grid, new Location(j, i));
                    }
            }

            for (int i = 0; i < 4; i++) {
                Block b = blocks[i];
                b.setColor(Color.WHITE);
            }
            display.showBlocks();
            DropThread t = new DropThread();
            t.start();
        }

    }

    /**
     * The method waits for 33 milliseconds (1 frame) and restores the color of the tetrad, as
     * well as the trail it leaves, to its original color. It also plays a sound effect.
     */
    private void returnColor()
    {
        try
        {

            Thread.sleep(33);
        }
        catch (Exception e)
        {

        }

        for (int j = 0; j < 4; j++)
        {
            Block b = blocks[j];
            b.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        }

        for (int i = 0; i < grid.getNumRows(); i++)
        {
            for (int j = 0; j < grid.getNumCols(); j++)
            {
                Block b = grid.get(new Location(i, j));
                if (b != null && b.getColor().equals(TRAIL))
                    b.removeSelfFromGrid();
            }
        }


    }


    /**
     * A DropThread is a subclass of Thread that restores the color of the tetrad after 1 frame
     * (used to apply an animation to the tetrad)
     *
     * @author  Pranav Sukesh
     * @version 3/16/2023
     */
    private class DropThread extends Thread
    {
        public void run()
        {
            try
            {
                returnColor();
            }
            catch (Exception e)
            {
                //do nothing
            }
        }
    }



}
