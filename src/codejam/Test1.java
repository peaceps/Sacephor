package codejam;

import java.io.BufferedReader;
import java.io.FileReader;

public class Test1
{
    public static void main( String[] args ) throws Exception
    {

        BufferedReader reader =
            new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/Thrump.large.1496811055229.input.txt" ) );
        reader.readLine();
        String line = null;
        int casecount = 0;
        while( ( line = reader.readLine() ) != null )
        {
            casecount++;
            String[] elements = line.split( " " );
            int maxThreshold = Integer.parseInt( elements[0] );
            String friends = elements[1];

            naviRecruit( casecount, maxThreshold, friends );
        }
        reader.close();
    }

    private static void naviRecruit( int casecount, int maxThreshold, String friends )
    {
        int navi = 0;
        int total = 0;
        for( int i = 0; i < friends.length() && total < maxThreshold; i++ )
        {
            total += friends.charAt( i ) - 0x30;
            if( total < i + 1 )
            {
                int l = i + 1 - total;
                navi += l;
                total += l;
            }
        }
        System.out.println( "Case #" + casecount + ": " + navi );
    }
}
