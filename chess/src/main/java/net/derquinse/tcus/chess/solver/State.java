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

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

/**
 * Immutable value representing a board state. A board state is defined by the the of available
 * positions (which can be iterated), that are those that are not occupied by another piece and are
 * not threatened by an existing piece.
 * @author Andres Rodriguez
 */
abstract class State implements Iterable<Integer> {
	/** Board size. */
	private final Size size;

	/** Returns an empty board state of the provided size. */
	static State empty(Size size) {
		return new Empty(size);
	}

	/**
	 * Creates a new state.
	 * @param size Board size.
	 * @param unavailable Availability set. WARNING: not defensively copied. Must not be used after
	 *          calling this method.
	 * @return the requested state.
	 */
	static State of(Size size, BitSet unavailable) {
		if (checkNotNull(unavailable, "The availability set must be provided").length() == 0) {
			return new Empty(size);
		}
		return new Regular(size, unavailable);
	}

	/** Constructor. */
	private State(Size size) {
		this.size = checkNotNull(size, "The board size must be provided");
	}

	final Size getSize() {
		return size;
	}

	/**
	 * Merges two states. States can be merged if their unanavailable positions do not intersect.
	 * @param other State to merge.
	 * @return The merged state or empty if they cannot be merged.
	 */
	final Optional<State> merge(State other) {
		final Size otherSize = checkNotNull(other, "The state to merge must be provided").getSize();
		checkArgument(size.equals(otherSize), "The state to merge must be of the same size");
		return doMerge(other);
	}

	/** Internal merge method. Do not call externally. */
	abstract Optional<State> doMerge(State other);

	/**
	 * Empty board state.
	 */
	private static final class Empty extends State {
		private Empty(Size size) {
			super(size);
		}

		@Override
		public Iterator<Integer> iterator() {
			return ContiguousSet.create(Range.closedOpen(0, getSize().getPositions()), DiscreteDomain.integers()).iterator();
		}

		@Override
		Optional<State> doMerge(State other) {
			return Optional.of(other);
		}
	}

	/**
	 * Regular board state (at least one unavailable position).
	 */
	private static final class Regular extends State {
		/**
		 * Board state as a bit set. False means available. BitSet is mutable, its state is published
		 * between threads by the blocking queue backing the executor.
		 */
		private final BitSet unavailable;

		private Regular(Size size, BitSet unavailable) {
			super(size);
			final int last = unavailable.length() - 1;
			checkArgument(last >= 0 && last < size.getPositions());
			this.unavailable = unavailable;
		}

		@Override
		public Iterator<Integer> iterator() {
			return new Availables();
		}

		@Override
		Optional<State> doMerge(State other) {
			if (other instanceof Regular) {
				final Regular r = (Regular) other;
				if (unavailable.intersects(r.unavailable)) {
					return Optional.empty();
				}
				BitSet merged = (BitSet) unavailable.clone();
				merged.or(r.unavailable);
				return Optional.of(of(getSize(), merged));
			}
			return Optional.of(this); // the other is empty
		}

		private final class Availables implements Iterator<Integer> {
			/** Current index. */
			private int current;

			/** Constructor. */
			Availables() {
				this.current = unavailable.nextClearBit(0);
			}

			@Override
			public boolean hasNext() {
				return current < getSize().getPositions();
			}

			@Override
			public Integer next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				final int value = current;
				current = unavailable.nextClearBit(current + 1);
				return value;
			}
		}

	}

}
