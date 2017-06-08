package codejam;

import java.util.Arrays;
import java.util.stream.Stream;

public class Test4
{
	
	private static int build = 0;
	public static void main(String[] args)
	{
		int districtCount = 5;
		int[][] distances = {{0,100,100,100,100},{100,0,5,1,17},{100,5,0,7,9},{100,1,7,0,20},{100,17,9,20,0}};
		int[][] routes = {{1,4,0,0,0}};
		int[][] ranks = getDistanceRanks(distances);
		
		Stream.of(ranks).forEach(r -> System.out.println(Arrays.toString(r)));
		
		for(int i =1 ; i <= districtCount ; i++){
			if(getRoute(routes, i) ==null ){
				int[] newRoute =new int[districtCount];
				if(!linkNearest(routes, ranks, distances, newRoute, i)){
					linkRoute(ranks, distances, newRoute);
					addRoute(routes, newRoute);
				}
				
				
			}
		}
		
		linkRoutes();
		
		System.out.println(build);
		
	}
	
	private static void linkRoute(int[][] ranks,int[][] distances, int[] newRoute){
		
	}
	
	private static void linkRoutes(int[][] ranks,int[][] distances, int[] newRoute){
		
	}
	
	private static boolean linkNearest(int[][] routes, int[][] ranks,int[][] distances, int[] newRoute, int i){
		int nearest = ranks[i-1][0]+1;
		int[] nearestRoute = getRoute(routes, nearest);
		if( nearestRoute==null){
			addToRoute(newRoute, i);
			if(contains(newRoute, nearest)){
				return false;
			}
			boolean finded = linkNearest(routes,ranks,distances,newRoute,nearest);
			if(!finded){
				return false;
			}
		}

		removeFromRoute(newRoute, i);
		addToRoute(nearestRoute, i);
		build+=distances[i-1][ranks[i-1][0]];
		return true;
	}
	
	private static int[] getRoute(int[][] arr, int val){
		for(int[] v:arr){
			if(contains(v, val)){
				return v;
			}
		}
		return null;
	}
	
	private static boolean contains(int[] arr, int val){
		for(int v:arr){
			if(val == v){
				return true;
			}
		}
		return false;
	}
	
	private static void addRoute(int[][] routes, int[] route){
		for(int i =0 ; i < routes.length ;  i++){
			if(routes[i] == null){
				routes[i] = route;
				break;
			}
		}
	}
	
	private static void addToRoute(int[] route, int district){
		for(int i =0 ; i < route.length ;  i++){
			if(route[i] == 0){
				route[i] = district;
				break;
			}
		}
	}
	
	private static void removeFromRoute(int[] route, int district){
		for(int i =0 ; i < route.length ;  i++){
			if(route[i] == district){
				route[i] = 0;
				break;
			}
		}
	}
	
	
	private static int[][] getDistanceRanks(int[][] distances){
		int length = distances.length;
		int[][] ranks = new int[length][length-1];
		for(int i =0; i <length;i++){
			ranks[i]=getDistanceRank(distances[i]);
		}
		return ranks;
	}
	
	private static int[] getDistanceRank(int[] distance){
		int length = distance.length;
		int[] rank = new int[length-1];
		int selfMask = 0;
		for(int i = 0 ; i < length-1; i++){
			if(distance[i]==0){
				selfMask++;
			}
			rank[i] = i+selfMask;
		}
		indexSort(distance, rank, 0, length-2);
		return rank;
	}
	
	private static void indexSort(int[] distance, int[] rank, int i, int j){
		int start =i;
		int end =j;
		int target = distance[rank[start]];
		boolean iFlag = false;
		while(i<j){
			if(iFlag){
				if(distance[rank[i]]>target){
					exchange(rank, j, i);
					iFlag = !iFlag;
				}
				i++;
			}else{
				if(distance[rank[j]]<target){
					exchange(rank, i, j);
					iFlag = !iFlag;
				}
				j--;
			}
		}
		if(i>start){
			indexSort(distance, rank, start, i);
		}
		if(i+1<end){
			indexSort(distance, rank, i+1, end);
		}
	}
	
	private static void exchange(int[] rank, int i, int j)
	{
		int temp = rank[j];
		rank[j] = rank[i];
		rank[i] = temp;
	}
	
}