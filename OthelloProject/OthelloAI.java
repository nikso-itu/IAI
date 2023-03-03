
public class OthelloAI implements IOthelloAI {

	public Position decideMove(GameState s) {
		MiniMax search = new MiniMax(s);
		Position result = search.minimaxSearch();
		return result;
	}
	
}
