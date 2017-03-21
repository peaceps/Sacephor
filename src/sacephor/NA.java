/*
 *  Copyright (c) 2017 Nokia. All rights reserved.
 *
 *  Revision History:
 *
 *  DATE/AUTHOR          COMMENT
 *  ---------------------------------------------------------------------
 *  2017.03.20/xinfu                            
 */
package sacephor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Worm analyzer
 * 
 * @author <a HREF="mailto:xin.1.fu@nokia.com">Fu Xin</a>
 */
public class NA
{

    public static void main( String[] args ) throws Exception
    {
        //t1-220.txt
        //t2-370.txt
        //t3-460.txt
        //t4-stable-270.txt
        List<Worm> worms = getWormsFromJStack( "t3-460.txt" );

        System.out.println( groupCount( worms, worm -> worm.getSbTrace() ) );
        //printWormsBasic( worms );
//        printWorms( getWormsFromHSErr( "hs_err_pid10760.log" ) );
    }

    private static void printWormsBasic( List<Worm> worms )
    {
        printMap( groupCount( worms, worm -> worm.getNameGroup() ), NA::needPrint );
        printMap( groupCount( worms, worm -> worm.getState() ) );

        Map<String, Map<String, Long>> m =
            worms.stream().filter( worm -> worm.getState() != null ).map( worm -> worm.getState() ).distinct().collect(
                Collectors.toMap( state -> state,
                    state -> groupCount( worms, worm -> state.equals( worm.getState() ),
                        worm -> worm.getNameGroup() ) ) );

        System.out.println();
        m.entrySet().forEach( entry -> {
            System.out.println( entry.getKey() + ":" );
            printMap( entry.getValue(), NA::needPrint );
            System.out.println();
        } );
    }

    private static boolean needPrint( Map.Entry<String, Long> entry )
    {
        return entry.getValue() > 1 || !entry.getKey().startsWith( "pool" );
    }

    private static <K, V> void printMap( Map<K, V> map )
    {
        printMap( map, entry -> true );
    }

    private static <K, V> void printMap( Map<K, V> map, Predicate<Map.Entry<K, V>> filter )
    {
        map.entrySet().stream().filter( filter ).forEach(
            entry -> System.out.println( entry.getKey() + "->" + entry.getValue() ) );
    }

    private static <T, S> Map<T, Long> groupCount( List<S> src, Function<S, T> keyMapper )
    {
        return groupCount( src, t -> true, keyMapper );
    }

    private static <T, S> Map<T, Long> groupCount( List<S> src, Predicate<S> filter, Function<S, T> keyMapper )
    {
        return src.stream().filter( s -> keyMapper.apply( s ) != null && filter.test( s ) ).collect(
            Collectors.groupingBy( keyMapper, Collectors.counting() ) );
    }

    private static LinkedList<Worm> getWormsFromJStack( String file ) throws Exception
    {
        LinkedList<Worm> worms = new LinkedList<>();
        BufferedReader reader = new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/" + file ) );
        String line = null;
        while( ( line = reader.readLine() ) != null )
        {
            if( line.startsWith( "\"" ) )
            {
                String name = line.substring( 1 );
                worms.add( new Worm( name.substring( 0, name.indexOf( '"' ) ) ) );
                continue;
            }
            if( worms.isEmpty() )
            {
                continue;
            }
            line = line.trim();
            Worm worm = worms.getLast();
            if( line.startsWith( "java.lang.Thread.State: " ) )
            {
                worm.setState( line.replace( "java.lang.Thread.State: ", "" ).replaceAll( "\\(.*?\\)", "" ).trim() );
                continue;
            }
            if( line.startsWith( "- parking to wait for" ) )
            {
                String condition = line.substring( line.indexOf( '<' ) + 1 );
                worm.setCondition( condition.substring( 0, condition.indexOf( '>' ) ) );
                continue;
            }
            if( line.startsWith( "- locked" ) )
            {
                String lc = line.substring( line.indexOf( '<' ) + 1 );
                lc = lc.substring( 0, lc.indexOf( '>' ) );
                if( worm.getState().contains( "WAITING" ) )
                {
                    worm.setCondition( lc );
                }
                else
                {
                    worm.addLock( lc );
                }
                continue;
            }
            if( line.startsWith( "- <" ) )
            {
                String lock = line.substring( line.indexOf( '<' ) + 1 );
                worm.addLock( lock.substring( 0, lock.indexOf( '>' ) ) );
                continue;
            }
            if( line.startsWith( "at com.nokia.sb" ) )
            {
                worm.setSbTrace( worm.getSbTrace() + "\n" + line.trim() );
            }
        }
        reader.close();
        return worms;
    }

    private static List<Worm> getWormsFromHSErr( String file ) throws Exception
    {
        List<Worm> worms = new ArrayList();
        String pattern =
            "^.{2}0x[0-9A-Za-z]{8} JavaThread \"(.*?)\" \\[(_.*?), id=.*?, stack\\(0x[0-9A-Za-z]{8},0x[0-9A-Za-z]{8}\\)\\]$";
        BufferedReader reader = new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/" + file ) );
        String line = null;
        boolean start = false;
        while( ( line = reader.readLine() ) != null )
        {
            if( line.startsWith( "Java Threads:" ) )
            {
                start = true;
                continue;
            }
            if( start && line.equals( "" ) )
            {
                break;
            }
            if( start && line.matches( pattern ) )
            {
                Matcher m = Pattern.compile( pattern ).matcher( line );
                m.find();
                Worm worm = new Worm( m.group( 1 ) );
                worm.setState( m.group( 2 ) );
                worms.add( worm );
            }
        }
        reader.close();
        return worms;
    }

}

class Worm
{
    private String name;

    private String state;

    private List<String> locks = new ArrayList<>();

    private String condition;

    private String sbTrace;

    public Worm( String name )
    {
        this.name = name;
        sbTrace = "";
    }

    public String getName()
    {
        return name;
    }

    public String getNameGroup()
    {
        String nameGroup = name;
        if( nameGroup.indexOf( '-' ) != -1 )
        {
            nameGroup = nameGroup.substring( 0, nameGroup.indexOf( '-' ) );
        }
        else if( nameGroup.indexOf( '@' ) != -1 )
        {
            nameGroup = nameGroup.substring( 0, nameGroup.indexOf( '@' ) );
        }
        else if( nameGroup.indexOf( ' ' ) != -1 )
        {
            nameGroup = nameGroup.substring( 0, nameGroup.indexOf( ' ' ) );
        }
        return nameGroup;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getState()
    {
        return state;
    }

    public void setState( String state )
    {
        this.state = state;
    }

    public List<String> getLocks()
    {
        return locks;
    }

    public void addLock( String lock )
    {
        locks.add( lock );
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition( String condition )
    {
        this.condition = condition;
    }

    public String getSbTrace()
    {
        return sbTrace;
    }

    public void setSbTrace( String sbTrace )
    {
        this.sbTrace = sbTrace;
    }

    public String toString()
    {
        return name + "->" + state + "->" + condition + "->" + locks + "->" + sbTrace;
    }
}
