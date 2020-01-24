/*
 * Author: Tyler Kirchner
 * 
 * Program will create an in memory avl tree and an avl tree
 * in a random access file.
 * 
 */

import java.io.*;
import java.util.*;

public class AVLTree {
	
	
	private RandomAccessFile f;
	private long root;  //the address of the root node in the file
	private long free;  //the address in the file of the first node in the free list
	private int numStringFields;  //the number of fixed length character fields
	private int fieldLengths[];    //the length of each character field
	private int numIntFields;     //the number of integer fields
	String currentFile;
	
	
	private class Node {
		
		private int key;
		private char stringFields[][];
		private int intFields[];
		private long left;
		private long right;
		private int height;
		
		//Node constructor 
		private Node(long l, int d, long r, char sFields[][], int iFields[]) {    
			height = 0;
			left = l;
			key = d;
			right = r;
			stringFields = sFields;
			intFields = iFields;
		}
		
		//Node constructor for a file node
		private Node(long addr) throws IOException {               
			for(int i = 0; i < numStringFields; i++) {
				stringFields = new char[numStringFields][fieldLengths[i]];
			}
			
			intFields = new int[numIntFields];
			
			//go to the address of the node we want to make
			f.seek(addr);
			key = f.readInt();
			
			//get all the chars from the string fields
			for(int i = 0; i < numStringFields; i++) {
				for(int j = 0; j < fieldLengths[i]; j++) {
					stringFields[i][j] = f.readChar();
					}
			}
			
			//get the int fields for the node
			for(int i = 0; i < numIntFields; i++) {
				intFields[i] = f.readInt();
			}
			
			//left, right, and height
			left = f.readLong();
			right = f.readLong();
			height = f.readInt();
	  
		}
		
		//Writes the node out to the raf
		private void writeNode(long addr) throws IOException {          
			f.seek(addr);
			f.writeInt(key);
			
			//write the strings into the file and fill with null characters
			for (int i = 0; i < numStringFields; i++) {
				for (int j = 0; j < fieldLengths[i]; j++) {
					if (stringFields[i][j] != '\0') {
						f.writeChar(stringFields[i][j]);
					} else {
						f.writeChar('\0');
					}
				}
			}
			
			//write out the number fields
			for (int i = 0; i < numIntFields; i++) {
				f.writeInt(intFields[i]);
			}
			
			f.writeLong(left);
			f.writeLong(right);
			f.writeInt(height);
		}
	}
	
	//creates a new empty AVL tree stored in the file fname
	//the number of character string fields is stringFieldLengths.length
	//stringFieldLengths contains the length of each string field
	public AVLTree(String fname, int stringFieldLength[], int numIntFields) throws IOException {  

		root = 0;
		free = 0;
		numStringFields = stringFieldLength.length;
		fieldLengths = stringFieldLength;
		this.numIntFields = numIntFields;
		currentFile = fname;
		
		//create a random access file named 'fname'
		f = new RandomAccessFile(currentFile, "rw");
		f.seek(0);
		
		//write 0 for first node and first node in free list along with 
		//the other fields
		
		f.writeLong(root);							//address of first node in the tree
		f.writeLong(free);							//address of first spot in the free list
		f.writeInt(numStringFields);   			    //number of string fields
		
		for(int i = 0; i < numStringFields; i++) {
			f.writeInt(fieldLengths[i]);
		}
		f.writeInt(numIntFields);					//how many int fields there are

	}
	
	
	
	//reuse an existing tree store in the file fname
	public AVLTree(String fname) throws IOException {
		
		currentFile = fname;
		f = new RandomAccessFile(currentFile, "rw");
		f.seek(0);
		
		//grab the basic info for the tree
		root = f.readLong();
		free = f.readLong();
		numStringFields = f.readInt();
		
		fieldLengths = new int[numStringFields];
		
		for(int i = 0; i < numStringFields; i++) {
			fieldLengths[i] = f.readInt();
		}
		numIntFields = f.readInt();
	}
	
	//PRE: the number and lengths of the sFields and iFields match the expected number and lengths
	//insert k and the fields into the tree
	//the string fields are null (‘\0’) padded
	//if k is in the tree do nothing
	public void insert(int k, char sFields[][], int iFields[]) throws IOException {

		root = insert(k, sFields, iFields, root);
		f.seek(0);
		f.writeLong(root);
	}
	
