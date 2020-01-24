/*
 * Author: Tyler Kirchner
 * Date: 12/6/2019
 * Class: CS340
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class BTree {
	
	private RandomAccessFile f;
	private int order;
	private int blockSize;
	private long root;
	private long free;
	
	//add more if needed
	int mid;
	Stack<BTreeNode> paths;
	static int stopper = 0;
	
	
	private class BTreeNode {
		private int count;
		private int keys[];
		private long children[];
		private long address; //the address of the node in the file
		
		//constructors and other method
		
		private BTreeNode(int c, int k[], long childs[], long addr) {
			count = c;
			keys = k;
			children = childs;
			this.address = addr;
		}
		
		
		private BTreeNode(long addr) {
			 if(addr != 0) {
				 keys = new int[order-1];
				 children = new long[order];
				 try {
					 f.seek(addr);
					 this.address = addr;
					 count = f.readInt();
					 for (int i = 0; i < order-1; i++) {
						 keys[i] = f.readInt();
					 }
			       
					 for (int i = 0; i < order; i++) {
						 children[i] = f.readLong();
					 }
			       
				 }  catch (IOException e) {
					 ////System.out.println("No I did!");
					 e.printStackTrace();
				 }
			 }	
		}
		
		
		private void writeNode() {
			try {
				f.seek(address);
		           
		        f.writeInt(count);
		        //Write out the keys
		        for(int i = 0; i < order-1; i++) {
		            f.writeInt(keys[i]);
		        }
		        //Write out the children
		        for(int i = 0; i < order; i++) {
		        	f.writeLong(children[i]);
		        }
			} catch (IOException e) {
		    	e.printStackTrace();
		    }
		}
		
		
		private BTreeNode copy() {
			return new BTreeNode(count, Arrays.copyOf(keys, keys.length), Arrays.copyOf(children,  children.length), address);
		}
		
		
		private boolean isLeaf() {
			return count < 0;
		}
		
		
		private boolean hasRoom() {
			return Math.abs(count) < order - 1;
		}
		
		
		private boolean isTooSmall() {
			if(this.address == root) {
				return count < 1;
			}
			return Math.abs(count) < minKeys();
		}
		
		
		private int minKeys() {
			return (int)(Math.ceil(order/2));
		}
		
		
		private boolean mergingRoot() {
			return address == root && count == 1;
		}
		
		
		private long borrowFrom(BTreeNode unchanged, int key) {
		    //Look for key. Look at k-1 and k+1. If count > min children then return true
		    int i = 0;
		    for(i = 0; i < Math.abs(count); i++) {
		        if (key < keys[i]) {
		        	break;
		        }
		    }
		       
		    /* Status Meaning
		     * -n : Left Neighbor
		     *  0 : Can't Borrow
		     *  n : Right Neighbor
		     */
		    long status = 0;

		    //We borrow from the right
		    if(i != Math.abs(count) && Math.abs(new BTreeNode(children[i + 1]).count) > minKeys()) {
		        status = children[i + 1];
		    } 
		    //Corner case where i
		    if(i != 0 && Math.abs(new BTreeNode(children[i-1]).count) > minKeys()) {
		    	status = children[i - 1]*-1;
		    }
		       
		    //When left and right can both be borrowed from, we'll choose the left neighbor
		    return status;
		}
		
		
		private boolean canBorrow(BTreeNode child, int key) {
			return borrowFrom(child, key) != 0;
		}
		
		
		private int findKeyIndex(int k) {
			for(int i = 0; i < Math.abs(count); i++) {
				if(k == keys[i]) {
					return i;
				}
			}
			return -1;
		}
		
		
		public String toString() {
			String str = Long.toString(address);
			str += " : " + count + " |";
			for (int key : keys) {
				str += " " + key;
			}
			str += " |";
			for (long children : children) {
				str += " " + children;
			}
			return str;
		}
		
		
		private void insertEntry(int k, long addr) {
			//add to count
			if(isLeaf()) {
				count--;
			} else {
				count++;
			}
			//n is our count
			int n = Math.abs(count);
			
			keys[n-1] = k;
			
			//if not a leaf, put the children at the last place in children
			if(!isLeaf()) {
				children[n] = addr;
			} else {
				children[n-1] = addr;
			}
			
			int max = keys[n-2] > k ? keys[n-2] : k;
			long[] hash = new long[++max];
			
			//save a table of all the connected children addrs
			if(!isLeaf()) {
				for(int i = 0; i < n; i++) {
					hash[keys[i]] = children[i + 1];
				}
			} else {
				for(int i = 0; i < n; i++) {
					hash[keys[i]] = children[i];
				}
			}
			
			for(int i = 1; i < n; ++i) {
				int key = keys[i];
				int j = i - 1;
				
				//shift values in array
				while(j >= 0 && keys[j] > key) {
					keys[j+1] = keys[j];
					j--;
				}
				
				keys[j+1] = key;
			}
			if(!isLeaf()) {
				//remake children array
				for(int i = 0; i < n; i++) {
					children[i+1] = hash[keys[i]];
				}
			} else {
				for(int i = 0; i < n; i++) {
					children[i] = hash[keys[i]];
				}
			}
		}
		
		
		private long removeEntry(int key) {
			int k = !isLeaf() ? 1 : 0;
		    int j = findKeyIndex(key) ;//+ k;
		    long retVal = children[j];
		    for(j = findKeyIndex(key); j < order-2; j++) {
		    	keys[j] = keys[j+1];
		        children[j+k] = children[j+1+k];
		    }
		      
		    if(isLeaf()) {
		    	count++;
		    } else {
		        count--;
		    }
		    return retVal;
		}
		
		
		private long removeEntry(int key, int childCffSet) {
		    int j = findKeyIndex(key) ;
		    long retVal = children[j];
		    for(j = findKeyIndex(key); j < order-2; j++) {
		        keys[j] = keys[j+1];
		        children[j+childCffSet] = children[j+1+childCffSet];
		    }
		       
		    if(isLeaf()) {
		    	count++;
		    } else {
		        count--;
		    }
		    return retVal;
		}

		
		private BTreeNode split(int key, long addr) {
		    //Make a new temp node that is 1 bigger than normal nodes
		       
			BTreeNode splitNode = new BTreeNode(count, Arrays.copyOf(keys, order),Arrays.copyOf(children, order+1), this.address);
		       
		    if(key != -1 && addr != -1) {
		        //Add the new entry to it
		    	splitNode.insertEntry(key,addr);
		    }
		    //Determine the count of the new node. ??? Maybe use count field instead?
		    int newCount = (int) Math.ceil((double)splitNode.keys.length/2);
		      
		    //split   the values  (including  k)  between node  and the newnode
		    //Update the caller //splitnode.count - newCount
		    int callerNewCount = Math.abs(count) + 1 - newCount;
		    if(isLeaf()) {
		        count = callerNewCount*-1;
		    } else {
		        count = callerNewCount;  
		    }
		    //Link will be copied into the last child addr for leaves so we can keep the linked list behavior
		    long link = children[order-1];
		      
		    //Get the new values for the caller. arr[0] to arr[count]
		    keys = Arrays.copyOf(Arrays.copyOfRange(splitNode.keys, 0, Math.abs(count)),order-1);
		    children = Arrays.copyOf(Arrays.copyOfRange(splitNode.children, 0, Math.abs(count)+1),order);
		       
		    //Get the new values for the new node. Starting at the middle until the end.
		    int[] keyArr = Arrays.copyOf(Arrays.copyOfRange(splitNode.keys, newCount-1, splitNode.keys.length),order-1);
		    long[] childrenArr = Arrays.copyOf(Arrays.copyOfRange(splitNode.children, newCount-1, splitNode.children.length),order);
		       
		    if(order % 2 == 0 || !splitNode.isLeaf()) {
		        mid = keyArr[0];
		        //This will copy the array starting 1 place over to the right
		        keyArr = Arrays.copyOf(Arrays.copyOfRange(splitNode.keys, newCount, splitNode.keys.length),order-1);
		        childrenArr = Arrays.copyOf(Arrays.copyOfRange(splitNode.children, newCount, splitNode.children.length),order);
		        if(order % 2 == 1) {
		        	newCount--;
		        }
		            
		    }
		       
		    if(splitNode.isLeaf()) {
		    	newCount*=-1;
		    } 
		       
		    //Maintain LinkedList and add a new node
		    childrenArr[order-1] = link;
		    BTreeNode newnode = new BTreeNode(newCount,keyArr,childrenArr,getFree());
		    children[order-1] = newnode.address;
		       
		    return newnode;
		}
		
		
		private void borrow(BTreeNode r, BTreeNode unchanged, int key) {
	        long status = r.borrowFrom(unchanged,key);
	        BTreeNode neighbor = null;
	        int splitCount = 0;
	        //Borrow from left
	        if(status < 0) {
	            ////System.out.println("Borrowing from the left at " + status*-1);
	            neighbor = new BTreeNode(status*-1);
	            splitCount = (int) Math.ceil((double)(Math.abs(neighbor.count)-minKeys())/2);
	            int i = 0;
	            int count = Math.abs(neighbor.count);
	            if(!isLeaf()) {
	                splitCount = 1;
	                for(int k = Math.abs(unchanged.count); k >= 0; k--) {
	                    keys[k+1] = keys[k];
	                    children[k+1] = children[k];
	                }
	                keys[0] = r.keys[r.count-1];
	                children[0] = neighbor.children[neighbor.count];
	                this.count++;
	               r.keys[r.count-1] = neighbor.keys[neighbor.count-1];
	                //Remove the entry from my neighbor
	                for(i = count - 1; i > count - splitCount - 1; i--) {
	                    neighbor.removeEntry(neighbor.keys[i]);
	                }
	            } else {
	                
	                for(i = count - 1; i > count - splitCount - 1; i--) {
	                    insertEntry(neighbor.keys[i],neighbor.children[i]);
	                }
	                for(i = count - 1; i > count - splitCount - 1; i--) {
	                    neighbor.removeEntry(neighbor.keys[i]);
	                }
	            }
	            if(isLeaf()) {
	                //Maintain parent
	                for(i = 0; i < Math.abs(r.count); i++) {
	                    if(unchanged.keys[0] < r.keys[i]) {
	                        ////System.out.println("Welp, " + r.keys[i]);
	                        break;
	                    }
	                }
	                r.keys[i-1] = keys[0];
	            }
	            
	        //Borrow from right
	        } else {
	           neighbor = new BTreeNode(status);
	           splitCount = (int) Math.ceil((double)(Math.abs(neighbor.count)-minKeys())/2.0);
	           
	           //If its a leaf take the immediant child
	           //Its its a nonleaf take the next child
	           for(int i = 0; i < splitCount; i++) {
	               insertEntry(neighbor.keys[i],neighbor.children[i]);
	           }
	           for(int i = 0; i < splitCount; i++) {
	               neighbor.removeEntry(neighbor.keys[i]);
	           }
	           
	           if(isLeaf()) {
	               //Maintain Parent
	               int i = 0;
	               for(i = 0; i < Math.abs(r.count); i++) {
	                   if(neighbor.keys[0] < r.keys[i]) {
	                       ////System.out.println("Welp, " + r.keys[i]);
	                       break;
	                   }
	               }
	               if(i==0) {
	                  r.keys[r.count-1] = neighbor.keys[0]; 
	               }else {
	                   r.keys[i-1] = neighbor.keys[0];
	               }
	           }
	        }
	        /*
	         * [X] Maintain Parent
	         * [ ] Non-Leaf Delete
	         * [X] Leaf Delete 
	         */
	        r.writeNode();
	        neighbor.writeNode();
	        writeNode();
	        
	    }
		
		
		private void combine(BTreeNode child) {
			/* 
			 * 1.) Find Neighbor
	         * 2.) Remove Parent Entry
	         * 3.) Loop insert the child into neighbor
	         * 4.) addToFree(child)
	         * */
			//System.out.println(child.toString());
			int i = 0;
	        //Find the index in the parent of the child
	        for(i = 0; i < Math.abs(count); i++) {
	            if (child.address == children[i]) {
	                break;
	            }
	        }
	        BTreeNode neighbor = null;
	        
	        if(mergingRoot()) {
	        	BTreeNode merge = new BTreeNode(children[i]);
	        	long childAddr = 0;
	        	if(i == 0) {
	        		childAddr = merge.children[1];
	        	} else {
	        		childAddr = merge.children[0];
	               
	        	}
	        	child.insertEntry(keys[0], childAddr);
	        }
	       
	        //Handle borrowing right
	        if(i == 0) {
	        	removeEntry(keys[0],0);
	        } else {
	        	removeEntry(keys[i-1]);
	        }
	        
	        //Remove the key from the parent
	        if(i == 0) {
	            i = 2;
	        }
	         neighbor = new BTreeNode(children[i - 1]);
	        
	        for(int j = 0; j < Math.abs(child.count); j++) {
	            neighbor.insertEntry(child.keys[j], child.children[j]);
	        }
	        
	        //Link up leafs to sibling
	        if(!child.isLeaf()) {
	            neighbor.children[order-1] = child.children[child.count];
	        } else {
	            neighbor.children[order-1] = child.children[order-1];
	        }
	        
	        addFree(child.address);
	        writeNode();
	        child.writeNode();
	        neighbor.writeNode();
	    }

	}
	
	
	public BTree(String filename, int bsize) {
		
		
		try {
			File path = new File(filename);
	        if (path.exists()) {
	            path.delete();
	        } 
	        f = new RandomAccessFile(path,"rw");
	        //Set everything up
	        root = 0;
	        free = 0;
	        blockSize = bsize; 
	        order = bsize / 12;
	        paths = new Stack<>();
	        //Write everything out
	        f.seek(0);
	        f.writeLong(root);
	        f.writeLong(free);
	        f.writeInt(blockSize);
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	
	
	public BTree(String filename) {
		//open an existing B+Tree
		try {
            f = new RandomAccessFile(filename,"rw");
            f.seek(0);
            root = f.readLong();
            free = f.readLong();
            blockSize = f.readInt();
            order = blockSize/12;
            paths = new Stack<>();
        } catch(IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public boolean insert(int key, long addr) {
		if (root == 0) {
            ////System.out.println("Inserted a new root!");
            insertRoot(key,addr);
            return true;
        }
        boolean inTable = 0 != search(key);
        boolean split = false;
        if(!inTable) {
            long loc = 0;
            int val = 0;
            
            //Do Insert
            BTreeNode r = paths.pop();
            
            //If there is room in node for new value: M-1 children Go BACK
            if (r.hasRoom()) {
                //Insert k into the node
                r.insertEntry(key,addr);
                
                //Write node to the file
                r.writeNode();
            } else {
                BTreeNode newnode = r.split(key,addr);

                //let val be  the smallest value in the newnode
                val = newnode.keys[0];
                //write node to the file (into the same location where is was previously located)  
                //write newnode into the file   
                r.writeNode();
                newnode.writeNode();
                //let location be the address in  the file of newnode
                loc = newnode.address;
                //set split   to  true 
                split = true;
            }
            
            while (!paths.empty() && split) {
                BTreeNode node = paths.pop();
                if(node.hasRoom()) {
                  //  insert  val and loc into    node   
                    node.insertEntry(val, loc);
//                  write node to  the file    (into   the same    location where   is  was previously  located)    
                    node.writeNode();
//                  set split to false 
                    split = false;
                } else {
                    BTreeNode newnode = node.split(val, loc);
                    val = mid;
                    loc = newnode.address;

                    node.writeNode();
                    newnode.writeNode();
                   
                    split = true;
                }
            }
            if (split) { 
                //BTreeNode rootNode = new BTreeNode(root);
                int[] keys = new int[order-1];
                long[] children = new long[order];
                keys[0] = val;
                children[0] = root;
                children[1] = loc;
                
                //Create a new root 
                BTreeNode newnode = new BTreeNode(1,keys,children,getFree());
                root = newnode.address;
                
                //Our right most child goes nuts
                
                newnode.writeNode();
            }
      }
        return !inTable;
		
	}
	
	
	public long remove(int key) {
		boolean tooSmall = false;
        if(0 == search(key)) {
            return 0;
        } 
        
        BTreeNode r = paths.pop();
        BTreeNode unchanged = r.copy();

       long retVal = r.removeEntry(key);
        r.writeNode();
        
        tooSmall = r.isTooSmall(); 
        while (!paths.empty() && tooSmall) {
            BTreeNode child = r; 
            unchanged = r.copy();
            r = paths.pop(); //the parent of child 

            //check the neighbors of child; the immediate left and right
            if(r.canBorrow(unchanged,key)) {
                  ////System.out.println("Borrowing with " + key);
                //shift values between the children and adjust the key 
                //in  node that is between the nodes involved in the borrowing  
                child.borrow(r,unchanged,key);
                tooSmall = false;
            } else {

               r.combine(child);
               
                //Check if r is too small now
                tooSmall = r.isTooSmall();
            }
            
        }   
        if(tooSmall) { //this mean the root is now empty
           // set the root to the leftmost child of the empty root and
           // free the space used by the old root
            root = r.children[0];
        }
        
        return retVal;
		
	}
	
	
	public long search(int k) {
		/* 
	       This is an equality search 
	       If the key is found return the address of the row with the key 
	       otherwise return 0  
	    */ 
		BTreeNode r = searchToLeaf(k);
	    if(root == 0) {	
	    	return 0;
	    }
	    long addr = 0;
	        
        //If leaf. Look at all the contents of the node.
	    int i = r.findKeyIndex(k);
	        
	    if(i != -1) {
	        addr = r.children[i];
	    }
	                
	    return addr;		
		
	}
	
	
	public LinkedList<Long> rangeSearch(int low, int high){
	    /* 
	       
	       return a list of row addresses for all keys in the range low to high inclusive 
	       return an empty list when no keys are in the range 
	    */ 
	        
	    LinkedList<Long> list = new LinkedList<>();
	    BTreeNode r = searchToLeaf(low);
	    int i = 0;
	    while (r.keys[i] <= high && r.keys[i] >= low) {
	    	list.add(r.children[i]);
	       	i++;
	       	if (i == r.count*-1) {
	       		if(r.children[order-1] == 0) {
	       			//We are at the end of the leaves
	       			break;
	       		}
	       		//Look to the next leaf
	       		r = new BTreeNode(r.children[order-1]);
	       		i = 0;
	       	}
	    }
	    return list;
		
	}
	
	
	public void print() {
		//print the B+Tree to standard output
		//print one node per line
		//this method can be helpful for debugging
		try {
            f.seek(20);
            //System.out.println("ROOT: " + root);
            //System.out.println("FREE: " + free);
            while(f.getFilePointer() < f.length()) {
                BTreeNode n = new BTreeNode(f.getFilePointer());
                System.out.println(n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public void close() {
		//close the B+Tree. The tree should not be accessed after close is called
		try {
            f.seek(0);
            f.writeLong(root);
            f.writeLong(free);
            f.writeInt(blockSize);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	private void insertRoot(int key, long addr) {
        int[] keys = new int[order-1];
        long[] children = new long[order];
        keys[0] = key;
        children[0] = addr;
        BTreeNode r = new BTreeNode(-1,keys,children,getFree());
        r.writeNode();
        root = r.address;
    }
	
	
	private BTreeNode searchToLeaf(int k) {
        int i = 0;
        paths = new Stack<>();
        BTreeNode r = new BTreeNode(root);
        paths.push(r);
        if(root == 0) {
            return r;
        }
        //Logic to follow search path and bring me to a leaf
        while(!r.isLeaf()) {
            for(i = 0; i <= Math.abs(r.count); i++) {
              //k is larger than everything else, look at last node
                if (i == Math.abs(r.count)) {
                    r = new BTreeNode(r.children[i]);
                    paths.push(r);
                    break;
                //Will look at the first node that is greater than k
                } else if (k < r.keys[i]) {
                    r = new BTreeNode(r.children[i]);
                    paths.push(r);
                    break;
                }  
            }
        }
        return r;
    }
	
	
	private long getFree() {
        long addr = 0;
        //When at the end of free, write to the end of file.
        try {
            if (free == 0) {
                addr = f.length();
            } else {
                //New address is where free is pointing
                addr = free;
                //Move free value to next in list.
                f.seek(free);
                free = f.readLong();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addr;
    }
	
	
	private void addFree(long addr){
        try {
          //Seek to position to write to
            f.seek(addr);
            //Write out old value of free
            f.writeLong(free);
            //Set free to new value
            free = addr;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
