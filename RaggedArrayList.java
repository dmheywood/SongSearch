import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;

/*
 * RaggedArrayList.java
 * Bob Boothe Sep 2012
 */

/**
 * The RaggedArrayList is a 2 level data structure that is an array of arrays.
 *  
 * It keeps the items in sorted order according to the comparator.
 * Duplicates are allowed.
 * New items are added after any equivalent items.
 * @param <E>
 */
public class RaggedArrayList<E> implements Iterable<E> {
   // must be even so when split get two equal pieces
    private static final int MINIMUM_SIZE = 4;    
    private int size;
    // really is an array of L2Array, but compiler won't let me cast to that
    private Object[] l1Array;     
    private int l1NumUsed;
    private Comparator<E> comp;

    // create an empty list  always have at least
    //  1 second level array even if empty, makes code easier 
    RaggedArrayList(Comparator<E> c){
        size = 0;
         // you can't create an array of a generic type
        l1Array = new Object[MINIMUM_SIZE];               
        l1Array[0] = new L2Array(MINIMUM_SIZE);  // first 2nd level array
        l1NumUsed = 1;
        comp = c;
    }

    // nested class for 2nd level arrays
    private class L2Array {
        public E[] items;  
        public int numUsed;

        L2Array(int capacity) {
            // you can't create an array of a generic type
            items = (E[])new Object[capacity];  
            numUsed = 0;
        }
    }

    //total size (number of entries) in the entire data structure
    public int size(){
        return size;
    }

    // null out all references so garbage collector can grab them
    // but keep otherwise empty l1Array and 1st L2Array
    public void clear(){
        size = 0;
        // clear all but first l2 array
        Arrays.fill(l1Array, 1, l1Array.length, null);  
        l1NumUsed = 1;
        L2Array l2Array = (L2Array)l1Array[0];
        // clear out l2array
        Arrays.fill(l2Array.items, 0, l2Array.numUsed, null);  
        l2Array.numUsed = 0;
    }

    // nested class for a list position
    // used only internally
    // 2 parts: level 1 index and level 2 index
    private class ListLoc {
        public int level1Index;
        public int level2Index;

        ListLoc(int level1Index, int level2Index) {
            this.level1Index = level1Index;
            this.level2Index = level2Index;
        }

        // since only used internally, can trust it is comparing 2 ListLoc's
        public boolean equals(Object otherObj) {
            ListLoc other = (ListLoc)otherObj;

            return level1Index == other.level1Index && 
                    level2Index == other.level2Index;
        }

        // move ListLoc to next entry
        // when is moves past the very last entry it will be 1 index past the 
        // last value in the used level 2 array 
        // used internally to scan through the array for sublist
        // also used to implement the iterator
        public void moveToNext() {
            level2Index++;  // move to next slot in current 2nd level array
            // move to next level 2 array
            if (level2Index >= ((L2Array)l1Array[level1Index]).numUsed) { 
                // do we have more level 2 arrays
                if (level1Index+1 < l1NumUsed) { 
                    level1Index++;
                    level2Index = 0;
                }
            }
        }
    }

