/*
 * Author: Tyler Kirchner
 * Date: 12/6/2019
 * Class: CS340
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class DBTable {
	
	private RandomAccessFile rows; //the file that stores the rows in the table
	private long free; //head of the free list space for rows
	private int numOtherFields;
	private int otherFieldLengths[];
	
	//add other stuff if needed
	BTree tree;
	
	private class Row {
		private int keyField;
		private char otherFields[][];
		
		//added
		private long addr;
		
		private Row(int key, char field[][], long addr) {
			keyField = key;
			otherFields = field;
			this.addr = addr;
		}
		
		
		private Row(long addr) {
			try {
				rows.seek(addr);
				this.addr = addr;
				keyField = rows.readInt();
				otherFields = new char[numOtherFields][];
				for(int i = 0; i < numOtherFields; i++) {
					otherFields[i] = new char[otherFieldLengths[i]];
					for(int j = 0; j < otherFields[i].length; j++) {
						otherFields[i][j] = rows.readChar();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		private void writeRow() {
			try {
				rows.seek(addr);;
				rows.writeInt(keyField);;
				for(int i = 0; i < numOtherFields; i++) {
					for(int j = 0; j < otherFieldLengths[i]; j++) {
						rows.writeChar(otherFields[i][j]);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		public String toString() {
			String str = Integer.toString(keyField) + " ";
			for(int i = 0; i < numOtherFields; i++) {
				for(int j = 0; j < otherFieldLengths[i]; j++) {
					try {
						char temp = rows.readChar();
						if(temp == '\0') {
							//Nada
						}
						str += temp;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				str += " ";
			}
			
			return str;
		}
	}
	
	
	public DBTable(String filename, int fL[], int bsize) {
		
		try {
			File path = new File(filename);
			File treePath = new File(filename + "_tree");
			if(path.exists()) {
				path.delete();
				treePath.delete();
			}
			rows = new RandomAccessFile(path, "rw");
			rows.seek(0);
			numOtherFields = fL.length;
			
			rows.writeInt(numOtherFields);;
			otherFieldLengths = new int[numOtherFields];
			for(int i = 0; i < numOtherFields; i++) {
				otherFieldLengths[i] = fL[i];
				rows.writeInt(fL[i]);
			}
			tree = new BTree(filename + "_tree", bsize);
			free = 0;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public DBTable(String filename) {
		//Use this constructor to open an existing DBTable
		
		try {
			rows = new RandomAccessFile(filename, "rw");
			rows.seek(0);
			
			numOtherFields = rows.readInt();
			otherFieldLengths = new int[numOtherFields];
			
			for(int i = 0; i < numOtherFields; i++) {
				otherFieldLengths[i] = rows.readInt();
			}
			free = rows.readLong();
			tree = new BTree(filename + "_tree");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public boolean insert(int key, char fields[][]) {

		if(tree.search(key) != 0) {
			return false;
		}
		
		long newAddr = getFree();
		tree.insert(key,  newAddr);
		
		Row r = new Row(key, fields, newAddr);
		r.writeRow();
		return true;
		
	}
	
	
	private long getFree() {
		long addr = 0;
		//when you at the end of the free list you should write to the 
		//end of the file
		
		try {
			if(free == 0) {
				addr = rows.length();
			} else {
				//New address is where free is pointing to
				addr = free;
				rows.seek(free);
				free = rows.readLong();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return addr;
	}
	
	
	public boolean remove(int key) {

		if(tree.search(key) == 0) {
			return false;
		}
		
		addFree(tree.remove(key));
		return true;
		
	}
	
	
	public LinkedList<String> search(int key){

		long dbAddr = tree.search(key);
		if(dbAddr == 0) {
			return new LinkedList<String>();
		}
		return getFields(new LinkedList<String>(), dbAddr);
		
	}
	
	
	private LinkedList<String> getFields(LinkedList<String> list, long dbAddr){
		Row r = new Row(dbAddr);
		String str = Integer.toString(r.keyField);
		list.add(str);
		for(int i = 0; i < numOtherFields; i++) {
			str = "";
			for(int j = 0; j < otherFieldLengths[i]; j++) {
				if(r.otherFields[i][j] == '\0') {
					break;
				}
				str += r.otherFields[i][j];
			}
			list.add(str);
		}
		return list;
	}
	
	
	public LinkedList<LinkedList<String>> rangeSearch(int low, int high){
	//PRE: low <= high
		
		LinkedList<LinkedList<String>> list = new LinkedList<LinkedList<String>>();
		
		for(long addr : tree.rangeSearch(low,  high)) {
			list.add(getFields(new LinkedList<>(), addr));
		}
		
		return list;
		
	}
	
	
	public void print() {
		//Print the rows to standard output is ascending order(based on the keys)
		//One row per line
		
	
		tree.print();
	
	}
	
	
	public void close() {
		//close the DBTable. The table should not be used after it is closed
		try {
			//go to the local of the free add
			rows.seek(4 * (numOtherFields + 1));;
			//System.out.println("I wrote free to " + (4 * (numOtherFields + 1)));
			rows.writeLong(free);;
			tree.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void addFree(long addr) {
		try {
			rows.seek(addr);
			rows.writeLong(free);
			free = addr;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
