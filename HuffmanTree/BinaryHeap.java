import java.util.*;

public class BinaryHeap <T1 extends Comparable <? super T1>, T2 extends Object> {
	//implements a binary heap where the heap rule is the value
	//of the key in the parent node is less than
	//or equal to the values of the keys in the child nodes
	
	//the implementation uses parallel arrays to store the priorities and the trees
	//you must use this implementation
	
	ArrayList< ? > pq;  //decide what goes in the ??????
	
	public Binary Heap() {
		
	}
	
	
	public void removeMin() {
		//PRE: size != 0
		//removes the item at the root of the heap
		
	}
	
	
	public T1 getMinKey() {
		//PRE: size != 0
		//return the priority (key) in the root of the heap
		
	}
	
	
	public T2 getMinOther() {
		//PRE: size != 0
		//return the other data in the root of the heap
		
	}
	
	
	public void insert(T1 k, T2 t) {
		//insert the priority k and the associated data into the heap
		
	}
	
	
	public int getSize() {
		//return the number of values (key, other) pairs in the heap
		
	}
}
