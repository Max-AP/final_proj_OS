package vmmanager;

import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

public class VirtualMemoryManagerV0 {

    MainMemory memory;
    int numBitsToAddress;

    // log2(): Convenient function to compute the log2 of an integer;
    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    boolean memContentFlag = true;

    // Constructor
    public VirtualMemoryManagerV0(MainMemory memory) throws MemoryException {
        this.memory = memory;
        //System.out.println(numBitsToAddress);
    }

    // Method to write a byte to memory given a physical address
    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {

        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBitsToAddress - 1);
        memory.writeByte(address, value);
        System.out.println("RAM Write: @" + BitwiseToolbox.getBitString(address, log2(memory.size())-1) + " --> " + value);
    }

    // Method to write a byte to memory given a physical address
    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        // TO IMPLEMENT

        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBitsToAddress - 1);
        byte valInAddr = memory.readByte(address);
        System.out.println("RAM read: @" + BitwiseToolbox.getBitString(address, log2(memory.size())-1) + " --> " + valInAddr);
        return valInAddr; // MUST RETURN THE VALUE THAT WAS READ INSTEAD OF JUST ZERO
    }

    // Method to print all memory content
    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++){
            System.out.println(BitwiseToolbox.getBitString(i, log2(memory.size())-1) + ": " + memory.readByte(i));
        }
    }

}
