package vmmanager;

import vmsimulation.BackingStore;
import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BooleanSupplier;

public class VirtualMemoryManagerV3 {


    MainMemory memory;    // The main memory
    BackingStore disk;    // The disk
    Integer pageSize;    // Page size

    int numBitsToAddress;
    int offsetBits; //offset in number of bits
    PageTable pageTable;
    int faults;
    // MORE INSTANCE VARIABLE NEEDED, MOST LIKELY

    // log2(): Convenient function to compute the log2 of an integer;
    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    // Constructor
    public VirtualMemoryManagerV3(MainMemory memory,
                                  BackingStore disk,
                                  Integer pageSize) throws MemoryException {
        this.memory = memory;
        this.disk = disk;
        this.pageSize = pageSize;
        offsetBits = log2(pageSize);
        pageTable = new PageTable(this.memory.size()/this.pageSize);
        faults = 0;





        // TO AUGMENT, MOST LIKELY
    }

    // Method to write a byte to memory given a virtual address
    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {
        //address in decimal form
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBitsToAddress - 1);

        //address in binary
        String virtualAddress = BitwiseToolbox.getBitString(address, log2(disk.size()-1));

        //number of bits that make the virtual Page Number
        int virtualPageNumberSize = virtualAddress.length() - offsetBits;

        //physical page number, this is the number to lookup in the table
        int physicalPageNumber = Integer.parseInt(virtualAddress.substring(0, virtualPageNumberSize), 2);

        //page offset in decimal
        int pageOffset =  Integer.parseInt(virtualAddress.substring(virtualPageNumberSize), 2);

        //check if page is in a frame already
        int frame = pageTable.lookup(physicalPageNumber);

        if (frame == -1){ // if the page is not in the table
            faults++; //add a fault
            int victim = pageTable.victimManip(); // get current victim (frame to target)
            if(!pageTable.hasSpace()){ //see if there are spaces in the table
                int pageNumber = pageTable.getPageTable().get(victim); // get page to evict
                String dirtyBitMsg = pageTable.getDirtyBit(victim) ? " (NOT DIRTY)" : "";
                System.out.println("Evicting page " + pageNumber + dirtyBitMsg);
                //write memory contents back to disk
                int memoryAddress = victim * pageSize;
                byte[] contentArray = new byte[pageSize];
                for (int content = 0; content < pageSize; content++) {
                    contentArray[content] = memory.readByte(memoryAddress++);
                }
                disk.writePage(pageNumber, contentArray);
            }
            pageTable.update(physicalPageNumber, victim, false); //add the page to the table
            frame = pageTable.lookup(physicalPageNumber) * pageSize; //update frame to represent starting memory address

            for(byte content : disk.readPage(physicalPageNumber)){
                memory.writeByte(frame++, content);
            }
        } else {
            System.out.println("Page " + physicalPageNumber + " is in memory");
            pageTable.cleanBit(pageTable.lookup(physicalPageNumber));
        }

        frame = pageTable.lookup(physicalPageNumber); // update frame

        //frame in bits
        String frameBits = BitwiseToolbox.getBitString(frame, virtualPageNumberSize-1);

        //offset in bits
        String offsetInBits = BitwiseToolbox.getBitString(pageOffset, offsetBits-1);

        //physical address in bits
        String physicalAddressBits = frameBits + offsetInBits;

        //physical address in decimal
        int physicalAddress = Integer.parseInt(physicalAddressBits, 2);

        memory.writeByte(physicalAddress, value);

