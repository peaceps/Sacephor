package codejam;

import java.io.BufferedReader;
import java.io.FileReader;

public class Test3
{
    public static void main( String[] args ) throws Exception
    {
        BufferedReader reader =
            new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/Chain.large.1496906878699.input.txt" ) );
        reader.readLine();
        String line = null;
        int count = 0;
        while( ( line = reader.readLine() ) != null )
        {
            String[] data = line.split( " " );
            papapa( Integer.parseInt( data[0] ), Integer.parseInt( data[1] ), ++count );
        }
        reader.close();
    }

    private static void papapa( int n, int k, int count )
    {
        System.out.println( "Case #" + count + ": " + ( ( k + 1 ) % ( 1 << n ) == 0 ? "ON" : "OFF" ) );
    }
}
