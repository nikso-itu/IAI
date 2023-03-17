
public class OthelloAI implements IOthelloAI {

	public Position decideMove(GameState s) {
		Minimax search = new Minimax();
		Position result = search.minimaxSearch(s);
		System.out.println(result);
		return result;
	}
	
}
