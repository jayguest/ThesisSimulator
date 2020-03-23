package jaguest.cachesimulator;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Entry class for running the cache implementation
 * @author Jason Guest
 * @version 1.0
 */
public class CacheRun {

    /**
     * Entry point for running the cache
     * @param args is for user input
     */
    public static void main(String args[]) {

       
        //  This will replace the associativity prompt 
        System.err.println("Hello, please enter a file to read:");
        Scanner scan = new Scanner(System.in);  // Take input
        String configFile = scan.nextLine();    // Read and store input
        //System.out.println("Please enter associativity: \n");
        //int associativity = scan.nextInt();     // Read and store input 
        
        // Take file name from input, begin reading to create cache
        File configName = new File(configFile); // From input
        BufferedReader configInput = null;
        try{
            configInput = new BufferedReader(new FileReader(configName));
        }catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        // Now to read the config file
        // Declare variables to be read:
        int cores = 0;                  // 0 by default
        CPU_Core[] coreSim = null;      // null object by default
        int L2 = 0;                         // 1 yes, 0 no shared L2, 0 default
        int L3 = 0;                         // 1 yes but not shared, 0 no L3, 2 yes and shared, 0 default
        int configAssociativity = 0;    // 0 by default
        int blockSize = 0;              // 0 by default
        int cacheSize = 0;              // 0 by default
        String toConvert;               // variable to hold read data
        
        try{
            // First handle creation of cores objects
            toConvert = configInput.readLine();
            cores = Integer.parseInt(toConvert,16);
            coreSim = new CPU_Core[cores];
            for(int i = 0; i < coreSim.length;i++){
                coreSim[i] = new CPU_Core(); 
            }
            
            // Next we handle L1 caches, not shared
            toConvert = configInput.readLine();
            configAssociativity = Integer.parseInt(toConvert,16);   // L1 Associativity
            toConvert = configInput.readLine();
            blockSize = Integer.parseInt(toConvert,16);             // L1 Block width
            toConvert = configInput.readLine();
            cacheSize = Integer.parseInt(toConvert,16);             // L1 size
            
            for(int i = 0;i < coreSim.length;i++){
                coreSim[i].setL1(new Cache(configAssociativity,blockSize,cacheSize)); // Initialize each Level one cache
                coreSim[i].setNumLevels(1);
            }
            
            // Now do the work for Level 2 caches
            toConvert = configInput.readLine(); // fifth line
            L2 = Integer.parseInt(toConvert,16);
            if(L2 == 1){    // Shared Level 2 cache
                toConvert = configInput.readLine();
                configAssociativity = Integer.parseInt(toConvert,16);   // L2 Associativity - 6th line in file
                toConvert = configInput.readLine();
                blockSize = Integer.parseInt(toConvert,16);             // L2 Block width
                toConvert = configInput.readLine();
                cacheSize = Integer.parseInt(toConvert,16);             // L2 size
                Cache levelTwo = new Cache(configAssociativity,blockSize,cacheSize);
                
                for(int i= 0; i < coreSim.length;i++){
                    coreSim[i].setL2(levelTwo); // Initialize all Level two caches
                    coreSim[i].setNumLevels(2);
                }
            }else if(L2 == 0){  // Level two cache is not shared
                // we will have input for L2 sizes enough for each core by assumption
                for(int i = 0; i < coreSim.length;i++){ // iterate to initialize level two cache
                    toConvert = configInput.readLine();
                    configAssociativity = Integer.parseInt(toConvert,16);   // L2 Associativity
                    toConvert = configInput.readLine();
                    blockSize = Integer.parseInt(toConvert,16);             // L2 Block width
                    toConvert = configInput.readLine();
                    cacheSize = Integer.parseInt(toConvert,16);             // L2 size
                    coreSim[i].setL2(new Cache(configAssociativity,blockSize,cacheSize));
                    coreSim[i].setNumLevels(2);
                }
            }else{ // No L2 cache
                // do nothing, no L2 cache
            }
            
            // And now for the finale, the Level 3 cache
            toConvert = configInput.readLine(); // ninth line
            L3 = Integer.parseInt(toConvert,16);
            if(L3 == 2){    // Shared Level three cache
                toConvert = configInput.readLine();
                configAssociativity = Integer.parseInt(toConvert,16);   // L3 Associativity
                toConvert = configInput.readLine();
                blockSize = Integer.parseInt(toConvert,16);             // L3 Block width
                toConvert = configInput.readLine();
                cacheSize = Integer.parseInt(toConvert,16);             // L3 size
                Cache levelThree = new Cache(configAssociativity,blockSize,cacheSize);
                
                for(int i = 0;i < coreSim.length;i++){
                    coreSim[i].setL3(levelThree);   // Initialize all L3 caches
                    coreSim[i].setNumLevels(3);
                }
            }
            else if(L3 == 1){   // Level three cache not shared
                for(int i = 0;i < coreSim.length;i++){  // Iterate to set Level three caches
                    toConvert = configInput.readLine();
                    configAssociativity = Integer.parseInt(toConvert,16);   // L3 Associativity
                    toConvert = configInput.readLine();
                    blockSize = Integer.parseInt(toConvert,16);             // L3 Block width
                    toConvert = configInput.readLine();
                    cacheSize = Integer.parseInt(toConvert,16);             // L3 size
                    coreSim[i].setL3(new Cache(configAssociativity,blockSize,cacheSize));
                    coreSim[i].setNumLevels(3);
                }
            }
            else{
                // Do nothing, there is no Level three cache
            }
            
            // That wraps up all that should be in the input file
            
        }catch(IOException ex){
           ex.printStackTrace();
        }

        Cache cacheSim = new Cache(configAssociativity,blockSize,cacheSize);  // Create the cache object from config file
        System.out.println("Jason Guest\n");    // Assignment specificaitons
        
        // Display the specs for our Simulated system
        System.out.println("Number of cores: " + cores);
        
        for(int i = 0;i < cores;i++){   // Display the specs of each core
            System.out.println("Core " + i + ":");
            System.out.println("L1 size: " + coreSim[i].getL1().getSize() + " bytes, Block width: " + coreSim[i].getL1().getBlockWidth() + " bytes");
            if((L2 == 1) || (L2 == 0)){ // there is L2 cache
                System.out.println("L2 size: " + coreSim[i].getL2().getSize() + " bytes, Block width: " + coreSim[i].getL2().getBlockWidth() + " bytes");
            }
            if(L3 != 0){    // there is an L3 cache
                System.out.println("L3 size: " + coreSim[i].getL3().getSize() + " bytes, Block width: " + coreSim[i].getL3().getBlockWidth() + " bytes");
            }
            System.out.println();   // give a space for readability
        }
        
        // Commented code below may be used for testing purposes, to print the INVALID cache state
//        System.out.println("Initial cache state:");
//        coreSim[0].getL1().printCache();
        //cacheSim.printCache();  // Print the initial invalid state of the cache

        // Open our file containing data to be processed
        File file = new File("javaFloatOne.txt");
        BufferedReader read = null;
        try {
            read = new BufferedReader(new FileReader(file)); // Create our reader
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("Reading input from file: " + file);

        // read data from the specified file
        String line;    // Object to store what we read
        int n;          // variable to store random number
        int check;      // variable to store the check for hit or miss
        Random rand = new Random(); // Used to generate random number
        try {
            while ((line = read.readLine()) != null) { // Read while there is something to be read
                // convert hex string to int for storage in the cache
                int i = Integer.parseInt(line,16); // I thought I'd have to change this for 32 bit hex numbers, but this should still work
                n = rand.nextInt(cores);    // Random number between 0 (inclusive) and the # of cores (exclusive)
                                            // etc. if we have 6 cores, random # from 0 - 5
                // To process the data, a simulated clock cycle is used
                // a Random number generator will pick one of the cores to put 
                // the data through, and each 'cycle' will do the same til the data
                // has all been processed
                
                
                // check if there is a hit, perform required actions
                check = coreSim[n].getL1().check(i);    // Data goes to the first cache, and filters to others on a miss
                
                if(check == 1){
                    // cache hit, do nothing
                }else{
                    // cache miss, check next level of cache
                    if(coreSim[n].getNumCaches() > 1){ // there is lev2 available
                        check = coreSim[n].getL2().check(i);
                        
                        if(check == 0){ // miss, look to L3 cache if available
                            
                            if(coreSim[n].getNumCaches() > 2){
                                check = coreSim[n].getL3().check(i); // last check needed, any hit or miss is recorded as usual
                            }
                            // if no L3 available, do nothing, it's a normal miss
                            // will need to implement a latency or penalty in time for main mem access on miss
                            
                        }else{
                            // hit, do nothing
                        }
                    }
                    // No 2 avaialable, it's a miss, do penalty
                    
                }
                
                // Following code for testing purposes
                //System.out.format("%04X\n",i);
                //System.out.println(Integer.parseInt(read.readLine(),16) + "\n");

            }
        }catch(IOException ex){
            ex.printStackTrace();
        }

        for(int i = 0;i < cores;i++){
            System.out.println("Core " + i + ": ");
            System.out.println("L1: Accesses: " + coreSim[i].getL1().accesses + " Hits: " + coreSim[i].getL1().hits);
            if((L2 == 1) || (L2 == 0)){
                 System.out.println("L2: Accesses: " + coreSim[i].getL2().accesses + " Hits: " + coreSim[i].getL2().hits);
            }
            if(L3 != 0){    // only print if there is an L3 cache
                System.out.println("L3: Accesses: " + coreSim[i].getL3().accesses + " Hits: " + coreSim[i].getL3().hits);
            }
//            coreSim[0].getL1().printCache();
            System.out.println();
        }

    }
}
