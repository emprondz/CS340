import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;


public class BinaryTree {
//Implements a Binary Tree of Strings
	
	private class Node { 
		private Node left; 
		private String data;
		private Node right;
		private Node parent; //reference to the parent node
		
		private Node(Node L, String d, Node r, Node p) {
			left = L;
			data = d;
			right = r;
			parent = p;
		}
	}
	
	private Node root;
	private String t;
	
	public BinaryTree() {
		//creates an empty tree
		root = null;
	}
	
	
	public BinaryTree(String d) {
		//create a tree with a single node
		Node newNode = new Node(null, d, null, null);
		root = newNode;
	}
	
	
	public BinaryTree(BinaryTree b1, String d, BinaryTree b2) {
		//merges the trees b1 and b2 with a common root with data d
		//this constructor must make a copy of the contents of b1 and b2
		
		BinaryTree b3 = new BinaryTree(b1.root.data);
		BinaryTree b4 = new BinaryTree(b2.root.data);
		
		if(b3.root.left != null) {
			b3.root.left = cloneTree(root.left);
		}
		if(b3.root.right != null) {
			b3.root.right = cloneTree(root.right);
		}
		if(b4.root.left != null) {
			b4.root.left = cloneTree(root.left);
		}
		if(b4.root.right != null) {
			b4.root.right = cloneTree(root.right);
		}

		BinaryTree newTree = new BinaryTree(d);
		newTree.root.left = b3.root;
		newTree.root.right = b4.root;
	}
	
	
	//CLONE TREE METHOD
	public Node cloneTree(Node root) {
		if(root == null) 
			return null;
		Node newNode = new Node(null, root.data, null, root.parent);
		newNode.right = cloneTree(root.right);
		newNode.left  = cloneTree(root.left);
		return newNode;
		
	}
	
	
	private int start = 0;
	
	public BinaryTree(String t, String open, String close, String empty) {
		/* create a binary tree from the post order format discussed
		 * in class. Assume t is a syntactically correct string 
		 * representation of the tree. Open and close are the strings 
		 * which represent the beginning and end markers of a tree. 
		 * Empty represents an empty tree.
		 * The example in class used () and ! for open, close and
		 * empty respectively.
		 * The data in the tree will not include strings matching 
		 * open, close or empty.
		 * All tokens (data, open, close and empty) will be separated 
		 * by white space
		 * Most of the work should be done in a private recursive method
		 * 
		 */
		t = t;
		Stack<String> stack = new Stack<>();
		Scanner scan = new Scanner(t);
		
		while(scan.hasNext()) {
			String s = scan.next();
			stack.push(s);
		}
		scan.close();
		
		stringTree(stack, open, close, empty);
		
		
		
	}
	
	
	private int i = 1;

	//RECURSIVE METHOD FOR PARSING STRING INTO BINARY TREE
	private Node stringTree(Stack<String> stack, String open, String close, String empty) {
		Node newNode;
		if(stack.isEmpty()) {
			return null;
		}
		else if(stack.peek() == open) {
			return null;
		}
		else if(stack.peek() == close) {
			
			stack.pop();
			newNode = new Node(null, stack.pop(), null, null);
			if(i == 1) {
				root = newNode;
			}
			i++;
			newNode.right = stringTree(stack, open, close, empty);
			
		} else if(stack.peek() == empty){
			return null;
		}else {
			newNode = new Node(null, stack.pop(), stringTree(stack,open,close,empty), null);
		}
		
		
		return newNode;
	}
	
	
	public class PostorderIterator implements Iterator<String> {
		//An iterator that returns data in the tree in an post order pattern
		//the implentation must use the parent pointer and must not use an
		//additional data structure
		Node currNode;
		
		public PostorderIterator() {
			currNode = root;	
		}
		
		
		public boolean hasNext() {
			if(currNode.left != null || currNode.right != null) {
				return true;
			}
			return false;
		}
		
		
		public String next() {
			if(!hasNext()) {
				Node tempNode = currNode.parent;
				if(tempNode.right != null && tempNode.right != currNode) {
					currNode = currNode.parent.right;
				} else {				
					currNode = currNode.parent;
					tempNode = tempNode.parent;
					return currNode.data;
				}
				
			}
			
			while(hasNext()) {
				if(currNode.left == null) {
					currNode = currNode.left;
				}
				else {
					currNode = currNode.right;
				}
			}
			return currNode.data;
		}
		
		
		public void remove() {
			if(currNode.left != null && currNode.right != null) {
				Node tempNode = currNode;
				currNode = currNode.right;
				while(currNode.left != null) {
					currNode = currNode.left;
				}
				tempNode.data = currNode.data;
				currNode.parent.left = null;
				return;
				
			}
		    if(currNode.left == null && currNode.right == null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = null;
		    		return;
		    	}
		    	currNode.parent.right = null;
		    	return;
		    }
		    if(currNode.left != null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = currNode.left;
		    		return;
		    	}
		    	currNode.parent.right = currNode.left;
		    	return;
		    }
		    if(currNode.right != null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = currNode.right;
		    		return;
		    	}
		    	currNode.parent.right = currNode.right;
		    	return;
		    }
		}
	}
	
	
	public class InorderIterator implements Iterator<String>{
		//an iterator that returns data in the tree in a in order pattern
		//This implentation must use a stack and must not use the parent pointer
		//You must use Java's stack class
		Stack<Node> nodes = new Stack<>();
		Node currNode;
		
		public InorderIterator() {
			currNode = root;
		}
		
		public boolean hasNext() {
			if(nodes.isEmpty()) {
				return false;
			}
			return true;
		}
		
		public String next() {
			while(hasNext() || currNode != null) {
				if(currNode != null) {
					nodes.push(currNode);
					currNode = currNode.left;
				}else {
					Node node = nodes.pop();
					String a = node.data;
					currNode = node.right;
					return a;
				}
			}
			return "";
		}
		
		public void remove() {
			if(currNode.left != null && currNode.right != null) {
				Node tempNode = currNode;
				currNode = currNode.right;
				while(currNode.left != null) {
					currNode = currNode.left;
				}
				tempNode.data = currNode.data;
				currNode.parent.left = null;
				return;
				
			}
		    if(currNode.left == null && currNode.right == null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = null;
		    		return;
		    	}
		    	currNode.parent.right = null;
		    	return;
		    }
		    if(currNode.left != null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = currNode.left;
		    		return;
		    	}
		    	currNode.parent.right = currNode.left;
		    	return;
		    }
		    if(currNode.right != null) {
		    	if(currNode.parent.left == currNode) {
		    		currNode.parent.left = currNode.right;
		    		return;
		    	}
		    	currNode.parent.right = currNode.right;
		    	return;
		    }
		}
	}
	
	
	public Iterator<String> inorder() {
		//return a new in order iterator object
		Iterator<String> I1 = new InorderIterator();
		return I1;
	}
	
	public Iterator<String> postorder() {
		Iterator<String> I1 = new PostorderIterator();
		return I1;
	}
	
	public String toString() {
		//returns the string representation of the tree using the post order format
		//discussed in class. If the tree was created from a string, use the
		//the values of open, close and empty given to the constructor otherwise
		//use (, ) and ! for open, close and empty respectively
		//most of the work should be done in a recursive private method
		if(this.t != null) {
			return t;
		}
		return string(this.root);
	}
	
	private String string(Node root) {
		String result = "(";
		if(root == null) {
			return "!";
		}
		result += "(" + string(root.left) + ")";
		result += "(" + string(root.right) + ")";
		result += root.data + ")";
		return result;
	}
}

