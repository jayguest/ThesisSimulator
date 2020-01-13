package jaguest.cachesimulator;

import java.io.*;
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
        CPU_Core[] coreSim;
        int L2;                         // 1 yes, 0 no shared L2
        int L3;                         // 1 yes but not shared, 0 no L3, 2 yes and shared
        int configAssociativity = 0;    // 0 by default
        int blockSize = 0;              // 0 by default
        int cacheSize = 0;              // 0 by default
        String toConvert;
        try{
            // First handle creation of cores objects
            toConvert = configInput.readLine();
            cores = Integer.parseInt(toConvert,16);
            coreSim = new CPU_Core[cores];
            for(int i = 0; i < coreSim.length;i++){
                coreSim[i] = new CPU_Core(); // 
            }
            
            // Next we handle L1 caches, not shared
            toConvert = configInput.readLine();
            configAssociativity = Integer.parseInt(toConvert,16);   // L1 Associativity
            toConvert = configInput.readLine();
            blockSize = Integer.parseInt(toConvert,16);             // L1 Block width
            toConvert = configInput.readLine();
            cacheSize = Integer.parseInt(toConvert,16);             // L1 size
            Cache levelOne = new Cache(configAssociativity,blockSize,cacheSize);
            
            for(int i = 0;i < coreSim.length;i++){
                coreSim[i].setL1(levelOne); // Initialize each Level one cache
            }
            
            // Now do the work for Level 2 caches
            toConvert = configInput.readLine();
            L2 = Integer.parseInt(toConvert,16);
            if(L2 == 1){    // Shared Level 2 cache
                toConvert = configInput.readLine();
                configAssociativity = Integer.parseInt(toConvert,16);   // L1 Associativity
                toConvert = configInput.readLine();
                blockSize = Integer.parseInt(toConvert,16);             // L1 Block width
                toConvert = configInput.readLine();
                cacheSize = Integer.parseInt(toConvert,16);             // L1 size
                Cache levelTwo = new Cache(configAssociativity,blockSize,cacheSize);
                
                for(int i= 0; i < coreSim.length;i++){
                    coreSim[i].setL2(levelTwo); // Initialize all Level two caches
                }
            }else{  // Level two cache is not shared
                // we will have input for L2 sizes enough for each core by assumption
                for(int i = 0; i < coreSim.length;i++){ // iterate to initialize level two cache
                    toConvert = configInput.readLine();
                    configAssociativity = Integer.parseInt(toConvert,16);   // L2 Associativity
                    toConvert = configInput.readLine();
                    blockSize = Integer.parseInt(toConvert,16);             // L2 Block width
                    toConvert = configInput.readLine();
                    cacheSize = Integer.parseInt(toConvert,16);             // L2 size
                    coreSim[i].setL2(new Cache(configAssociativity,blockSize,cacheSize));
                }
            }
            
            // And now for the finale, the Level 3 cache
            toConvert = configInput.readLine();
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
        System.out.println("Cache size: " + cacheSim.getSize() + " bytes");
        System.out.println("Block width: " + cacheSim.getBlockWidth() + " bytes");
        System.out.println("Initial cache state:");
        cacheSim.printCache();  // Print the initial invalid state of the cache

        // Open our file
        File file = new File("trace1.txt"); // This will change with JSON config
        BufferedReader read = null;
        try {
            read = new BufferedReader(new FileReader(file)); // Create our reader
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("Read file: " + file);

        // read the file
        String line;    // Object to store what we read
        try {
            while ((line = read.readLine()) != null) { // Read while there is something to be read
                // convert hex string to int for storage in the cache
                int i = Integer.parseInt(line,16);

                // check if there is a hit, perform required actions
                cacheSim.check(i);

                // Following code for testing purposes
                //System.out.format("%04X\n",i);
                //System.out.println(Integer.parseInt(read.readLine(),16) + "\n");

            }
        }catch(IOException ex){
            ex.printStackTrace();
        }

        cacheSim.printCache();  // Print final state of cache
        // Print the hits and accesses for this simulation
        System.out.println("Accesses: " + cacheSim.accesses + " Hits: " + cacheSim.hits);

    }
}
