package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** An instance is a Cavern through which the finderSprite can move.<br>
 * It's a grid of Tile objects with a weighted graph pr all non-floor tiles.<br>
 *
 * There is an entrance to the cavern and a target location<br>
 * (which may also be the entrance). */
public class Cavern {

	/** Maximum weight on an edge. */
	public static final int MAX_EDGE_WEIGHT= 15;

	/** What is it? */
	private static final double DENSITY= 0.6;

	/** Probability that a floor tile has gold. */
	private static final double GOLD_PROBABILITY= 0.33;

	/** Max value of gold on a tile. */
	public static final int MAX_GOLD_VALUE= 1000;

	/** Whatever. */
	public static final int TASTY_VALUE= 5000;

	/** An enum representing a grid direction. */
	public enum Direction {
		/** North */
		NORTH(-1, 0),
		/** East */
		EAST(0, 1),
		/** South */
		SOUTH(1, 0),
		/** West */
		WEST(0, -1);

		private final Point dir;

		/** Constructor: an instance with direction row, col. */
		private Direction(int row, int col) {
			dir= new Point(row, col);
		}

		/** Return the direction. */
		public Point getPoint() {
			return dir;
		}
	}

	/** An enum representing a point on the grid. */
	private static class Point {
		private final int row;
		private final int col;

		/** Constructor: an instance for (r, c). */
		private Point(int r, int c) {
			row= r;
			col= c;
		}

		/** Return a new point that is this point with p added to it. */
		public Point add(Point p) {
			return new Point(row + p.row, col + p.col);
		}

		/** Return tree iff this and ob are of the same class and<br>
		 * have the same row and col. */
		@Override
		public boolean equals(Object ob) {
			if (this == ob) return true;
			if (ob == null || getClass() != ob.getClass()) return false;
			Point p= (Point) ob;
			return p.row == row && p.col == col;
		}

		@Override
		public int hashCode() {
			return Objects.hash(row, col);
		}
	}

	private final int rows;
	private final int cols;

	private final Set<Node> graph;
	private final Node entrance;
	private final Node target;

	private final Node[][] tiles;

	/** Return a new random cavern of the given size where there is no gold, <br>
	 * all edges have weight 1, and there is an orb a reasonable distance from the exit.
	 *
	 * @param rows The number of rows in this cavern.
	 * @param cols The number of columns in this cavern.
	 * @param rand A source of randomness to use for the cavern generation. */
	public static Cavern digFindCavern(int rows, int cols, Random rand) {
		int minOrbDist= minOrbDistance(rows, cols);

		Cavern cavern= new Cavern(rows, cols, rand, () -> 1, () -> 0, Tile.Type.ORB);
		while (cavern.minPathLengthToTarget(cavern.getEntrance()) < minOrbDist) {
			cavern= new Cavern(rows, cols, rand, () -> 1, () -> 0, Tile.Type.ORB);
		}
		return cavern;
	}

	/** Return the minimum allowable path distance from the entrance to the orb. */
	private static int minOrbDistance(int rows, int cols) {
		return (rows + cols) / 2;
	}

	/** Return a new random cavern with random gold and edge weights. <br>
	 * It is guaranteed that ({@code currentRow}, {@code currentCol}) will be an open floor cell.
	 *
	 * @param rows       The number of rows in this cavern.
	 * @param cols       The number of columns in this cavern.
	 * @param currentRow The row of the cell that must be open floor.
	 * @param currentCol The column of the cell that must be open floor.
	 * @param rand       A source of randomness to use for the cavern generation. */
	public static Cavern digFindCavern(int rows, int cols, int currentRow, int currentCol,
		Random rand) {
		Supplier<Integer> edgeWeightGen= () -> rand.nextInt(MAX_EDGE_WEIGHT) + 1;
		Supplier<Integer> goldGen= () -> Cavern.generateGoldValue(rand);
		Cavern potentialCavern= new Cavern(rows, cols, rand, edgeWeightGen, goldGen,
			Tile.Type.ENTRANCE);
		while (potentialCavern.getTileAt(currentRow, currentCol).getType() != Tile.Type.FLOOR) {
			potentialCavern= new Cavern(rows, cols, rand, edgeWeightGen, goldGen,
				Tile.Type.ENTRANCE);
		}
		return potentialCavern;
	}

