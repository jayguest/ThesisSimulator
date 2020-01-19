package jaguest.cachesimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

// TODO: 
// Fix current status by assignment specs (LRU check)
// Add cache levels (L1, L2, L3)funtionality
// Implement multi-core / threading
// Add functionality to choose which levels are shared between cores/threads
//  - latency via delay
//  - latency via counter
// Need to also implement a main memory class (as accurate as possible / slow)
// Read JSON configuration file for setup/layout - using .txt for now


/**
 * Cache class defining the structure of the Cache implementation
 * @author Jason Guest
 * @verison 2.0
 */
public class Cache {

    // TODO: Add cache size variable
    // Attribute variables
    // TODO: Calculate # of blocks by the cache size
    private int SIZE; // A variable that will be set based on input
    private int BLOCKS; // All begin in INVALID state
    // TODO: This will probably something set in the JSON config file
    private int BLOCK_WIDTH; // 1 byte wide, no offset bits
    private int associativity; // e.g. 4-way (4), 16-way (16)
    // options will be 1,2,4,8,16

    // Runtime counter variables
    public int accesses;
    public int hits;

    // Storage
    private Block[][] sets; // set of blocks, length determined by associativity
    private int indexBits; // Maps to the number of sets

    // LRU check queue
    private ArrayList<int[]> LRU; // This implementation may change

    /**
     * Bit extraction method
     * @param start is the start bit
     * @param end is the end bit
     * @return the required bits
     */
    int GETBITS(int value,int start, int end){
        int mask = (1<<(end-start+1))-1;
        return (value>>>start)&mask;
    }
    
    int getSize(){
        return this.SIZE;
    }
    
    int getBlockWidth(){
        return this.BLOCK_WIDTH;
    }

    /**
     * Main Cache constructor
     * @param associativity is the defined associativity from user input
     * @param blockSize is the defined block width from input 
     * return a new Cache object
     */
    public Cache(int associativity,int blockSize,int cacheSize){
        this.SIZE = cacheSize;                  // Initialize cache size
        this.BLOCKS = cacheSize / blockSize;    // Find our number of blocks
        this.BLOCK_WIDTH = blockSize;           // Initialize block width
        this.hits = 0;                          // Initialize hits to 0
        this.associativity = associativity;     // Initialize associativity
        
        // Perform value checks for provided input
        if(associativity > this.BLOCKS){
            System.out.println("Thee associativity cannot be greater than the # of blocks");
            System.out.println("Blocks available: "+ this.BLOCKS +", Please choose associativity:");
            Scanner scan = new Scanner(System.in);  // Take input
            while(this.associativity > this.BLOCKS){
                this.associativity = scan.nextInt();
            }
        }
        
        this.sets = new Block[BLOCKS/associativity][associativity]; // Creation of sets array
        for(int i = 0;i < sets.length;i++){
            for(int j = 0;j < sets[i].length;j++){
                sets[i][j] = new Block(12498); // initialize to INVL
                // 12498 selected as it is a number very unlikely to be a value used
            }
        }
        this.indexBits = (int)(Math.log(sets.length)/Math.log(2)); // index mapping to each set
        // System.out.println(this.indexBits); // For testing purposes
        this.LRU = new ArrayList<>(); // Declare our 'queue' for LRU
    }

    /**
     * Method to check if an element is present in the cache
     * This method called before every insertion
     * @param num is the element to search for
     * @return a 1 for a hit, 0 for a miss
     */
    public int check(int num){

        // 16 block cache will have 2^n = 16 => 4 bits for the index
        // the rest will be for the tag
        this.accesses++;
        int index = 0;
        int tag;
        if(this.associativity == 16){ // no index bits required, it's one set
            tag = num; // Whole number is the tag
        }
        else if(this.associativity == 8){
            tag = GETBITS(num,2,32);
            index = GETBITS(num,0,0);         
        }else{
            index = GETBITS(num,0,this.indexBits-1); // pull index from num
            tag = GETBITS(num,this.indexBits,32); // pull tag from num
        }
        
        
        int check = this.hits;
        int position = 0;
        for(int i = 0;i < sets[index].length;i++){
            if(sets[index][i].getTag() == tag){ // check each tag in sets
                this.hits++; // inc hit if we find the tag we are looking for
                //hitUpdate(index,i); // update the LRU queue
                return 1;
            }
        }
        if(check == this.hits){ // # of hits hasn't changed, item not in cache, miss
            insert(tag,index); // add the item to the cache, hopefully it'll hit next time
            for(int i = 0;i < this.sets[index].length;i++){
                if(sets[index][i].getTag() == tag){
                    position = i; // Get the specific block written to
                }
            }
            update(index,position); // update LRU with the block coordinate
            // We only update LRU on a miss
            // Send a return value to check the other levels of cache
            return 0;
        }
        return 0;
    }

    /**
     * Method for inserting a tag to the cache, only called on a miss
     * @param tag is the tag to insert
     * @param index is the set to find a block for it in
     */
    public void insert(int tag,int index){
        
        // favor the INVL spots first, so we should check for them first
        for(int i = 0;i < sets[index].length;i++){
            if (sets[index][i].getTag() == 12498) {
                sets[index][i].setTag(tag); // insert the tag
                update(index,i); // update LRU for the block write

                return; // done once we've inserted the tag
            }

        }

        // If we haven't inserted over an INVALID, we run LRU check
        for(int j = 0;j < LRU.size();j++){ // iterate starting from least recently used
            if(index == LRU.get(j)[0]){ // index matches the LRU block we found
                sets[index][LRU.get(j)[1]].setTag(tag); // LRU.get(j)[1] is the block position
            }
        }
    }

    /**
     * Method keeping track of the LRU queue additions
     * LRU holds 'coordinates' for each block in the cache
     * @param index is the index bit
     * @param position is the specific block
     */
    public void addToQueue(int index,int position){
        int[] coord = {index,position};
        this.LRU.add(coord);
    }

    /**
     * Method to update the LRU queue when there is a hit
     * @param index is the index map to the set
     * @param position is the specific block
     */
    public void update(int index,int position){
        int[] coord = {index,position};
        for(int i = 0;i < LRU.size();i++){
            if(Arrays.equals(LRU.get(i), coord)){ //find the miss tag
                LRU.remove(i);  // index found, remove from list
                // Update the queue
                LRU.add(coord); // add it back to the end of the list
            }
        }
    }

    /**
     * Method to print out the current state of the cache blocks,
     * also showing the structure of the cache currently used.
     */
    public void printCache(){
        for(int i = 0;i < this.sets.length;i++){
            System.out.print("Set " + i + " ");
            for(int j = 0;j < sets[i].length;j++){
                // print blocks separated by space and labelled by set
                System.out.print(sets[i][j].toString() + " ");
            }
            System.out.println(); // new line for each set
        }
        System.out.println();
    }
}
