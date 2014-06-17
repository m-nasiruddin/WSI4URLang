package org.getalp.wsi.lesk;

/**
 * Created by Mohammad on 20/05/2014.
 */

import org.getalp.wsi.CorpusIface;
import org.getalp.wsi.generator.LineCorpus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class Lesk {
    public Map<Integer, String> hMSenses = new HashMap<>();
    public Map<Integer, Integer> hMSynsetIDs = new HashMap<>();
    public Map<Integer, String> hMDefinitions = new HashMap<>();
    public Map<Integer, String> hMExamples = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        Lesk at = new Lesk();
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
            //System.out.println(ssl);

            Integer senseIDs = Integer.parseInt(sensesEntry[0]);
            String senses = sensesEntry[1];
            String posTags = sensesEntry[2];
            Integer synsetIDs = Integer.parseInt(sensesEntry[3]);

            hMSenses.put(senseIDs, senses);
            hMSynsetIDs.put(senseIDs, synsetIDs);
        }
    }

    public void loadGlosses(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String chrs = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of Glosses are loaded...");
        for (String chr : lc.getAllSentences()) {
            String[] glossesEntry = chr.split("\t");
            //System.out.println(ssl);

            Integer synsetIDs = Integer.parseInt(glossesEntry[0]);
            String definitions = glossesEntry[1];
            String examples = glossesEntry[2];

            hMDefinitions.put(synsetIDs, definitions);
            hMExamples.put(synsetIDs, examples);
        }
    }

    public void processCorpus(List<String> documents) throws FileNotFoundException {
        for (String sent : documents) {
            String line = "";
            for (String w : sent.split(" ")) {
                if (hMSenses.containsValue(w)) {
                    //Integer synsetID = hMSenses.get(w);
                    System.out.println(w);
                    //System.out.println(synsetID);
                }
            }
        }

    }
}