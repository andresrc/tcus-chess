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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

/**
 * Immutable value representing a board size. Iterable through the available positions.
 * @author Andres Rodriguez
 */
public final class Size implements Iterable<Position> {
	/** Number of rows. */
	private final int rows;
	/** Number of columns. */
	private final int columns;
	/** Number of positions. Calculated, not part of the object's state. */
	private final int positions;

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
		this.positions = rows * columns;
	}

	/** Returns whether the board is a square. */
	public boolean isSquare() {
		return rows == columns;
	}

	/**
	 * Ensures the size is a square.
	 * @throws IllegalStateException if the board is not a square.
	 */
	void checkSquare() {
		checkArgument(isSquare(), "The board must be a square");
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
		return positions;
	}

	/** Returns whether an index is valid. */
	boolean isIndex(int index) {
		return index >= 0 && index < positions;
	}

	/**
	 * Ensures an index is valid.
	 * @return The provided argument.
	 * @throws IllegalArgumentException if invalid.
	 */
	int checkIndex(int index) {
		checkArgument(isIndex(index), "Invalid index for size %s", this);
		return index;
	}

	/** Returns whether a row number is valid. */
	boolean isRow(int row) {
		return row >= 0 && row < rows;
	}

	/**
	 * Ensures a row number is valid.
	 * @return The provided argument.
	 * @throws IllegalArgumentException if invalid.
	 */
	int checkRow(int row) {
		checkArgument(isRow(row), "Invalid row for size %s", this);
		return row;
	}

	/** Returns whether a column number is valid. */
	boolean isColumn(int column) {
		return column >= 0 && column < columns;
	}

	/**
	 * Ensures a column number is valid.
	 * @return The provided argument.
	 * @throws IllegalArgumentException if invalid.
	 */
	int checkColumn(int column) {
		checkArgument(isColumn(column), "Invalid column for size %s", this);
		return column;
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
	public Iterator<Position> iterator() {
		return new PositionIterator();
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

	/**
	 * Draws a board
	 * @param a Drawing destination.
	 * @param contents Board contents.
	 * @return The provided builder for method chaining.
	 */
	public Appendable draw(Appendable a, Function<Position, Character> contents) throws IOException {
		final String line = getRowLine();
		a.append(line);
		for (int r = 0; r < rows; r++) {
			a.append('|');
			for (int c = 0; c < columns; c++) {
				final Position p = getPosition(r, c);
				final char ch = MoreObjects.firstNonNull(contents.apply(p), ' ');
				a.append(ch).append('|');
			}
			a.append('\n').append(line);
		}
		return a;
	}

	/**
	 * Draws a board
	 * @param contents Board contents.
	 */
	public String draw(Function<Position, Character> contents) {
		try {
			return draw(new StringBuilder(), contents).toString();
		} catch (IOException e) {
			// Should not happen
			throw new RuntimeException(e);
		}
	}

	private String getRowLine() {
		StringBuilder b = new StringBuilder().append('+');
		for (int i = 0; i < columns; i++) {
			b.append("-+");
		}
		return b.append('\n').toString();
	}

	/** Position Iterator. */
	private final class PositionIterator implements Iterator<Position> {
		private int index = 0;

		/** Constructor. */
		private PositionIterator() {
		}

		@Override
		public boolean hasNext() {
			return index < positions;
		}

		@Override
		public Position next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			final Position p = getPosition(index);
			index++;
			return p;
		}
	}

}