	//helper method to insert that will insert a node into the avl tree
	// and check for balance
	private Long insert(int k, char sFields[][], int iFields[], long r) throws IOException {
		
		//check to see if trying to insert a non root
		if (r == 0) {
						
			Node n = new Node(0, k, 0, sFields, iFields);	
			long addr = getFree();
			n.writeNode(addr);
			return addr;
			
		}
		
		Node t = new Node(r);
		
		if (k < t.key) {
			
			// insert on the left (k < root key)
			t.left = insert (k, sFields, iFields, t.left);
			
			if (height(t.left) - height(t.right) == 2) {
				
				// unbalanced tree: single LL or double LR rotation.
				Node t1 = new Node(t.left);
				
				if (k < t1.key) {
					
					r = rotateWithLeftChild (r);
					t = new Node (r);
					
				} else {
					
					r = doubleWithLeftChild (r);
					t = new Node (r);
					
				}
				
			} 
			
		} else if (k > t.key) {
			
			// insert on the right (k > root key)
			t.right = insert (k, sFields, iFields, t.right);
			if (height(t.right) - height (t.left) == 2) {
				
				// unbalanced tree: single RR or double RL roatation.
				Node t2 = new Node (t.right);
				
				if (k > t2.key) {
					
					r = rotateWithRightChild(r);
					t = new Node (r);
					
				} else {
					
					r = doubleWithRightChild(r);
					t = new Node (r);
					
				}
				
			}
			
		}
		
		// update height as needed and return
	
		t.height = Math.max(height(t.left),height(t.right)) + 1;
		t.writeNode(r);
		return r;
	}
	
	//find out address of the free list
	private long getFree() throws IOException {
		if (free == 0) {
			
			return f.length();
		}
		
		long oldFree = free;
		f.seek(free);
		free = f.readLong();
		return oldFree;
		
	}
	
	//return the height of a tree
	private int height(long address) throws IOException {
		
		if (address == 0) {
		
			return - 1;
		
		}
		
		Node t = new Node (address);
		return t.height;
		
	}
	
	//Does a single rotation on the left chiild
	private Long rotateWithLeftChild(long r) throws IOException {
		
		Node oldRoot = new Node (r);
		Long root = oldRoot.left;
		Node newRoot = new Node (oldRoot.left);
		oldRoot.left = newRoot.right;
		newRoot.right = r;
		oldRoot.height = Math.max(height (oldRoot.left), height(oldRoot.right)) + 1;
		newRoot.height = Math.max(height (newRoot.left), oldRoot.height) + 1;
		newRoot.writeNode(root);
		oldRoot.writeNode(r);
		return root;
		
	}
	
	
	//does a double rotation on the right
	private long doubleWithRightChild(long r) throws IOException {
		
		Node oldRoot = new Node (r);
		// first single-rotate oldRoot's right child with its left child.
        oldRoot.right = rotateWithLeftChild( oldRoot.right );
        oldRoot.writeNode(r);
        // then rotate with the (new) right child of oldRoot.
        return rotateWithRightChild( r );
		
	}

	//does a double rotation on the left
	private long doubleWithLeftChild(long r) throws IOException {
		
		Node oldRoot = new Node (r);
		// first single-rotate oldRoot's left child with its right child.
        oldRoot.left = rotateWithRightChild( oldRoot.left );
        oldRoot.writeNode(r);
        // then rotate with the (new) left child of oldRoot.
        return rotateWithLeftChild( r );
        
	}

	//does a single rotation to the right
	private Long rotateWithRightChild(long r) throws IOException {
		
		Node oldRoot = new Node (r);
		Long root = oldRoot.right;
		Node newRoot = new Node (oldRoot.right);
		oldRoot.right = newRoot.left;
		newRoot.left = r;
		oldRoot.height = Math.max(height (oldRoot.left), height(oldRoot.right)) + 1;
		newRoot.height = Math.max(height (newRoot.right), oldRoot.height) + 1;
		newRoot.writeNode(root);
		oldRoot.writeNode(r);
		return root;
		
	}
	
	//prints out the contents of a tree
	public void print() throws IOException {
		if (root == 0) {
			
			System.out.println("Tree is Empty!");
			
		} else {
			
			StringBuilder builder = new StringBuilder();
			print(root, builder);
			// builder.delete(builder.length() - 2, builder.length());
			System.out.println(builder.toString());
			
		}
	}
	
	//recursive helper method to print that builds the strings for 
	//each node
	private void print(long r, StringBuilder builder) throws IOException {
		
		if (r != 0) {
			String kee;
			Node t = new Node(r);
			print(t.left, builder);
			if(t.key < 10) {
				kee = t.key + " ";
			} else {
				kee = t.key + "";
			}
			
			builder.append("Key = " + kee + " : ");
			
			//snags the string fields without the null characters
			for (int i = 0; i < numStringFields; i++) {
			
				builder.append("String " + (i + 1) + " = ");
				
				for (int j = 0; j < t.stringFields[i].length; j++) {
					if(t.stringFields[i][j] != '\0') {
						builder.append(t.stringFields[i][j]);
					}
				}
				
				
				builder.append("   ");

			}
			
			//grabs the num fields
			for (int i = 0; i < numIntFields; i++) {
				builder.append("Int " + (i + 1) + " = ");
				
				builder.append(t.intFields[i]);
				
				builder.append("   ");

				
			}

			//height
			builder.append(" [h = " + t.height + "]\n");			
			print(t.right, builder);

		}

	}
	
