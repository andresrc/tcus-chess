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
public final class Position {
	/** Board size. */
	private final Size size;
	/** Position index in the board (row-based). */
	private final int index;

	/** 2-D Constructor. */
	Position(Size size, int row, int column) {
		this.size = checkNotNull(size, "The size must be provided");
		checkArgument(row >= 0 && row < size.getRows(), "Illegal row number %d for %s", row, size);
		checkArgument(column >= 0 && column < size.getColumns(), "Illegal row number %d for %s", row, size);
		checkArgument(column >= 0 && column < size.getColumns(), "Illegal column number %d for %s", row, size);
		this.index = row * size.getColumns() + column;
	}

	/** 1-D Constructor. */
	Position(Size size, int index) {
		this.size = checkNotNull(size, "The size must be provided");
		checkArgument(index >= 0 && index < size.getPositions(), "Illegal index %d for %s", index, size);
		this.index = index;
	}

	/** Returns the position as an index in the board. */
	public int getIndex() {
		return index;
	}

	/** Returns the row coordinate. */
	public int getRow() {
		return index / size.getColumns();
	}

	/** Returns the column coordinate. */
	public int getColumn() {
		return index % size.getColumns();
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
	public String toString() {
		return String.format("Position[%d](%d,%d)-of-%s", index, getRow(), getColumn(), size);
	}

	/**
	 * Class used by pieces to build their state starting from the current position.
	 * All threaten operations are independent and start from the piece position.
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

		/** Virtually threaten and if the resulting position is inside the board mark as unavailable. */
		StateBuilder threatenIfPossible(int nRows, int nColumns) {
			Integer index = delta(1, nRows, nColumns);
			if (index != null) {
				bitSet.set(index);
			}
			return this;
		}

		/** Virtually threaten and while the resulting position is inside the board mark as unavailable. */
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
