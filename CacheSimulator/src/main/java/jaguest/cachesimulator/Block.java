package jaguest.cachesimulator;

/**
 * Class defining the block object which the Cache
 * will consist multiples of.
 * @author Jason Guest
 * @version 1.0
 */
public class Block {
    private int tag;
    private int data;

    public Block(int tag){
        this.tag = tag;
    }

    public void setTag(int tag){
        this.tag = tag;
    }

    public int getTag(){
        return this.tag;
    }

    /**
     * Method to print out the blocks tag value.
     * Necessary to get the invalid state printed when tags are
     * stored as integers
     * @return a new string
     */
    public String toString(){
        if(this.tag == 12498){
            return "INVL";
        }else{
            return String.format("%08X",this.tag);
        }
    }
}
