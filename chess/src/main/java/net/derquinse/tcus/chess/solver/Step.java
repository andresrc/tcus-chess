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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Value representing a solver step. Even though is not immutable, it is safely published by the
 * blocking queue backing the executor service.
 * @author Andres Rodriguez
 */
final class Step {
	/** Current state. */
	private final State state;
	/** Placed pieces. */
	private final Map<Integer, Piece> positions;
	/** Pending pieces. */
	private final List<Piece> pieces;

	Step initial(Problem problem) {
		checkNotNull(problem, "The problem must be provided");
		List<Piece> pieces = problem.getPieces().stream().sorted(Comparator.comparing(p -> p.getSearchOrder()))
				.collect(Collectors.toList());
		return new Step(State.empty(problem.getSize()), ImmutableMap.of(), pieces);
	}

	/** Constructor. */
	private Step(State state, Map<Integer, Piece> positions, List<Piece> pieces) {
		this.state = state;
		this.positions = positions;
		this.pieces = pieces;
	}

	/** Returns whether this step represents a solution. */
	boolean isSolution() {
		return pieces.isEmpty();
	}

	/**
	 * Returns the solution represented by this step.
	 * @throws IllegalStateException if the step is not a solution.
	 */
	Solution getSolution() {
		checkState(isSolution(), "Not a solution");
		return Solution.of(state.getSize(), positions);
	}

	/** Return the available positions in the board. */
	Iterable<Integer> getAvailable() {
		return state;
	}

	/**
	 * Computes the next step in the search.
	 * @param index Available index to use in the next step.
	 * @return The next step or empty if not possible and the search through this path must end.
	 * @throws IllegalStateException if the step is a solution.
	 * @throws IllegalArgumentException if the index is invalid or not available.
	 */
	Optional<Step> next(int index) {
		checkState(!isSolution(), "Already a solution");
		checkArgument(state.isAvailable(index), "Provided index not available");
		final Piece next = pieces.get(0);
		final Position p = state.getSize().getPosition(index);
		final State pieceState = next.getState(p);
		final Optional<State> merged = state.merge(pieceState);
		if (merged.isPresent()) {
			Map<Integer, Piece> map = Maps.newHashMap(positions);
			map.put(index, next);
			return Optional.of(new Step(merged.get(), map, pieces.subList(1, pieces.size())));
		}
		return Optional.empty();
	}

}
