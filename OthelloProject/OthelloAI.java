
public class OthelloAI implements IOthelloAI {

	public Position decideMove(GameState s) {
		MiniMax search = new MiniMax();
		Position result = search.minimaxSearch(s);
		return result;
	}
	
}
