package codejam;

import java.io.BufferedReader;
import java.io.FileReader;

public class Test3
{
    public static void main( String[] args ) throws Exception
    {
        BufferedReader reader =
            new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/Chain.small.1496906258140.input.txt" ) );
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
        boolean[] elec = new boolean[ n ];
        elec[0] = true;
        boolean[] on = new boolean[ n ];
        for( int i = 0; i < k; i++ )
        {
            pa( elec, on );
        }
        System.out.println( "Case #" + count + ": " + ( elec[n - 1] && on[n - 1] ? "ON" : "OFF" ) );
    }

    private static void pa( boolean[] elec, boolean[] on )
    {
        on[0] = !on[0];
        for( int i = 1; i < elec.length; i++ )
        {
            on[i] = elec[i] ? !on[i] : on[i];
            elec[i] = elec[i - 1] & on[i - 1];
        }
    }
}
