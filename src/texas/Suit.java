package texas;

public enum Suit
{	
	DIAMOND('♦'), CLUB('♣'), HEART('♥'), SPADE('♠');
	
	private char symbol;
	
	private Suit(char symbol){
		this.symbol = symbol;
	}
	
	public String toString(){
		return String.valueOf(symbol);
	}
	
}