    // find 1st matching entry
    // returns ListLoc of 1st matching item
    // or of 1st item greater than the item if no match
    // this could be an unused slot at the end of a level 2 array
    // (searches forwards)
    private ListLoc findFront(E item){
        if (size == 0)                  // special case for empty list
            return new ListLoc(0,0);

        // linear search level 1, compare to last entry in each level 2 array
        int i1, i2;
        
        for (i1 = 0; i1 < l1NumUsed; i1++){
            L2Array l2Array = (L2Array)l1Array[i1];
            
            if (comp.compare(item, l2Array.items[l2Array.numUsed-1]) <= 0) {
               // this is the block
                // linear search this second level array
                for (i2 = 0; i2 < l2Array.numUsed; i2++) 
                    if (comp.compare(item, l2Array.items[i2]) <= 0)
                       // stop at first match or larger value
                        break;
                return new ListLoc(i1, i2);
            }
        }

        // if gets here it is past last item on list
        // return slot in last block 1 past end
        return new ListLoc(l1NumUsed-1,((L2Array)l1Array[l1NumUsed-1]).numUsed);
    }

     
    // find location after the last matching entry
    // or if no match, it finds the index of the next larger item 
    // this is the position to add a new entry
    // this could be an unused slot at the end of a level 2 array
    // (searches the list backwards)
    private ListLoc findEnd(E item){
        if (size == 0)                  // special case for empty list
            return new ListLoc(0,0);

        // linear search level 1 array backwards, 
        // compare to 1st entry in each level 2 array
        for (int i1 = l1NumUsed-1; i1 >=0 ; i1--){
            L2Array l2Array = (L2Array)l1Array[i1];
            if (comp.compare(item, l2Array.items[0]) >= 0) {  
                // is this the block
                // now linear search this second level array backwards
                for (int i2 = l2Array.numUsed-1; i2 >= 0; i2--) {
                    if (comp.compare(item, l2Array.items[i2]) >= 0)  
                        // are we >= this item?
                        // insert after this one, return next index
                        return new ListLoc(i1, i2+1);  
                }
                return null;  // should never get here since in this block
            }
        }
        
        // if gets here, the item belongs before the 1st item on the list
        return new ListLoc(0,0);
    }
    
    /** 
     * add object after any other matching values
     * findEnd will give the insertion position
     */
    boolean add(E item){
        ListLoc loc = findEnd(item);  // insertion position
        L2Array l2Array = (L2Array)l1Array[loc.level1Index];
        
        // move up to insert
        int pos = loc.level2Index;
        System.arraycopy(l2Array.items, pos, l2Array.items, pos+1, 
                l2Array.numUsed-pos);
        l2Array.items[pos] = item;
        l2Array.numUsed++;
        size++;
        
        // are full
        if (l2Array.numUsed < l2Array.items.length)
            return true;    // not full, so all done
        
        // grow or split block
        int l2Length = l2Array.items.length;
        
        if (l2Length  < l1Array.length) { // small block, just double it
            l2Array.items = Arrays.copyOf(l2Array.items, l2Length*2);
        } else {                                    // large block, split it
            L2Array l2ArrayB = new L2Array(l2Length);
            System.arraycopy(l2Array.items, l2Length/2, l2ArrayB.items, 
                    0, l2Length/2);  // copy top half
             // fill old top with nulls
            Arrays.fill(l2Array.items, l2Length/2, l2Length, null);                
            l2Array.numUsed = l2Length/2;
            l2ArrayB.numUsed = l2Length/2;
            // now insert into l1Array
            int posl1 = loc.level1Index+1;   // index of inserted block B
            System.arraycopy(l1Array, posl1, l1Array, posl1+1, l1NumUsed-posl1);
            l1Array[posl1] = l2ArrayB;
            l1NumUsed++;
            // is level 1 array full? then double it
            if (l1NumUsed == l1Array.length) {
                l1Array = Arrays.copyOf(l1Array, l1NumUsed*2);
            }
        }

        return true;
    }

    /**
     * check if list contains a match
     */
    boolean contains(E item){
       // if not found this could be a location past the end of numUsed
        ListLoc loc = findFront(item);  
        if (loc.level2Index >= ((L2Array)l1Array[loc.level1Index]).numUsed)
            return false;
        return comp.compare(
                ((L2Array)l1Array[loc.level1Index]).items[loc.level2Index], 
                item) == 0;
    }

    /**
     * copy the contents of the RaggedArrayList into the given array
     * @param a - an array of the actual type and of the correct size
     * @return the filled in array
     */
    public E[] toArray(E[] a){
        Iterator<E> itr = this.iterator();
        int cnt = 0;
        while (itr.hasNext()) {
            if (a.length <= cnt) {
                System.err.println
                 ("RaggedArrayList.toArray - array too small to hold result");
                break;
            }
            a[cnt++] = itr.next();
        }
        return a;
    }

