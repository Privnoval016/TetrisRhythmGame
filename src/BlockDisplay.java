import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Anu Datar
 *
 * Changed block size and added a split panel display for next block and Score
 *
 * @author Ryan Adolf
 * @version 1.0
 *
 * Fixed the lag issue with block rendering
 * Removed the JPanel
 *
 * @author Pranav Sukesh
 * @version 3/15/2023
 */
// Used to display the contents of a game board
public class BlockDisplay extends JComponent implements KeyListener
{
	private static final Color BACKGROUND = new Color(30,30,30,255);

	private static final Color BORDER = Color.BLACK;

	private static final int OUTLINE = 2;
	private static final int BLOCKSIZE = 20;

	private MyBoundedGrid<Block> board;
	private JFrame frame;
	private ArrowListener listener;

	private boolean cringeControls;

	// Constructs a new display for displaying the given board
	public BlockDisplay(MyBoundedGrid<Block> board)
	{
		cringeControls = false;
		this.board = board;

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});

		//Wait until display has been drawn
		try
		{
			while (frame == null || !frame.isVisible())
				Thread.sleep(1);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private void createAndShowGUI()
	{
		//Create and set up the window.
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this);
		frame.addKeyListener(this);

		//Display the window.
		this.setPreferredSize(new Dimension(
				BLOCKSIZE * board.getNumCols(),
				BLOCKSIZE * board.getNumRows()
		));

		frame.pack();
		frame.setVisible(true);
	}

	public void paintComponent(Graphics g)
	{
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(BORDER);
		g.fillRect(0, 0, BLOCKSIZE * board.getNumCols() + OUTLINE, BLOCKSIZE * board.getNumRows());

		for (int row = 0; row < board.getNumRows(); row++)
			for (int col = 0; col < board.getNumCols(); col++)
			{
				Location loc = new Location(row, col);

				Block square = board.get(loc);

				if (square == null)
				{
					g.setColor(BACKGROUND);
				}
				else if (square.getColor().equals(Tetrad.TRAIL))
				{
					g.setColor(Tetrad.TRAIL);
					g.fillRect(col * BLOCKSIZE, row * BLOCKSIZE,
							BLOCKSIZE, BLOCKSIZE);
				}
				else
				{
					if (!(square.getColor().equals(Tetrad.SHADOW)))
					{
						Color c = square.getColor();

						g.setColor(c);
						g.fillRect(col * BLOCKSIZE + OUTLINE/2 + (BLOCKSIZE) / 5,
								row * BLOCKSIZE + OUTLINE/2 + (BLOCKSIZE) / 5,
								BLOCKSIZE*3/5 - 1, BLOCKSIZE*3/5 - 1);

					}
					else
						g.setColor(square.getColor());

					g.fillRect(col * BLOCKSIZE + OUTLINE / 2, row * BLOCKSIZE + OUTLINE / 2,
							BLOCKSIZE - OUTLINE, BLOCKSIZE - OUTLINE);
				}



			}

	}

	//Redraws the board to include the pieces and border colors.
	public void showBlocks()
	{
		repaint();
	}

	// Sets the title of the window.
	public void setTitle(String title)
	{
		frame.setTitle(title);
	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		if (listener == null)
			return;
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_E)
		{
			cringeControls = !cringeControls;
		}

		if (code == KeyEvent.VK_LEFT)
			listener.moveLeft();
		else if (code == KeyEvent.VK_RIGHT)
			listener.moveRight();
		else if (code == KeyEvent.VK_DOWN)
			listener.moveDown();
		else if (code == KeyEvent.VK_M)
			listener.muteSFX();
		else if (code == KeyEvent.VK_A)
			listener.haltAnims();

		if (cringeControls)
		{
			if (code == KeyEvent.VK_X)
				listener.rotateCW();
			else if (code == KeyEvent.VK_UP)
				listener.hardDrop();
			else if (code == KeyEvent.VK_SPACE)
				listener.hold();
			else if (code == KeyEvent.VK_Z)
				listener.rotateCCW();
		}
		else
		{
			if (code == KeyEvent.VK_UP)
				listener.rotateCW();
			else if (code == KeyEvent.VK_SPACE)
				listener.hardDrop();
			else if (code == KeyEvent.VK_C)
				listener.hold();
			else if (code == KeyEvent.VK_Z)
				listener.rotateCCW();
		}
	}

	public void setArrowListener(ArrowListener listener)
	{
		this.listener = listener;
	}
}
