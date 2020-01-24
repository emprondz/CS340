/*
 * BY: Tyler Kirchner
 * Homework 1 CS 340
 * 
 * Program will create a sorted list with 2 values. 
 * Value one will be sorted by ascending order and value
 * one will be in descending order.
 * 
 */

import java.io.*;
import java.util.*;


public class SortedLists<T1 extends Comparable <? super T1>, T2 extends Comparable <? super T2>> {

	
	
	private class Node {
		
		
		private T1 data1;
		private T2 data2;
		private Node nextT1;
		private Node nextT2;
		private Node prevT1;
		private Node prevT2;
		
		private Node(T1 d1, T2 d2, Node n1, Node n2, Node p1, Node p2) {
			data1 = d1;
			data2 = d2;
			nextT1 = n1;
			nextT2 = n2;
			prevT1 = p1;
			prevT2 = p2;
		}
	}
	
	private Node head1;
	private Node head2;
	
	
	public SortedLists() {
		//no sentinal nodes
		head1 = null;
		head2 = null;
	}
	
	
	//inserts a node into the sorted lists
	public void insert(T1 d1, T2 d2) {
		//checks if the list is empty
		Node newNode = new Node(d1, d2, null, null, null, null);  
		
		if(head1 == null && head2 == null) {
			head1 = newNode;
			head2 = newNode;
		} 
		
		addT1(newNode);
		addT2(newNode);
	}
	
	
	//helper method to insert, will add the node to the T1 list
	public void addT1(Node node) {
		
		Node currNode = head1;
	
		if(currNode.data1.compareTo(node.data1) > 0 ) {
			currNode.prevT1 = node;
			node.nextT1 = currNode;
			head1 = node;
			return;
		}
		
		while(currNode.nextT1 != null && currNode.nextT1.data1.compareTo(node.data1) < 0) {
			currNode = currNode.nextT1;
		}
		

		//insert first node links here
		if(currNode.nextT1 == null) {
			currNode.nextT1 = node;
			node.prevT1 = currNode;
			node.nextT1 = null;
		} else {
			node.nextT1 = currNode.nextT1;
			currNode.nextT1 = node;
			node.prevT1 = currNode;
			currNode.nextT1.prevT1 = node;
		}
		
		
	}
	
	
	//helper method to insert, will add node to the T2 list
	public void addT2(Node newNode) {
		Node currNode = head2;
		
		if(currNode.data2.compareTo(newNode.data2) < 0 ) {
			currNode.prevT2 = newNode;
			newNode.nextT2 = currNode;
			head2 = newNode;
			return;
		}
		
		while(currNode.nextT2 != null && currNode.nextT2.data2.compareTo(newNode.data2) > 0) {   
			currNode = currNode.nextT2;
		}
		
		//insert second set node links here	
		if(currNode.nextT2 == null) {
			currNode.nextT2 = newNode;
			newNode.prevT2 = currNode;
			newNode.nextT2 = null;
		} else {
			newNode.nextT2 = currNode.nextT2;
			currNode.nextT2 = newNode;
			newNode.prevT2 = currNode;
			currNode.nextT2.prevT2 = newNode;
		}
	}
			
	
	//removes any node from the lists that has the the values d1 and d2 in it.
	public void remove(T1 d1, T2 d2) {
		if(head1 == null || head2 == null) {
			return;
		}
		
		Node currNode  = head1;
		
			
		while(currNode != null) {
			if(currNode.data1.equals(d1) && currNode.data2.equals(d2)) {
				//remove from T1 list
				
				if(currNode.nextT1 == null) {
					currNode.prevT1.nextT1 = null;
					
				}else if(currNode == head1) {
					head1 = currNode.nextT1;
					currNode.nextT1.prevT1 = null;
					
				} else {
					currNode.prevT1.nextT1 = currNode.nextT1;
					currNode.nextT1.prevT1 = currNode.prevT1;
					
				}
				
				//remove from T2 list
				if(currNode.nextT2 == null) {
					currNode.prevT2.nextT2 = null;
					
				}else if(currNode == head2) {
					head2 = currNode.nextT2;
					currNode.nextT2.prevT2 = null;
					
				}else {
					currNode.prevT2.nextT2 = currNode.nextT2;
					currNode.nextT2.prevT2 = currNode.prevT2;
				}
				
			}
			currNode = currNode.nextT1;
		}
	
	}
	
	
	//finds all the T2s that have the value d1 in it and puts it into a linked list
	public LinkedList<T2> findT2s(T1 d1) {
		LinkedList<T2> list = new LinkedList<>();
		
		if (head1 == null) {
			return list;
		}
		Node currNode = head1;
		if (currNode.data1.compareTo(d1) == 0) {
			list.add(currNode.data2);
		}
		
		while(currNode.nextT1 != null) {
			currNode = currNode.nextT1;
			if (currNode.data1.compareTo(d1) == 0) {
				list.add(currNode.data2);
			}
		}

		return list;
	}
	
	
	//same as above, but for T1s to d2s
	public LinkedList<T1> findT1s(T2 d2) {
		LinkedList<T1> list = new LinkedList<>();
		
		if (head2 == null) {
			return list;
		}
		Node currNode = head2;
		if (currNode.data2.compareTo(d2) == 0) {
			list.add(currNode.data1);
		}
		
		while(currNode.nextT2 != null) {
			currNode = currNode.nextT2;
			if (currNode.data2.compareTo(d2) == 0) {
				list.add(currNode.data1);
			}
		}

		return list;
	}
	
	
	
	//Iterator class that can move through the T1 list
	public class T1Iterator implements Iterator<String> {
		Node currNode;
		
		
		public T1Iterator() {
			currNode = head1;
		}
		
		public boolean hasNext() {
			return currNode != null;
		}
		
		public String next() {
			if(!hasNext()) {
				return "";
			} else {
				String y = "(" + currNode.data1 + ", " + currNode.data2 + ")";
				currNode = currNode.nextT1;  
				return y;
			}
		}
		
		public void remove() {
			if(currNode.nextT1 == null) {
				currNode.prevT1.nextT1 = null;
				return;
			}
			if(currNode == head1) {
				head1 = currNode.nextT1;
				currNode.nextT1.prevT1 = null;
				return;
			}
			currNode.prevT1.nextT1 = currNode.nextT1;
			currNode.nextT1.prevT1 = currNode.prevT1;
			return;
		}
	}
	
	public Iterator<String> T1_Order() {
		Iterator<String> I1 = new T1Iterator();
		return I1;
	}
	
	
	//Iterator that can move through the T2 list
	public class T2Iterator implements Iterator<String> {
		Node currNode;
		
		public T2Iterator() {
			currNode = head2;
		}
		
		public boolean hasNext() {
			return currNode != null;
		}
		
		public String next() {
			if(!hasNext()) {
				return "";
			} else {
				String y = "(" + currNode.data1 + ", " + currNode.data2 + ")";
				currNode = currNode.nextT2;
				return y;  
			}
		}
		
		public void remove() {
			if(currNode.nextT2 == null) {
				currNode.prevT2.nextT2 = null;
				return;
			}
			if(currNode == head2) {
				head2 = currNode.nextT2;
				currNode.nextT1.prevT2 = null;
				return;
			}
			currNode.prevT2.nextT2 = currNode.nextT2;
			currNode.nextT1.prevT2 = currNode.prevT2;
			return;
		
		}
	}
	
	public Iterator<String> T2_Order() {
		Iterator<String> I2 = new T2Iterator();
		return I2;
	}
}