        //printing a message
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddress, log2(memory.size())-1) + " <-- " + value);
    }


    // Method to read a byte to memory given a virtual address
    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        boolean readFromDisk = true;
        //address in decimal form
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBitsToAddress - 1);

        //address in binary
        String virtualAddress = BitwiseToolbox.getBitString(address, log2(disk.size()-1));

        //number of bits that make the virtual Page Number
        int virtualPageNumberSize = virtualAddress.length() - offsetBits;

        //physical page number, this is the number to lookup in the table
        int physicalPageNumber = Integer.parseInt(virtualAddress.substring(0, virtualPageNumberSize), 2);

        //page offset in decimal
        int pageOffset =  Integer.parseInt(virtualAddress.substring(virtualPageNumberSize), 2);

        //check if page is in a frame already
        int frame = pageTable.lookup(physicalPageNumber);

        if (frame == -1){ // if the page is not in the table
            faults++; //add a fault
            int victim = pageTable.victimManip(); // get current victim (frame to target)
            if(!pageTable.hasSpace()){ //see if there are spaces in the table
                int pageNumber = pageTable.getPageTable().get(victim); // get page to evict
                String dirtyBitMsg = pageTable.getDirtyBit(victim) ? " (NOT DIRTY)" : "";
                System.out.println("Evicting page " + pageNumber + dirtyBitMsg);
                //write memory contents back to disk
                int memoryAddress = victim * pageSize;
                byte[] contentArray = new byte[pageSize];
                for (int content = 0; content < pageSize; content++) {
                    contentArray[content] = memory.readByte(memoryAddress++);
                }
                disk.writePage(pageNumber, contentArray);
            }
            pageTable.update(physicalPageNumber, victim, true); //add the page to the table
            frame = pageTable.lookup(physicalPageNumber) * pageSize; //update frame to represent starting memory address

            for(byte content : disk.readPage(physicalPageNumber)){
                memory.writeByte(frame++, content);
            }

        } else {
            System.out.println("Page " + physicalPageNumber + " is in memory");
            readFromDisk = false;
        }

        frame = pageTable.lookup(physicalPageNumber);

        //frame in bits
        String frameBits = BitwiseToolbox.getBitString(frame, virtualPageNumberSize-1);

        //offset in bits
        String offsetInBits = BitwiseToolbox.getBitString(pageOffset, offsetBits-1);

        //physical address in bits
        String physicalAddressBits = frameBits + offsetInBits;

        //physical address in decimal
        int physicalAddress = Integer.parseInt(physicalAddressBits, 2);

        byte valInAddr;
        if (readFromDisk){
            //Getting the value stored in the disk
            valInAddr = disk.readPage(physicalPageNumber)[pageOffset];
        } else {
            valInAddr = memory.readByte(physicalAddress);
        }

        //printing a message
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddress, log2(memory.size())-1) + " --> " + valInAddr);
        return valInAddr; // MUST RETURN THE VALUE THAT WAS READ INSTEAD OF JUST ZERO
    }

    private MemoryState managePageTable(int pageNumber) {

        return null;
    }

    // Method to print all memory content
    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++){
            System.out.println(BitwiseToolbox.getBitString(i, log2(memory.size())-1) + ": " + memory.readByte(i));
        }
    }

    // Method to print all disk content
    public void printDiskContent() throws MemoryException {
        for (int i = 0; i < disk.size()/pageSize; i++)
        {
            System.out.print("PAGE " + i + ": ");
            for (int j = 0; j < pageSize; j++){
                System.out.print(disk.readPage(i)[j]);
                if (j == pageSize -1){
                    System.out.println();
                } else {
                    System.out.print(",");
                }
            }
        }
    }

    // Method to write back all pages to disk
    public void writeBackAllPagesToDisk() throws MemoryException {
        int memoryAddress = 0;

        for(int entry: pageTable.getPageTable()){
            byte[] contentArray = new byte[pageSize];
            for (int content = 0; content < pageSize; content++) {
                contentArray[content] = memory.readByte(memoryAddress++);
            }
            disk.writePage(entry, contentArray);
        }
    }

    // Method to retrieve the page fault count
    public int getPageFaultCount() {

        // TO IMPLEMENT
        return faults; // MUST RETURN THE NUMBER OF PAGE FAULTS INSTEAD OF JUST ZERO
    }

    // Method to retrieve the number of bytes transfered between RAM and disk
    public int getTransferedByteCount() {

        // TO IMPLEMENT
        return faults * pageSize *2; // MUST RETURN THE NUMBER OF BYTES TRANSFERRED INSTEAD OF JUST ZERO
    }


}

class PageTable {
    private final ArrayList<Integer> pageTable;
    private final ArrayList<Boolean> dirtyBitTable;
    private final int max_size;
    private int victim;
    PageTable(int max_size){
        pageTable = new ArrayList<>();
        dirtyBitTable = new ArrayList<>();
        this.max_size = max_size;
        victim = 0;
    }

    public ArrayList<Integer> getPageTable() {
        return pageTable;
    }

    public int lookup(int virtualPageNumber){
        if (!pageTable.contains(virtualPageNumber)){
            return -1; //if page is not in return -1
        } else {
            return pageTable.indexOf(virtualPageNumber); //if page is in returns frame number
        }
    }

    public void update(int virtualPageNumber, int victim, boolean dirtyBit){
        System.out.println("Bringing page " + virtualPageNumber + " into frame " + victim);
        if(pageTable.size() != victim) {
            pageTable.remove(victim);
            dirtyBitTable.remove(victim);
        }
        pageTable.add(victim, virtualPageNumber);
        dirtyBitTable.add(victim, dirtyBit);
    }

    public boolean hasSpace(){
        return pageTable.size() < max_size;
    }

    public int victimManip(){
        if (victim == max_size){
            victim = 0;
        }
        return victim++;
    }

    public boolean getDirtyBit(int victim){
        return dirtyBitTable.get(victim);
    }

    public void cleanBit(int victim){
        dirtyBitTable.remove(victim);
        dirtyBitTable.add(victim, false);
    }
}

class MemoryState {
    private boolean pageLoaded = true;
    private Integer frameNum;
    private int displacement;

    public boolean getLoadedState() {
        return pageLoaded;
    }

    public void setLoadedState(boolean loaded) {
        pageLoaded = loaded;
    }

    public void setFrameNum(Integer frameNum) {
        this.frameNum = frameNum;
    }

    public Integer getFrameNum() {
        return frameNum;
    }

    public void clearState() {
        pageLoaded = true;
    }

    public void setDisplacement(int disp) {
        displacement = disp;
    }

    public int getDisplacement() {
        return displacement;
    }

}
