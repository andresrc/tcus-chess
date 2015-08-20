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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Default challenge solver.
 * @author Andres Rodriguez
 */
final class DefaultSolver implements Solver {
	/** Executor service. */
	private final ExecutorService executor;

	/** Constructor. */
	DefaultSolver(int numThreads) {
		checkArgument(numThreads > 0, "The number of threads must be at least 1");
		// As we don't provide a way to stop the solver and we wait for the solutions, we use daemon
		// threads.
		this.executor = Executors.newFixedThreadPool(numThreads, new ThreadFactoryBuilder().setDaemon(false).build());
	}

	@Override
	public Set<Solution> solve(Problem problem) {
		checkNotNull(problem, "The problem must be provided");
		// Degenerate cases
		final int n = problem.getPieces().size();
		if (n == 0 || n > problem.getSize().getPositions()) {
			return ImmutableSet.of();
		}
		final Step initial = Step.initial(problem);
		final Search search = new Search();
		search.newTasks(initial);
		search.await();
		return ImmutableSet.copyOf(search.solutions);
	}

	private final class Search {
		/** Solutions so far. */
		private final Set<Solution> solutions = Sets.newConcurrentHashSet();
		/** Completion service. */
		private final CompletionService<Integer> tasks;
		/** Task count. */
		private final AtomicInteger count = new AtomicInteger();

		Search() {
			this.tasks = new ExecutorCompletionService<Integer>(executor);
		}

		/** Generate new tasks based for a subset of available positions in the current step. */
		void newTasks(Step step, Predicate<Integer> filter) {
			step.getAvailable().forEach(i -> {
				if (filter.test(i)) newTask(step, i); 
			});
		}

		/** Generate new tasks based on available positions in the current step. */
		void newTasks(Step step) {
			for (int i : step.getAvailable()) {
				newTask(step, i);
			}
		}

		/** Generate a new task for an available position in the current step. */
		private void newTask(final Step step, final int index) {
			final int taskId = count.incrementAndGet();
			//System.out.printf("Submitted task (%d):%s[%d]\n", taskId, step, index);
			tasks.submit(() -> {
				final Optional<Step> next = step.next(index);
				//System.out.printf("Task [%d]: %s(%d) -> %s\n", taskId, step, index, next);
				if (next.isPresent()) {
					Step s = next.get();
					if (s.isSolution()) {
						solutions.add(s.getSolution());
					} else {
						// If we have the same type of piece we only look forward
						if (step.getNextPiece() == s.getNextPiece()) {
							newTasks(s, i -> i > index);
						} else {
							newTasks(s);
						}
					}
				}
			}, taskId);
		}

		/** Awaits for the solution. */
		@SuppressWarnings("unused")
		void await() {
			try {
				while (count.get() > 0) {
					final Future<Integer> id = tasks.take();
					final int current = count.decrementAndGet();
					// System.out.printf("Taken task [%d], %d left\n", id.get(), current);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

}
