package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class Test1
{
    public static void main( String[] args ) throws Exception
    {
    	String folder = "C:/Users/Sacephor/Desktop/";
    	FileWriter writer = new FileWriter(folder + "Test1Output.txt");
        BufferedReader reader =  new BufferedReader( new FileReader( folder+"1.txt" ) );
        reader.readLine();
        String line = null;
        int casecount = 0;
        String result = "";
        while( ( line = reader.readLine() ) != null )
        {
            casecount++;
            String[] elements = line.split( " " );
            int maxThreshold = Integer.parseInt( elements[0] );
            String friends = elements[1];

            result +=naviRecruit( casecount, maxThreshold, friends )+"\n";
        }
        reader.close();        
        writer.write(result.substring(0, result.length()-1));
        writer.close();
    }

    private static String naviRecruit( int casecount, int maxThreshold, String friends )
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
        return "Case #" + casecount + ": " + navi ;
    }
}
