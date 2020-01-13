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

        // TODO: Add a prompt for a file to read containing the cache configuration
        //  This will replace the associativity prompt 
        System.err.println("Hello, please enter a file to read:");
        Scanner scan = new Scanner(System.in);  // Take input
        String configFile = scan.nextLine();    // Read and store input
        System.out.println("Please enter associativity: \n");
        int associativity = scan.nextInt();     // Read and store input 
        
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
        int configAssociativity = 0;    // 0 by default
        int cacheSize = 0;              // 0 by default
        String toConvert;
        try{
            toConvert = configInput.readLine();
            configAssociativity = Integer.parseInt(toConvert,16);   // Hex to int
            toConvert = configInput.readLine();
            cacheSize = Integer.parseInt(toConvert,16);
            
        }catch(IOException ex){
           ex.printStackTrace();
        }

        Cache cacheSim = new Cache(configAssociativity,cacheSize);  // Create the cache object from config file
        System.out.println("Jason Guest\n");    // Assignment specificaitons
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
