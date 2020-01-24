import java.io.*;

public class HuffmanInputStream {
	//add additional private variables and methods as needed
	//DO NOT modify the public method signatures or add public methods
	private String tree;
	private int totalChars;
	private DataInputStream d;
	
	private int currentByte;
	private int bitCount;
	
	
	public HuffmanInputStream(String filename) {
		
		try {
			d = new DataInputStream(new FileInputStream(filename));
			tree = d.readUTF();
			//System.out.print(tree);
			totalChars = d.readInt();
		} catch (IOException e) {
			
		}
		//add other initialization statements as needed
		
		currentByte = 0;
		bitCount = 0;
		getByte();
	}
	
	
	public int readBit() {
		//returns the next bit is the file
		//the value returned will be either a 0 or a 1
		
		if(currentByte == -1) {
			close();
			return -1;
		}
		
		//get da bits
		int bit = currentByte % 2;
		currentByte /= 2;
		
		bitCount++;
		if(bitCount == 8) {
			getByte();
		}
		
		return bit;
	}
	
	
	private void getByte() {
		try {
			currentByte = d.readUnsignedByte(); 
		} catch (IOException e) {
			
		}
		
		bitCount = 0;
	}
	
	public String getTree() {
		//return the tree representation read from the file
		
		return tree;
	}
	
	
	public int getTotalChars() {
		//return the character count read from the file
		
		return totalChars;
	}
	
	
	public void close() {
		try {
			d.close();
		} catch (IOException e) {
			
		}
	}
	
	
	
}