	/** Return a randomly determined gold value for a given tile. */
	private static int generateGoldValue(Random rand) {
		if (rand.nextDouble() > GOLD_PROBABILITY) { return 0; }

		int val= rand.nextInt(MAX_GOLD_VALUE) + 1;
		if (val == MAX_GOLD_VALUE) {
			val= TASTY_VALUE;
		}
		return val;
	}

	/** Constructor: a new cavern of size (rws, cls). <br>
	 * Randomness rand is used to determine which grid tiles are open. <br>
	 * This uses {@code edgeWeightGenerator} and {@code goldGenerator} <br>
	 * to generate edge weights and gold values. <br>
	 * Precondition: {@code targetType} must be either <br>
	 * {@code Tile.Type.ORB} or {@code Tile.Type.ENTRANCE}. */
	private Cavern(int rws, int cls, Random rand,
		Supplier<Integer> edgeWeightGenerator,
		Supplier<Integer> goldGenerator,
		Tile.Type targetType) {
		rows= rws;
		cols= cls;

		graph= generateGraph(rand, targetType, goldGenerator);
		entrance= graph.stream().filter((n) -> n.getTile().getType() == Tile.Type.ENTRANCE)
			.findAny().get();
		target= graph.stream().filter((n) -> n.getTile().getType() == targetType).findAny().get();

		// Set tiles for the floor and then add walls wherever floor is missing.
		tiles= new Node[rows][cols];
		for (Node node : graph) {
			Tile t= node.getTile();
			tiles[t.getRow()][t.getColumn()]= node;
		}
		for (int i= 0; i < rows; i++ ) {
			for (int j= 0; j < cols; j++ ) {
				if (tiles[i][j] == null) {
					tiles[i][j]= new Node(new Tile(i, j, 0, Tile.Type.WALL), cols);
				}
			}
		}
		createEdges(tiles, edgeWeightGenerator);
	}

	/** Constructor; a graph constructed from the given graph g and tiles t, with<br>
	 * target trgt. <br>
	 * Preconditions: <br>
	 * 1. g and t represent the same graph (i.e.<br>
	 * .. g contains all non-floor nodes in t and edges are along the grid). <br>
	 * 2. trgt is a node in t. */
	private Cavern(Set<Node> g, Node[][] t, Node trgt) {
		tiles= t;
		rows= tiles.length;
		cols= tiles[0].length;

		graph= Collections.unmodifiableSet(g);
		entrance= graph.stream().filter((n) -> n.getTile().getType() == Tile.Type.ENTRANCE)
			.findAny().get();
		target= trgt;
	}

	/** Given tiles t without edges and a supplier to return edge weights, <br>
	 * add edges to the nodes between adjacent non-wall tiles. <br>
	 * Precondition: all elements of {@code tiles} are non-null. */
	private void createEdges(Node[][] t, Supplier<Integer> edgeWeightGenerator) {
		for (int i= 0; i < t.length - 1; i++ ) {
			for (int j= 0; j < t[i].length - 1; j++ ) {
				Node node= t[i][j];
				if (node.getTile().getType() == Tile.Type.WALL) {
					continue;
				}

				final Point p= new Point(i, j);
				Stream.of(Direction.SOUTH, Direction.EAST)
					.map(Direction::getPoint)
					.map(p::add)
					.map((q) -> t[q.row][q.col])
					.filter((m) -> m.getTile().getType() != Tile.Type.WALL)
					.forEach((m) -> {
						int weight= edgeWeightGenerator.get();
						node.addEdge(new Edge(node, m, weight));
						m.addEdge(new Edge(m, node, weight));
					});
			}
		}
	}

	/** Return true iff p is on the grid. */
	private boolean isValid(Point p) {
		return p.row > 0 && p.row < rows - 1 && p.col > 0 && p.col < cols - 1;
	}

