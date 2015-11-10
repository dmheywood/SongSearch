
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Date April 27, 2015
 * @author David Heywood
 */
public class SearchByLyricsPhrase {
    
    // array of songs where the lyrics of that song contains every word in the phrase being search for
    private Song[] byLyricsResults;
    private static TreeMap<Integer, TreeSet<Song>> phraseSongMap; // map of key song ranking and song sets
    private TreeSet<Song> songMatch; // holds a song where the lyrics contains the phrase in order
    private SearchByLyricsWords sblw;
    private ArrayList<Song> songMatchList; // holds all songs where the lyrics contains the phrase in order

    /**
     * holds the ranking of song 
     */
    public int rank;     

    /**
     *
     * @param sc
     */
        public SearchByLyricsPhrase(SongCollection sc) {
        sblw = new SearchByLyricsWords(sc);
    }

    /**
     *
     * @param lyricsPhrase
     * @return
     */
    public Song[] search(String lyricsPhrase) {
        phraseSongMap = new TreeMap<>(); // holds Song objects and the ranks(key) for those Songs
        byLyricsResults = sblw.search(lyricsPhrase); // uses search method in sblw class and returns the results
        songMatchList = new ArrayList<>(); // holds all the Song objects with lyrics that match the phrase
        for (Song s : byLyricsResults) { // grabs a Song object from byLyricsResults ArrayList to search
            songMatch = new TreeSet<>(); // holds a Song ob ject where the lyrics contains the phrase in order
            songMatch.clear(); // clears current Song object from the TreeSet
            rank = 0; // initialize the rank
            int fromIndex = 0; // holds the index value where the next search will start from
            int indexVal = 0; // holds the index value of the word that was found           

            String lyrics = s.getLyrics().toLowerCase(); // converts lyrics from song object to lower case
            // ArrayList that holds all the indexs where the first word in the phrase was found
            ArrayList<Integer> firstPhraseWord = new ArrayList<Integer>();
            ArrayList<String> phrase = new ArrayList<String>(); // ArrayList that will hold the entire phrase
            phrase.addAll(Arrays.asList(lyricsPhrase.split(" "))); // adds the phrase to the ArrayList
            // findWord = single word out of lyricsPhrase to search for
            String firstWord = phrase.get(0); // grabs the first word from the phrase ArrayList
            for (int i = 0; i < lyrics.length(); i++) {
                indexVal = lyrics.indexOf(firstWord, fromIndex); // find current word and return its index
                fromIndex = indexVal + firstWord.length(); // sets the next starting index for search                
                if (indexVal == -1) { // if indexVal == -1 search reached end of lyrics
                    break;
                } else if (lyrics.charAt(fromIndex) == 32) { // Checks if index after firstWord is a space
                    firstPhraseWord.add(indexVal); // Adds the index value from every firstWord found in lyrics
                }
            }

            rank = rankSong(lyrics, phrase, firstPhraseWord); // calls rankSong to find smallest rank for this lyric
            if (rank == 0) {
                continue;
            }
            if (phraseSongMap.containsKey(rank)) { // if rank(key) already exists add song to rank(key)
                songMatch = phraseSongMap.get(rank);
                songMatch.add(s); // add Song object that matches phrase to SongMatch
                songMatchList.add(s); // create a list of songMatches
                phraseSongMap.put(rank, songMatch); // add rank(key) and Song object to TreeMap
            } else {
                songMatch.add(s); // add Song object that matches phrase to SongMatch
                songMatchList.add(s); // create a list of songMatches
                phraseSongMap.put(rank, songMatch); // add rank(key) and Song object to TreeMap
            }
        }

        Song[] results = new Song[songMatchList.size()]; // create Song[] size to hold list of matching songs
        results = songMatchList.toArray(results); // copy contents of songMatchList ArrayList to Song[] results        

        return results;
    }

