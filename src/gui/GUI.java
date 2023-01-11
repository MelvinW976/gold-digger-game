package gui;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import graph.Cavern;
import graph.GameState;
import graph.Node;
import graph.Tile;
import submit.Pollack;

/** An instance is a GUI for the game. Run this file as a Java application to test the project. */
public class GUI extends JFrame {
    private static final long serialVersionUID= 1L;
    /** Width of the entire screen */
    public static int SCREEN_WIDTH= 1050;

    /** Height of the entire screen */
    public static int SCREEN_HEIGHT= 600;

    /** Width of the game portion (prop of total) */
    public static final double GAME_WIDTH_PROP= 0.78;

    /** Height of the game portion (prop of total) */
    public static final double GAME_HEIGHT_PROP= 1.0;

    /** Frame rate of game (fps) */
    public static int FRAMES_PER_SECOND= 60;

    /** How many frames does a single move take us? */
    public static int FRAMES_PER_MOVE= 25;

    private CavernPanel cavernPanel;       // The panel for generating and drawing the maze
    private FinderSprite finder;       // The panel for updating and drawing the finder
    private DataPanel dPanel;      // The panel for showing stats / displaying dPanel
    private SelTilePanel tileSelect;// Panel that provides more info on selected tile
    private JLayeredPane master;       // The panel that holds all other panels

    private static final int ERROR_WIDTH= 500;	// Width of the error pane (in pixels)
    private static final int ERROR_HEIGHT= 150;	// Height of the error pane (in pixels)

    private static final double INFO_SIZE= 0.5; // How much of the screen should the info use?

    /** Constructor a new display for cavern cav with the player at (playerRow, playerCol) using
     * random number seed seed. */
    public GUI(Cavern cav, int playerRow, int playerCol, long seed) {
        // Initialize frame
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocation(150, 150);

        var GAME_WIDTH= (int) (GAME_WIDTH_PROP * SCREEN_WIDTH);
        var GAME_HEIGHT= (int) (GAME_HEIGHT_PROP * SCREEN_HEIGHT);
        var PANEL_WIDTH= SCREEN_WIDTH - GAME_WIDTH;

        // Create the maze
        cavernPanel= new CavernPanel(cav, GAME_WIDTH, GAME_HEIGHT, this);
        // cavernPanel.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
        cavernPanel.setBounds(PANEL_WIDTH, 0, GAME_WIDTH, GAME_HEIGHT);  // gries
        cavernPanel.setVisited(playerRow, playerCol);

        // Create the finder
        finder= new FinderSprite(playerRow, playerCol);
        finder.setBounds(PANEL_WIDTH, 0, GAME_WIDTH, GAME_HEIGHT);// gries
        finder.setOpaque(false);

        // Create the panel for stats and dPanel
        // dPanel= new DataPanel(GAME_WIDTH, 0, SCREEN_WIDTH - GAME_WIDTH, (int)(SCREEN_HEIGHT *
        // INFO_SIZE), seed);
        dPanel= new DataPanel(0, 0, SCREEN_WIDTH - GAME_WIDTH,
            (int) (SCREEN_HEIGHT * INFO_SIZE), seed);  // gries

        // Create the panel for tile information
        // tileSelect= new SelTilePanel(GAME_WIDTH, (int)(SCREEN_HEIGHT * INFO_SIZE),
        // SCREEN_WIDTH - GAME_WIDTH, (int)(SCREEN_HEIGHT * (1 - INFO_SIZE)), this);
        tileSelect= new SelTilePanel(0, (int) (SCREEN_HEIGHT * INFO_SIZE), // gries
            SCREEN_WIDTH - GAME_WIDTH, (int) (SCREEN_HEIGHT * (1 - INFO_SIZE)), this);

        // Layer the finder and maze into master panel
        master= new JLayeredPane();
        master.add(cavernPanel, Integer.valueOf(1));
        master.add(dPanel, Integer.valueOf(1));
        master.add(tileSelect, Integer.valueOf(1));
        master.add(finder, Integer.valueOf(2));

        // Display GUI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(master);
        setVisible(true);

        // Repaint the GUI to fit the new size
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                SCREEN_WIDTH= getWidth();
                SCREEN_HEIGHT= getHeight();
                var GAME_WIDTH= (int) (GAME_WIDTH_PROP * SCREEN_WIDTH);
                var GAME_HEIGHT= (int) (GAME_HEIGHT_PROP * SCREEN_HEIGHT);
                var PANEL_WIDTH= SCREEN_WIDTH - GAME_WIDTH;
                cavernPanel.updateScreenSize(GAME_WIDTH, GAME_HEIGHT);
                cavernPanel.setBounds(PANEL_WIDTH, 0, GAME_WIDTH, GAME_HEIGHT);// gries changed from
                                                                               // 0
                finder.setBounds(PANEL_WIDTH, 0, GAME_WIDTH, GAME_HEIGHT);  // gries changed from 0
                finder.repaint();
                dPanel.setBounds(0, 0, SCREEN_WIDTH - GAME_WIDTH,
                    (int) (SCREEN_HEIGHT * INFO_SIZE)); // gries changed from GAME_WIDTH
                tileSelect.updateLoc(0, (int) (SCREEN_HEIGHT * INFO_SIZE), // gries changed from
                                                                           // GAME_WIDTH
                    SCREEN_WIDTH - GAME_WIDTH, (int) (SCREEN_HEIGHT * (1 - INFO_SIZE)));
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
    }