	/** Generate a new random graph that fits within the grid and return the set of nodes. */
	private Set<Node> generateGraph(Random rand,
		Tile.Type targetType,
		Supplier<Integer> goldGenerator) {
		List<Node> nodes= new ArrayList<>();

		Set<Point> pointsSeen= new HashSet<>();
		Set<Point> openPoints= new HashSet<>();
		Queue<Node> frontier= new ArrayDeque<>();

		Point entrancePoint= getEntrancePoint(rand);
		Node entrance= new Node(new Tile(entrancePoint.row,
			entrancePoint.col, 0, Tile.Type.ENTRANCE), cols);
		nodes.add(entrance);

		pointsSeen.add(entrancePoint);
		openPoints.add(entrancePoint);
		frontier.add(entrance);
		while (!frontier.isEmpty()) {
			Node node= frontier.remove();
			Point p= new Point(node.getTile().getRow(), node.getTile().getColumn());

			// We want to make sure there's a way out if we can get one.
			// This will prevent stupid degenerate graphs.
			int existingExits= 0;
			List<Point> newExits= new ArrayList<>();
			for (Direction dir : Direction.values()) {
				Point newPt= dir.getPoint().add(p);
				if (isValid(newPt)) {
					if (openPoints.contains(newPt)) {
						existingExits++ ;
					} else if (pointsSeen.add(newPt)) {
						newExits.add(newPt);
					}
				}
			}

			int nExits= newExits.size();
			if (nExits > 0) {
				double modifiedDensity;
				Point forcedExit;
				// Modify the density function so that the expected number of open exits
				// is the same even though we're forcing something to be open.
				if (existingExits < 2) {
					modifiedDensity= nExits == 1 ? 0.0 : (nExits * DENSITY - 1) / (nExits - 1);
					forcedExit= newExits.get(rand.nextInt(newExits.size()));
				} else {
					modifiedDensity= DENSITY;
					forcedExit= null;
				}
				newExits.stream()
					.filter((q) -> q.equals(forcedExit) || rand.nextDouble() < modifiedDensity)
					.peek(openPoints::add)
					.map((q) -> new Node(
						new Tile(q.row, q.col, goldGenerator.get(), Tile.Type.FLOOR), cols))
					.peek(frontier::add)
					.forEach(nodes::add);
			}
		}

		if (targetType != Tile.Type.ENTRANCE) {
			// Grab a random tile that's not the entrance and make it the target.
			int targetIdx= rand.nextInt(nodes.size() - 1) + 1;
			nodes.get(targetIdx).getTile().setType(targetType);
		}

		return Collections.unmodifiableSet(new HashSet<>(nodes));
	}

	/** Return a randomly chosen entrance to the cavern (the only non-wall tile along an edge of the
	 * grid), using rand. */
	private Point getEntrancePoint(Random rand) {
		switch (rand.nextInt(4)) {
		case 0: // North wall
			return new Point(rand.nextInt(rows - 2) + 1, 0);
		case 1: // South wall
			return new Point(rand.nextInt(rows - 2) + 1, cols - 1);
		case 2: // West wall
			return new Point(0, rand.nextInt(cols - 2) + 1);
		case 3: // East wall
			return new Point(rows - 1, rand.nextInt(cols - 2) + 1);
		default:
			throw new IllegalStateException("Unexpected random value!");
		}
	}

	/** Return the number of open floor tiles in this cavern <br>
	 * (this is the size of the graph). */
	public int numOpenTiles() {
		return graph.size();
	}

	/** Return the number of rows in the grid. */
	public int getRowCount() {
		return rows;
	}

	/** Return the number of columns in the grid. */
	public int getColumnCount() {
		return cols;
	}

	/** Return the set of all nodes in the graph. <br>
	 * This is an umodifiable view of the graph. */
	public Set<Node> getGraph() {
		return graph;
	}

	/** Return the node corresponding to the entrance to the cavern. */
	public Node getEntrance() {
		return entrance;
	}

	/** Return the target node in this cavern. */
	public Node getTarget() {
		return target;
	}

	/** Return the Tile information for tile (r, c). <br>
	 * Precondition: (r, c) must be in the grid. */
	public Tile getTileAt(int r, int c) {
		return tiles[r][c].getTile();
	}

	/** Return the node at the given (r, c). <br>
	 * Precondition: (r, c) must be in the grid. */
	public Node getNodeAt(int r, int c) {
		return tiles[r][c];
	}

