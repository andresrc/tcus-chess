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
import static com.google.common.base.Preconditions.checkState;

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
	/** Current state. */
	private final State state;
	/** Step key. */
	private final Key key;

	/** Creates the inital step for a problem. */
	static Step initial(Problem problem) {
		checkNotNull(problem, "The problem must be provided");
		List<Piece> pieces = problem.getPieces().stream().sorted(Comparator.comparing(p -> p.getSearchOrder()))
				.collect(Collectors.toList());
		return new Step(ImmutableList.copyOf(pieces), State.empty(problem.getSize()), new Key());
	}

	/** Constructor. */
	private Step(ImmutableList<Piece> pieces, State state, Key key) {
		this.pieces = pieces;
		this.state = state;
		this.key = key;
	}

	/** Returns the step key, to control visited steps. */
	Key getKey() {
		return key;
	}

	/** Returns whether this step represents a solution. */
	boolean isSolution() {
		return key.positions.length == pieces.size();
	}

	/**
	 * Returns the solution represented by this step.
	 * @throws IllegalStateException if the step is not a solution.
	 */
	Solution getSolution() {
		checkState(isSolution(), "Not a solution");
		ImmutableMap.Builder<Integer, Piece> b = ImmutableMap.builder();
		for (int i = 0; i < pieces.size(); i++) {
			b.put(key.positions[i], pieces.get(i));
		}
		return Solution.of(state.getSize(), b.build());
	}

	/**
	 * Returns the piece to be used for the next step in the search.
	 * @throws IllegalStateException if the step is a solution.
	 */
	private Piece getNextPiece() {
		checkState(!isSolution(), "Already a solution");
		return pieces.get(key.positions.length);
	}

	/**
	 * Computes the next steps in the search.
	 * @param index Available index to use in the next step.
	 * @return The next steps to search (which may be solutions). Empty if the search through this
	 *         path must end.
	 * @throws IllegalStateException if the step is a solution.
	 */
	Iterable<Step> nextSteps() {
		checkState(!isSolution(), "Already a solution");
		// Available positions
		final int n = state.getAvailablePositions();
		if (n < pieces.size() - key.positions.length) {
			//System.out.printf("No room left in state %s\n", this);
			return ImmutableList.of();
		}
		final List<Step> steps = Lists.newArrayListWithCapacity(n);
		final Piece next = getNextPiece();
		// Initial state
		if (key.positions.length == 0) {
			// All positions are available
			for (Position p : state) {
				final State pieceState = next.getState(p);
				steps.add(new Step(pieces, pieceState, new Key(key, pieces, p.getIndex())));
			}
		} else {
			for (Position p : state) {
				final State pieceState = next.getState(p);
				if (validState(pieceState)) {
					final State merged = state.merge(pieceState);
					steps.add(new Step(pieces, merged, new Key(key, pieces, p.getIndex())));
				}
			}
		}
		return steps;
	}

	/** Checks that the provided state does not threaten any of the current step positions. */
	private boolean validState(State s) {
		for (int p : key.positions) {
			if (!s.isAvailable(p)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Step[%s]%s[%s]", state, Arrays.toString(key.positions),
				pieces.subList(key.positions.length, pieces.size()));
	}

	static final class Key {
		/** Placed pieces positions. */
		private final int[] positions;
		/** Hash code (precalculated). */
		private final int hash;

		/** Constructor for an initial key. */
		private Key() {
			this.positions = new int[0];
			this.hash = Arrays.hashCode(positions);
		}

		/** Constructor for a derived key. */
		private Key(Key key, List<Piece> pieces, int nextPosition) {
			int n = key.positions.length;
			this.positions = Arrays.copyOf(key.positions, n + 1);
			this.positions[n] = nextPosition;
			// To make equivalent steps have equal keys, we order the positions for pieces of the same kind
			while (n > 0 && pieces.get(n) == pieces.get(n-1) && positions[n] < positions[n-1]) {
				int t = positions[n];
				positions[n] = positions[n-1];
				positions[n-1] = t;
				n--;						
			}
			this.hash = Arrays.hashCode(positions);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				final Key other = (Key) obj;
				return hash == other.hash && Arrays.equals(positions, other.positions);
			}
			return false;
		}
	}

}
