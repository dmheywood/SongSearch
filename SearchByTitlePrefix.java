import java.util.Comparator;

/**
 *
 * @author David Heywood
 */

public class SearchByTitlePrefix {
    private Song[] songs;  // keep a direct reference to the song array
    private RaggedArrayList<Song> RAList;
    private Comparator<Song> comp;        

    /**
     * constructor takes in a songCollection object
     * and places those songs into a RaggedArrayList
     * @param sc
     */
        public SearchByTitlePrefix(SongCollection sc) {
        songs = sc.getAllSongs();  // a copy of all the songs in allSongs.txt
        comp = new Song.CompareTitle();
        ((CmpCnt)comp).resetCmpCnt();        
        RAList = new RaggedArrayList<Song>(comp);
        int count = 0;       
                 
        for (int i = 0; i < songs.length; i++) {
            RAList.add(songs[i]);
            count++;
            System.out.println(count);
        }
        System.out.println("\n" + "\"Statistics for the RaggedArrayList\"");
        System.out.println("The total number of compares to build ragged array list: "
                + ((CmpCnt) comp).getCmpCnt() + ".");
        RAList.stats();
    }    

    /**
     * find all songs matching title prefix
     * 
     * @param titlePrefix
     * @return
     */
    public Song[] search(String titlePrefix) {        
        titlePrefix = titlePrefix.toLowerCase();
        // Song object to store title prefix being searched for
        Song key = new Song("dummy", titlePrefix, "dummy");
        ((CmpCnt)comp).resetCmpCnt();
        // find the last character of the titlePrefix to create prefix search  
        char titlesLastChar = titlePrefix.charAt(titlePrefix.length() - 1);
        // Increments the last character to next letter in the alphabet
        titlesLastChar++;
        
        String endTitlePrefix = titlePrefix.substring(0, titlePrefix.length() - 1) + titlesLastChar;       
        // Song object to store the incremented last character from the titlePrefix
        Song endTitlePrefixSong = new Song("dummy", endTitlePrefix, "dummy");
        // create a subList of a search for the titlePrefix
        RaggedArrayList matches = RAList.subList(key, endTitlePrefixSong);         
        Song[] sa = new Song[matches.size()]; // create an array that is the right size
        sa = (Song[]) matches.toArray(sa);
        System.out.println("The total number of songs is " + sa.length);

        return sa;
    }       
            
    /**
     * Testing routine
     * @param args
     */
        public static void main(String[] args){        
        if (args.length > 2) {
           System.err.println("usage: prog songfile title");
            System.exit(1);
        }
        
        SongCollection sc = new SongCollection("allSongs.txt");
        SearchByTitlePrefix sbtp = new SearchByTitlePrefix(sc);
        
        System.out.println("searching for: " + "Angel");
        Song[] byTitleResult = sbtp.search("Angel");     

        for (int i = 0; i < 10; i++) {
            System.out.println(byTitleResult[i]);
        }
    }    
}
