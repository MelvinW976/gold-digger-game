package graph;

import java.util.Collection;

/** The state of the game while performing exploration.<br>
 * In order to determine the next move you need to call the various methods<br>
 * of this interface. To move through the cavern, you need to call moveTo(long).
 *
 * An instance provides all the information necessary<br>
 * to search through the cavern and find the Orb. */
public interface FindState {
	/** Return the unique identifier associated with Pollack's current location. */
	long currentLoc();

	/** Return an unordered collection of NodeStatus objects<br>
	 * associated with all direct neighbors of Pollack's current location.<br>
	 * Each status contains a unique identifier for the neighboring node<br>
	 * as well as the distance of that node to the Orb along the grid<br>
	 * <br>
	 * (NB: This is NOT the distance in the graph, it is only the number<br>
	 * of rows and columns away from the Orb.)<br>
	 * <br>
	 * It is possible to move directly to any node identifier in this collection. */
	Collection<NodeStatus> neighbors();

	/** Return Pollack's current distance along the grid (NOT THE GRAPH) from the Orb. */
	int distanceToOrb();

	/** Change Pollack's current location to the node given by id.<br>
	 * <br>
	 * Throw an IllegalArgumentException if the node with id id is not<br>
	 * adjacent to Pollack's current location. */
	void moveTo(long id);
}
