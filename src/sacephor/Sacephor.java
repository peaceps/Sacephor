package sacephor;

public class Sacephor
{
	public static void main(String[] args) throws Exception
	{
		new Test();
	}
}

class TestBase{
	protected static int kkkk = 1111;
	static{System.out.println(kkkk);}
	
	protected int ab = 2;

	{
		System.out.println(1);
	}
	public TestBase(){
		System.out.println("TestBase con"+getD());
	}
   protected int getD(){
	   return ab;
   }
   
	{
		System.out.println(ab);
	}
}

class Test extends TestBase{
	protected static int kkkk = 2222;
	static{System.out.println(kkkk);}
	private  int a = 8;

	
	public Test(){
		System.out.println("TestBase con"+getD());
	}
	
   protected  int getD(){
	   return a;
   }
	
	{
		System.out.println(a);
	}
}