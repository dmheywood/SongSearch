/**
 * CmpCnt.java
 * Bob Boothe Oct 2007
 * 
 * Interface for adding comparison counting to a comparator	in a clean fashion.
 * 	
 * You must add a counter and implement getCmpCnt() & resetCmpCnt()
 * 
 * The basic idea is:
 *    we add a counter inside the comparator.
 *    we declare that the comparator class implements cmpCnt
 *    the constructor initializes this counter to zero
 *    
 *    after we are done sorting or searching, we cast the comparator to cmpCnt
 *    and call getCmpCnt to retrieve the comparison count from inside the comparator
 */
public interface CmpCnt {

    /**
     *
     * @return the value of the embedded counter
     */
    int getCmpCnt();

    /**
     * reset the counter to 0
     */
    void resetCmpCnt();
}
