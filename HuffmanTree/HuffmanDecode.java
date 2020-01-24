import java.io.*;
import java.util.*;

public class HuffmanDecode {

	
	public HuffmanDecode(String in, String out) {
		//implements the Huffman Decode Algorithm
		//Add private methods and instance variables as needed
		try {
			convertFile(in,out);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
	
	
	private void convertFile(String in, String out) throws FileNotFoundException {
		HuffmanInputStream reader = new HuffmanInputStream(in);
		PrintWriter writer = new PrintWriter(out);
		//HuffmanOutputStream writer = new HuffmanOutputStream(out);
		
		HuffmanTree tree = new HuffmanTree(reader.getTree(), (char)128);
		int charsRead = 0;
		int bit = -1;
		
		while(charsRead != reader.getTotalChars()) {
			if(!tree.atLeaf()) {
				bit = reader.readBit();
				//System.out.println("hi");
				if(bit == 0) {
					tree.moveToLeft();
				}
				else {
					tree.moveToRight();
				}
			}
			else {
				charsRead++;
				writer.write(tree.current());
				//System.out.println(tree.current());
				tree.moveToRoot();
				//System.out.println("aye");
			}
		}
		writer.close();
		reader.close();
	}
	
	
	public static void main(String args[]) {
		//args[0] is the name of the input file (a file created by Huffman Encode)
		//args[1] is the name of the output file fo rthe uncompressed file
		
		new HuffmanDecode("book3Comp.txt", "book3UnComp.txt");
		//do not add anything here
		
	}
}
