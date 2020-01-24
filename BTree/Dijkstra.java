// Kyle Schmidt
import java.io.*;
import java.util.*;

public class Dijkstra {

	// Used for PriorityQueue.
	@SuppressWarnings("rawtypes")
	private class Item implements Comparable {

		private int distance;
		private int node;
		private int last;

		private Item(int d, int x, int y) {

			distance = d;
			node = x;
			last = y;

		}

		public int compareTo(Object z) {

			return distance - ((Item) z).distance;

		}

	}

	private class Vertex {

		private EdgeNode edges1;
		private EdgeNode edges2;

		private Vertex() {

			edges1 = null;
			edges2 = null;

		}
		
		public String toString () {
			
			return "Edge1: " + edges1 + " Edge2: " + edges2;
			
		}

	}

	private class EdgeNode {
		
		private int vertex1;
		private int vertex2;
		private EdgeNode next1;
		private EdgeNode next2;
		private int weight;

		// PRE: v1 < v2
		private EdgeNode(int v1, int v2, EdgeNode e1, EdgeNode e2, int w) {

			vertex1 = v1;
			vertex2 = v2;
			next1 = e1;
			next2 = e2;
			weight = w;

		}

		public String toString () {
			
			return "v1: " + vertex1 + " v2: " + vertex2 + " weight: " + weight;
			
		}
		
	}
	

	public void addEdge(int v1, int v2, int w) {
	// PRE: v1 and v2 are legitimate vertices
	// (i.e. 0 <= v1 < g.length and 0 <= v2 < g.length 
		if (v1 > v2) {
			
			int temp = v1;
			v1 = v2;
			v2 = temp;
			
		}	
	
		EdgeNode e = new EdgeNode (v1, v2, g[v1].edges1, g[v2].edges2, w);
		g[v1].edges1 = e;
		g[v2].edges2 = e;
		
	}
	

	public void printRoutes(int j) {
	// find and print the best routes from j to all other nodes in the graph
	// Note discussion in class of the limitation of Javas Priority
	// Queue for this algorithm
		int numPathsFound = 0;
		boolean visited [] = new boolean [g.length];
		items = new Item[g.length];
		PriorityQueue<Item> p = new PriorityQueue<>();
		p.add(new Item (0, j, -1));
	
		while (numPathsFound < g.length) { //Maybe change.
			
			//System.out.print(numPathsFound);
			while (visited [p.peek().node] == true) {
		
				p.remove();
				
			}
		
			// save item as shortest path.
			Item i = p.poll();
			visited [i.node] = true;
			items [i.node] = i;
			numPathsFound ++;
			
			//add unvisited children.
			EdgeNode e = g[i.node].edges1;
			while (e != null) {
			
				if (visited[e.vertex2] == false) {
				
					p.add(new Item (i.distance + e.weight, e.vertex2, i.node));
				
				}
			
				e = e.next1;
			
			}
			
			e = g[i.node].edges2;
			
			while (e != null) {
			
				if (visited[e.vertex1] == false) {
				
					p.add(new Item (i.distance + e.weight, e.vertex1, i.node));
				
				}
			
				e = e.next2;
			
			}
	
		}
		
		Item temp;
		String [] paths = new String [items.length];
		
		for (int i = 0; i < items.length; i ++) {
			
			temp = items[i];
			paths[i] = "";
			
			while (temp.last != -1) {
				
				if (paths[i].length() != 0) {
				
					paths [i] = temp.node + "->" + paths [i];
					
				} else {
					
					paths [i] = temp.node + paths[i];
					
				}
				temp = items[temp.last];
				
			} 
			
			if (items[i].node == j) {
				
				paths[i] = paths[i] + j;
				
			}
			
			paths [i] = "Shortest path is from " + j + " to " + items[i].node + " is " + j + "->" + paths [i] + " with a distance of: " + items[i].distance;

				System.out.println(paths [i]);
			
		}
	
	}	
	
	private Vertex[] g;
	Item items [];
	
	public Dijkstra(int size) {

		g = new Vertex [size];
		
		for (int i = 0; i < size; i++) {
			
			g[i] = new Vertex();
			
		}
		
	}

	@SuppressWarnings("resource")
	public static void main(String args[]) throws IOException {
	
		BufferedReader b = new BufferedReader(new FileReader(args[0]));
		String line = b.readLine();
		int numNodes = new Integer(line);
		line = b.readLine();
		int source = new Integer(line);
		System.out.println(source);
		Dijkstra g = new Dijkstra(numNodes);
		line = b.readLine();
		
		while (line != null) {
		
			Scanner scan = new Scanner(line);
			g.addEdge(scan.nextInt(), scan.nextInt(), scan.nextInt());
			line = b.readLine();
		
		}
		
		g.printRoutes(source);
	
	}

}