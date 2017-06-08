package codejam;

import java.util.Arrays;
import java.util.stream.Stream;

public class Test4
{
	
	private static int build = 0;
	public static void main(String[] args)
	{
		int districtCount = 5;
		int[][] distances = {{0,100,100,100,100},{100,0,2,80,3},{100,2,0,80,4},{100,80,80,0,80},{100,3,4,80,0}};
		int[][] routes = {{1,4,0,0,0},null,null,null,null};
		int[][] ranks = getDistanceRanks(distances);
		
	Stream.of(ranks).forEach(r -> System.out.println(Arrays.toString(r)));
		
		for(int i =districtCount ; i >=1  ; i--){
			if(getRoute(routes, i) ==null ){
				int[] newRoute =new int[districtCount];
				if(!linkNearest(routes, ranks, distances, newRoute, i)){
					addRoute(routes, newRoute);
				}
			}
		}
		Stream.of(routes).forEach(r -> System.out.println(Arrays.toString(r)));
		
		linkRoutes(routes,ranks,distances);
		
		System.out.println(build);
		
	}
		
	private static void linkRoutes(int[][] routes, int[][] ranks,int[][] distances){
		
	}
	
	private static boolean linkNearest(int[][] routes, int[][] ranks,int[][] distances, int[] newRoute, int i){
		
		int nearest =0;
		int[] nearestRoute = null;
		
		while( nearestRoute==null && !contains(newRoute, i)){
			addToRoute(newRoute, i);
			
			nearest = ranks[i-1][0]+1;
			if(!contains(newRoute, nearest)){
			build+=distances[i-1][ranks[i-1][0]];}
			
			nearestRoute = getRoute(routes, nearest);
			i = nearest;
		}
		
		if(nearestRoute!=null){
			for(int d:newRoute){
				if(d!=0){
				addToRoute(nearestRoute, d);}
			}
		}
		
		return nearestRoute!=null;
	}
	
	private static int[] getRoute(int[][] arr, int val){
		for(int[] v:arr){
			if(v!=null&&contains(v, val)){
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