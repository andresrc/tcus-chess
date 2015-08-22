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
package net.derquinse.tcus.chess;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.derquinse.tcus.chess.solver.Piece;
import net.derquinse.tcus.chess.solver.Problem;
import net.derquinse.tcus.chess.solver.Size;
import net.derquinse.tcus.chess.solver.Solution;
import net.derquinse.tcus.chess.solver.Solver;
import net.derquinse.tcus.chess.solver.Solvers;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.validators.PositiveInteger;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * Chess Challenge main class.
 * @author Andres Rodriguez
 */
public final class ChessChallenge {
	/** Number of rows. */
	@Parameter(names = "-rows", description = "Number of rows", validateWith = GreaterThanZero.class)
	private int rows = 8;
	/** Number of columns. */
	@Parameter(names = "-columns", description = "Number of columns", validateWith = GreaterThanZero.class)
	private int columns = 8;
	/** Number of kings. */
	@Parameter(names = "-kings", description = "Number of kings", validateWith = PositiveInteger.class)
	private int kings = 0;
	/** Number of queens. */
	@Parameter(names = "-queens", description = "Number of queens", validateWith = PositiveInteger.class)
	private int queens = 0;
	/** Number of bishops. */
	@Parameter(names = "-bishops", description = "Number of bishops", validateWith = PositiveInteger.class)
	private int bishops = 0;
	/** Number of rooks. */
	@Parameter(names = "-rooks", description = "Number of rooks", validateWith = PositiveInteger.class)
	private int rooks = 0;
	/** Number of knights. */
	@Parameter(names = "-knights", description = "Number of knights", validateWith = PositiveInteger.class)
	private int knights = 0;
	/** Number of threads to use. */
	@Parameter(names = "-threads", description = "Number of threads to use", validateWith = GreaterThanZero.class)
	private int threads = 1;
	/** Output file (solution boards). */
	@Parameter(names = "-output", description = "Output file (solution boards)", converter = FileConverter.class)
	private File output = null;

	/** Constructor. */
	private ChessChallenge() {
	}

	/** Entry point. */
	public static void main(String[] args) {
		final ChessChallenge challenge = new ChessChallenge();
		try {
			final JCommander jc = new JCommander(challenge, args);
			jc.setProgramName("ChessChallenge");
			if (args.length == 0) {
				jc.usage();
			} else {
				challenge.run();
			}
		} catch (ParameterException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private void run() {
		final Problem p = Problem.builder(Size.of(rows, columns)).addPieces(Piece.KING, kings)
				.addPieces(Piece.QUEEN, queens).addPieces(Piece.BISHOP, bishops).addPieces(Piece.ROOK, rooks)
				.addPieces(Piece.KNIGHT, knights).build();
		System.out
				.printf(
						"Solving for %d king(s), %d queen(s), %d bishop(s), %d rook(s) and %d knight(s) in a %d row(s) by %d column(s) board\n",
						kings, queens, bishops, rooks, knights, rows, columns);
		final Solver solver = Solvers.defaultSolver(threads);
		final int count;
		final List<Solution> solutions;
		final Stopwatch w = Stopwatch.createStarted();
		if (output != null) {
			solutions = solver.solveAndGet(p);
			count = solutions.size();
		} else {
			count = solver.solve(p);
			solutions = ImmutableList.of();
		}
		System.out.printf("Found % d solutions in %s using %d threads\n", count, w, threads);
		if (output != null && !solutions.isEmpty()) {
			try (Writer writer = Files.newWriter(output, Charsets.UTF_8)) {
				for (Solution s : solutions) {
					s.draw(writer);
					writer.append('\n');
				}
			} catch (IOException e) {
				System.err.printf("Error writing output file [%s]: %s\n", output, e.getMessage());
			}
		}

	}

	/**
	 * A validator for greater than zero arguments.
	 */
	public static class GreaterThanZero implements IParameterValidator {

		public void validate(String name, String value) throws ParameterException {
			int n = Integer.parseInt(value);
			if (n < 1) {
				throw new ParameterException("Parameter " + name + " should be greater than 0 (found " + value + ")");
			}
		}

	}

}
