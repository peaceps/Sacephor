package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class Test3
{
    public static void main( String[] args ) throws Exception
    {
    	String folder = "C:/Users/Sacephor/Desktop/";
    	FileWriter writer = new FileWriter(folder + "Test3Output.txt");
        BufferedReader reader =  new BufferedReader( new FileReader( folder+"1.txt" ) );
        reader.readLine();
        String line = null;
        String result="";
        int count = 0;
        while( ( line = reader.readLine() ) != null )
        {
            String[] data = line.split( " " );
            result +=papapa( Integer.parseInt( data[0] ), Integer.parseInt( data[1] ), ++count )+"\n";
        }
        reader.close();
        writer.write(result.substring(0,result.length()-1));
        writer.close();
    }

    private static String papapa( int n, int k, int count )
    {
        return "Case #" + count + ": " + ( ( k + 1 ) % ( 1 << n ) == 0 ? "ON" : "OFF" ) ;
    }
}
