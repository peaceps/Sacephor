package codejam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

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
        	
        //	Stream.of(adjacencies).forEach(a -> System.out.println(Arrays.toString(a)));
        }

  //  	System.out.println(result);
        reader.close();
        writer.write(result.substring(0, result.length()-1));
        writer.close();
    }
    
    private static int build(boolean[][] adjacencies ,boolean[] cover ){
    	int build = 0;
    	
    	while(!coverAll(cover)){
	       build += buildLeafMountPoint(adjacencies, cover);
	       build +=buildIsolateCount(adjacencies, cover);
	       build +=buildLoop(adjacencies, cover);
    	}
       
       return build;
    }

	private static int buildLeafMountPoint(boolean[][] adjacencies, boolean[] cover)
	{
		int build =0;
		int leafMountPoint;
		while((leafMountPoint=getOffLeafMountPoint(adjacencies, cover))!=-1){
	    	   build+=buildSite(adjacencies, cover, leafMountPoint);
	       }
		return build;
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
    
    private static int getOffLeafMountPoint(boolean[][] adjacencies, boolean[] cover ){
    	int point =-1;
    	for(int i = 0 ; i < adjacencies.length ; i++){
    		if(cover[i]){
    			continue;
    		}
    		for(int j = 0 ; j<adjacencies[i].length;j++){
	    		if(adjacencies[i][j]){
	    			if(point==-1){
	    				point = j;
	    			}else{
	    				point = -1;
	    				break;
	    			}
	    		}
	    	}
    		if(point!=-1){
    			return point;
    		}
	    }
    	return point;
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
    
    private static int buildLoop(boolean[][] adjacencies, boolean[] cover ){
    	int maxEdge =0;
    	int maxIndex=-1;
    	for(int i =0; i < adjacencies.length;i++){
    		int edgeCount = 0;
    		for(boolean neighbor: adjacencies[i]){
    			if(neighbor){
    				edgeCount++;
    			}
    		}
    		if(edgeCount>maxEdge){
    			maxEdge = edgeCount;
    			maxIndex = i;
    		}
    	}
    	return maxEdge>0?buildSite(adjacencies, cover, maxIndex):0;
    }
    
    private static boolean coverAll(boolean[] cover){
    	for(boolean coverd: cover){
    		if(!coverd){
    			return false;
    		}
    	}
    	return true;
    }
    
    private static String getResultString(int casecount, int result){
    	return "Case #" + casecount + ": " + result +"\n";
    }
}
