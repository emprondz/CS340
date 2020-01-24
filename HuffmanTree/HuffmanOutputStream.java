import java.io.*;


public class HuffmanOutputStream {

	DataOutputStream d;
	
	private int currentByte;
	private int bitCount;
	
	public HuffmanOutputStream(String filename, String tree, int totalChars) {
		
		try {
			d = new DataOutputStream(new FileOutputStream(filename));
			d.writeUTF(tree);
			d.writeInt(totalChars);
		} catch (IOException e) {
			
		}
		
		currentByte = 0;
		bitCount = 0;
	}
	
	
	public void writeBit(char bit) {
		//PRE: bit == '0' || bit == '1'
		
		currentByte += bit << bitCount;
		bitCount++;
		
		//We have 8 bits so flush oput
		if(bitCount == 8) {
			flush();
		}
		
	}
	
	
	private void flush() {
		//Write out the byte
		//should pad with 0's even if not full
		try {
			d.write(currentByte);
		} catch (IOException e) {
			
		}
		//reset the count
		currentByte = 0;
		bitCount = 0;
	}
	
	
	public void close() {
		//write final byte (if needed)
		//close the DataOutputStream
		
		if(bitCount > 0) 
			flush();
		
		try {
			d.close();
		} catch (IOException e) {
			
		}
	}
	
}