    /** Return the CavernPanel associated with this GUI. */
    public CavernPanel getMazePanel() {
        return cavernPanel;
    }

    /** Return the DataPanel associated with this GUI. */
    public DataPanel getOptionsPanel() {
        return dPanel;
    }

    /** Move the player on the GUI to destination dest. Note : This blocks until the player has
     * moved. Precondition : dest is adjacent to the player's current location */
    public void moveTo(Node dest) {
        try {
            cavernPanel.setVisited(dest.getTile().getRow(), dest.getTile().getColumn());
            finder.moveTo(dest);
        } catch (InterruptedException e) {
            throw new RuntimeException("GUI moveTo : Must wait for move to finish");
        }
    }

    /** Update the bonus multiplier as displayed by the GUI by bonus */
    public void updateBonus(double bonus) {
        dPanel.updateBonus(bonus);
    }

    /** Update the number of coins picked up as displayed on the GUI.
     *
     * @param coins the number of coins to be displayed
     * @param score the player's current score */
    public void updateGold(int coins, int score) {
        dPanel.updateGold(coins, score);
        tileSelect.repaint();
    }

    /** Update the steps remaining as displayed on the GUI. <br>
     */
    public void updateStepsLeft(int stepsLeft) {
        dPanel.updateStepsLeft(stepsLeft);
    }

    /** What is the specification? */
    public void updateCavern(Cavern c, int numStepsRemaining) {
        cavernPanel.setCavern(c);
        dPanel.updateMaxStepsLeft(numStepsRemaining);
        updateStepsLeft(numStepsRemaining);
        tileSelect.repaint();
    }

    /** Set the cavern to be all light or all dark, depending on light. */
    public void setLighting(boolean light) {
        cavernPanel.setLighting(light);
    }

    /** Return an image representing tile type. */
    public BufferedImage getIcon(Tile.Type type) {
        return cavernPanel.getIcon(type);
    }

    /** Return an icon for the gold on tile n, or null otherwise. */
    public BufferedImage getGoldIcon(Node n) {
        return cavernPanel.getGoldIcon(n);
    }

    /** Select node n on the GUI. This displays information on that node's panel on the screen to
     * the right. */
    public void selectNode(Node n) {
        tileSelect.selectNode(n);
    }

    /** Display error e to the player. */
    public void displayError(String e) {
        var errorFrame= new JFrame();
        errorFrame.setTitle("Error in Solution");
        var errorText= new JLabel(e);
        errorText.setHorizontalAlignment(JLabel.CENTER);
        errorFrame.add(errorText);
        errorFrame.setSize(ERROR_WIDTH, ERROR_HEIGHT);
        errorFrame.setLocation(new Point(getX() + getWidth() / 2 - ERROR_WIDTH / 2,
            getY() + getHeight() / 2 - ERROR_HEIGHT / 2));
        errorFrame.setVisible(true);
    }

    /** The main program. */
    public static void main(String[] args) {
        List<String> argList= new ArrayList<>(Arrays.asList(args));
        var seedIndex= argList.indexOf("-s");
        var seed= 0L;
        if (seedIndex >= 0) {
            try {
                seed= Long.parseLong(argList.get(seedIndex + 1));
            } catch (NumberFormatException e) {
                System.err.println("Error, -s must be followed by a numerical seed");
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Error, -s must be followed by a seed");
                return;
            }
        }

        GameState.runNewGame(seed, true, new Pollack());
    }
}
