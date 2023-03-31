import java.util.ArrayList;
import java.util.Arrays;

public class SudokuSolver implements ISudokuSolver {

	int[][] puzzle;
	int size;
	ArrayList<ArrayList<Integer>> D; // = new ArrayList<ArrayList<Integer>>();

	public int[][] getPuzzle() {
		return puzzle;
	}

	public void setValue(int col, int row, int value) {
		puzzle[col][row] = value;
	}

	public void setup(int size1) {
		size = size1;
		puzzle = new int[size * size][size * size];
		D = new ArrayList<ArrayList<Integer>>(size * size * size * size);

		// Initialize each D[X] (domain value)
		for (int i = 0; i < size * size * size * size; i++) {
			ArrayList<Integer> d = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
			D.add(d);
		}
	}

	public boolean solve() {
		// update domains in order to ensure consistency with initial assignment
		ArrayList<Integer> asn = GetAssignment(puzzle);

		// INITIAL_FC
		INITIAL_FC(asn);

		// FC
		ArrayList<Integer> newAsn = FC(asn); // get solution
		if (newAsn == null)
			return false;

		// Update puzzle with solution
		puzzle = GetPuzzle(newAsn);

		return true;
	}

	public void readInPuzzle(int[][] p) {
		puzzle = p;
	}

	// ---------------------------------------------------------------------------------
	// YOUR TASK: Implement FC(asn)
	// ---------------------------------------------------------------------------------
	public ArrayList<Integer> FC(ArrayList<Integer> asn) {
		return RecursiveFC(asn);
	}

	public ArrayList<Integer> RecursiveFC(ArrayList<Integer> asn) {
		// check whether all variables have values different from 0
		boolean isComplete = asn.stream().allMatch(i -> i != 0);
		System.out.println(asn.stream().filter(i -> i == 0).count());

		// if all value are non-zero, we have a complete assignment
		if (isComplete)
			return asn;

		// find first unassigned variable
		int unassigned = -1;
		for (int i = 0; i < asn.size(); i++) {
			if (asn.get(i) == 0) {
				unassigned = i;
				break;
			}
		}

		// save old domain
		ArrayList<ArrayList<Integer>> oldD = deepCopy(D);

		// try value from domain, and rollback otherwise
		for (Integer domainValue : D.get(unassigned)) {
			if (AC_FC(unassigned, domainValue)) {
				// assign the unassigned variable
				asn.set(unassigned, domainValue);

				// recursively forward-chain the new assignment
				ArrayList<Integer> R = RecursiveFC(asn);

				// if the new assignment doesn't fail, we keep the choice.
				if (R != null) {
					return R;
				}

				// the new assignment failed, therefore we unassign the variable again
				asn.set(unassigned, 0);

				// rollback
				D = oldD;
			} else {
				// rollback
				D = oldD;
			}
		}
		return null;// failure
	}

