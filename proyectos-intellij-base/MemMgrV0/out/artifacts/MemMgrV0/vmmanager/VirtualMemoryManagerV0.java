package vmmanager;

import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

public class VirtualMemoryManagerV0 {

	MainMemory memory;
	int numBitsToAddress;

  	// log2(): Convenient function to compute the log2 of an integer;
	private int log2(int x) {
		return (int)(Math.log(x)/Math.log(2));
	}
	boolean memContentFlag = true;

	// Constructor
	public VirtualMemoryManagerV0(MainMemory memory) throws MemoryException {

		//System.out.println(numBitsToAddress);
	}

  	// Method to write a byte to memory given a physical address
  	public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {

    }

  	// Method to write a byte to memory given a physical address
  	public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
  		// TO IMPLEMENT





        return null; // MUST RETURN THE VALUE THAT WAS READ INSTEAD OF JUST ZERO
  	}

  	// Method to print all memory content
  	public void printMemoryContent() throws MemoryException {

  	}

}