    /**
     *
     * @param lyrics contains the lyrics from one song
     * @param phrase contains the phrase being searched for in the lyrics
     * @param firstPhraseWord contains all the indexes where the first word in the phrase was found in the lyrics
     * @return the ranking for that lyrics
     */
    public int rankSong(String lyrics, ArrayList<String> phrase, ArrayList<Integer> firstPhraseWord) {
        int fromIndex = 0; // holds the index value where the next search will start from
        int indexVal = 0; // holds the index value of the word that was found
        int firstIndex = 0; // holds the index value of the first word found in the phrase
        int lastIndex = 0; // holds the index value of the last word fond in the phrase

        ArrayList<Integer> songRank = new ArrayList<Integer>();
        for (int i = 0; i < firstPhraseWord.size(); i++) {
            // sets starting search index by adding index value in firstPhraseWord to length of first word in phrase
            fromIndex = firstPhraseWord.get(i) + phrase.get(0).length(); 
            firstIndex = firstPhraseWord.get(i); // sets firstIndex value to be used to determine ranking
            // skips first word in phrase. We already found all the indexes for first word which are contained
            // in the ArrayList firstPhrasaeWord
            for (int j = 1; j < phrase.size(); j++) {
                // findWord = next word out of lyricsPhrase to search for
                String findWord = phrase.get(j);
                do {
                    indexVal = lyrics.indexOf(findWord, fromIndex); // find current word and return its index
                    if (indexVal == -1) { // if indexVal == -1 we have reached the end of the lyrics
                        break;
                    }
                    // change starting index from current index + length of search word
                    fromIndex = indexVal + findWord.length();
                  
                    if (indexVal == -1) { // if indexVal == -1 we have reached the end of the lyrics
                        break;
                    }
                // continue while loop if index after word found does not contain a space(32), period(46), LF(10),
                // apostrophe(44) or a question mark(63)
                } while (lyrics.charAt(fromIndex) != 32 && lyrics.charAt(fromIndex) != 46
                        && lyrics.charAt(fromIndex) != 10 && lyrics.charAt(fromIndex) != 44
                        && lyrics.charAt(fromIndex) != 63);

                // checks to see if beginning index for ranking has been set already
                if (indexVal == -1) { // if indexVal == -1 you reached the end of the lyrics
                    break; // break out of the loop
                }

                lastIndex = fromIndex; // set index value for last index

                if (indexVal == -1) { // if indexVal == -1 you reached the end of the lyrics
                    continue; // continue with loop
                }
                if (j == phrase.size() - 1) { // if j == phrase.size() - 1 we have reached the end of the phrase
                    rank = lastIndex - firstIndex; // determines this songs rank
                    songRank.add(rank); // add ranking of song to lyricsRank ArrayList
                }
            }
        }
        if (songRank.isEmpty()) { // if songRank.isEmpty() add zero to songRank so the return does not fail
            songRank.add(0);
        } else {
            Collections.sort(songRank); // sort the song rankings into a natural order
        }
        return songRank.get(0); // return songRank[0] which will be the lowest ranking for the song
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SongCollection sc = new SongCollection("allSongs.txt"); // place songs in text file into SongCollection
        String phraseToSearch = "she loves you"; // set phrase to search for
        SearchByLyricsPhrase sblp = new SearchByLyricsPhrase(sc);
        // calls search method with returns an arr of Songs that contain the phrase being serached for
        Song[] byTitlePhraseResult = sblp.search(phraseToSearch);
        System.out.println("\nsearching for: " + phraseToSearch);
        System.out.println("Total songs is " + byTitlePhraseResult.length + ", first 10 matches:");
        System.out.println("rank    artist      title");
        
        // for loop that prints the song rankings and the songs that have that rank
        for (Map.Entry<Integer, TreeSet<Song>> entry : phraseSongMap.entrySet()) {
            String key = entry.getKey().toString();
            TreeSet<Song> value = entry.getValue();            
            for (Song s : value) {
                System.out.println(key + "  " + s);
            }
        }
    }
}
