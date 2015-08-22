# tcus-chess
This repository contains a Java solution to the Chess Challenge as described in [this document](https://docs.google.com/document/d/1xukSjzQdkncmuMRZ-S0LFE7WTdZBX2YmWkQu0LYQu_A/edit).

Some aspects of the solution may seem over-engineered given the size of the problem, but as required in the challenge description, the solution tries to illustrate the use of best practices.
Design considerations:
- The solution is based on depth-first search. Some experiments were done using breadth-first but provided no improvement and the "visited-set" supposed a memory burden.
- Some optimizations are applied to the basic DFS:
  - In each step the problem is constrained with the positions threatened by the already places figures. A bitset is used to represent board state.
  - Pieces are ordered by kind, placing first those that would provide greater constraints, in order to identify sooner impossible paths.
  - When two pieces of the same kind are placed paths followed by the first are avoided in the second.
  - Symmetry reduction: in square boards, only a quarted of the board is considered for the first placement (in rectangular boards, a half). the rest of solutions are obtained applying rotations.
  - When there are less available positions than pieces to place tha path is eagerly abandoned.
- The solution is approached using immutable objects in order to simplify analysis of parallelizable solutions. The presented solution distributes the work on the first level of the DFS. For a 7x7 or 8x8 board the search is distributed in 16 tasks.

The solution is structured in a package provided a very simple public API (`net.derquinse.tcus.chess.solver`, see interface `Solver`) and a command line application that is a client of this API. The provided solver is parametrized by the number of threads to use in finding the solution.

The command line application reads the problem description from the command line arguments, builds a `Problem` object, feeds it to a solver (that uses a number of threads already specified in the command line) and obtains the `Solution`'s which may be written to an output file.

The solution uses Java 8 and is built using Maven 3. After cloning the repository, change to the `chess` directory and build the solution using:

```
mvn clean package
```

After that you may run the executable:

```
$ java -jar target/chess-1.0.0.jar 
Usage: <main class> [options]
  Options:
    -bishops
       Number of bishops
       Default: 0
    -columns
       Number of columns
       Default: 8
    -kings
       Number of kings
       Default: 0
    -knights
       Number of knights
       Default: 0
    -output
       Output file (solution boards)
    -queens
       Number of queens
       Default: 0
    -rooks
       Number of rooks
       Default: 0
    -rows
       Number of rows
       Default: 8
    -threads
       Number of threads to use
       Default: 1
```

For the requested challenge, the command line would be:

```
java -jar target/chess-1.0.0.jar -rows 7 -columns 7 -kings 2 -queens 2 -bishops 2 -knights 1 -threads 1 -output challenge.txt
```

Which would show the number of solutions found and the elapsed time in the standard output and write the solutions to the `challenge.txt` file.

On a Intel Core i3 (circa 2010) with 2 cores at 3.06 GHz, results are:

On a Intel Xeon E5-2620 with 4 cores at 2 GHz (virtualized), results are:




