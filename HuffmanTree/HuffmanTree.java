import java.util.*;

public class HuffmanTree {
//DO NOT include the frequency or priority in the tree
	
	private class Node {
		private Node left;
		private char data;
		private Node right;
		private Node parent;
		
		private Node(Node L, char d, Node R, Node P) {
			left = L;
			data = d;
			right = R;
			parent = P;  
		}
	}
	
	private Node root;
	private Node current; //this value is changed by the move methods
	
	
	public HuffmanTree() {
		root = null;
		current = null;
	}
	
	
	public HuffmanTree(char d) {
		//makes a single node tree
		root = current = new Node(null, d, null, null);
	}
	
	
	public HuffmanTree(String t, char nonLeaf) {
		//Assumes t represents a post order representation of the tree as discussed
		//in class
		//nonLeaf is the char value of the data in the non-leaf nodes
		//in class we used (char) 128 for the non-leaf value
		
		//Pop. with tree string split up into chars
		char arr[] = t.toCharArray();
		Stack<HuffmanTree> stack = new Stack<>();
		
		for( int i = 0; i < arr.length; i++) {
			//pop off 2 items when get nonleaf
			if (arr[i] == nonLeaf) {
				HuffmanTree rightItem = stack.pop();
				HuffmanTree leftItem = stack.pop();
				
				stack.push(new HuffmanTree(leftItem, rightItem, nonLeaf));
			}
			else {
				stack.push(new HuffmanTree(arr[i]));
			}
		}
		root = current = stack.pop().root;
	}
	
	
	public HuffmanTree(HuffmanTree b1, HuffmanTree b2, char d) {
		//makes a new tree where b1 is the left subtree and 2 is the right subtree
		//d is the data in the root
		root = current = new Node(b1.root, d, b2.root, null);
		
	}
	
	
	//use the move methods to travers the tree
	//the move methods change the value of current
	//use these in the decoding process
	
	public void moveToRoot() {
		//change current to reference the root of the tree
		current = root;
	}
	
	
	public void moveToLeft() {
		//PRE: the current node is not a leaf
		//change current to reference the left child of the current node
		current = current.left;
		
	}
	
	
	public void moveToRight() {
		//PRE: the current node is not a leaf
		//change current to reference the right chile of the current node
		current = current.left;
		
	}
	
	
	public void moveToParent() {
		//PRE: the current node is not the root
		//change current to reference the parent of the current node
		current = current.parent;
		
	}
	
	
	public boolean atRoot() {
		//returns true if the current node is the root otherwise returns false
		if(current.parent == null) {
			return true;
		} 
		return false;
		
	}
	
	
	public boolean atLeaf() {
		//returns true if current references a leaf otherwise returns false
		
		if(current.right == null && current.left == null) {
			return true;
		}
		return false;
		
	}
	
	
	public char current() {
		//reutrns the data value in the node referenced by current
		return current.data;
		
	}
	
	
//	public String[] pathsToLeaves() {
//		/*returns an array of strings with all paths from the root to the leaves
//		 * each value in the array contains a leaf value followed by a sequence of
//		 * 0s and 1s. The 0s and 1s represent the path from the root to the node
//		 * containing the leaf value.
//		 */
//	}
	
	//changed this method to the iterator below
	
	
	public class HuffIterator implements Iterator<String> {
		private LinkedList<String> paths;
		//private String[] path;
		
		public HuffIterator() {
			paths = new LinkedList<>();
			//path = new String[100]; 
			
			makePath(root, "");
			
			
		}
		
		public boolean hasNext() {
			return !paths.isEmpty();
		}
		
		public String next() {
			return paths.poll();
		}
		
		private void makePath(Node r, String path) {
			if(r.left == null) {
				paths.add(r.data + path);
				return;
			}
			makePath(r.left, path + "0");
			makePath(r.right,path + "1");
		}
	}
	
	public Iterator<String> iterator(){
		return new HuffIterator();
	}
	
	
	public String toString() {
		//returns a string representation of the tree using the postorder format
		//discussed in class
		return toString(this.root);
	}
	
	
	private String toString(Node r) {
		if(r == null) {
			return "";
		}
		
		return toString(r.left) + toString(r.right) + r.data;  
	}
}
