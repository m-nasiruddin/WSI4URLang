package org.getalp.wsi.dictionary;

/**
 * Created by Mohammad on 31/05/2014.
 */

import org.getalp.wsi.generator.LineCorpus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("ALL")
public class DictionaryGeneratorWithPOSTag {
    public Map<String, List<Integer>> hMSynsetIDs = new HashMap<>();
    public Map<String, List<String>> hMPOSTags = new HashMap<>();
    public Map<String, Integer> hMNumbers = new HashMap<>();
    public Map<Integer, String> hMWords = new HashMap<>();
    public Map<Integer, String> hMDefinitions = new HashMap<>();
    public Map<Integer, String> hMExamples = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        DictionaryGeneratorWithPOSTag at = new DictionaryGeneratorWithPOSTag();
        if (args.length > 2) {
            at.loadSenses(args[0]);
            at.loadGlosses(args[1]);
            at.loadWords(args[2]);
        }
        at.generateDictionary();
    }

    public void loadSenses(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String chrs = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of Senses are loaded...");
        for (String chr : lc.getAllSentences()) {
            String[] sensesEntry = chr.split("\t");
            String sense = sensesEntry[1];
            String posTag = sensesEntry[2];
            Integer synsetID = Integer.parseInt(sensesEntry[3]);
            if (!hMSynsetIDs.containsKey(sense)) {
                hMSynsetIDs.put(sense, new ArrayList<Integer>());
            }
            hMSynsetIDs.get(sense).add(synsetID);
            if (!hMPOSTags.containsKey(sense)) {
                hMPOSTags.put(sense, new ArrayList<String>());
            }
            hMPOSTags.get(sense).add(posTag);
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
            String example = sensesEntry[2];
            hMDefinitions.put(synsetID, definition);
            hMExamples.put(synsetID, example);
        }
    }

    public void loadWords(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String chrs = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of Words are loaded...");
        for (String chr : lc.getAllSentences()) {
            String[] sensesEntry = chr.split("\t");
            Integer number = Integer.parseInt(sensesEntry[0]);
            String word = sensesEntry[1];
            hMWords.put(number, word);
            hMNumbers.put(word, number);
        }
    }

    public void generateDictionary() throws FileNotFoundException {
        System.out.println("Words printed below are missing words (from definitions and examples) in the Index file.\n" +
                "[Blank means few words are just white spaces.]");
        PrintStream dictionary = new PrintStream("dictionary.xml");
        dictionary.println("<dict>");
        for (String word : hMSynsetIDs.keySet()) {
            int tagindex = 0;
            for (String posTag : hMPOSTags.get(word)) {
                dictionary.println("<word tag=\"" + word + "%" + posTag.toLowerCase() + "\">");
                dictionary.println("<sense>");
                Integer synsetID = hMSynsetIDs.get(word).get(tagindex);
                dictionary.println("<ids>" + word + "%" + synsetID + "</ids>");
                String definition = hMDefinitions.get(synsetID);
                String example = hMExamples.get(synsetID);
                dictionary.print("<def> ");
                List<Integer> gloss = new ArrayList<>();
                for (String defword : definition.split(" ")) {
                    if (hMNumbers.containsKey(defword)) {
                        gloss.add(hMNumbers.get(defword));
                    } else System.out.println("Missing defwords in the Index file: " + defword);
                }
                for (String exaword : example.split(" ")) {
                    if (hMNumbers.containsKey(exaword)) {
                        gloss.add(hMNumbers.get(exaword));
                    } else System.out.println("Missing exawords in the Index file: " + exaword);
                }
                Collections.sort(gloss);
                for (Integer dw : gloss) {
                    dictionary.print(dw + " ");
                }
                dictionary.println("</def>");

                tagindex++;
            }
            dictionary.println("</sense>");
            dictionary.println("</word>");
        }
        dictionary.println("</dict>");
        dictionary.flush();
        dictionary.close();
    }
}