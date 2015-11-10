
import java.util.Comparator;

/*
 * CSCI 290 
 * Project1
 * @author David Heywood
 * Description: Song class holds the data for a single song
 */
public class Song implements Comparable<Song> {

    private String artist;
    private String title;
    private String lyrics;
    private static int compares; // so all the songs have the same one

    int cmpCnt;

    /**
     * Default constructor
     */
    public Song() {
        this("", "", "");
    }

    /**
     *
     * @param a is the artist of the song
     * @param t is the title of the song
     * @param l is the lyrics for the song
     */
    public Song(String a, String t, String l) {
        artist = a;
        title = t;
        lyrics = l;
    }

    /**
     *
     */
    public void resetCompares() {
        compares = 0;
    }

    /**
     *
     * @return
     */
    public String getArtist() {
        return artist;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    public String getLyrics() {
        return lyrics;
    }

    /**
     *
     * @return
     */
    public int getCompares() {
        return compares;
    }

    /**
     *
     */
    public void resetCmpCnt() {
        this.cmpCnt = 0;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        //return artist + ", \"" + title + "\"\n" + lyrics +"\n";
        return artist + ", \"" + title + "\"";
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public int compareTo(Song t) {
        compares++; //counts how many times the method gets called
        final int BEFORE = -1; // not needed, no numeric data in object
        final int EQUAL = 0;
        final int AFTER = 1;

        if (t == null) {
            return AFTER; // if object is null put it at the end
        }                 //this optimization is usually worthwhile, and can
        //always be added ... if the addresses are the same they are equal
        if (this == t) {
            return EQUAL;
        }

        int comparison = this.artist.compareToIgnoreCase(t.artist);
        if (comparison != EQUAL) {
            return comparison;
        }

        comparison = this.title.compareToIgnoreCase(t.title);
        if (comparison != EQUAL) {
            return comparison;
        }

        return EQUAL;
    }

    /**
     * Compares two Songs and counts the number of comparisons
     */
    public static class CompareArtist implements Comparator<Song>, CmpCnt {

        int cmpCnt;

        CompareArtist() {
            cmpCnt = 0;
        }

        @Override
        public int getCmpCnt() {
            return cmpCnt;
        }

        @Override
        public void resetCmpCnt() {
            this.cmpCnt = 0;
        }

        /**
         *
         * @param s1
         * @param s2
         * @return
         */
        @Override
        public int compare(Song s1, Song s2) {
            cmpCnt++;
            String str1;
            //System.out.println(s1.artist + " being compared to " + s2.artist); // debugging
            if (s2.artist.length() <= s1.artist.length()) {
                str1 = s1.artist.toLowerCase().substring(0, s2.artist.length());
                //return s1.artist.compareToIgnoreCase(s2.artist); //returns integer returned by compareTo
                return str1.compareTo(s2.artist);
            } else {
                str1 = s1.artist;
            }
            return str1.compareTo(s2.artist);
        }
    }
    
    /**
     * Compares two titles
     */
    public static class CompareTitle implements Comparator<Song>, CmpCnt {
         int cmpCnt;

        CompareTitle() {
            cmpCnt = 0;
        }

        @Override
        public int getCmpCnt() {
            return cmpCnt;
        }

        @Override
        public void resetCmpCnt() {
            this.cmpCnt = 0;
        }

        @Override
        public int compare(Song s1, Song s2) {
            cmpCnt++;
            String str1;
            //System.out.println(s1.title + " being compared to " + s2.title); // debugging
            if (s2.title.length() <= s1.title.length()) {
                str1 = s1.title.toLowerCase().substring(0, s2.title.length());
                //return s1.artist.compareToIgnoreCase(s2.artist); //returns integer returned by compareTo
                return str1.compareToIgnoreCase(s2.title);
            } else {
                str1 = s1.title;
            }
            return str1.compareToIgnoreCase(s2.title);
            //return s1.getTitle().compareToIgnoreCase(s2.getTitle());
        }
    }

    /**
     * Testing method
     *
     * @param args
     */
    public static void main(String[] args) {
        Song s1 = new Song("Metallica", "Enter Sandman", "Say your prayers, little one\n"
                + "Don't forget, my son\n"
                + "To include everyone");
        Song s2 = new Song("Guns N' Roses", "Sweet Child O' Mine", "She's got a smile that it seems to me\n"
                + "Reminds me of childhood memories\n"
                + "Where everything\n"
                + "Was as fresh as the bright blue sky");
        System.out.println(s1.toString());
        System.out.println(s2.toString());
    }
}
