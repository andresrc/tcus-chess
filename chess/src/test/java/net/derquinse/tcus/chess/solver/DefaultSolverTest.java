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

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;

/**
 * Tests for Default Solver.
 * @author Andres Rodriguez
 */
public final class DefaultSolverTest {
	private final DefaultSolver solver = new DefaultSolver(2);

	private Set<Solution> check(Problem p, int expectedSolutions) {
		Set<Solution> solutions = solver.solve(p);
		assertEquals(solutions.size(), expectedSolutions);
		return solutions;
	}

	private Set<Solution> check(Problem.Builder b, int expectedSolutions) {
		return check(b.build(), expectedSolutions);
	}

	/** Base case. */
	@Test
	public void base() {
		check(Problem.builder(Size.of(2, 2)).addPieces(Piece.QUEEN, 1), 4);
	}

	/** Example 1. */
	@Test
	public void example1() {
		for(Solution s: check(Problem.builder(Size.of(3, 3)).addPieces(Piece.KING, 2).addPieces(Piece.ROOK, 1), 4)) {
			System.out.println(s.draw());
		}
	}

	/** Example 2. */
	@Test
	public void example2() {
		for(Solution s: check(Problem.builder(Size.of(4, 4)).addPieces(Piece.KNIGHT, 4).addPieces(Piece.ROOK, 2), 8)) {
			System.out.println(s.draw());
		}
	}
	
	/** Empty. */
	@Test
	public void empty() {
		check(Problem.builder(Size.of(15, 15)), 0);
	}
	
	
	/** One piece. */
	@Test
	public void onePiece() {
		check(Problem.builder(Size.of(8, 8)).addPieces(Piece.QUEEN, 1), 64);
	}
	
	private void queens(int n, int sols) {
		check(Problem.builder(Size.of(n, n)).addPieces(Piece.QUEEN, n), sols);
	}
	
	/** Four queens. */
	@Test
	public void fourQueens() {
		queens(4, 2);
	}

	/** Five queens. */
	@Test
	public void fiveQueens() {
		queens(5, 10);
	}

	/** Six queens. */
	@Test
	public void sixQueens() {
		queens(6, 4);
	}
	
	/** Eight queens. */
	@Test
	public void eightQueens() {
		check(Problem.builder(Size.of(8, 8)).addPieces(Piece.QUEEN, 8), 92);
	}

	/** Simplified Challenge (1 more queen). */
	@Test
	public void simplifiedChallenge() {
		check(Problem.builder(Size.of(7, 7)).addPieces(Piece.KING, 2).addPieces(Piece.QUEEN, 3).addPieces(Piece.BISHOP, 2).addPieces(Piece.KNIGHT, 1), 169464);
	}
	
}
