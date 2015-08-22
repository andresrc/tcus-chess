# tcus-chess
This repository contains a Java solution to the Chess Challenge as described in [this document](https://docs.google.com/document/d/1xukSjzQdkncmuMRZ-S0LFE7WTdZBX2YmWkQu0LYQu_A/edit).

Some aspects of the solution may seem over-engineered given the size of the problem, but as required in the challenge description, the solution tries to illustrate the use of best practices.
Design considerations:
- The solution is based on depth-first search with backtracking. Some experiments were done using breadth-first but provided no improvement and the "visited-set" supposed a memory burden.
  - In each step the problem is constrained with the positions threatened by the already placed figures. A bitset is used to represent board state.
  - Pieces are ordered by kind, placing first those that would provide greater constraints, in order to identify sooner impossible paths.
  - When two pieces of the same kind are placed paths followed by the first are avoided in the second.
  - When there are less available positions than pieces to place that path is eagerly abandoned.
- The solution is approached using immutable objects in order to simplify analysis of parallelizable solutions. The presented solution distributes the work on the first level of the DFS.
- A symmetry reduction was tried: in square boards, only a quarted of the board is considered for the first placement (in rectangular boards, a half). The rest of solutions are obtained applying rotations. The problem with this approach is that it requires keeping track of solutions to identify duplicates, which (see below) was a big performance hit.

The solution is structured in a package provided a very simple public API (`net.derquinse.tcus.chess.solver`, see interface `Solver`) and a command line application that is a client of this API. The provided solver is parametrized by the number of threads to use in finding the solution.

First versions always kept track of found solutions, in order to be able to show them, but it was a big performance hit for large number of solutions, so it was made optional, providing two methods, one returning the found solutions and the other only the number.

The command line application reads the problem description from the command line arguments, builds a `Problem` object, feeds it to a solver (that uses a number of threads already specified in the command line) and obtains the `Solution`'s. If an output file is requested, the solve method returning all the solutions is used and they are written to an output file.

The solution uses Java 8 and is built using Maven 3. After cloning the repository, change to the `chess` directory and build the solution using:

```
mvn clean package
```

After that you may run the executable:

```
$ java -jar target/chess-1.0.0.jar 
Usage: ChessChallenge [options]
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

For example, the second example in the challenge:

```
$ java -jar target/chess-1.0.0.jar -rows 4 -columns 4 -rooks 2 -knights 4 -threads 2 -output example2.txt
Solving for 0 king(s), 0 queen(s), 0 bishop(s), 2 rook(s) and 4 knight(s) in a 4 row(s) by 4 column(s) board
Found  8 solutions in 120,2 ms using 2 thread(s)
```

For the requested challenge, the command line would be (use the desired number of threads):

```
java -jar target/chess-1.0.0.jar -rows 7 -columns 7 -kings 2 -queens 2 -bishops 2 -knights 1 -threads 1 
```

which would show the number of solutions found and the elapsed time in the standard output.

On a Intel Core i3 (circa 2010) with 2 cores at 3.06 GHz, results are:

```
$ java -jar target/chess-1.0.0.jar -rows 7 -columns 7 -kings 2 -queens 2 -bishops 2 -knights 1 -threads 1
Solving for 2 king(s), 2 queen(s), 2 bishop(s), 0 rook(s) and 1 knight(s) in a 7 row(s) by 7 column(s) board
Found  3063828 solutions in 3,212 s using 1 thread(s)
$ java -jar target/chess-1.0.0.jar -rows 7 -columns 7 -kings 2 -queens 2 -bishops 2 -knights 1 -threads 2
Solving for 2 king(s), 2 queen(s), 2 bishop(s), 0 rook(s) and 1 knight(s) in a 7 row(s) by 7 column(s) board
Found  3063828 solutions in 3,144 s using 2 thread(s)
```

Additional work can be performed in the parallelized version, which now only provides improvement in scenarios requiring significant execution time, such as (results on a Intel Xeon E5-2620 with 4 cores at 2 GHz, virtualized):

```
Solving for 2 king(s), 3 queen(s), 2 bishop(s), 1 rook(s) and 1 knight(s) in a 8 row(s) by 8 column(s) board
Found  12719440 solutions in 41,78 s using 1 thread(s)
Found  12719440 solutions in 30,63 s using 2 thread(s)
Found  12719440 solutions in 29,37 s using 3 thread(s)
Found  12719440 solutions in 27,75 s using 4 thread(s)
```



