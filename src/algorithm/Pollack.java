package algorithm;

import java.util.HashSet;

import graph.FindState;
import graph.Finder;
import graph.FleeState;
import graph.NodeStatus;

/** A solution with find-the-Orb optimized and flee getting out as fast as possible. */
public class Pollack extends Finder {

    /** a HashSet contains all ID of the tiles that has been visited */
    private HashSet<Long> visited;

    /** Get to the orb in as few steps as possible. <br>
      */

    @Override
    public void find(FindState state) {
        // TODO 1: Walk to the orb
        visited= new HashSet<Long>();
        dfsWalk(state);

    }

    /** Get out the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before steps runs out, and this should be prioritized above collecting gold.
     * // Traverse the nodes in moveOut sequentially, starting at the node<br>
     * // pertaining to state <br>
     * // public void moveAlong(FleeState state, List<Node> moveOut) */
    @Override
    public void flee(FleeState state) {
        // TODO 2. Get out of the cavern in time, picking up as much gold as possible.
        var path= Path.shortestPath(state.currentNode(), state.exit());
        for (int i= 1; i < path.size(); i++ ) {
            state.moveTo(path.get(i));
            if (state.currentNode() == state.exit()) return;
        }
    }

    /** Make the tile of id visited */
    private void visit(long id) {
        visited.add(id);
    }

    /** return true if the of id visited */
    private boolean visited(long id) {
        return visited.contains(id);
    }

    private void dfsWalk(FindState state) {
        if (state.distanceToOrb() == 0) return;
        long id= state.currentLoc();
        visit(id);
        for (NodeStatus w : state.neighbors()) {
            long wid= w.getId();
            if (!visited(wid)) {
                state.moveTo(wid);
                dfsWalk(state);
                if (state.distanceToOrb() == 0) return;
                state.moveTo(id);
            }
        }
    }

}