	//if k is in the tree return a linked list of the strings fields associated with k
	//otherwise return null
	//The strings in the list must NOT include the padding (i.e the null chars)
	public LinkedList<String> stringFind(int k) throws IOException {
		return findS(k, root);
	}
	
	// private recursive findString method. 
	private LinkedList<String> findS (int k, long r) throws IOException {
	
		StringBuilder builder;
		LinkedList <String> l = new LinkedList <> ();
		
		if (r == 0) {
			
			return null;
			
		}
		
		Node t = new Node(r);
		
		if (k < t.key) {
			
			return findS (k, t.left);
			
		} else if (k > t.key) {
			
			return findS (k, t.right);
			
		} else {
			
			for (int i = 0; i < numStringFields; i++) {
				
				builder = new StringBuilder();
				
				for (int j = 0; j < fieldLengths[i]; j++){
				
					builder.append(t.stringFields[i][j]);
					
				}
				l.add(builder.toString() + " ");
			}
		}
		
		return l;
		
	}

	//if k is in the tree return a linked list of the integer fields associated with k
	//otherwise return null
	public LinkedList<Integer> intFind(int k) throws IOException {
		return findI(k, root);
	}
	
	//private recursive helper method for intFind
	public LinkedList<Integer> findI(int k, long r) throws IOException {
		
		LinkedList <Integer> l = new LinkedList <> ();
		
		if (r == 0) {
			
			return null;
			
		}
		
		Node t = new Node(r);
		
		if (k < t.key) {
			
			return findI (k, t.left);
			
		} else if (k > t.key) {
			
			return findI (k, t.right);
			
		} else {
			
			for (int i = 0; i < numIntFields; i++) {
				l.add(t.intFields[i]);
			}
		}
		
		return l;
	}
	
	//if k is in the tree removed the node with key k from the tree
	//otherwise do nothing
	public void remove(int k) throws IOException {
		root = remove (k, root);
	}
	
	// private recursive remove method. 
	private long remove(int k, long r) throws IOException {
		
		// Item not found at leaf; do nothing
        if ( r == 0 ) {
        	
            return r;

        }
        
        Node t = new Node(r);
        
        
        if ( k < t.key ) {
        	
            t.left = remove( k, t.left );
            t.writeNode(r);
            
        }
        else if ( k > t.key ) {
        	
            t.right = remove( k, t.right );
            t.writeNode(r);
            
        } else if ( t.left != 0 && t.right != 0 ) {
        	
            // System.out.println("Here!");
        	Node t1;
            t1 = findMin( t.right );
            t.key = t1.key;
            t.stringFields = t1.stringFields;
            t.intFields = t1.intFields;
        	// t = new Node (r);
            // t.writeNode(r);
            t.right = remove( t.key, t.right );
            t.writeNode(r);
            
        } else if ( t.left != 0 ) {
        	
            r = t.left;
            t = new Node (r);
            t.writeNode(r);
            
        } else {
        	
            r = t.right;
            t = new Node (r);
            t.writeNode(r);
            
        }
        
        // After deletion re-balance the tree if necessary.
        if ( r != 0 ) {
        	
            r = rebalance( r );
            t = new Node (r);
            t.height = Math.max( height( t.left ), height( t.right ) ) + 1;
            t.writeNode(r);
            
        }
        
        return r;

	}
	
	// private helper method to rebalance the tree following a remove. 
	private long rebalance(long r) throws IOException {
		
        Node t = new Node (r);
        
		// over-large on left
        if ( height( t.left ) - height( t.right ) == 2 ) {
        	
            // 2 cases: Left-Left and Left-Right imbalance
        	Node t1 = new Node (t.left);
        	
            if ( height( t1.left ) >= height( t1.right ) ) {
            	
                r = rotateWithLeftChild( r );
                
            }
            else {
            	
                r = doubleWithLeftChild( r );
                
            }
            
        }
        
        // over-large on right
        if ( height( t.right ) - height( t.left ) == 2 ) {
        	
            // 2 cases: Right-Right and Right-Left imbalance
        	Node t1 = new Node (t.right);
        	
            if ( height( t1.right ) >= height( t1.left ) ) {
            	
                r = rotateWithRightChild( r );
                
            } else {
            	
                r = doubleWithRightChild( r );
                
            }
            
        }
        
        return r;
        
	}
		
	// private helped method to get the minimum key Node in the tree. 
	private Node findMin(long r) throws IOException {
		Node t = new Node (r);
		
		while (t.left != 0) {
			
			t = new Node (r);
			r = t.left;
			
		}

		return t;
		
	}
	
	//update root and free in the file (if necessary)
	//close the random access file
	public void close() throws IOException {
		f.seek(0);
		f.writeLong(root);
		f.writeLong(free);
		f.close();
	}
}
