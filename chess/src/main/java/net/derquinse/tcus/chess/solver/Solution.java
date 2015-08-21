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

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

/**
 * Immutable value representing a problem solution.
 * @author Andres Rodriguez
 */
public final class Solution {
	/** Board size. */
	private final Size size;
	/** Final positions. */
	private final ImmutableMap<Integer, Piece> positions;
	/** Hash code (cached, as solutions are returned as a set). */
	private final int hash;

	/** Creates a new solution. */
	static Solution of(Size size, Map<Integer, Piece> positions) {
		checkNotNull(size, "The board size must be provided");
		checkNotNull(positions, "The finals positions must be provided");
		checkArgument(!positions.isEmpty(), "Positions cannot be empty");
		checkArgument(positions.keySet().stream().allMatch(p -> p >= 0 && p < size.getPositions()),
				"Solution positions must be valid");
		return new Solution(size, ImmutableMap.copyOf(positions));
	}

	/** Constructor. */
	private Solution(Size size, ImmutableMap<Integer, Piece> positions) {
		this.size = size;
		this.positions = positions;
		this.hash = Objects.hash(size, positions);
	}

	/** Returns the board size. */
	public Size getSize() {
		return size;
	}

	/** Returns the final positions (immutable). */
	public ImmutableMap<Integer, Piece> getPositions() {
		return positions;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Solution) {
			Solution other = (Solution) obj;
			return hash == other.hash && size.equals(other.size) && positions.equals(other.positions);
		}
		return false;
	}

	/**
	 * Draws a board containing a solution.
	 * @param b Builder to add the drawing to.
	 * @return The provided builder for method chaining.
	 */
	public StringBuilder draw(StringBuilder b) {
		return size.draw(b, p -> {
			int i = p.getIndex();
			if (positions.containsKey(i)) {
				return positions.get(i).getRepresentation();
			}
			return null;
		});
	}

	/**
	 * Draws a board containing a solution.
	 */
	public String draw() {
		return draw(new StringBuilder()).toString();
	}

}
