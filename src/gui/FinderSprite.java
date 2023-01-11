package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JPanel;

import graph.Cavern;
import graph.Node;
import graph.Cavern.Direction;

/** Responsible for managing the finder and drawing it on the screen. <br>
 * Handles functions to update the finder and update its drawing as well. */
public class FinderSprite extends JPanel {
	private static final long serialVersionUID= 1L;

	private Sprite sprite;                      // Sprite class to handle animating the finder

	private int SPRITE_WIDTH= 29;               // Width (in pixels) of a single finder image on
								                // the spritesheet
	private int SPRITE_HEIGHT= 36;              // Height (in pixels) if a single finder image on
								                // the spritesheet

	private int row;                            // finder's row index (updates only once move
					                            // completes)
	private int col;                            // finder's column index (updates only once move
					                            // completes)
	private int posX;                           // x-coordinate (pixels)
	private int posY;                           // y-coordinate(pixels)
	private BlockingQueue<MovePair> queuedMoves;// List of moves we need to make to get to the goal
												// location
	private Cavern.Direction dir= Direction.NORTH;       // Which direction is the finder
												         // currently facing?
	private Semaphore blockUntilDone;           // Allow our moveTo to block until complete.

	private Thread updateThread;                // Thread that updates finder's location
	private Thread animationUpdateThread;       // Thread that updates finder's animation

	private double ANIMATION_FPS= 10;    // Number of animation frames displayed per second

	private String spriteSheet= "res/hunter_sprites.png";    // Location of the spritesheet image

	/** Constructor: an instance starting at (startRow, startCol). */
	public FinderSprite(int startRow, int startCol) {
		// Initialize fields
		sprite= new Sprite(spriteSheet, SPRITE_WIDTH, SPRITE_HEIGHT, 3);
		queuedMoves= new SynchronousQueue<>();
		blockUntilDone= new Semaphore(0);

		// Initialize our starting location
		row= startRow;
		col= startCol;
		posX= row * CavernPanel.TILE_WIDTH;
		posY= col * CavernPanel.TILE_HEIGHT;
		// Create a thread that will periodically update the finder's position
		updateThread= new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						int frames= GUI.FRAMES_PER_MOVE;
						MovePair move= queuedMoves.take();
						// Move to the goal
						for (int i= 1; i <= frames; i++ ) {
							long startTime= System.currentTimeMillis();
							// Get the next move to make
							update(frames, i, move);
							long lagTime= System.currentTimeMillis() - startTime;
							if (lagTime < 1000 / GUI.FRAMES_PER_SECOND) {
								Thread.sleep(1000 / GUI.FRAMES_PER_SECOND - lagTime);
							}
						}
						blockUntilDone.release();

					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});

		updateThread.start();

		// Create a thread that will periodically update the finder's animation
		animationUpdateThread= new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						long startTime= System.currentTimeMillis();
						sprite.tick();
						long lagTime= System.currentTimeMillis() - startTime;
						if (lagTime < 1000 / ANIMATION_FPS) {
							Thread.sleep((long) (1000 / ANIMATION_FPS - lagTime));
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});

		animationUpdateThread.start();
	}

	/** Return the image representing the current state of the finder. */
	public BufferedImage sprite() {
		/* Use the direction to determine which offset into the
		 * spritesheet to use. The Sprite class handles animation. */
		switch (dir) {
		case NORTH:
			return sprite.getSprite(0, 0);
		case SOUTH:
			return sprite.getSprite(0, 3);
		case WEST:
			return sprite.getSprite(1, 0);
		case EAST:
			return sprite.getSprite(1, 3);
		default:
			return sprite.getSprite(0, 0);
		}
	}

	/** Return the finder's row on the grid. <br>
	 * Will remain the finder's old position until the finder <br>
	 * has completely arrived at the new one. */
	public int getRow() {
		return row;
	}

	/** Return the hunter's column on the grid. <br>
	 * Will remain the finder's old position until the finder <br>
	 * has completely arrived at the new one. */
	public int getCol() {
		return col;
	}

	/** Tell the finder to move from its current location to node dst.<br>
	 * After making move, calling thread will block until the move completes on GUI.<br>
	 * Precondition: dst must be adjacent to the current location and not currently moving.<br>
	 *
	 * @throws InterruptedException */
	public void moveTo(Node dst) throws InterruptedException {
		dir= getDirection(row, col, dst.getTile().getRow(), dst.getTile().getColumn());
		// Determine sequence of moves to add to queue to get to goal
		int xDiff= (dst.getTile().getColumn() - col) * CavernPanel.TILE_WIDTH;
		int yDiff= (dst.getTile().getRow() - row) * CavernPanel.TILE_HEIGHT;
		queuedMoves.put(new MovePair(xDiff, yDiff));

		blockUntilDone.acquire();
		row= dst.getTile().getRow();
		col= dst.getTile().getColumn();
	}

	/** Draw the finder on its own panel. */
	@Override
	public void paintComponent(Graphics page) {
		super.paintComponent(page);
		page.drawImage(sprite(), posX, posY, CavernPanel.TILE_WIDTH, CavernPanel.TILE_HEIGHT, null);
	}

	/** Update the location of the finder as necessary. */
	private void update(int framesPerMove, int framesIntoMove, MovePair move) {
		// Make the move toward our destination
		posX= CavernPanel.TILE_WIDTH * getCol() + framesIntoMove * move.xDiff / framesPerMove;
		posY= CavernPanel.TILE_HEIGHT * getRow() + framesIntoMove * move.yDiff / framesPerMove;
		repaint();
	}

	/** Return the direction from current location (row, col) to (goalRow, goalCol). <br>
	 * If already there, return the current direction. */
	private Direction getDirection(int row, int col, int goalRow, int goalCol) {
		if (goalRow < row) return Direction.NORTH;
		if (goalRow > row) return Direction.SOUTH;
		if (goalCol < col) return Direction.WEST;
		if (goalCol > col) return Direction.EAST;
		return dir;
	}

	/** Store information that uniquely represents a move that can be made. */
	private class MovePair {
		public final int xDiff;
		public final int yDiff;

		/** Constructor: an instance with change (xChange, yChange). */
		public MovePair(int xChange, int yChange) {
			xDiff= xChange;
			yDiff= yChange;
		}
	}
}
