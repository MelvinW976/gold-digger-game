package graph;

import java.util.Objects;

/** An instance describes the status of a node, views during find-the-orb phase. The idea is that
 * during this phase, the whole graph is not made visible but only that needed to do a dfs walk. */
public class NodeStatus implements Comparable<NodeStatus> {
	private final long id;
	private final int distance;

	/** Constructor: an instance with id nodeId and distance dist to the Orb. */
	/* package */ NodeStatus(long nodeId, int dist) {
		id= nodeId;
		distance= dist;
	}

	/** Return the Id of the Node that corresponds to this NodeStatus. */
	public long getId() {
		return id;
	}

	/** Return the distance to the orb from the Node that corresponds to this NodeStatus. */
	public int getDistanceToTarget() {
		return distance;
	}

	/** Return neg or pos number depending on whether this's distance is<br>
	 * < or > other's distance. <br>
	 * If the distances are equal, return neg, 0 or pos depending on whether <br>
	 * this id is <, = or > other's id. */
	@Override
	public int compareTo(NodeStatus other) {
		if (distance != other.distance) { return Integer.compare(distance, other.distance); }
		return Long.compare(id, other.id);
	}

	/** Return true iff this and ob are of the same class and have the same id. */
	@Override
	public boolean equals(Object ob) {
		if (ob == this) return true;
		if (ob == null || getClass() != ob.getClass()) return false;
		return id == ((NodeStatus) ob).id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
