package jaguest.cachesimulator;

/*
 * This is a class representing the individual cores of the simulated CPU
 * By creating instances of this object, I can simulate having multiple cores
 * Each of these cores can run on a separate thread, much like they actually do
 * @author Jason Guest
 * @version 1.0
 */
public class CPU_Core {
    
    // Here will be the attributes, such as levels of cache
    private Cache Lev1; // This level private to each core
    public Cache Lev2;  // May be shared
    public Cache Lev3;  // May be shared
    
    
    
}