	// create copy of nested arrays
	private ArrayList<ArrayList<Integer>> deepCopy(ArrayList<ArrayList<Integer>> toCopy) {
		ArrayList<ArrayList<Integer>> copy = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < toCopy.size(); i++) {
			ArrayList<Integer> entry = new ArrayList<>();
			for (int j = 0; j < toCopy.get(i).size(); j++) {
				entry.add(toCopy.get(i).get(j));
			}
			copy.add(entry);
		}
		return copy;
	}

	// ---------------------------------------------------------------------------------
	// CODE SUPPORT FOR IMPLEMENTING FC(asn)
	//
	// It is possible to implement FC(asn) by using only AC_FC function from below.
	//
	// If you have time, I strongly reccomend that you implement AC_FC and REVISE
	// from scratch
	// using only implementation of CONSISTENT algorithm and general utility
	// functions. In my opinion
	// by doing this, you will gain much more from this exercise.
	//
	// ---------------------------------------------------------------------------------

	// ------------------------------------------------------------------
	// AC_FC
	//
	// Implementation of acr-consistency for forward-checking AC-FC(cv).
	// This is a key component of FC algorithm, and the only function you need to
	// use in your FC(asn) implementation
	// ------------------------------------------------------------------
	public boolean AC_FC(Integer X, Integer V) {
		// Reduce domain Dx
		D.get(X).clear();
		D.get(X).add(V);

		// Put in Q all relevant Y where Y>X
		ArrayList<Integer> Q = new ArrayList<Integer>(); // list of all relevant Y
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;

		// all variables in the same column
		for (int i = 0; i < size * size; i++) {
			if (GetVariable(i, col) > X) {
				Q.add(GetVariable(i, col));
			}
		}
		// all variables in the same row
		for (int j = 0; j < size * size; j++) {
			if (GetVariable(row, j) > X) {
				Q.add(GetVariable(row, j));
			}
		}
		// all variables in the same size*size box
		for (int i = cell_x * size; i <= cell_x * size + 2; i++) {
			for (int j = cell_y * size; j <= cell_y * size + 2; j++) {
				if (GetVariable(i, j) > X) {
					Q.add(GetVariable(i, j));
				}
			}
		}

		// REVISE(Y,X)
		boolean consistent = true;
		while (!Q.isEmpty() && consistent) {
			Integer Y = (Integer) Q.remove(0);
			if (REVISE(Y, X)) {
				consistent = !D.get(Y).isEmpty();
			}
		}
		return consistent;
	}

	// ------------------------------------------------------------------
	// REVISE
	// ------------------------------------------------------------------
	public boolean REVISE(int Xi, int Xj) {
		Integer zero = 0;

		assert (Xi >= 0 && Xj >= 0);
		assert (Xi < size * size * size * size && Xj < size * size * size * size);
		assert (Xi != Xj);

		boolean DELETED = false;

		ArrayList<Integer> Di = D.get(Xi);
		ArrayList<Integer> Dj = D.get(Xj);

		for (int i = 0; i < Di.size(); i++) {
			Integer vi = (Integer) Di.get(i);
			ArrayList<Integer> xiEqVal = new ArrayList<Integer>(size * size * size * size);
			for (int var = 0; var < size * size * size * size; var++) {
				xiEqVal.add(var, zero);
			}

			xiEqVal.set(Xi, vi);

			boolean hasSupport = false;
			for (int j = 0; j < Dj.size(); j++) {
				Integer vj = (Integer) Dj.get(j);
				if (CONSISTENT(xiEqVal, Xj, vj)) {
					hasSupport = true;
					break;
				}
			}

			if (hasSupport == false) {
				Di.remove((Integer) vi);
				DELETED = true;
			}

		}

		return DELETED;
	}

	// ------------------------------------------------------------------
	// CONSISTENT:
	//
	// Given a partiall assignment "asn" checks whether its extension with
	// variable = val is consistent with Sudoku rules, i.e. whether it violates
	// any of constraints whose all variables in the scope have been assigned.
	// This implicitly encodes all constraints describing Sudoku.
	//
	// Before it returns, it undoes the temporary assignment variable=val
	// It can be used as a building block for REVISE and AC-FC
	//
	// NOTE: the procedure assumes that all assigned values are in the range
	// {0,..,9}.
	// -------------------------------------------------------------------
	public boolean CONSISTENT(ArrayList<Integer> asn, Integer variable, Integer val) {
		Integer v1, v2;

		// variable to be assigned must be clear
		assert (asn.get(variable) == 0);
		asn.set(variable, val);

		// alldiff(col[i])
		for (int i = 0; i < size * size; i++) {
			for (int j = 0; j < size * size; j++) {
				for (int k = 0; k < size * size; k++) {
					if (k != j) {
						v1 = (Integer) asn.get(GetVariable(i, j));
						v2 = (Integer) asn.get(GetVariable(i, k));
						if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
							asn.set(variable, 0);
							return false;
						}
					}
				}
			}
		}

		// alldiff(row[j])
		for (int j = 0; j < size * size; j++) {
			for (int i = 0; i < size * size; i++) {
				for (int k = 0; k < size * size; k++) {
					if (k != i) {
						v1 = (Integer) asn.get(GetVariable(i, j));
						v2 = (Integer) asn.get(GetVariable(k, j));
						if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
							asn.set(variable, 0);
							return false;
						}
					}
				}
			}
		}

		// alldiff(block[size*i,size*j])
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int i1 = 0; i1 < size; i1++) {
					for (int j1 = 0; j1 < size; j1++) {
						int var1 = GetVariable(size * i + i1, size * j + j1);
						for (int i2 = 0; i2 < size; i2++) {
							for (int j2 = 0; j2 < size; j2++) {
								int var2 = GetVariable(size * i + i2, size * j + j2);
								if (var1 != var2) {
									v1 = (Integer) asn.get(var1);
									v2 = (Integer) asn.get(var2);
									if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
										asn.set(variable, 0);
										return false;
									}
								}
							}
						}

					}
				}
			}
		}

		asn.set(variable, 0);
		return true;
	}

	// ------------------------------------------------------------------
	// INITIAL_FC
	// ------------------------------------------------------------------
	public boolean INITIAL_FC(ArrayList<Integer> anAssignment) {
		// Enforces consistency between unassigned variables and all
		// initially assigned values;
		for (int i = 0; i < anAssignment.size(); i++) {
			Integer V = (Integer) anAssignment.get(i);
			if (V != 0) {
				ArrayList<Integer> Q = GetRelevantVariables(i);
				boolean consistent = true;
				while (!Q.isEmpty() && consistent) {
					Integer Y = (Integer) Q.remove(0);
					if (REVISE(Y, i)) {
						consistent = !D.get(Y).isEmpty();
					}
				}
				if (!consistent)
					return false;
			}
		}

		return true;
	}

	// ------------------------------------------------------------------
	// GetRelevantVariables
	// ------------------------------------------------------------------
	public ArrayList<Integer> GetRelevantVariables(Integer X) {
		// Returns all variables that are interdependent of X, i.e.
		// all variables involved in a binary constraint with X
		ArrayList<Integer> Q = new ArrayList<Integer>(); // list of all relevant Y
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;

		// all variables in the same column
		for (int i = 0; i < size * size; i++) {
			if (GetVariable(i, col) != X) {
				Q.add(GetVariable(i, col));
			}
		}
		// all variables in the same row
		for (int j = 0; j < size * size; j++) {
			if (GetVariable(row, j) != X) {
				Q.add(GetVariable(row, j));
			}
		}
		// all variables in the same size*size cell
		for (int i = cell_x * size; i <= cell_x * size + 2; i++) {
			for (int j = cell_y * size; j <= cell_y * size + 2; j++) {
				if (GetVariable(i, j) != X) {
					Q.add(GetVariable(i, j));
				}
			}
		}

		return Q;
	}

	// ------------------------------------------------------------------
	// Functions translating between the puzzle and an assignment
	// -------------------------------------------------------------------
	public ArrayList<Integer> GetAssignment(int[][] p) {
		ArrayList<Integer> asn = new ArrayList<Integer>();
		for (int i = 0; i < size * size; i++) {
			for (int j = 0; j < size * size; j++) {
				asn.add(GetVariable(i, j), p[i][j]);
				if (p[i][j] != 0) {
					// restrict domain
					D.get(GetVariable(i, j)).clear();
					D.get(GetVariable(i, j)).add(p[i][j]);
				}
			}
		}
		return asn;
	}

	public int[][] GetPuzzle(ArrayList<Integer> asn) {
		int[][] p = new int[size * size][size * size];
		for (int i = 0; i < size * size; i++) {
			for (int j = 0; j < size * size; j++) {
				Integer val = (Integer) asn.get(GetVariable(i, j));
				p[i][j] = val.intValue();
			}
		}
		return p;
	}

	// ------------------------------------------------------------------
	// Utility functions
	// -------------------------------------------------------------------
	public int GetVariable(int i, int j) {
		assert (i < size * size && j < size * size);
		assert (i >= 0 && j >= 0);
		return (i * size * size + j);
	}

	public int GetRow(int X) {
		return (X / (size * size));
	}

	public int GetColumn(int X) {
		return X - ((X / (size * size)) * size * size);
	}

}
