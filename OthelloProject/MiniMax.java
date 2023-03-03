public class MiniMax {

    public Position minimaxSearch(GameState s) {
        UtilityMove result = maxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return result.move;
    }

    private UtilityMove maxValue(GameState s, int alpha, int beta) {
        // Max wants to get as high utility for the player as possible
        if (isTerminal(s)) {
            return new UtilityMove(s, null);
        }
        int bestUtility = Integer.MIN_VALUE;
        Position bestMove = null;
        for (Position a : s.legalMoves()) {
            // find move that results in highest utility
            UtilityMove m = minValue(result(s, a), alpha, beta);
            if (m.utility > bestUtility) {
                bestUtility = m.utility; // override best utility
                bestMove = a; // override best move
                alpha = Math.max(alpha, bestUtility);
            }
            if (bestUtility >= beta && bestMove != null)
                return new UtilityMove(bestUtility, bestMove);

        }
        // return best utility and move
        return new UtilityMove(bestUtility, bestMove);
    }

    private UtilityMove minValue(GameState s, int alpha, int beta) {
        // Min wants to get as low utility for the player (Max) as possible
        // Low score for Max means higher probability for Min to win
        if (isTerminal(s)) {
            return new UtilityMove(s, null);
        }
        int bestUtility = Integer.MAX_VALUE;
        Position bestMove = null;
        for (Position a : s.legalMoves()) {
            // find move that results in lowest utility
            UtilityMove m = maxValue(result(s, a), alpha, beta);
            if (m.utility < bestUtility) {
                bestUtility = m.utility; // override best utility
                bestMove = a; // override best move
                beta = Math.min(beta, bestUtility);
            }
            if (bestUtility <= alpha && bestMove != null)
                return new UtilityMove(bestUtility, bestMove);
        }

        // return best utility and move
        return new UtilityMove(bestUtility, bestMove);
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
        public final int utility;
        public final Position move;

        public UtilityMove(int utility, Position move) {
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
        private int utility(GameState state) {
            // most tokens on board wins
            int[] tokens = state.countTokens();
            if (tokens[1] > tokens[0])
                return 1; // white wins
            if (tokens[1] < tokens[0])
                return -1; // black wins
            return 0; // draw
        }
    }
}
