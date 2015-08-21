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
	/** Current board state. */
	private final State state;
	/** Placed pieces positions. */
	private final int[] positions;

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
		this.positions = new int[0];
	}

	/** Constructor for incremental step. */
	private Step(Step current, Position p, State s) {
		this.pieces = current.pieces;
		this.state = current.state.merge(s);
		int n = current.positions.length;
		this.positions = Arrays.copyOf(current.positions, n + 1);
		this.positions[n] = p.getIndex();
	}

	/** Returns whether this step represents a solution. */
	boolean isSolution() {
		return positions.length == pieces.size();
	}

	/**
	 * Returns the solution represented by this step.
	 * @throws IllegalStateException if the step is not a solution.
	 */
	Solution getSolution() {
		checkState(isSolution(), "Not a solution");
		ImmutableMap.Builder<Integer, Piece> b = ImmutableMap.builder();
		for (int i = 0; i < pieces.size(); i++) {
			b.put(positions[i], pieces.get(i));
		}
		return Solution.of(state.getSize(), b.build());
	}

	/**
	 * Returns the last piece placed on the board.
	 * @throws IllegalStateException if the step is an initial step.
	 */
	private Piece getLastPiece() {
		checkState(positions.length > 0, "Initial state");
		return pieces.get(positions.length - 1);
	}

	/**
	 * Returns the piece to be used for the next step in the search.
	 * @throws IllegalStateException if the step is a solution.
	 */
	private Piece getNextPiece() {
		checkState(!isSolution(), "Already a solution");
		return pieces.get(positions.length);
	}

	/**
	 * Computes the next steps in the search.
	 * @param index Available index to use in the next step.
	 * @return The next steps to search (which may be solutions). Empty if the search through this
	 *         path must end.
	 * @throws IllegalStateException if the step is a solution.
	 */
	List<Step> nextSteps() {
		if (isSolution()) {
			return ImmutableList.of(this);
		}
		// Available positions
		final int n = state.getAvailablePositions();
		if (n < 1 || n < pieces.size() - positions.length) {
			// No room left for remaining pieces
			return ImmutableList.of();
		}
		final List<Step> steps = Lists.newLinkedList();
		final Piece nextPiece = getNextPiece();
		// Initial state
		if (positions.length == 0) {
			// All positions are available
			for (Position p : state) {
				final State pieceState = nextPiece.getState(p);
				steps.add(new Step(this, p, pieceState));
			}
		} else {
			final boolean samePiece = nextPiece.equals(getLastPiece());
			final int lastPos = positions[positions.length-1];
			for (Position p : state) {
				// Optimization: if two pieces are the same kind, only look forward
				if (!samePiece || p.getIndex() > lastPos) {
					final State pieceState = nextPiece.getState(p);
					if (validState(pieceState)) {
						final Step next = new Step(this, p, pieceState);
						steps.addAll(next.nextSteps());
					}
				}
			}
		}
		return steps;
	}

	/** Checks that the provided state does not threaten any of the current step positions. */
	private boolean validState(State s) {
		for (int p : positions) {
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
