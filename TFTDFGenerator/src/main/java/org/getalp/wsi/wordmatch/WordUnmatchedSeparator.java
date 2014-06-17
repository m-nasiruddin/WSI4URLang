package org.getalp.wsi.wordmatch;

/**
 * Created by Mohammad on 20/05/2014.
 * This class load a text corpus read line by line then word by word,
 * search each word in the Bangla WordNet senses. It a word is matched
 * then it prints in a MatchedWords.txt file.
 */

import org.getalp.wsi.CorpusIface;
import org.getalp.wsi.generator.LineCorpus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class WordUnmatchedSeparator {
    public Map<String, Integer> hMSenses = new HashMap<>();
    public Map<Integer, Integer> hMSynsetIDs = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        WordUnmatchedSeparator at = new WordUnmatchedSeparator();
        if (args.length > 1) {
            at.loadSenses(args[1]);
        }
        at.processCorpus(sts);
    }

    public void loadSenses(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String chrs = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of Senses are loaded...");
        for (String chr : lc.getAllSentences()) {
            String[] sensesEntry = chr.split("\t");

            Integer senseIDs = Integer.parseInt(sensesEntry[0]);
            String senses = sensesEntry[1];
            String posTags = sensesEntry[2];
            Integer synsetIDs = Integer.parseInt(sensesEntry[3]);

            hMSenses.put(senses, senseIDs);
            hMSynsetIDs.put(senseIDs, synsetIDs);
        }
    }

    public void processCorpus(List<String> documents) throws FileNotFoundException {
        PrintStream matched = new PrintStream("MatchedWords.txt");
        PrintStream matched1CharLess = new PrintStream("MatchedWords1CharLess.txt");
        PrintStream matched2CharsLess = new PrintStream("MatchedWords2CharsLess.txt");
        PrintStream unmatched = new PrintStream("UnmatchedWords.txt");
        PrintStream statistics = new PrintStream("WordMatchingStatistics.txt");

        Integer totalSentences = 0;
        Integer totalWords = 0;
        Integer totalMatched = 0;
        Integer total1CharLess = 0;
        Integer total2CharLess = 0;
        Integer totalUnmatched = 0;

        for (String sent : documents) {
            totalSentences++;
            for (String w : sent.split(" ")) {
                totalWords++;
                if (hMSenses.containsKey(w)) {
                    matched.println(w);
                    totalMatched++;
                } else {
                    boolean submatched = false;
                    if (w.length() > 1) {
                        String x = w.substring(0, w.length() - 1);
                        if (hMSenses.containsKey(x)) {
                            matched1CharLess.println(x + "\t" + w);
                            total1CharLess++;
                            submatched = true;
                        } else {
                            if (w.length() > 2) {
                                String y = w.substring(0, w.length() - 2);
                                if (hMSenses.containsKey(y)) {
                                    matched2CharsLess.println(y + "\t" + w);
                                    total2CharLess++;
                                    submatched = true;
                                }
                            }
                        }
                    }
                    if (!submatched) {
                        unmatched.println(w);
                        totalUnmatched++;
                    }
                }
            }
        }
        String a = "Statistics\n----------\n\t\t\tNo of Sentences in the corpus: " + totalSentences;
        String b = "\t\t\tNo of Words in the corpus\t : " + totalWords + " (100%)";
        String c = "\n# of Words Matched in the Bangla WordNet (exact match)\t: " + totalMatched + " (" + (totalMatched * 100) / totalWords + "%)";
        String d = "# of Words Matched in the Bangla WordNet (1 Char less)\t: " + total1CharLess + " (" + (total1CharLess * 100) / totalWords + "%)";
        String e = "# of Words Matched in the Bangla WordNet (2 Chars less) : " + total2CharLess + " (" + (total2CharLess * 100) / totalWords + "%)";
        Integer grandTotalWords = totalMatched + total1CharLess + total2CharLess;
        String f = "----------------------------------------------------------------------\n" +
                "Total No of Words Matched in the Bangla WordNet\t\t\t: " + grandTotalWords + " (" + (grandTotalWords * 100) / totalWords + "%)";
        String g = "\n\tNo of Words Unmatched in the Bangla WordNet\t\t\t: " + totalUnmatched + " (" + (totalUnmatched * 100) / totalWords + "%)";

        statistics.println(a);
        statistics.println(b);
        statistics.println(c);
        statistics.println(d);
        statistics.println(e);
        statistics.println(f);
        statistics.println(g);

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
        System.out.println(e);
        System.out.println(f);
        System.out.println(g);

        matched.flush();
        matched1CharLess.flush();
        matched2CharsLess.flush();
        unmatched.flush();
        statistics.flush();
        matched.close();
        matched1CharLess.close();
        matched2CharsLess.close();
        unmatched.close();
        statistics.close();
    }
}