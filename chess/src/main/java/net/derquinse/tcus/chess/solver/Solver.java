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

import java.util.List;

/**
 * Interface for a challenge solver.
 * @author Andres Rodriguez
 */
public interface Solver {
	/**
	 * Solves a problem
	 * @param problem Problem to solve.
	 * @return The number of found solutions.
	 */
	int solve(Problem problem);

	/**
	 * Solves a problem, returning the found solution.
	 * @param problem Problem to solve.
	 * @return The set of found solutions.
	 */
	List<Solution> solveAndGet(Problem problem);

}
