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

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
		search.newTask(initial);
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

		/** Generate a new task to process a non-final step. */
		void newTask(final Step step) {
			final Task task = new Task(step);
			//System.out.printf("Submitted task (%d) : %s\n", task.id, step);
			tasks.submit(task);
		}

		/** Awaits for the solution. */
		void await() {
			int taken = 0;
			try {
				while (taken < count.get()) {
					@SuppressWarnings("unused")
					final Future<Integer> id = tasks.take();
					taken++;
					//System.out.printf("Completed task (%d)\n", id.get());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private final class Task implements Callable<Integer> {
			/** Task id. */
			private final int id = count.incrementAndGet();
			/** Step to process. */
			private final Step step;

			Task(Step step) {
				this.step = step;
			}

			@Override
			public Integer call() throws Exception {
				for (Step next : step.nextSteps()) {
					if (next.isSolution()) {
						solutions.add(next.getSolution());
					} else {
						newTask(next);
					}
				}
				return id;
			}
		}

	}

}
