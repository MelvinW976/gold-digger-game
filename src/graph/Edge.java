package graph;

import java.util.Map;

/** An Edge represents an immutable directed, weighted edge.
 *
 * @author eperdew */
public class Edge {
	/** The Node this edge is coming from */
	private final Node src;

	/** The node this edge is going to */
	private final Node dest;

	/** The length of this edge */
	public final int length;

	/** Constructor: an edge from src to dest with length len. */
	public Edge(Node src, Node dest, int len) {
		this.src= src;
		this.dest= dest;
		length= len;
	}

	/** Constructor: an edge that is isomporphic to isomorphism. */
	public Edge(Edge e, Map<Node, Node> isomorphism) {
		src= isomorphism.get(e.src);
		dest= isomorphism.get(e.dest);
		length= e.length;
	}

	/** Return the Node on this edge that is not equal to n. <br>
	 * Throw an IllegalArgumentException if n is not in this Edge. */
	public Node other(Node n) {
		if (src == n) return dest;
		if (dest == n) return src;
		throw new IllegalArgumentException("getOther: Edge must contain provided node");

	}

	/** Return the length of this Edge */
	public int length() {
		return length;
	}

	/** Return the source of this edge. */
	public Node getSource() {
		return src;
	}

	/** Return destination of edge */
	public Node getDest() {
		return dest;
	}
}
