
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author David Heywood
 */
public class SearchByLyricsWords {

    private Song[] songs;  // keep a direct reference to the song array    
    private TreeMap<String, TreeSet<Song>> lyricsSongMap; // map of key lyric words and song sets
    private TreeSet<String> commonWords;  // keep a direct reference to common words found in songs   
    private int insertion = 0;

    /**
     * constructor takes in a songCollection object
     *
     * @param sc a variable that holds allSongs.txt
     */
    public SearchByLyricsWords(SongCollection sc) {
        songs = sc.getAllSongs(); // a copy of all the songs in allSongs.txt
        lyricsSongMap = new TreeMap<String, TreeSet<Song>>();
        createCommonWordsTreeSet();
        createSongLyricsTreeSet();
        statisticsGathering();
    }

    public TreeSet<String> getCommonWords() {
        return commonWords;
    }

    public TreeMap<String, TreeSet<Song>> getLyricsSongMap() {
        return lyricsSongMap;
    }

    /**
     * Checks to see if commonWords.txt is present If commonWords.txt is present add each word in the text file and add
     * them to the commonWordsTreeSet so it can be used to parse the lyrics of songs later on
     */
    public void createCommonWordsTreeSet() {
        Scanner in = null;
        try {
            in = new Scanner(new FileReader("commonWords.txt"));
        } catch (FileNotFoundException ex) {
            System.out.println("File not found.");
            System.exit(1);
        }

        commonWords = new TreeSet<String>();
        String word;
        while (in.hasNext()) {
            word = in.next();
            commonWords.add(word);
        }
    }

    /**
     * Parse each word in the current lyrics and remove all common words, punctuation and numbers Add the edited lyrics
     * to the songLyrics TreeSet
     */
    public void createSongLyricsTreeSet() {
        // set of lyrics without common words, puncuation and numbers
        TreeSet<String> songLyrics = new TreeSet<String>();
        for (Song s : songs) { // loops through the Song[]
            String lyrics = s.getLyrics().toLowerCase();
            for (String lyricsWord : lyrics.split("[^a-zA-Z]+")) {
                if (lyricsWord.length() > 1) {
                    songLyrics.add(lyricsWord);
                }
            }
            songLyrics.removeAll(commonWords);
            insertion = createSongTreeMap(songLyrics, s);
            songLyrics.clear();
        }
    }

    /**
     *
     * @param songLyrics a copy of the lyrics to one song with commonWords, punctuation and numbers removed
     * @param s a copy of one songs with artist, title and lyrics
     * @return
     */
    public int createSongTreeMap(TreeSet<String> songLyrics, Song s) {
        Iterator<String> lyricsIterator = songLyrics.iterator();

        while (lyricsIterator.hasNext()) {
            String lyricsWord = lyricsIterator.next();
            TreeSet<Song> songsSet = new TreeSet<Song>();
            if (lyricsSongMap.containsKey(lyricsWord)) {
                songsSet = lyricsSongMap.get(lyricsWord);
                songsSet.add(s);
                lyricsSongMap.put(lyricsWord, songsSet);
                insertion++;
            } else {
                songsSet.add(s);
                lyricsSongMap.put(lyricsWord, songsSet);
                insertion++;
            }
        }
        return insertion;
    }

    /**
     * Argument is a string containing lyrics words, and uses the map to conduct the search search terms are parsed and
     * common words, single letters, punctuation and numbers should be ignored
     *
     * @param lyricsWords A String that contains a set of words to search for in the list of songs
     * @return a Song[] of all the songs that match the lyrics search criteria
     */
    public Song[] search(String lyricsWords) {
        Scanner searchWordsIterator = new Scanner(lyricsWords.toLowerCase());
        TreeSet<String> wordsToSearchFor = new TreeSet<String>(); // lyric words to search for
        TreeSet<Song> songMatches = new TreeSet<Song>(); // set of songs that match search criteria

        while (searchWordsIterator.hasNext()) {
            // findWord = single word out of a set of lyrics words to search for
            String findWord = searchWordsIterator.next();
            wordsToSearchFor.add(findWord);
        }

        wordsToSearchFor.removeAll(commonWords);

        Iterator<String> wordsToSearch = wordsToSearchFor.iterator();
        while (wordsToSearch.hasNext()) {
            String wordToSearchFor = (String) wordsToSearch.next();
            TreeSet<Song> foundSongs = new TreeSet<Song>();
            if (lyricsSongMap.containsKey(wordToSearchFor) == true) {
                foundSongs = lyricsSongMap.get(wordToSearchFor);
                if (songMatches.size() == 0) {
                    songMatches.addAll(foundSongs);
                } else if (songMatches.size() > 1) {
                    songMatches.retainAll(foundSongs);
                }
            }
        }

        // create an array that is the right size and convert to an array of Songs[]
        Song[] sa = songMatches.toArray(new Song[songMatches.size()]);

        return sa;
    }

    // Gather statistics and print them
    public void statisticsGathering() {
        System.out.println("\nStatistics Gathering: ");
        System.out.println("Total indexing terms: " + insertion);
        double indexAvgPerSong = insertion / songs.length;
        System.out.println("Average number of indexing terms per song:  " + indexAvgPerSong);
        Set<String> totalNumKeys = lyricsSongMap.keySet();
        int NumMapKeys = totalNumKeys.size();
        Collection<TreeSet<Song>> totalValues = lyricsSongMap.values();
        int totalValuesSize = totalValues.size();
        System.out.println("Number of keys in the map: " + NumMapKeys);
        System.out.println("The total number of values in the TreeMap is: " + totalValuesSize);

        int numOfMapRef = 0;
        for (TreeSet<Song> value : lyricsSongMap.values()) {
            int numPerWord = value.size();
            numOfMapRef += numPerWord;
        }
        System.out.println("Total mapped song references: " + numOfMapRef);
        double avgReg = numOfMapRef / NumMapKeys;
        System.out.println("Average number of song references per key: " + avgReg);
    }

    /**
     * Testing Routine
     *
     * @param args
     */
    public static void main(String[] args) {
        SongCollection sc = new SongCollection("allSongs.txt");
        SearchByLyricsWords sblw = new SearchByLyricsWords(sc);
        String lyricsWords = "she loves my car";

        //Show first ten songs
        System.out.println("\nSearching for: " + lyricsWords);
        Song[] byLyricsResults = sblw.search(lyricsWords);
        System.out.println("The total number of songs is " + byLyricsResults.length);

        System.out.println("First ten songs that match the search criteria: ");
        if (byLyricsResults.length > 10) {
            for (int i = 0; i < 10; i++) {
                System.out.println(byLyricsResults[i]);
            }
        } else {
            for (int i = 0; i <= byLyricsResults.length - 1; i++) {
                System.out.println(byLyricsResults[i]);
            }
        }
    }
}
