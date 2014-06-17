package org.getalp.wsi.wordmatch;

/**
 * Created by Mohammad on 20/05/2014.
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
public class MultiwordMatch {
    public Map<Integer, String> hMSenses = new HashMap<>();
    public Map<String, Integer> hMSenseIDs = new HashMap<>();
    public Map<Integer, String> hMPOSTags = new HashMap<>();
    public Map<Integer, Integer> hMSynsetIDs = new HashMap<>();
    public Map<Integer, String> hMPOSTagsSynsetIDs = new HashMap<>();
    public Map<Integer, String> hMDefinitions = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The n-gram is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        MultiwordMatch at = new MultiwordMatch();
        if (args.length > 2) {
            at.loadSenses(args[1]);
            at.loadGlosses(args[2]);
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

            Integer senseID = Integer.parseInt(sensesEntry[0]);
            String sense = sensesEntry[1];
            String posTag = sensesEntry[2];
            Integer synsetID = Integer.parseInt(sensesEntry[3]);

            //hMSenseIDs.put(sense, senseID);
            hMSenses.put(senseID, sense);
            hMPOSTags.put(senseID, posTag);
            hMSynsetIDs.put(senseID, synsetID);
            hMPOSTagsSynsetIDs.put(synsetID, posTag);
        }
    }

    public void loadGlosses(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String chrs = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of Glosses are loaded...");
        for (String chr : lc.getAllSentences()) {
            String[] sensesEntry = chr.split("\t");

            Integer synsetID = Integer.parseInt(sensesEntry[0]);
            String definition = sensesEntry[1];
            String gloss = sensesEntry[2];

            hMDefinitions.put(synsetID, definition);
            //hMUniqueSenses.put(senses, senseIDs);
            //hMSynsetIDs.put(senseIDs, synsetIDs);
        }
    }

    public void processCorpus(List<String> expressions) throws FileNotFoundException {
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
        Integer single = 0;
        Integer multi = 0;
        Integer single1Chars = 0;
        Integer multi1Chars = 0;
        Integer single2Chars = 0;
        Integer multi2Chars = 0;

        for (String sent : expressions) {
            totalSentences++;
            String[] words = sent.split("\t");
            totalWords++;
            if (hMSenseIDs.containsKey(words[0])) {
                if (!words[0].trim().contains("_"))
                    single++;
                else multi++;
                matched.println(sent);
                totalMatched++;
            } else {
                boolean submatched = false;
                if (words[0].length() > 1) {
                    String x = words[0].substring(0, words[0].length() - 1);
                    if (hMSenseIDs.containsKey(x)) {
                        if (!x.trim().contains("_"))
                            single1Chars++;
                        else multi1Chars++;
                        matched1CharLess.println(sent);
                        total1CharLess++;
                        submatched = true;
                    } else {
                        if (words[0].length() > 2) {
                            String y = words[0].substring(0, words[0].length() - 2);
                            if (hMSenseIDs.containsKey(y)) {
                                if (!y.trim().contains("_"))
                                    single2Chars++;
                                else multi2Chars++;
                                matched2CharsLess.println(sent);
                                total2CharLess++;
                                submatched = true;
                            }
                        }
                    }
                }
                if (!submatched) {
                    unmatched.println(sent);
                    totalUnmatched++;
                }
            }
        }
        String a = "Statistics\n----------\n\t\tNo of words in Bangla WordNet\t\t: " + hMSenses.size() + " (100%)";
        String b = "\t\tNo of unique words in Bangla WordNet: " + hMSenseIDs.size() + " (" + (hMSenseIDs.size() * 100) / hMSenses.size() + "%)";
        String c = "\t\tNo of Synsets in Bangla WordNet\t\t: " + hMDefinitions.size() + " (100%)";
        Integer synsetNoun = 0;
        Integer synsetVerb = 0;
        Integer synsetAdjective = 0;
        Integer synsetAdverb = 0;
        for (String count : hMPOSTagsSynsetIDs.values()) {
            if ((count.trim().contains("N")))
                synsetNoun++;
            else if ((count.trim().contains("V")))
                synsetVerb++;
            else if ((count.trim().contains("J")))
                synsetAdjective++;
            else
                synsetAdverb++;
        }
        String d = "\t# of Nouns\t\t: " + synsetNoun;
        String e = "\t# of Verbs\t\t: " + synsetVerb;
        String f = "\t# of Adjectives\t: " + synsetAdjective;
        String g = "\t# of Adverbs\t: " + synsetAdverb + "\n\tTotal\t\t\t: " + (synsetNoun + synsetVerb + synsetAdjective + synsetAdverb);
        Integer polysemous = 0;
        Integer monosemous = 0;
        Map<Integer, Integer> hMCounts = new HashMap<>();
        for (Integer count : hMSynsetIDs.values()) {
            if (hMCounts.containsKey(count)) {
                Integer cnt = hMCounts.get(count);
                hMCounts.put(count, cnt + 1);
            } else hMCounts.put(count, 1);
        }
        for (Integer count : hMCounts.values()) {
            if (count == 1)
                monosemous++;
            else polysemous++;
        }
        String h = "\t\t\tNo of polysemous words\t: " + polysemous + " (" + (polysemous * 100) / (polysemous + monosemous) + "%)";
        String i = "\t\t\tNo of monosemous words\t: " + monosemous + " (" + (monosemous * 100) / (polysemous + monosemous) + "%)" + "\n\t\t\tTotal\t\t\t\t\t: " + (polysemous + monosemous) + " (100%)";
        Integer singleword = 0;
        Integer multiword = 0;
        for (String count : hMSenses.values()) {
            if (!(count.trim().contains("_")))
                singleword++;
            else multiword++;
        }
        String j = "# of Single Word Expressions: " + singleword + " (" + (singleword * 100) / hMSenses.size() + "%)";
        String k = "# of Multiword Expressions\t: " + multiword + " (" + (multiword * 100) / hMSenses.size() + "%)";
        Integer noun = 0;
        Integer verb = 0;
        Integer adjective = 0;
        Integer adverb = 0;
        for (String count : hMPOSTags.values()) {
            if ((count.trim().contains("N")))
                noun++;
            else if ((count.trim().contains("V")))
                verb++;
            else if ((count.trim().contains("J")))
                adjective++;
            else
                adverb++;
        }
        String l = "\t# of Nouns\t\t: " + noun + " (" + (noun * 100) / hMSenses.size() + "%)";
        String m = "\t# of Verbs\t\t: " + verb + " (" + (verb * 100) / hMSenses.size() + "%)";
        String n = "\t# of Adjectives\t: " + adjective + " (" + (adjective * 100) / hMSenses.size() + "%)";
        String o = "\t# of Adverbs\t: " + adverb + " (" + (adverb * 100) / hMSenses.size() + "%) " + "\n\tTotal\t\t\t: " + (noun + verb + adjective + adverb);
        String p = "\n\t\tNo of sentences in the corpus\t: " + totalSentences;
        String q = "\t\tNo of words in corpus\t\t\t: " + totalWords + " (100%)";
        String r = "\n(exact match)\n# of Single Word Expressions matched: " + single + " (" + (single * 100) / singleword + "%)";
        String s = "# of Multiword Expressions matched\t: " + multi + " (" + (multi * 100) / multiword + "%)";
        String t = "Total no of words matched in Bangla WordNet (exact match)\t: " + totalMatched + " (" + (totalMatched * 100) / totalWords + "%)";
        String u = "\n(1 character less match)\n# of Single Word Expressions matched: " + single1Chars + " (" + (single1Chars * 100) / singleword + "%)";
        String v = "# of Multiword Expressions matched\t: " + multi1Chars + " (" + (multi1Chars * 100) / multiword + "%)";
        String w = "Total no of words matched in Bangla WordNet (1 char less)\t: " + total1CharLess + " (" + (total1CharLess * 100) / totalWords + "%)";
        String x = "\n(2 characters less match)\n# of Single Word Expressions matched: " + single2Chars + " (" + (single2Chars * 100) / singleword + "%)";
        String y = "# of Multiword Expressions matched\t: " + multi2Chars + " (" + (multi2Chars * 100) / multiword + "%)";
        String z = "Total no of words matched in Bangla WordNet (2 chars less)\t: " + total2CharLess + " (" + (total2CharLess * 100) / totalWords + "%)";
        Integer grandTotalWords = totalMatched + total1CharLess + total2CharLess;
        String aa = "----------------------------------------------------------------------\n" +
                "Total no of words matched in Bangla WordNet\t\t\t\t\t: " + grandTotalWords + " (" + (grandTotalWords * 100) / totalWords + "%)";
        String ab = "\n\tNo of words unmatched in Bangla WordNet\t\t\t\t\t: " + totalUnmatched + " (" + (totalUnmatched * 100) / totalWords + "%)";

        statistics.println(a);
        statistics.println(b);
        statistics.println(c);
        statistics.println(d);
        statistics.println(e);
        statistics.println(f);
        statistics.println(g);
        statistics.println(h);
        statistics.println(i);
        statistics.println(j);
        statistics.println(k);
        statistics.println(l);
        statistics.println(m);
        statistics.println(n);
        statistics.println(o);
        statistics.println(p);
        statistics.println(q);
        statistics.println(r);
        statistics.println(s);
        statistics.println(t);
        statistics.println(u);
        statistics.println(v);
        statistics.println(w);
        statistics.println(x);
        statistics.println(y);
        statistics.println(z);
        statistics.println(aa);
        statistics.println(ab);

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

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
        System.out.println(e);
        System.out.println(f);
        System.out.println(g);
        System.out.println(h);
        System.out.println(i);
        System.out.println(j);
        System.out.println(k);
        System.out.println(l);
        System.out.println(m);
        System.out.println(n);
        System.out.println(o);
        System.out.println(p);
        System.out.println(q);
        System.out.println(r);
        System.out.println(s);
        System.out.println(t);
        System.out.println(u);
        System.out.println(v);
        System.out.println(w);
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
        System.out.println(aa);
        System.out.println(ab);
    }
}