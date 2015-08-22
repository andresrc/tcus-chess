/*
 * Copyright (C) the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.derquinse.tcus.chess.solver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Value representing a solver step. Even though is not immutable, it is safely published by the
 * blocking queue backing the executor service.
 * @author Andres Rodriguez
 */
final class Step {
	/** Problem pieces. */
	private final ImmutableList<Piece> pieces;
	/** Current board state. */
	private final State state;
	/** Placed pieces positions. */
	private final Position[] positions;

	/** Creates the inital step for a problem. */
	static Step initial(Problem problem) {
		checkNotNull(problem, "The problem must be provided");
		List<Piece> pieces = problem.getPieces().stream().sorted(Comparator.comparing(p -> p.getSearchOrder()))
				.collect(Collectors.toList());
		return new Step(ImmutableList.copyOf(pieces), State.empty(problem.getSize()));
	}

	/** Constructor for initial state. */
	private Step(ImmutableList<Piece> pieces, State state) {
		this.pieces = pieces;
		this.state = state;
		this.positions = new Position[0];
	}

	/** Constructor for incremental step. */
	private Step(Step current, Position p, State s) {
		this.pieces = current.pieces;
		this.state = current.state.merge(s);
		int n = current.positions.length;
		this.positions = Arrays.copyOf(current.positions, n + 1);
		this.positions[n] = p;
	}

	/** Returns whether this step represents a solution. */
	private boolean isSolution() {
		return positions.length == pieces.size();
	}

	/**
	 * Returns the solution represented by this step. Assumes it is a solution.
	 */
	private Solution getSolution() {
		ImmutableMap.Builder<Position, Piece> b = ImmutableMap.builder();
		final Size size = state.getSize();
		for (int i = 0; i < pieces.size(); i++) {
			b.put(positions[i], pieces.get(i));
		}
		return Solution.of(size, b.build());
	}

	/**
	 * Returns the last piece placed on the board.
	 */
	private Piece getLastPiece() {
		return pieces.get(positions.length - 1);
	}

	/**
	 * Returns the piece to be used for the next step in the search.
	 */
	private Piece getNextPiece() {
		return pieces.get(positions.length);
	}

	/**
	 * Computes the next steps in the search.
	 * @param counter Solution counter.
	 * @param aggregator Solution aggregator (may be {@code null}).
	 * @return The next steps to search. Empty if the search through this path must end.
	 * @throws IllegalStateException if the step is a solution.
	 */
	List<Step> nextSteps(SolutionCounter counter, SolutionAggregator aggregator) {
		if (isSolution()) {
			counter.accept(1);
			if (aggregator != null) {
				aggregator.accept(ImmutableList.of(getSolution()));
			}
			return ImmutableList.of();
		}
		// Available positions
		final int n = state.getAvailablePositions();
		if (n < 1 || n < pieces.size() - positions.length) {
			// No room left for remaining pieces
			return ImmutableList.of();
		}
		// Initial state
		if (positions.length == 0) {
			final Piece nextPiece = getNextPiece();
			final List<Step> steps = Lists.newArrayListWithCapacity(n);
			// All positions are available.
			for (Position p : state) {
				final State pieceState = nextPiece.getState(p);
				steps.add(new Step(this, p, pieceState));
			}
			return steps;
		} else {
			// Only one piece placed
			final List<Solution> solutions = aggregator != null ? Lists.newLinkedList() : null;
			final int count = recurse(solutions);
			// Aggregate into global
			counter.accept(count);
			if (aggregator != null) {
				aggregator.accept(solutions);
			}
			return ImmutableList.of();
		}
	}

	private int recurse(List<Solution> solutions) {
		if (isSolution()) {
			if (solutions != null) {
				solutions.add(getSolution());
			}
			return 1;
		}
		final Piece nextPiece = getNextPiece();
		final boolean samePiece = nextPiece.equals(getLastPiece());
		final Position lastPos = positions[positions.length - 1];
		int count = 0;
		for (Position p : state) {
			// If two pieces are the same kind, only look forward
			if (!samePiece || p.compareTo(lastPos) > 0) {
				final State pieceState = nextPiece.getState(p);
				if (validState(pieceState)) {
					final Step next = new Step(this, p, pieceState);
					// Recurse
					count += next.recurse(solutions);
				}
			}
		}
		return count;
	}

	/** Checks that the provided state does not threaten any of the current step positions. */
	private boolean validState(State s) {
		for (Position p : positions) {
			if (!s.isAvailable(p)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Step[%s]%s[%s]", state, Arrays.toString(positions),
				pieces.subList(positions.length, pieces.size()));
	}
}
