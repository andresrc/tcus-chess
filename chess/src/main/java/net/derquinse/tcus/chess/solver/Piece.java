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

import net.derquinse.tcus.chess.solver.Position.StateBuilder;

/**
 * Enumeration representing available pieces.
 * @author Andres Rodriguez
 */
public enum Piece {
	KING(4, 'K') {
		@Override
		StateBuilder buildState(StateBuilder b) {
			return b.threatenIfPossible(-1, 0).threatenIfPossible(-1, 1).threatenIfPossible(0, 1).threatenIfPossible(1, 1)
					.threatenIfPossible(1, 0).threatenIfPossible(1, -1).threatenIfPossible(0, -1).threatenIfPossible(-1, -1);
		}
	},
	QUEEN(0, 'Q') {
		@Override
		StateBuilder buildState(StateBuilder b) {
			return b.threatenWhilePossible(-1, 0).threatenWhilePossible(-1, 1).threatenWhilePossible(0, 1)
					.threatenWhilePossible(1, 1).threatenWhilePossible(1, 0).threatenWhilePossible(1, -1)
					.threatenWhilePossible(0, -1).threatenWhilePossible(-1, -1);
		}
	},
	BISHOP(1, 'B') {
		@Override
		StateBuilder buildState(StateBuilder b) {
			return b.threatenWhilePossible(-1, 1).threatenWhilePossible(1, 1).threatenWhilePossible(1, -1)
					.threatenWhilePossible(-1, -1);
		}
	},
	ROOK(2, 'R') {
		@Override
		StateBuilder buildState(StateBuilder b) {
			return b.threatenWhilePossible(-1, 0).threatenWhilePossible(0, 1).threatenWhilePossible(1, 0)
					.threatenWhilePossible(0, -1);
		}
	},
	KNIGHT(3, 'N') {
		@Override
		StateBuilder buildState(StateBuilder b) {
			return b.threatenIfPossible(-2, -1).threatenIfPossible(-2, 1).threatenIfPossible(-1, 2).threatenIfPossible(1, 2)
					.threatenIfPossible(2, -1).threatenIfPossible(2, 1).threatenIfPossible(-1, -2).threatenIfPossible(1, -2);
		}
	};
	
	/** Search order: we start searching by those with grater mobility, in order to indetify invalid paths as soon as possible. */
	private final int searchOrder;
	/** Representation. */
	private final char representation;
	
	private Piece(int searchOrder, char representation) {
		this.searchOrder = searchOrder;
		this.representation = representation;
	}
	
	/** Returns the search order. */
	final int getSearchOrder() {
		return searchOrder;
	}
	
	/** Returns the character used in representations. */
	final char getRepresentation() {
		return representation;
	}

	/** Builds the state for a piece in a given position. */
	final State getState(Position p) {
		return buildState(p.builder()).build();
	}

	/** Internal method to build the state. */
	abstract StateBuilder buildState(StateBuilder b);
}
