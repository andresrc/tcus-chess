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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
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
		this.executor = Executors.newFixedThreadPool(numThreads, new ThreadFactoryBuilder().setDaemon(true).build());
	}

	@Override
	public List<Solution> solveAndGet(Problem problem) {
		final Search s = solve(problem, true);
		if (s == null) {
			return ImmutableList.of();
		}
		return s.getSolutions();
	}

	@Override
	public int solve(Problem problem) {
		final Search s = solve(problem, false);
		if (s == null) {
			return 0;
		}
		return s.getCount();
	}

	private Search solve(Problem problem, boolean save) {
		checkNotNull(problem, "The problem must be provided");
		// Degenerate cases
		final int n = problem.getPieces().size();
		if (n == 0 || n > problem.getSize().getPositions()) {
			return null;
		}
		final Step initial = Step.initial(problem);
		final Search search = new Search(save);
		search.newTask(initial);
		search.await();
		return search;
	}

	private final class Search {
		/** Solution count. */
		private final GlobalCounter counter = new GlobalCounter();
		/** Solution aggregator. */
		private final GlobalAggregator solutions;
		/** Completion service. */
		private final CompletionService<Integer> tasks;
		/** Task count. */
		private final AtomicInteger count = new AtomicInteger(0);

		Search(boolean save) {
			this.tasks = new ExecutorCompletionService<Integer>(executor);
			this.solutions = save ? new GlobalAggregator() : null;
		}

		/** Generate a new task to process a non-final step. */
		void newTask(final Step step) {
			final Task task = new Task(step);
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
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/** Returns the solution count. */
		int getCount() {
			return counter.getCount();
		}

		/** Returns the solution through a method that safely publishes the mutable list. */
		List<Solution> getSolutions() {
			return solutions != null ? solutions.getSolutions() : ImmutableList.of();
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
				for (Step next : step.nextSteps(counter, solutions)) {
					newTask(next);
				}
				return id;
			}
		}
	}

	/** Global solution count consumer. */
	private static final class GlobalCounter implements SolutionCounter {
		private final AtomicInteger count = new AtomicInteger(0);

		@Override
		public void accept(Integer t) {
			count.addAndGet(t);
		}

		@Override
		public int getCount() {
			return count.get();
		}
	}

	/** Global solution aggregator. */
	private static final class GlobalAggregator implements SolutionAggregator {
		/** Solutions so far. */
		private final List<Solution> solutions = Lists.newLinkedList();
		/** Solutions list lock. */
		private final Lock lock = new ReentrantLock();

		@Override
		public void accept(List<Solution> list) {
			if (list != null && !list.isEmpty()) {
				lock.lock();
				try {
					solutions.addAll(list);
				} finally {
					lock.unlock();
				}
			}
		}

		/** Returns the solution list, safely published through the same lock. */
		@Override
		public List<Solution> getSolutions() {
			lock.lock();
			try {
				return solutions;
			} finally {
				lock.unlock();
			}
		}

	}

}
