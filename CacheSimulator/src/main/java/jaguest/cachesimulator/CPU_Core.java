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
    private Cache Lev2;  // May be shared
    private Cache Lev3;  // May be shared
    
    void setL1(Cache L1){
        this.Lev1 = L1;
    }
    
    void setL2(Cache L2){
        this.Lev2 = L2;
    }
    
    void setL3(Cache L3){
        this.Lev3 = L3;
    }
    
    public Cache getL1(){
        return this.Lev1;
    }
    
    public Cache getL2(){
        return this.Lev2;
    }
    
    public Cache getL3(){
        return this.Lev3;
    }
    
}
