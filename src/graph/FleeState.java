package graph;

import java.util.Collection;

/** A FleeState provides all the information necessary to<br>
 * get out of the cavern and collect gold on the way.
 *
 * This interface provides access to the complete graph of the cavern,<br>
 * which will allow computation of the path.<br>
 * Once you have determined how Pres Pollock should get out, call<br>
 * moveTo(Node) repeatedly to move to each node. */
public interface FleeState {
    /** Return the Node corresponding to Pres Pollock's location in the graph. */
    Node currentNode();

    /** Return the Node associated with the exit from the cavern. <br>
     * Pres Pollock has to move to this Node in order to get out. */
    Node exit();

    /** Return a collection containing all the nodes in the graph. <br>
     * They in no particular order. */
    Collection<Node> allNodes();

    /** Change Pres Pollock's location to n. <br>
     * Throw an IllegalArgumentException if n is not directly connected to Pres Pollock's
     * location. */
    void moveTo(Node n);

    /** Pick up the gold on the current tile. Students: Don't call this method because <br>
     * gold on a node is automatically picked up when the node is reached. <br>
     * Throw an IllegalStateException if there is no gold at the current location, <br>
     * either because there never was any or because it was already picked up. */
    void grabGold();

    /** Return the steps remaining to get out of the cavern. <br>
     * This value will change with every call to moveTo(Node), and <br>
     * if it reaches 0 before you get out, you have failed to get out. */
    int stepsLeft();
}
