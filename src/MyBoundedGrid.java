import java.util.ArrayList;
/**
 * A MyBoundedGrid is a rectangular grid with a finite number of rows and columns.
 * Each location in a MyBoundedGrid is identified by a row and a column number, and you can store
 * an object at each location.  The rows and columns are numbered starting from 0.
 *
 * @param <E> the type of object that can be stored in this grid
 *
 * @author Pranav Sukesh
 * @version 3/8/2023
 */
public class MyBoundedGrid<E>
{
    private E[][] grid;
    private int rows;
    private int cols;

    /**
     * Constructs an empty MyBoundedGrid with the given dimensions.
     * @param rows the number of rows in this MyBoundedGrid
     * @param cols the number of columns in this MyBoundedGrid
     */
    public MyBoundedGrid(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
        grid = (E[][]) new Object[rows][cols];
    }


    /**
     * Returns the number of rows in this MyBoundedGrid.
     * @return the number of rows in this MyBoundedGrid
     */
    public int getNumRows()
    {
        return rows;
    }

    /**
     * Returns the number of columns in this MyBoundedGrid.
     * @return the number of columns in this MyBoundedGrid
     */
    public int getNumCols()
    {
        return cols;
    }

    /**
     * Determines whether a location is valid in this MyBoundedGrid.
     *
     * @param loc the location to check
     * @return true if loc is valid in this MyBoundedGrid
     */
    public boolean isValid(Location loc)
    {
        if (loc == null)
        {
            return false;
        }

        if (loc.getRow() >= 0 && loc.getRow() < rows && loc.getCol() >= 0 && loc.getCol() < cols)
        {
            return true;
        }
        return false;
    }

    /**
     * Puts an object at a location in this MyBoundedGrid.
     * @param loc the location to put the object at
     * @param obj the object to put at location loc
     * @return the object that was previously at location loc (or null if the location was empty)
     */
    public E put(Location loc, E obj)
    {
        if (isValid(loc))
        {
            E old = (E) grid[loc.getRow()][loc.getCol()];
            grid[loc.getRow()][loc.getCol()] = obj;
            return old;
        }
        return null;
    }

    /**
     * Removes the object at a location from this MyBoundedGrid.
     * @param loc the location to remove the object from
     * @return the object that was removed (or null if the location was empty)
     */
    public E remove(Location loc)
    {
        if (isValid(loc))
        {
            E old = (E) grid[loc.getRow()][loc.getCol()];
            grid[loc.getRow()][loc.getCol()] = null;
            return old;
        }
        return null;
    }

    /**
     * Gets the object at a location in this MyBoundedGrid.
     * @param loc the location to get the object from
     * @return the object at location loc (or null if the location is empty)
     */
    public E get(Location loc)
    {
        if (isValid(loc))
        {
            return (E) grid[loc.getRow()][loc.getCol()];
        }
        return null;
    }

    /**
     * Returns a list of the occupied locations in this MyBoundedGrid.
     * @return a list of the occupied locations in this MyBoundedGrid
     */
    public ArrayList<Location> getOccupiedLocations()
    {
        ArrayList<Location> locs = new ArrayList<Location>();
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (grid[r][c] != null)
                {
                    locs.add(new Location(r, c));
                }
            }
        }
        return locs;
    }

}
