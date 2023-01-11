package graph;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** A Node of the graph */
public class Node {

	/** The unique numerical identifier of this Node */
	private final long id;
	/** Represents the edges outgoing from this Node */
	private final Set<Edge> edges;
	private final Set<Node> neighbors;

	private final Set<Edge> unmodifiableEdges;
	private final Set<Node> unmodifiableNeighbors;

	/** Extra state that belongs to this node */
	private final Tile tile;

	/** Constructor: a Node for tile t using t's row /* package */
	Node(Tile t, int numCols) {
		this(t.getRow() * numCols + t.getColumn(), t);
	}

	/** Constructor: a node for tile t with id givenId. */
	/* package */ Node(long givenId, Tile t) {
		id= givenId;
		edges= new LinkedHashSet<>();
		neighbors= new LinkedHashSet<>();

		unmodifiableEdges= Collections.unmodifiableSet(edges);
		unmodifiableNeighbors= Collections.unmodifiableSet(neighbors);

		tile= t;
	}

	/* package */ void addEdge(Edge e) {
		edges.add(e);
		neighbors.add(e.other(this));
	}

	/** Return the unique Identifier of this Node. */
	public long getId() {
		return id;
	}

	/** Return the Edge of this Node that connects to Node q. Throw an IllegalArgumentException if
	 * edge doesn't exist */
	public Edge edge(Node q) {
		for (Edge e : edges) {
			if (e.getDest().equals(q)) { return e; }
		}
		throw new IllegalArgumentException("getEdge: Node must be a neighbor of this Node");
	}

	/** Return an unmodifiable view of the Edges leaving this Node. */
	public Set<Edge> exits() {
		return unmodifiableEdges;
	}

	/** Return an unmodifiable view of the Nodes neighboring this Node. */
	public Set<Node> getNeighbors() {
		return unmodifiableNeighbors;
	}

	/** Return the Tile corresponding to this Node. */
	public Tile getTile() {
		return tile;
	}

	/** Return true if this and ob are of the same class and<br>
	 * have the same id. */
	@Override
	public boolean equals(Object ob) {
		if (ob == this) { return true; }
		if (ob == null || getClass() != ob.getClass()) return false;
		return id == ((Node) ob).id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
