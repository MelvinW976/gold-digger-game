package graph;

import java.util.Objects;

/** A Pair&lt;X,Y&gt; represents an immutable ordered pair of two Objects of types X and Y.
 *
 * @author eperdew */
public final class Pair<X, Y> {
	X first;
	Y second;

	/** Constructor: a pair (x, y). */
	public Pair(X x, Y y) {
		first= x;
		second= y;
	}

	/** Return the first object in this Pair. */
	public X getFirst() {
		return first;
	}

	/** Return the second object in this Pair. */
	public Y getSecond() {
		return second;
	}

	/** Return true iff this and ob are of the same class and<br>
	 * they have the same pair values. */
	@Override
	public boolean equals(Object ob) {
		if (ob == null || getClass() != ob.getClass()) return false;
		Pair<?, ?> p= (Pair<?, ?>) ob;
		return first.equals(p.first) && second.equals(p.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

}
