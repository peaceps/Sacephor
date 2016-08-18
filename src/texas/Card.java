package texas;

public class Card
{
	private Suit suit;	
	private int point;
	
	public Card(Suit suit, int point){
		this.suit = suit;
		this.point = point;
	}
	
	public String toString(){
		char p;
		switch (point)
		{
			case 11:
				p = 'J';
				break;
			case 12:
				p = 'Q';
				break;
			case 13:
				p = 'K';
				break;
			case 14:
				p = 'A';
				break;
			default:
				p = (char)(0x30+point);
		}
		return p+suit.toString();
	}
}
