import java.io.*;
import java.util.*;

public class HuffmanEncode {

	private int totalChars;
	private PriorityQueue<Item> queue;
	
	private class Item implements Comparable<Item> {
		private int freq;
		private HuffmanTree data;
		
		private Item(int f, HuffmanTree t) {
			freq = f;
			data = t;
		}
		
		public int compareTo(Item item) {
			return this.freq - item.freq;
		}
		
		public String toString() {
			String str = "Freq: ";
			str += this.freq;
			str += " Data: ";
			str += this.data.toString();
			return str;
		}
	}
	
	
	public HuffmanEncode(String in, String out) {
		//implements the huffman encoding algorithm
		//add private methods and instance variables as needed
		
		queue = new PriorityQueue<>(128);
		totalChars = 0;
		
		//Read in the file
		readFile(in);
		buildTree();
		writeFile(in, out);
	
	}
	
	
	private void readFile(String in) {
		BufferedReader r = null;
		int[] arr = new int[128];
		
		try {
			r = new BufferedReader( new FileReader(in));

			int c = 0;
			while((c = r.read()) != -1) {
				totalChars ++;
				arr[c] ++;
				
			}
			
			
			for(int i = 0; i < arr.length; i++) {
				if(arr[i] != 0) {
					queue.add(new Item(arr[i], new HuffmanTree((char)i)));
				}
			}
		} catch (Exception e) {
			System.out.println("what");
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				
			}
		}
	}
	
	
	private void buildTree() {
		while(queue.size() > 1) {
			//pop off 2 highest items
			Item leftItem = queue.poll();
			Item rightItem = queue.poll();
			
			//merge them
			//change freq.
			Item mergedItem = new Item(leftItem.freq + rightItem.freq, new HuffmanTree(leftItem.data, rightItem.data, (char)128));
			
			queue.add(mergedItem);
		}
	}
	
	
	public void writeFile(String in, String out) {
		String[] arr = new String[128];
		
		HuffmanTree tree = queue.poll().data;
		Iterator<String> iter = tree.iterator();
		
		while(iter.hasNext()) {
			String str = iter.next();
			arr[str.charAt(0)] = str.substring(1);
		}
		
		try {
			HuffmanOutputStream writer = new HuffmanOutputStream(out, tree.toString(), totalChars);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			
			int c = 0;
			while((c = reader.read()) != -1) {
				for(int i = 0; i < arr[c].length(); i++) {
					writer.writeBit(arr[c].charAt(i));
				}
			}
			writer.close();
			reader.close(); 
			
		} catch( IOException e ) {
			
		}
	}
	
	
	public static void main(String args[]) {
		//args[0] is the name of the file whose contents should be compressed
		//args[1] is the name of the output file that will hold the compressed
		//content of the input file
		
		new HuffmanEncode("book3.txt", "book3Comp.txt");
		//do not add anything here
	}
}
