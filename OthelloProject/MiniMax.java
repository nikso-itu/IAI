public class MiniMax {
    
    private final int DEPTH_LIMIT = 10;
    private double[][] positionUtility;
    private int BOARD_SIZE;
    
    private final double DANGER_UTILITY = 0;
    private final double CORNER_UTILITY = 2;
    private final double EDGE_UTILITY = 1;
    private final double DEFAULT_UTILITY = 0;
    
    public Position minimaxSearch(GameState s) {
        computePositionUtility(s);
        UtilityMove result = maxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        if(result.move == null) return s.legalMoves().get(0);
        return result.move;
    }

    private void computePositionUtility(GameState s) {
        BOARD_SIZE = s.getBoard().length;
        positionUtility = new double[BOARD_SIZE][BOARD_SIZE];

        // Default
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                positionUtility[i][j] = DEFAULT_UTILITY;
            }
        }

        // Edges
        for (int i = 0; i < BOARD_SIZE; i++) {
            positionUtility[0][i] = EDGE_UTILITY;
            positionUtility[BOARD_SIZE-1][i] = EDGE_UTILITY;
            positionUtility[i][BOARD_SIZE-1] = EDGE_UTILITY;
            positionUtility[i][0] = EDGE_UTILITY;
        }

        // Corners (override corners after edges)
        positionUtility[0][0] = CORNER_UTILITY;
        positionUtility[BOARD_SIZE-1][0] = CORNER_UTILITY;
        positionUtility[0][BOARD_SIZE-1] = CORNER_UTILITY;
        positionUtility[BOARD_SIZE-1][BOARD_SIZE-1] = CORNER_UTILITY;

        // Danger zone
        for (int i = 1; i < BOARD_SIZE - 1; i++) {
            positionUtility[1][i] = DANGER_UTILITY;
            positionUtility[BOARD_SIZE-2][i] = DANGER_UTILITY;
            positionUtility[i][BOARD_SIZE-2] = DANGER_UTILITY;
            positionUtility[i][1] = DANGER_UTILITY;
        }
    }

    private UtilityMove maxValue(GameState s, double alpha, double beta, int depth) {
        // Max wants to get as high utility for the player as possible
        if (isCutOff(s, depth)) {
            // stop search as we have reached cut off
            return new UtilityMove(s, null);
        }
        double bestUtility = Integer.MIN_VALUE;
        Position bestMove = null;
        for (Position a : s.legalMoves()) {
            // find move that results in highest utility
            UtilityMove m = minValue(result(s, a), alpha, beta, depth + 1);
            if (m.utility > bestUtility) {
                bestUtility = m.utility; // override best utility
                bestMove = a; // override best move
                alpha = Math.max(alpha, bestUtility);
            }
            if (bestUtility >= beta)
                return new UtilityMove(bestUtility, bestMove);
        }

        // return best utility and move
        return new UtilityMove(bestUtility, bestMove);
    }

    private UtilityMove minValue(GameState s, double alpha, double beta, int depth) {
        // Min wants to get as low utility for the player (Max) as possible
        // Low score for Max means higher probability for Min to win
        if (isCutOff(s, depth)) {
            // stop search as we have reached cut off
            return new UtilityMove(s, null);
        }
        double bestUtility = Integer.MAX_VALUE;
        Position bestMove = null;
        for (Position a : s.legalMoves()) {
            // find move that results in lowest utility
            UtilityMove m = maxValue(result(s, a), alpha, beta, depth + 1);
            if (m.utility < bestUtility) {
                bestUtility = m.utility; // override best utility
                bestMove = a; // override best move
                beta = Math.min(beta, bestUtility);
            }
            if (bestUtility <= alpha)
                return new UtilityMove(bestUtility, bestMove);
        }
        // return best utility and move
        return new UtilityMove(bestUtility, bestMove);
    }

    private boolean isCutOff(GameState s, int depth) {
        // use depth limit to terminate search
        return (isTerminal(s) || depth >= DEPTH_LIMIT);
    }

    private boolean isTerminal(GameState s) {
        // determine whether given state is final state
        return s.isFinished();
    }

    private GameState result(GameState s, Position a) {
        // return state that results from applying action `a` in state `s`
        GameState newState = new GameState(s.getBoard(), s.getPlayerInTurn());
        // make copy of game state, and simulate move in the new state
        newState.insertToken(a);
        return newState;
    }

    class UtilityMove {
        public final double utility;
        public final Position move;

        public UtilityMove(double utility, Position move) {
            this.utility = utility;
            this.move = move;
        }

        public UtilityMove(GameState state, Position move) {
            this.utility = utility(state);
            this.move = move;
        }

        /**
         * determine outcome of game state
         */
        private double utility(GameState state) {
            double accUtility = 0.0;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    // for each position, count up the utility if the token is placed by the current player
                    if (state.getBoard()[i][j] == state.getPlayerInTurn()) {
                        // get utility from our utility array
                        accUtility += positionUtility[i][j];
                    }
                }
            }
            return accUtility;
        }
    }
}