    /**
     * returns a new independent RaggedArrayList 
     * whose elements range from fromElemnt, inclusive, to toElement, exclusive
     * the original list is unaffected
     * @param fromElement
     * @param toElement
     * @return the sublist
     */
     public RaggedArrayList<E> subList(E fromElement, E toElement){
        ListLoc loc1 = findFront(fromElement);
        ListLoc loc2 = findFront(toElement);
        RaggedArrayList<E> result = new RaggedArrayList<E>(comp);
        // may give a rather skewed ragged array because I 
        // build by adding to the end.
        while (!loc1.equals(loc2)) {
            L2Array l2array = (L2Array)l1Array[loc1.level1Index];
            result.add(l2array.items[loc1.level2Index]);
            loc1.moveToNext();
        }

        return result;
    }

    /**
     * returns an iterator for this list
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Iterator is just a list loc
     * it starts at (0,0) and finish with index2 1 past the 
     * last item in the last block
     */
    private class Itr implements Iterator<E> {
        private ListLoc loc;

        Itr(){
            loc = new ListLoc(0, 0);
        }

        /**
         * check is more items
         */
        public boolean hasNext() {
            return loc.level1Index < l1NumUsed-1 || 
                    loc.level2Index < 
                    ((L2Array)l1Array[loc.level1Index]).numUsed;
        }

        /**
         * return item and move to next
         * throws NoSuchElementException if off end of list
         */
        public E next() {
            L2Array l2Array = (L2Array)l1Array[loc.level1Index];
            if (loc.level2Index >= l2Array.numUsed)
                throw new IndexOutOfBoundsException();
            E val = l2Array.items[loc.level2Index]; 
            loc.moveToNext();
            return val;
        }

        /**
         * Remove is not implemented. Just use this code.
         */
        public void remove() {
            throw new UnsupportedOperationException();	
        }

    }


    /**
     * Main routine for testing the RaggedArrayList by itself.
     * There is a default test case of a-g.
     * You can also specify arguments on the command line that will be
     * processed as a sequence of characters to insert into the list.
     * 
     * DO NOT MODIFY I WILL BE USING THIS FOR MY TESTING
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        //animation(); // for demo int lecture 7 
        System.out.println("testing routine for RaggedArrayList");
        System.out.println("usage: any command line arguments are added "
                + "by character to the list");
        System.out.println("       if no arguments, then a default test "
                + "case is used");

        // setup the input string
        String order = "";
        if (args.length == 0)
            order = "abcdefg";  // default test
        else
            for (int i=0; i < args.length; i++)  // concatenate all args
                order += args[i];
        
        /* big random test case
        StringBuilder strb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            char c = (char)('0' + rand.nextInt(84));      
            strb.append(c);
        }
        // random letters from song file
        StringBuilder strb = new StringBuilder();
        Scanner scan = new Scanner(new File("allSongs.txt"));
        for (int i = 0; i < 1000; i++) {
            String t = scan.next();
            strb.append(t.charAt(0));
        }
        String order = strb.toString();
        */
        
        // insert them character by character into the list
        System.out.println("insertion order: "+order);
        Comparator<String> comp = new StringCmp();
        ((CmpCnt)comp).resetCmpCnt();// reset the counter inside the comparator
        RaggedArrayList<String> ralist = new RaggedArrayList<String>(comp);
        for (int i = 0; i < order.length(); i++){
            String s = order.substring(i, i+1);
            ralist.add(s);
        }
        System.out.println("The number of comparison to build the "
                + "RaggedArrayList = "+
                ((CmpCnt)comp).getCmpCnt());

        System.out.println("TEST: after adds - data structure dump");
        ralist.dump();
        ralist.stats();     

        System.out.println("TEST: contains(\"c\") ->" + ralist.contains("c"));
        System.out.println("TEST: contains(\"7\") ->" + ralist.contains("7"));

        System.out.println("TEST: toArray");
        String[] a = new String[ralist.size()];
        ralist.toArray(a);
        for (int i=0; i<a.length; i++)
            System.out.print("["+a[i]+"]");
        System.out.println();

