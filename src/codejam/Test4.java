package codejam;

import java.io.BufferedReader;
import java.io.FileReader;



public class Test4
{
	
	private static int build = 0;

    public static void main( String[] args ) throws Exception
	{
        BufferedReader reader =
            new BufferedReader( new FileReader( "D:/userdata/xinfu/Desktop/Subway_large_1496988703403" ) );

        String caseId = reader.readLine();
        int districtCount = Integer.parseInt( reader.readLine() );

        reader.readLine();
        int[][] distances = new int[ districtCount - 1 ][];
        for( int i = 0; i < distances.length; i++ )
        {
            distances[i] = new int[ i + 1 ];
            String[] data = reader.readLine().split( " +" );
            for( int j = 0; j <= i; j++ )
            {
                distances[i][j] = Integer.parseInt( data[j] );
            }
        }

        int[][] routes = new int[ Integer.parseInt( reader.readLine() ) ][];
        for( int i = 0; i < routes.length; i++ )
        {
            String[] data = reader.readLine().split( " " );
            int[] line = new int[ 2 ];
            line[0] = Integer.parseInt( data[0] );
            line[1] = Integer.parseInt( data[1] );

            buildRouteByEdge( routes, line[1], line[0], districtCount, true );
        }
        reader.close();


        int[][] sort = getSorted( distances );

        while( !full( routes ) )
        {
            for( int i = 0; i < sort.length; i++ )
            {
                if( sort[i] == null )
                {
                    continue;
                }

                if( !buildRouteByEdge( routes, sort[i][0] + 2, sort[i][1] + 1, districtCount, false ) )
                {
                    continue;
                }
                build += distances[sort[i][0]][sort[i][1]];
                sort[i] = null;
                break;
            }
        }

        System.out.println( caseId.substring( 0, 4 ) + " " + caseId.substring( 4 ) + " " + build );
	}

    private static boolean buildRouteByEdge( int[][] routes, int i, int j, int districtCount,
                                             boolean init )
    {
        int[] iRoute = getRoute( routes, i );
        int[] jRoute = getRoute( routes, j );
        if( size( routes ) == 0 || init && iRoute == null && jRoute == null )
        {
            int[] route = new int[ districtCount ];
            route[0] = i;
            route[1] = j;
            for( int k = 0; k < routes.length; k++ )
            {
                if( routes[k] == null )
                {
                    routes[k] = route;
                    break;
                }
            }
        }
        else if( iRoute != jRoute )
        {
            if( iRoute == null )
            {
                addToRoute( jRoute, i );
            }
            else if( jRoute == null )
            {
                addToRoute( iRoute, j );
            }
            else
            {
                for( int d : jRoute )
                {
                    if( d != 0 )
                    {
                        addToRoute( iRoute, d );
                    }
                }
                removeRoute( routes, jRoute );
            }
        }
        else
        {
            return false;
        }
        return true;
    }
	
    private static int[][] getSorted( int[][] distances )
    {
        int size = distances.length;
        int[][] sort = new int[ ( size + 1 ) * size / 2 ][];
        int k = 0;
        for( int i = 0; i < distances.length; i++ )
        {
            for( int j = 0; j < distances[i].length; j++ )
            {
                sort[k] = new int[ 2 ];
                sort[k][0] = i;
                sort[k][1] = j;
                k++;
            }
        }
        indexQuickSort( distances, sort, 0, sort.length - 1 );
        return sort;
	}

    private static int size( int[][] routes )
    {
        int count = 0;
        for( int[] route : routes )
        {
            if(route!=null){
                count++;
            }
        }
        return count;
    }

    private static int[] getRoute( int[][] routes, int district )
    {
        for( int[] route : routes )
        {
            if( route != null && contains( route, district ) )
            {
                return route;
            }
        }
        return null;
    }

    private static void removeRoute( int[][] routes, int[] route )
    {
        for( int i = 0; i < routes.length; i++ )
        {
            if( route == routes[i] )
            {
                routes[i] = null;
                break;
            }
        }
    }

    private static boolean contains( int[] arr, int val )
    {
        for( int v : arr )
        {
            if( val == v )
            {
                return true;
            }
        }
        return false;
    }

    private static boolean full( int[][] routes )
    {
        if( size( routes ) != 1 )
        {
            return false;
        }
        for( int[] route : routes )
        {
            if( route == null )
            {
                continue;
            }
            for( int i : route )
            {
                if( i == 0 )
                {
                    return false;
                }
            }
        }
        return true;
    }
	
    private static void addToRoute( int[] route, int district )
    {
        for( int i = 0; i < route.length; i++ )
        {
            if( route[i] == 0 )
            {
                route[i] = district;
                break;
            }
        }
    }
	
    private static void indexQuickSort( int[][] distances, int[][] sort, int i, int j )
    {
        int start = i;
        int end = j;
        int target = distances[sort[i][0]][sort[i][1]];
        boolean iFlag = false;
        while( i < j )
        {
            if( iFlag )
            {
                if( distances[sort[i][0]][sort[i][1]] > target )
                {
                    exchange( sort, j, i );
                    iFlag = !iFlag;
                }
                else
                {
                    i++;
                }
            }
            else
            {
                if( distances[sort[j][0]][sort[j][1]] < target )
                {
                    exchange( sort, i, j );
                    iFlag = !iFlag;
                }
                else
                {
                    j--;
                }
            }
        }
        
        if( i > start )
        {
            indexQuickSort( distances, sort, start, i );
        }
        if( i + 1 < end )
        {
            indexQuickSort( distances, sort, i + 1, end );
        }
    }

    private static void exchange( int[][] sort, int i, int j )
    {
        int[] temp = sort[j];
        sort[j] = sort[i];
        sort[i] = temp;
    }
	
}