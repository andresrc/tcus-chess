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

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import net.derquinse.tcus.chess.solver.Position.StateBuilder;

import org.testng.annotations.Test;

/**
 * Tests for StateBuilder.
 * @author Andres Rodriguez
 */
public final class StateBuilderTest {

	private static void check(State s, Integer... indexes) {
		assertEquals(newHashSet(s), newHashSet(indexes));
	}

	private static void check(StateBuilder b, Integer... indexes) {
		check(b.build(), indexes);
	}

	/** Exercise with simple movements. */
	@Test
	public void simple() {
		final Size size = Size.of(3, 3);
		final Position p = size.getPosition(1, 1);
		check(p.builder(), 0, 1, 2, 3, 5, 6, 7, 8);
		check(p.builder().threatenIfPossible(-1, 1), 0, 1, 3, 5, 6, 7, 8);
		check(p.builder().threatenIfPossible(-1, 1).threatenIfPossible(-1, 5), 0, 1, 3, 5, 6, 7, 8);
	}

	/** Exercise with continuous movements. */
	@Test
	public void continuous() {
		final Size size = Size.of(4, 4);
		final Position p = size.getPosition(1, 1);
		check(p.builder().threatenWhilePossible(1, 0), 0, 1, 2, 3, 4, 6, 7, 8, 10, 11, 12, 14, 15);
		check(p.builder().threatenWhilePossible(1, 0).threatenWhilePossible(-1, 0), 0, 2, 3, 4, 6, 7, 8, 10, 11, 12, 14, 15);
		check(p.builder().threatenWhilePossible(1, 0).threatenWhilePossible(-1, 0).threatenWhilePossible(1, 1), 0, 2, 3, 4,
				6, 7, 8, 11, 12, 14);
	}

	/** Knight test. */
	@Test
	public void knight() {
		final Size size = Size.of(4, 4);
		final Position p = size.getPosition(0, 1);
		check(Piece.KNIGHT.getState(p), 0, 2, 3, 4, 5, 6, 9, 11, 12, 13, 14, 15);
	}

}