        System.out.println("TEST: iterator");
        Iterator<String> itr = ralist.iterator();
        while (itr.hasNext())
            System.out.print("["+itr.next()+"]");
        System.out.println();

        System.out.println("TEST: sublist(b,k)");
        RaggedArrayList<String> sublist = ralist.subList("b", "k");
        sublist.dump();	
    }

    public static void animation() {
       Comparator<String> comp = new StringCmp();
       RaggedArrayList<String> ralist = new RaggedArrayList<String>(comp);
       Scanner scan = new Scanner(System.in);
       
       System.out.println("Empty ragged array list\n");
       ralist.dump();
       
       for (;;){
          System.out.print("\nadd: ");
          String token = scan.next();
          System.out.println();
          ralist.add(token);
          ralist.dump();
       }
    }

    /**
     * string comparator with cmpCnt for testing
     */
    public static class StringCmp implements Comparator<String>, CmpCnt {
        int cmpCnt;

        StringCmp(){
            cmpCnt=0;
        }

        public int getCmpCnt() {
            return cmpCnt;
        }
        public void resetCmpCnt() {
            this.cmpCnt = 0;
        }

        public int compare(String s1, String s2) {
            cmpCnt++;
            return s1.compareTo(s2);
        }
    }

    /**
     * print out an organized display of the list
     * intended for testing purposes on small examples
     * it looks nice for the test case where the objects are characters
     *
     * DO NOT MODIFY I WILL BE USING THIS FOR MY TESTING
     */
    public void dump(){
        for (int i1 = 0; i1 < l1Array.length; i1++) {
            L2Array l2array = (L2Array)l1Array[i1];
            System.out.print("[" + i1 + "] -> ");
            if (l2array == null)
                System.out.println("null");
            else {
                for (int i2 = 0; i2 < l2array.items.length; i2++) {
                    E item = l2array.items[i2];
                    if (item == null)
                        System.out.print("[ ]");
                    else
                        System.out.print("["+item+"]");
                }
                System.out.println();
            }
        }
    }

    /**
     * calculate and display statistics
     * 
     * It use a comparator that implements the given CmpCnt interface.
     * It then runs through the list searching for every item and calculating
     * search statistics.
     * 
     * DO NOT MODIFY I WILL BE USING THIS FOR MY TESTING
     */
    public void stats(){
        System.out.println("STATS:");
        System.out.println("list size N = "+ size);

        // level 1 array
        System.out.println("level 1 array " + l1NumUsed + " of " + 
                l1Array.length + " used.");

        // level 2 arrays
        int minL2size = Integer.MAX_VALUE, maxL2size = 0;
        for (int i1 = 0; i1 < l1NumUsed; i1++) {
            L2Array l2Array = (L2Array)l1Array[i1];
            minL2size = Math.min(minL2size, l2Array.numUsed);
            maxL2size = Math.max(maxL2size, l2Array.numUsed);
        }
        System.out.println("level 2 array sizes: min = "+minL2size + 
                " used, avg = " + (double)size/l1NumUsed +
                " used, max = " + maxL2size + " used");

        // search stats, search for every item
        int totalCmps = 0, minCmps = Integer.MAX_VALUE, maxCmps = 0;
        Iterator<E> itr = iterator();
        while (itr.hasNext()) {
            E obj = itr.next();
            ((CmpCnt)comp).resetCmpCnt();
           if (!contains(obj)){}
////                System.err.println("Did not expect an unsuccesful "
////                        + "search in stats");
            int cnt = ((CmpCnt)comp).getCmpCnt();
            totalCmps += cnt;
            if (cnt > maxCmps)
                maxCmps = cnt;
            if (cnt < minCmps)
                minCmps = cnt;
        }
        System.out.println("Successful search: min cmps = " + minCmps + 
                " avg cmps = " + (double)totalCmps/size +
                " max cmps = " + maxCmps);
    }
}