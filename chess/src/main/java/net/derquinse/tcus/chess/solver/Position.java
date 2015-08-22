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

import java.util.BitSet;
import java.util.Objects;

/**
 * Immutable value representing a position in a board of a certain size..
 * @author Andres Rodriguez
 */
public final class Position implements Comparable<Position> {
	/** Board size. */
	private final Size size;
	/** Position index in the board (row-based). */
	private final int index;
	/** Row coordinate. Calculated, not part of state. */
	private final int row;
	/** Column coordinate. Calculated, not part of state. */
	private final int column;

	/** 2-D Constructor. */
	Position(Size size, int row, int column) {
		this.size = checkNotNull(size, "The size must be provided");
		this.row = size.checkRow(row);
		this.column = size.checkColumn(column);
		this.index = row * size.getColumns() + column;
	}

	/** 1-D Constructor. */
	Position(Size size, int index) {
		this.size = checkNotNull(size, "The size must be provided");
		this.index = size.checkIndex(index);
		this.row = index / size.getColumns();
		this.column = index % size.getColumns();
	}

	/** The size is position is referenced to. */
	public Size getSize() {
		return size;
	}

	/** Returns the position as an index in the board. */
	public int getIndex() {
		return index;
	}

	/** Returns the row coordinate. */
	public int getRow() {
		return row;
	}

	/** Returns the column coordinate. */
	public int getColumn() {
		return column;
	}

	/**
	 * Gets the position as is the board rotated 90deg clockwise.
	 * @throws IllegalStateException if the board is not a square.
	 */
	public Position rotate90() {
		size.checkSquare();
		return new Position(size, column, size.getColumns() - 1 - row);
	}

	/**
	 * Gets the position as is the board rotated 180deg clockwise.
	 */
	public Position rotate180() {
		return new Position(size, size.getRows() - 1 - row, size.getColumns() - 1 - column);
	}

	/** Creates a state builder based on the current position. */
	StateBuilder builder() {
		return new StateBuilder();
	}

	@Override
	public int hashCode() {
		return Objects.hash(size, index);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			final Position other = (Position) obj;
			return index == other.index && size.equals(other.size);
		}
		return false;
	}

	@Override
	public int compareTo(Position o) {
		checkArgument(size.equals(o.size), "Position sizes must be equal to be compared");
		return index - o.index;
	}

	@Override
	public String toString() {
		return String.format("Position[%d](%d,%d)-of-%s", index, row, column, size);
	}

	/**
	 * Class used by pieces to build their state starting from the current position. All threaten
	 * operations are independent and start from the piece position.
	 */
	final class StateBuilder {
		private final BitSet bitSet = new BitSet();
		/** Whether the state has been built. */
		private boolean built = false;

		private StateBuilder() {
			bitSet.set(index);
		}

		private void checkNotBuilt() {
			checkState(!built, "State already built");
		}

		private Integer delta(int steps, int nRows, int nColumns) {
			checkNotBuilt();
			checkArgument(steps > 0, "The number of steps must be > 0");
			int row = getRow() + steps * nRows;
			int column = getColumn() + steps * nColumns;
			if (row >= 0 && column >= 0 && row < size.getRows() && column < size.getColumns()) {
				return row * size.getColumns() + column;
			}
			return null;
		}

		/** Virtually threaten if the resulting position is inside the board. */
		StateBuilder threatenIfPossible(int nRows, int nColumns) {
			Integer index = delta(1, nRows, nColumns);
			if (index != null) {
				bitSet.set(index);
			}
			return this;
		}

		/** Virtually threaten while the resulting position is inside the board mark. */
		StateBuilder threatenWhilePossible(int nRows, int nColumns) {
			for (int steps = 1;; steps++) {
				Integer index = delta(steps, nRows, nColumns);
				if (index != null) {
					bitSet.set(index);
				} else {
					break;
				}
			}
			return this;
		}

		/** Builds the state. */
		State build() {
			checkNotBuilt();
			built = true;
			return State.of(size, bitSet);
		}

	}

}
