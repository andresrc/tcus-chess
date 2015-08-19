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
	
	@Override
	public int hashCode() {
		return Objects.hash(size, index);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			final Position other = (Position)obj;
			return index == other.index && size.equals(other.size);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("Position[%d](%d,%d)-of-%s", index, getRow(), getColumn(), size);
	}
	
}
