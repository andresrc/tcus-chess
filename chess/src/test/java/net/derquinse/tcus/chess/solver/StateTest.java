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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;

/**
 * Tests for State.
 * @author Andres Rodriguez
 */
public final class StateTest {
	/** Exercise empty state. */
	@Test
	public void empty() {
		final Size size = Size.of(7, 8);
		final State empty = State.empty(size);
		assertNotNull(empty);
		Set<Position> available = Sets.newHashSet(empty);
		assertEquals(available.size(), 56);
		for (int i = 0; i < 56; i++) {
			assertTrue(available.contains(size.getPosition(i)));
		}
	}

	private static void setBits(BitSet set, Iterable<Integer> positions) {
		for (int p : positions) {
			set.set(p);
		}
	}

	@SuppressWarnings("unused")
	private static void setBits(BitSet set, Integer... positions) {
		setBits(set, Arrays.asList(positions));
	}

	private static BitSet bitSet(Integer... positions) {
		final BitSet s = new BitSet();
		setBits(s, Arrays.asList(positions));
		return s;
	}

	private static State state(Size size, Integer... positions) {
		return State.of(size, bitSet(positions));
	}

	/** Exercise regular state. */
	@Test
	public void regular() {
		final Size size = Size.of(7, 8);
		final BitSet set = new BitSet();
		final Set<Integer> unavailable = newHashSet(10, 20, 30, 40);
		setBits(set, unavailable);
		final State regular = State.of(size, set);
		assertNotNull(regular);
		Set<Position> available = newHashSet(regular);
		assertEquals(available.size(), 52);
		for (int i = 0; i < 56; i++) {
			assertTrue(available.contains(size.getPosition(i)) != unavailable.contains(i));
		}
	}

	/** Invalid regular state. */
	@Test
	public void invalidRegular() {
		final Size size = Size.of(7, 8);
		final Set<Integer> limits = newHashSet(56, 60, 120);
		for (int limit : limits) {
			try {
				final BitSet set = bitSet(10, 20, 30);
				set.set(limit);
				State.of(size, set);
				fail(String.format("Should have failed with input (%d)", limit));
			} catch (IllegalArgumentException e) {
			}
		}
	}

	/** Regular merge. */
	@Test
	public void merge() {
		final Size size = Size.of(7, 8);
		final State s1 = state(size, 10, 20, 30, 40);
		final State s2 = state(size, 1, 15, 17, 38, 40);
		final State s3 = s1.merge(s2);
		final State s4 = s2.merge(s1);
		final Set<Position> a3 = newHashSet(s3);
		final Set<Position> a4 = newHashSet(s4);
		assertEquals(a4, a3);
		assertEquals(a3, Sets.intersection(newHashSet(s1), newHashSet(s2)));
	}

	/** Merge with empty. */
	@Test
	public void mergeWithEmpty() {
		final Size size = Size.of(7, 8);
		final State state = state(size, 10, 20, 30, 40);
		final State empty = State.empty(size);
		assertSame(state.merge(empty), state);
		assertSame(empty.merge(state), state);
		assertSame(empty.merge(empty), empty);
	}

	/** Invalid merge. */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void invalidMerge() {
		state(Size.of(3, 4), 4, 6).merge(State.empty(Size.of(3, 5)));
	}
}
