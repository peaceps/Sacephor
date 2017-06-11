package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class Test5
{
    public static void main( String[] args ) throws Exception
    {
    	String folder = "C:/Users/Sacephor/Desktop/";
    	FileWriter writer = new FileWriter(folder + "Test5Output.txt");
        BufferedReader reader =  new BufferedReader( new FileReader( folder+"1.txt" ) );
        reader.readLine();
        
        int casecount = 0;
        String result = "";
        String line = null;
        while ((line = reader.readLine())!=null){
        	casecount++;
        	int blank = line.indexOf(' ');
        	int total = Integer.parseInt(blank==-1?line:line.substring(0,blank));
        	if(blank==-1 || blank == line.length()-1){
        		result +=  getResultString(casecount, total);
        		continue;
        	}
        	boolean[] cover = new boolean[total];
        	boolean[][] adjacencies = new boolean[total][];
        	for(int i = 0 ; i < total; i++){
        		adjacencies[i] = new boolean[total];
        	}
        	String[] edges = line.substring(blank+1).split(" ");
        	for(int i = 0 ; i < edges.length; i+=2){
        		int a = Integer.parseInt(edges[i]);
        		int b = Integer.parseInt(edges[i+1]);
        			adjacencies[a-1][b-1] = true;
        			adjacencies[b-1][a-1] = true;
        	}
        	result += getResultString(casecount, build(adjacencies, cover));
        }

  //  	System.out.println(result);
        reader.close();
        writer.write(result.substring(0, result.length()-1));
        writer.close();
    }
    
    private static int build(boolean[][] adjacencies ,boolean[] cover ){
    	int build = 0;
	       build += buildEndpoint(adjacencies, cover);
	       build +=buildIsolateCount(adjacencies, cover);
    	while(!coverAll(cover)){
	       build +=buildLoop(adjacencies, cover,0);
    	}
       return build;
    }

	private static int buildEndpoint(boolean[][] adjacencies, boolean[] cover)
	{
		int build =0;
		int leafMountPoint;
		while((leafMountPoint=getEndpointNeighbor(adjacencies, cover))!=-1){
	    	   build+=buildSite(adjacencies, cover, leafMountPoint);
	       }
		return build;
	}

	private static int getEndpointNeighbor(boolean[][] adjacencies, boolean[] cover ){
		int neighbor =-1;
		for(int i = 0 ; i < adjacencies.length ; i++){
			if(cover[i]){
				continue;
			}
			for(int j = 0 ; j<adjacencies[i].length;j++){
	    		if(adjacencies[i][j]){
	    			if(neighbor==-1){
	    				neighbor = j;
	    			}else{
	    				neighbor = -1;
	    				break;
	    			}
	    		}
	    	}
			if(neighbor!=-1){
				return neighbor;
			}
	    }
		return neighbor;
	}

	private static int buildIsolateCount(boolean[][] adjacencies, boolean[] cover ){
		int isoCount = 0;
		for(int i =0 ; i < adjacencies.length ; i++){
			if(cover[i]){
				continue;
			}
			boolean iso = true;
			for(boolean neighbor: adjacencies[i]){
				if(neighbor){
					iso=false;
					break;
				}
			}
			if(iso){
				isoCount++;
				cover[i] = true;
			}
		}
		return isoCount;
	}

	private static int buildLoop(boolean[][] adjacencies, boolean[] cover , int depth){
    	int[] index = getSortedIndex(adjacencies);
    	int minBuild = -1;
    	boolean[][] minAdjacencies = adjacencies;
    	boolean[] minCover = cover;
    	
    	for(int maxEdgeIndex : index){
    		if(cover[maxEdgeIndex]){
    			break;
    		}

        	boolean[] coverCopy = Arrays.copyOf(cover, cover.length);
        	boolean[][] adjacenciesCopy = new boolean[adjacencies.length][];
        	for(int i = 0; i < adjacencies.length;i++){
        		adjacenciesCopy[i] = Arrays.copyOf(adjacencies[i], adjacencies[i].length);
        	}
    		
        	int build = 0;
            	build+= buildSite(adjacenciesCopy, coverCopy, maxEdgeIndex);
            	if(build == minBuild){
            		break;
            	}
       	       build += buildEndpoint(adjacenciesCopy, coverCopy);
           	if(build == minBuild){
        		break;
        	}
      	       build +=buildIsolateCount(adjacenciesCopy, coverCopy);
           	if(build == minBuild){
        		break;
        	}
      	       if(!coverAll(coverCopy)){
      	    	   build+= buildLoop(adjacenciesCopy, coverCopy,depth+1);
      	       }
      	       
        	if(minBuild== -1 || build< minBuild){
        		minBuild = build;
        		minAdjacencies = adjacenciesCopy;
        		minCover = coverCopy;
        	}
    	}
    	
		for(int i = 0 ; i < minCover.length;i++ ){
			cover[i] = minCover[i];
			adjacencies[i] = minAdjacencies[i];
		}
    	return minBuild;
    }
        
    private static int buildSite(boolean[][] adjacencies, boolean[] cover, int site)
	{
		for(int i = 0 ; i < adjacencies[site].length;i++){
			   if(adjacencies[site][i]){
			    	cover[i] = true;
			    	for (int j=0; j<adjacencies.length;j++){
						adjacencies[j][i] = false;
						adjacencies[i][j] = false;
			    	}
			   }
		   }
		   cover[site] = true;
		   return 1;
	}

	private static boolean coverAll(boolean[] cover){
    	for(boolean coverd: cover){
    		if(!coverd){
    			return false;
    		}
    	}
    	return true;
    }
    
    private static int[] getSortedIndex( boolean[][] adjacencies )
    {
    	int[] edgeCounts = new int[adjacencies.length];
    	for(int i = 0 ; i < adjacencies.length;i++){
    		int count = 0 ;
    		for(boolean neighbor : adjacencies[i]){
    			if(neighbor){
    				count++;
    			}
    		}
    		edgeCounts[i] = count;
    	}
    	int[] index = new int[edgeCounts.length];
    	for(int i = 0 ; i < index.length ;i++){
    		index[i] = i;
    	}

    	indexQuickSort(edgeCounts, index, 0, edgeCounts.length-1);
    	
    	return index;
    }
    
    private static void indexQuickSort( int[] edgeCounts, int[] index, int i, int j )
    {
        int start = i;
        int end = j;
        int target = edgeCounts[index[i]];
        boolean iFlag = false;
        while( i < j )
        {
            if( iFlag )
            {
                if( edgeCounts[index[i]] < target )
                {
                    exchange( index, j, i );
                    iFlag = !iFlag;
                    j--;
                }
                else
                {
                    i++;
                }
            }
            else
            {
                if(  edgeCounts[index[j]] > target )
                {
                    exchange( index, i, j );
                    iFlag = !iFlag;
                    i++;
                }
                else
                {
                    j--;
                }
            }
        }
        
        if( i > start )
        {
            indexQuickSort( edgeCounts, index, start, i );
        }
        if( i + 1 < end )
        {
            indexQuickSort( edgeCounts, index, i + 1, end );
        }
    }

    private static void exchange( int[] index, int i, int j )
    {
        int temp = index[j];
        index[j] = index[i];
        index[i] = temp;
    }

	private static String getResultString(int casecount, int result){
		return "Case #" + casecount + ": " + result +"\n";
	}
}
