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
import static org.testng.Assert.assertNotNull;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

/**
 * Tests for Size and Position.
 * @author Andres Rodriguez
 */
public final class SizeTest {
	/** Checks valid arguments. */
	@Test
	public void valid() {
		Size size = Size.of(5, 9);
		assertEquals(size.getRows(), 5);
		assertEquals(size.getColumns(), 9);
		assertEquals(size.getPositions(), 45);
		assertEquals(Sets.newHashSet(size).size(), 45);
		System.out.println(size.draw(p -> null));
	}

	/** Checks valid arguments. */
	@Test
	public void invalid() {
		final int[][] values = { { 0, 0 }, { 0, 3 }, { 3, 0 }, { -1, 4 }, { 4, -1 }, { -3, -5 } };
		for (int[] test : values) {
			try {
				Size.of(test[0], test[1]);
				Assert.fail(String.format("Should have failed with inputs (%d, %d)", test[0], test[1]));
			} catch (IllegalArgumentException e) {

			}
		}
	}

	/** Checks valid position by index. */
	@Test
	public void validPosition1D() {
		Size size = Size.of(5, 9);
		Position p = size.getPosition(13);
		assertNotNull(p);
		assertEquals(p.getIndex(), 13);
		assertEquals(p.getRow(), 1);
		assertEquals(p.getColumn(), 4);
	}

	/** Checks invalid positions by index. */
	@Test
	public void invalidPosition1D() {
		final Size size = Size.of(5, 9);
		final int[] values = { -100, -10, -1, 45, 46, 47, 1000 };
		for (int test : values) {
			try {
				size.getPosition(test);
				Assert.fail(String.format("Should have failed with input (%d)", test));
			} catch (IllegalArgumentException e) {

			}
		}
	}

	/** Checks valid position by coordinates. */
	@Test
	public void validPosition2D() {
		final Size size = Size.of(5, 9);
		final Position p = size.getPosition(2, 7);
		assertNotNull(p);
		assertEquals(p.getIndex(), 25);
		assertEquals(p.getRow(), 2);
		assertEquals(p.getColumn(), 7);
	}

	/** Checks invalid positions by coordinates. */
	@Test
	public void invalidPosition2D() {
		final Size size = Size.of(5, 9);
		final int[][] values = { { -1, 3 }, { 3, -1 }, { -3, -5 }, { 1, 9 }, { 5, 1 }, { -3, 15 }, { 36, -15 }, { 5, 9 } };
		for (int[] test : values) {
			try {
				size.getPosition(test[0], test[1]);
				Assert.fail(String.format("Should have failed with inputs (%d, %d)", test[0], test[1]));
			} catch (IllegalArgumentException e) {

			}
		}
	}

}
