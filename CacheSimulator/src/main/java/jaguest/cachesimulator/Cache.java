package jaguest.cachesimulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

// TODO: Add some latency counters for convenience in showing performance gains

/**
 * Cache class defining the structure which the simulated cache uses to hold tags and values.
 * This specific implementation uses only tags and values, and not a valid or "dirty" bit.
 * @author Jason Guest
 * @verison 2.1
 */
public class Cache {

    private int SIZE; // A variable that will be set based on input
    private int BLOCKS; // All begin in INVALID state
    private int BLOCK_WIDTH; // 1 byte wide, no offset bits default
    private int associativity; // e.g. 4-way (4), 16-way (16)
    // options will be 1,2,4,8,16 up to # of blocks

    // Runtime counter variables
    public int accesses;
    public int hits;

    // Storage
    private Block[][] sets; // set of blocks, length determined by associativity
    private int indexBits; // Maps to the number of sets

    // LRU data structure
    public HashMap<Integer, LinkedList> LRU; // Hashtable is sized to have spaces for each set or line in the cache
    // The table will hold < set #, list of blocks in set >

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
     * @param cacheSize is the size of the cache in bytes
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
            System.out.println("The associativity cannot be greater than the # of blocks");
            System.out.println("Blocks available: "+ this.BLOCKS +", Please choose associativity:");
            Scanner scan = new Scanner(System.in);  // Take input
            while(this.associativity > this.BLOCKS){
                this.associativity = scan.nextInt();
            }
        }
        
        // Set up te LRU hashtable and populate with indeces and linked lists
        this.LRU = new HashMap(BLOCKS / associativity); // defines the size to be equal to the # of sets
        for(int i = 0;i < (BLOCKS / associativity);i++){ // iterate over # of sets
            this.LRU.put(i,new LinkedList<Integer>()); // populate the hashtable with lists associated with a key for each set number
        }
        
        this.sets = new Block[BLOCKS/associativity][associativity]; // Creation of sets array
        for(int i = 0;i < sets.length;i++){
            for(int j = 0;j < sets[i].length;j++){
                this.sets[i][j] = new Block(12498); // initialize to INVL
                // 12498 selected as it is a number unlikely to be a value used
                this.LRU.get(i).add(j); // will add the block number to list
                
                // now both the cache and the LRU hashtable should be fully populated with initial INVL values after nested loop ends
            }
        }
        this.indexBits = (int)(Math.log(sets.length)/Math.log(2)); // index mapping to each set
        // System.out.println(this.indexBits); // For testing purposes
        
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
            tag = GETBITS(num,2,31);
            index = GETBITS(num,0,1);         
        }else{
            index = GETBITS(num,0,this.indexBits-1); // pull index from num
            tag = GETBITS(num,this.indexBits,31); // pull tag from num
        }
        
        
        int check = this.hits;
        for(int i = 0;i < sets[index].length;i++){
            if(sets[index][i].getTag() == tag){ // check each tag in sets for a hit
                this.hits++; // inc hit if we find the tag we are looking for
                //hitUpdate(index,i); // update the LRU queue
                return 1;
            }
        }
        if(check == this.hits){ // # of hits hasn't changed, so the item is not in cache, miss
            insert(tag,index); // add the item to the cache, hopefully it'll hit next time
                               // LRU hashtable is updated in the insert function
            return 0; // return miss value
        }
        
        return 0; // miss is the default return value
    }

    /**
     * Method for inserting a tag to the cache, only called on a miss
     * @param tag is the tag to insert
     * @param index is the set to find a block for it in
     */
    public void insert(int tag,int index){
        
        // favor the INVL spots first
        for (Block set : sets[index]) {
            if (set.getTag() == 12498) {
                set.setTag(tag); // insert the tag over the INVL slot
                return; // done once we've inserted the tag
            }
        }
        // If we haven't inserted over an INVALID, we run LRU check
        // The provided index is the key we will use to get to the list we want
        Integer whereInsert = (Integer)this.LRU.get(index).pollFirst(); // find the least recently used block, get and remove it from the list
                                                                        // will be first item in the linked list associated with the key: index
        this.LRU.get(index).addLast(whereInsert); // add the chosen block index to the most recently used slot in the list (update list)
        this.sets[index][whereInsert].setTag(tag); // insert into the block least recently used
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
