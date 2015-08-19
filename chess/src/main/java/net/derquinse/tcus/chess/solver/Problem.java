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

import com.google.common.collect.ImmutableMultiset;

/**
 * Immutable value representing a problem.
 * @author Andres Rodriguez
 */
public final class Problem {
	/** Board size. */
	private final Size size;
	/** Pieces. */
	private final ImmutableMultiset<Piece> pieces;

	/** Creates a new problem builder. */
	public static Builder builder(Size size) {
		return new Builder(size);
	}

	/** Constructor. */
	private Problem(Builder builder) {
		this.size = builder.size;
		this.pieces = builder.pieces.build();
	}

	/** Returns the board size. */
	public Size getSize() {
		return size;
	}

	/** Return the pieces to place in the board. */
	public ImmutableMultiset<Piece> getPieces() {
		return pieces;
	}

	public static final class Builder {
		/** Board size. */
		private final Size size;
		/** Final positions. */
		private final ImmutableMultiset.Builder<Piece> pieces = ImmutableMultiset.builder();

		/** Constructor. */
		private Builder(Size size) {
			this.size = checkNotNull(size, "The board size must be provided");
		}

		/**
		 * Adds pieces to the problem.
		 * @param piece Piece kind to add.
		 * @param amount Number of pieces to add.
		 * @return This builder.
		 */
		public Builder addPieces(Piece piece, int amount) {
			checkNotNull(piece, "The piece to add must be provided");
			checkArgument(amount >= 0, "The amount to add must be >= 0");
			pieces.addCopies(piece, amount);
			return this;
		}

		public Problem build() {
			return new Problem(this);
		}

	}

}
