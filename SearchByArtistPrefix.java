import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author David Heywood
 */

public class SearchByArtistPrefix {

    private Song[] songs;  // keep a direct reference to the song array
    
    /**
     * constructor takes in a songCollection object
     * @param sc
     */
        public SearchByArtistPrefix(SongCollection sc) {
        songs = sc.getAllSongs();
    }

    /**
     * find all songs matching artist prefix uses binary search should operate in time log n + k (# matches)
     *
     * @param artistPrefix
     * @return
     */
    public Song[] search(String artistPrefix) {
        ArrayList<Song> matches = new ArrayList<Song>(); // create a list for results
        artistPrefix = artistPrefix.toLowerCase();

        Song key = new Song(artistPrefix, "dummy", "dummy");
        Comparator<Song> cmp = new Song.CompareArtist(); // declaring a comparator for our Song type
        ((CmpCnt) cmp).resetCmpCnt();

        int index = Arrays.binarySearch(songs, key, cmp); // gets return value from array.binary search

        if (index >= 0) { // this is the code for we found a match
            System.out.println("match found at index " + index);
            // find the first one by walking back in the array
            while (index > 0 && cmp.compare(songs[index - 1], key) == 0) { // back up in the array
                index--;
            }
            // build the arrayList of all matches
            while (index < songs.length && songs[index].getArtist().toLowerCase().startsWith(artistPrefix)) {
                matches.add(songs[index]);
                index++;
            }
        } else {
            index = -index + 1;
            System.out.println("match not found, but would be at index " + index);
        }
        // create an array that is the right size
        Song[] sa = new Song[matches.size()];
        // convert the arrayList to an array
        sa = matches.toArray(sa);

        // Print Statistics
        // The number of Ssongs
        System.out.println("The total number of songs is " + sa.length);
        // Number of binary compares
        System.out.println("It took " + ((CmpCnt) cmp).getCmpCnt() + " comparisons.");

        return sa;
    }

    /**
     * testing routine
     * @param args
     */
        public static void main(String[] args) {
        if ((args.length > 2)) {
            System.err.println("usage: prog songfile artist");
            System.exit(1);
        }
        SongCollection sc = new SongCollection("allSongs.txt");
        SearchByArtistPrefix sbap = new SearchByArtistPrefix(sc);

        System.out.println("searching for: " + "Joh");
        Song[] byArtistResult = sbap.search("Joh");

        // to do: show first 10 songs
        for (int i = 0; i < 10; i++) {            
            System.out.println(byArtistResult[i]);
        }
    }    
}
