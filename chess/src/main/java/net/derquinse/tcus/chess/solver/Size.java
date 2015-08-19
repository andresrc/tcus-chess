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

/**
 * Immutable value representing a board size.
 * @author Andres Rodriguez
 */
public final class Size {
	/** Number of rows. */
	private final int rows;
	/** Number of columns. */
	private final int columns;
	
	/** Returns a Size object with the provided numbers of rows and columns. */
	public static Size of(int rows, int columns) {
		return new Size(rows, columns);
	}

	/** Constructor. */
	private Size(int rows, int columns) {
		checkArgument(rows > 0, "The number of rows must be > 0");
		checkArgument(columns > 0, "The number of columns must be > 0");
		this.rows = rows;
		this.columns = columns;
	}

	/** Returns the number of rows. */
	public int getRows() {
		return rows;
	}

	/** Returns the number of columns. */
	public int getColumns() {
		return columns;
	}
	
	/** Returns the numbers of positions. */
	public int getPositions() {
		return rows*columns;
	}
	
	/** Returns the position with the provided index. */
	Position getPosition(int index) {
		return new Position(this, index);
	}

	/** Returns the position with the provided row and column values. */
	Position getPosition(int row, int column) {
		return new Position(this, row, column);
	}
	
	@Override
	public int hashCode() {
		return 31 * rows + columns;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Size) {
			final Size other = (Size) obj;
			return rows == other.rows && columns == other.columns;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("Size(%d,%d)", rows, columns);
	}

}
