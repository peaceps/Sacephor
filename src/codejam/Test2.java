package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Test2
{
    public static void main( String[] args ) throws Exception
    {
        Map<String, Integer> failed = new HashMap<>();
        Map<String, Set<String>> childs = new HashMap<>();
        Map<String, Integer> depth = new HashMap<>();
        Map<String, String> taskSrc = new HashMap<>();
        Map<String, List<List<String>>> srcPackages = new HashMap<>();
        Map<String, Boolean> resultBuffer = new HashMap<>();

        BufferedReader reader =
            new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/StatOfDistTask.small.1496815744663.input" ) );
        String line = null;
        while( ( line = reader.readLine() ) != null )
        {
            process( line, failed, childs, depth, taskSrc, srcPackages, resultBuffer );
        }
        reader.close();

        resultBuffer.entrySet().forEach(
            entry -> processResult( failed, taskSrc, srcPackages, resultBuffer, entry.getKey(), entry.getValue(), true,
                true ) );
        taskSrc.keySet().forEach(
            timeout -> processResult( failed, taskSrc, srcPackages, resultBuffer, timeout, false, true, false ) );

        failed.entrySet().stream().sorted( ( x, y ) -> depth.get( x.getKey() ) - depth.get( y.getKey() ) ).forEach(
            entry -> {
            if( entry.getValue() > 0 )
            {
                System.out.print( '{' );
                System.out.print( depth.get( entry.getKey() ) );
                System.out.print( ", " );
                System.out.print( entry.getKey() );
                System.out.print( ", " );
                System.out.print( entry.getValue() );
                System.out.println( '}' );
            }
        } );
    }

    private static void process( String line, Map<String, Integer> failed, Map<String, Set<String>> childs,
                                 Map<String, Integer> depth, Map<String, String> taskSrc,
                                 Map<String, List<List<String>>> srcPackages, Map<String, Boolean> resultBuffer )
    {
        if( line.contains( "List" ) )
        {
            processTasks( line, childs, depth, taskSrc, srcPackages );
        }
        else
        {
            processResults( line, failed, taskSrc, srcPackages, resultBuffer );
        }
    }
    
    private static void processTasks( String line, Map<String, Set<String>> childs, Map<String, Integer> depth,
                                      Map<String, String> taskSrc, Map<String, List<List<String>>> srcPackages )
    {
        String src = line.substring( line.indexOf( '{' ) + 1, line.indexOf( ',' ) );
        String target = line.substring( line.indexOf( ' ' ) + 1, line.indexOf( 'L' ) - 2 );
        String tasks = line.substring( line.indexOf( '(' ) + 1, line.indexOf( ')' ) );
        List<String> taskList = Arrays.asList( tasks.split( ", " ));

        if( !childs.containsKey( src ) )
        {
            childs.put( src, new HashSet<>() );
        }
        childs.get( src ).add( target );

        int parentDepth = depth.containsKey( src ) ? depth.get( src ) : 0;
        depth.put( src, parentDepth );
        if( depth.get( target ) == null )
        {
            depth.put( target, parentDepth + 1 );
        }
        else if( parentDepth + 1 != depth.get( target ) )
        {
            resetDepth( childs, depth, target, parentDepth + 1 - depth.get( target ) );
        }

        taskList.forEach( task -> taskSrc.put( task, src ) );
        if( !srcPackages.containsKey( src ) )
        {
            srcPackages.put( src, new ArrayList<>() );
        }
        srcPackages.get( src ).add( taskList );
    }

    private static void resetDepth( Map<String, Set<String>> childs, Map<String, Integer> depth, String root,
                                     int offset )
    {
        depth.put( root, depth.get( root ) + offset );
        if( childs.get( root ) != null )
        {
            for( String child : childs.get( root ) )
            {
                resetDepth( childs, depth, child, offset );
            }
        }
    }

    private static void processResults( String line, Map<String, Integer> failed, Map<String, String> taskSrc,
                                        Map<String, List<List<String>>> srcPackages, Map<String, Boolean> resultBuffer )
    {
        String result = line.substring( line.indexOf( ' ' ) + 1, line.lastIndexOf( ',' ) );
        processResult( failed, taskSrc, srcPackages, resultBuffer, result, line.contains( "uccess" ), false, true );
    }

    private static void processResult( Map<String, Integer> failed, Map<String, String> taskSrc,
                                       Map<String, List<List<String>>> srcPackages, Map<String, Boolean> resultBuffer,
                                       String result, boolean success, boolean readover, boolean hasResult )
    {
        String src = taskSrc.get( result );
        if( src == null && !readover )
        {
            resultBuffer.put( result, success );
            return;
        }

        if( hasResult )
        {
            taskSrc.remove( result );
        }

        if( !success )
        {
            List<String> owner = null;
            for( List<String> p : srcPackages.get( src ) )
            {
                if( p.contains( result ) )
                {
                    owner = p;
                    break;
                }
            }
            if( owner == null )
            {
                return;
            }
            srcPackages.get( src ).remove( owner );

            if( !failed.containsKey( src ) )
            {
                failed.put( src, 1 );
            }
            else
            {
                failed.put( src, failed.get( src ) + 1 );
            }
        }
    }
}