	/** Package-private implementation of Dijkstra's algorithm that <br>
	 * returns only the minimum distance between the given node and the <br>
	 * target node for this cavern (no path). <br>
	 * Precondition: start must be a node in the graph of this cavern. */
	int minPathLengthToTarget(Node start) {
		Map<Long, Integer> pathWeights= new HashMap<>();
		InternalMinHeap<Node> heap= new InternalMinHeap<>();

		pathWeights.put(start.getId(), 0);
		heap.add(start, 0);
		while (!heap.isEmpty()) {
			Node n= heap.poll();
			if (n.equals(target)) { return pathWeights.get(n.getId()); }

			int nWeight= pathWeights.get(n.getId());

			for (Edge e : n.exits()) {
				Node m= e.other(n);
				int weightThroughN= nWeight + e.length();
				Integer existingWeight= pathWeights.get(m.getId());
				if (existingWeight == null) {
					pathWeights.put(m.getId(), weightThroughN);
					heap.add(m, weightThroughN);
				} else if (weightThroughN < existingWeight) {
					pathWeights.put(m.getId(), weightThroughN);
					heap.changePriority(m, weightThroughN);
				}
			}
		}
		throw new IllegalStateException("The above loop should always reach the desired location.");
	}

	/** Serialize this cavern to a list of strings which can be written out to a file. <br>
	 * The list of strings can be converted back into a Cavern using deserialize(). */
	public List<String> serialize() {
		List<String> nodes= new ArrayList<>();
		nodes.add(rows + ":" + cols + ",trgt:" + target.getId());
		for (Node n : graph) {
			Tile t= n.getTile();
			String nodeStr= n.getId() + "," + t.getRow() + "," + t.getColumn() +
				"," + t.gold() + "," + t.getType().name();

			String edges= n.exits().stream()
				.map((e) -> e.other(n).getId() + "-" + e.length())
				.collect(Collectors.joining(","));
			nodes.add(nodeStr + "=" + edges);
		}
		return nodes;
	}

	/** Convert nodeStrList, which was output by serialize(), back into a Cavern.<br>
	 * Precondition: The list of strings is of the format output by serialize(). */
	public static Cavern deserialize(List<String> nodeStrList) {
		String extraInfo= nodeStrList.get(0);
		String[] infoParts= extraInfo.split(",");
		String[] dimensions= infoParts[0].split(":");
		int rows= Integer.parseInt(dimensions[0]);
		int cols= Integer.parseInt(dimensions[1]);
		long targetId= Long.parseLong(infoParts[1].split(":")[1]);

		Map<Long, Node> idToNode= new HashMap<>();
		for (String nodeStr : nodeStrList) {
			if (nodeStr.equals(extraInfo)) {
				continue;
			}

			String nodeInfo= nodeStr.substring(0, nodeStr.indexOf("="));
			String[] splitInfo= nodeInfo.split(",");

			long nodeId= Long.parseLong(splitInfo[0]);
			Node n= new Node(nodeId,
				new Tile(Integer.parseInt(splitInfo[1]),
					Integer.parseInt(splitInfo[2]),
					Integer.parseInt(splitInfo[3]),
					Tile.Type.valueOf(splitInfo[4])));
			idToNode.put(nodeId, n);
		}

		Node[][] tiles= new Node[rows][cols];
		for (String nodeStr : nodeStrList) {
			// The first line is not a node, it's metadata, so skip it.
			if (nodeStr.equals(extraInfo)) {
				continue;
			}

			String[] nodeAndEdgeInfo= nodeStr.split("=");
			long nodeId= Long.parseLong(nodeAndEdgeInfo[0].split(",")[0]);

			Node n= idToNode.get(nodeId);
			tiles[n.getTile().getRow()][n.getTile().getColumn()]= n;
			for (String edgeStr : nodeAndEdgeInfo[1].split(",")) {
				String[] idAndWeight= edgeStr.split("-");
				long otherId= Long.parseLong(idAndWeight[0]);
				int weight= Integer.parseInt(idAndWeight[1]);
				n.addEdge(new Edge(n, idToNode.get(otherId), weight));
			}
		}

		for (int i= 0; i < rows; i++ ) {
			for (int j= 0; j < cols; j++ ) {
				if (tiles[i][j] == null) {
					tiles[i][j]= new Node(new Tile(i, j, 0, Tile.Type.WALL), cols);
				}
			}
		}
		return new Cavern(new HashSet<>(idToNode.values()), tiles, idToNode.get(targetId));
	}
}
