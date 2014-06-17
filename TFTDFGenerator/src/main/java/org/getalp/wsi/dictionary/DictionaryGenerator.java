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
public class DictionaryGenerator {
    public Map<String, List<Integer>> hMSynsetIDs = new HashMap<>();
    public Map<String, Integer> hMNumbers = new HashMap<>();
    public Map<Integer, String> hMWords = new HashMap<>();
    public Map<Integer, String> hMDefinitions = new HashMap<>();
    public Map<Integer, String> hMExamples = new HashMap<>();
    public Map<String, Integer> revHMDefinitions = new HashMap<>();
    public Map<String, Integer> revHMExamples = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        DictionaryGenerator at = new DictionaryGenerator();
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
            Integer synsetID = Integer.parseInt(sensesEntry[3]);
            if (!hMSynsetIDs.containsKey(sense)) {
                hMSynsetIDs.put(sense, new ArrayList<Integer>());
            }
            hMSynsetIDs.get(sense).add(synsetID);
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
            revHMDefinitions.put(definition, synsetID);
            revHMExamples.put(example, synsetID);
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
        PrintStream dictionary = new PrintStream("Dictionary.txt");
        dictionary.println("<dict>");
        for (String word : hMSynsetIDs.keySet()) {
            dictionary.println("<word tag=\"" + word + "%v\">");
            dictionary.println("<sense>");
            for (Integer synsetID : hMSynsetIDs.get(word)) {
                dictionary.println("<ids>" + word + "%" + synsetID + "</ids>");
                String definition = hMDefinitions.get(synsetID);
                dictionary.print("<def> ");
                List<Integer> newdef = new ArrayList<>();
                for (String defword : definition.split(" ")) {
                    if (hMNumbers.containsKey(defword)) {
                        newdef.add(hMNumbers.get(defword));
                    }
                }
                Collections.sort(newdef);
                for (Integer dw : newdef) {
                    dictionary.print(dw + " ");
                }
                dictionary.println("</def>");
            }
            dictionary.println("</sense>");
            dictionary.println("</word>");
        }
        dictionary.println("</dict>");
        dictionary.flush();
        dictionary.close();
    }
}