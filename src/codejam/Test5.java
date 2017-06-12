package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class Test5
{
    public static void main( String[] args ) throws Exception
    {
 //       String folder = "D:/userdata/xinfu/Desktop/";
        String folder = "C:/Users/Sacephor/Desktop/";
    	FileWriter writer = new FileWriter(folder + "Test5Output.txt");
        BufferedReader reader = new BufferedReader( new FileReader( folder + "1.txt" ) );
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
    	if(!coverAll(cover)){
	       build +=buildLoop(adjacencies, cover);
    	}
       return build;
    }

	private static int buildEndpoint(boolean[][] adjacencies, boolean[] cover)
	{
		int build =0;
		int[] kkk = getEndpoints(adjacencies);
		for(int k:kkk){
	    	   build+=buildSite(adjacencies, cover, k);
		}
		return build;
	}
	
	private static int[] getEndpoints(boolean[][] adjacencies){
		int[] k = new int[adjacencies.length];
		int n =0;
		for(int i =0; i <adjacencies.length;i++){
			int count =0;
			int neighbor = -1;
			for(int j=0;j< adjacencies[i].length;j++){
				if(adjacencies[i][j]){
					neighbor = j;
					count++;
				}
				if(count>1){
					break;
				}
			}
			if(count==1&&!contains(k, neighbor)){
				k[n++]=neighbor;
			}
		}
		k = Arrays.copyOf(k, n);
		return k;
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
	
	private static int buildLoop(boolean[][] adjacencies, boolean[] cover){
		int min =1 ;
		
		while(min<cover.length){
			if(buildLoopWithGivenNumber(adjacencies, cover, getSortedIndex(adjacencies, cover), min)){
				break;
			}
			min++;
		}
		return min;
	}
	
	private static boolean buildLoopWithGivenNumber(boolean[][] adjacencies, boolean[] cover , int[] selectable, int left){
		if(left==1){
			for(int point: selectable){
		    	boolean[][] adjacenciesCopy = copyMatrix(adjacencies);
		    	boolean[] coverCopy = Arrays.copyOf(cover, cover.length);
				buildSite(adjacenciesCopy, coverCopy,point);
				if(coverAll(coverCopy)){
					return true;
				}
			}
			return false;
		}

    	boolean[][] adjacenciesCopy = copyMatrix(adjacencies);
    	boolean[] coverCopy = Arrays.copyOf(cover, cover.length);
    	
		buildSite(adjacencies, cover, selectable[0]);
		if(buildLoopWithGivenNumber(adjacencies, cover, getSortedIndex(adjacencies, cover), left-1)){
			return true;
		}
		buildSite(adjacenciesCopy, coverCopy, selectable[1]);
		if(buildLoopWithGivenNumber(adjacenciesCopy, coverCopy, remove(getSortedIndex(adjacenciesCopy, coverCopy),selectable[0]),left-1)){
			return true;
		}
		
		return false;
	}

	private static int buildSite(boolean[][] adjacencies, boolean[] cover, int site)
	{
		boolean act = !cover[site];
		for(int i=0;i<adjacencies.length;i++){
			if(adjacencies[site][i]){
				adjacencies[site][i]= false;
				adjacencies[i][site] = false;
				act |= !cover[i];
				cover[i] = true;
			}
		}
		cover[site] = true;
	return act?1:0;
	}

	private static boolean[][] copyMatrix(boolean[][] matrix){
		boolean[][] copy = new boolean[matrix.length][];
    	for(int i = 0; i < matrix.length;i++){
    		copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
    	}
    	return copy;
	}
        
    private static boolean coverAll(boolean[] cover){
    	for(boolean coverd: cover){
    		if(!coverd){
    			return false;
    		}
    	}
    	return true;
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
	
	private static int[] remove(int[] arr, int val){
		int [] newarr = new int[arr.length-1];
		boolean met = false;
		for(int i = 0; i< arr.length;i++){
			if(arr[i]==val){
				met = true;
				continue;
			}
			if(!met){
				newarr[i] = arr[i];
			}else{
				newarr[i-1] = arr[i];
			}
		}
		return newarr;
	}
    
    private static int[] getSortedIndex( boolean[][] adjacencies , boolean[] cover)
    {
    	int[] edgeCounts = new int[adjacencies.length];
    	for(int i = 0 ; i < adjacencies.length;i++){
    		int count = 0 ;
    		for(int j=0;j< adjacencies[i].length;j++){
    			if(adjacencies[i][j]&&!cover[j]){
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
