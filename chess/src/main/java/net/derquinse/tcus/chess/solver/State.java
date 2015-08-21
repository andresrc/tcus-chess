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

/**
 * Immutable value representing a board state. A board state is defined by the the of available
 * positions (which can be iterated), that are those that are not occupied by another piece and are
 * not threatened by an existing piece.
 * @author Andres Rodriguez
 */
abstract class State implements Iterable<Position> {
	/** Board size. */
	private final Size size;

	/** Returns an empty board state of the provided size. */
	static State empty(Size size) {
		return new Empty(size);
	}

	/**
	 * Creates a new state.
	 * @param size Board size.
	 * @param unavailable Availability set. WARNING: not defensively copied. MUST NOT not be used
	 *          after calling this method.
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

	/** Returns the board size. */
	final Size getSize() {
		return size;
	}

	/** Return the number of available positions. */
	abstract int getAvailablePositions();

	/**
	 * Merges two states. Their unavailability zones are or'd
	 * @return The merged state.
	 * @throws IllegalArgumentException if the state to merge is not the same size.
	 */
	final State merge(State other) {
		final Size otherSize = checkNotNull(other, "The state to merge must be provided").getSize();
		checkArgument(size.equals(otherSize), "The state to merge must be of the same size");
		return doMerge(other);
	}

	/** Internal merge method. Do not call externally. */
	abstract State doMerge(State other);

	/** Method to check if a position is available. */
	abstract boolean isAvailable(Position p);

	/**
	 * Empty board state.
	 */
	private static final class Empty extends State {
		private Empty(Size size) {
			super(size);
		}

		@Override
		public Iterator<Position> iterator() {
			return getSize().iterator();
		}

		@Override
		State doMerge(State other) {
			return other;
		}

		@Override
		int getAvailablePositions() {
			return getSize().getPositions();
		}

		@Override
		boolean isAvailable(Position p) {
			return true;
		}

		@Override
		public String toString() {
			return "EmptyState";
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
		public Iterator<Position> iterator() {
			return new Availables();
		}

		@Override
		State doMerge(State other) {
			if (other instanceof Regular) {
				final Regular r = (Regular) other;
				BitSet merged = (BitSet) unavailable.clone();
				merged.or(r.unavailable);
				return of(getSize(), merged);
			}
			return this; // the other is empty
		}

		@Override
		int getAvailablePositions() {
			return getSize().getPositions() - unavailable.cardinality();
		}

		@Override
		boolean isAvailable(Position p) {
			return !unavailable.get(p.getIndex());
		}

		@Override
		public String toString() {
			return String.format("State[%s]", unavailable);
		}

		private final class Availables implements Iterator<Position> {
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
			public Position next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				final int value = current;
				current = unavailable.nextClearBit(current + 1);
				return getSize().getPosition(value);
			}
		}

	}

}
