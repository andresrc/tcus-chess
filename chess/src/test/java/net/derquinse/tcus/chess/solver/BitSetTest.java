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

import java.util.BitSet;

import org.testng.annotations.Test;

/**
 * Tests to check expected behaviour of BitSet class.
 * @author Andres Rodriguez
 */
public final class BitSetTest {
	/** Checks length calcularion and that clear bits can be searched at any position. */
	@Test
	public void checkBitSet() {
		BitSet set = new BitSet();
		set.set(73);
		final int s1 = set.size();
		assertEquals(set.cardinality(), 1);
		assertEquals(set.length(), 74);
		assertEquals(set.nextSetBit(3), 73);
		assertEquals(set.nextClearBit(0), 0);
		assertEquals(set.nextClearBit(10), 10);
		assertEquals(set.nextClearBit(73), 74);
		assertEquals(set.nextClearBit(315), 315);
		assertEquals(set.size(), s1);
	}

}
