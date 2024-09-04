import java.awt.*;
/**
 * Class Block encapsulates a Block abstraction which can be placed into a Gridworld style grid
 * You are expected to comment this class according to the style guide.
 * @author Pranav Sukesh
 * @version 3/8/2023
 */
public class Block
{
    private MyBoundedGrid<Block> grid;
    private Location location;
    private Color color;

	/**
    * constructs a blue block, because blue is the greatest color ever!
    */
    public Block()
    {
        color = Color.BLUE;
        grid = null;
        location = null;
    }
	/**
	 * Returns the color of this block.
     * @return the color of this block
     */
    public Color getColor()
    {
        return color;
    }

    /**
	 * Sets the color of this block to newColor.
     * @param newColor the new color
	 */
    public void setColor(Color newColor)
    {
        color = newColor;
    }
    
	/**
     * Returns the grid in which this block is located.
     * @return the grid in which this block is located
     */
    public MyBoundedGrid<Block> getGrid()
    {
        return grid;
    }
    
	/**
	 * Returns the location of this block in its grid.
     * @return the location of this block in its grid
	 */
    public Location getLocation()
    {
        return location;
    }
    
	/**
	 * Removes this block from its grid.
     * @postcondition    This block is not contained in a grid
	 */
    public void removeSelfFromGrid()
    {
        if (grid != null)
        {
            grid.remove(location);
        }
        location = null;
        grid = null;
    }
    
	/**
	 * Puts this block into a given grid at a given location.
     * @param gr the grid into which this block should be placed
     * @param loc the location into which this block should be placed
     * @precondition     This block is not contained in a grid
     * @postcondition    This block is contained in gr and at location loc.
	 */
    public void putSelfInGrid(MyBoundedGrid<Block> gr, Location loc)
    {
        if (gr.get(loc) != null)
            gr.get(loc).removeSelfFromGrid();

        if (gr.isValid(loc))
        {
            location = loc;
            grid = gr;
            grid.put(loc, this);
        }

    }

    /**
	 * Moves this block to a new location in the same grid.
     * @param newLocation the new location
     * @precondition     This block is contained in a grid
     * @postcondition    This block is at location newLocation
	 */
    public void moveTo(Location newLocation)
    {
        if (grid.isValid(newLocation))
        {
            MyBoundedGrid gr = grid;
            removeSelfFromGrid();
            putSelfInGrid(gr, newLocation);
        }
    }

    /**
	* returns a string with the location and color of this block
	*/
    public String toString()
    {
        return "Block[location=" + location + ",color=" + color + "]";
    }

}