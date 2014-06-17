package org.getalp.wsi.semeval;

/**
 * Created by Mohammad on 31/05/2014.
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
public class SemEvalFormat {
    public Map<Integer, String> hMSenses = new HashMap<>();
    public Map<String, String> hMPOSTags = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        SemEvalFormat at = new SemEvalFormat();
        if (args.length > 1) {
            at.loadSenses(args[1]);
        }
        at.generateSemEvalFormat(sts);
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
            hMPOSTags.put(senses, posTags);
        }
    }

    public void generateSemEvalFormat(List<String> documents) throws FileNotFoundException {
        PrintStream semeval = new PrintStream("bng-coarse-all-words.xml");

        semeval.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<!DOCTYPE corpus SYSTEM \"coarse-all-words.dtd\">\n" +
                "<corpus lang=\"bn\">");
        semeval.println("<text id=\"d001\">");
        Integer i = 1;
        for (String paragraphs : documents) {
            String[] sentences = paragraphs.split("\n");
            for (String w : sentences) {
                String sentencenumber = String.format("%03d", i);
                String idumn = "d001.s" + sentencenumber;
                semeval.println("<sentence id=\"d001.s" + sentencenumber + "\">");
                String[] words = w.split(" ");
                Integer j = 1;
                for (String x : words) {
                    if (hMSenses.containsValue(x)) {
                        String tnum = String.format("%03d", j);
                        semeval.println("<instance id=\"" + idumn + ".t" + tnum + "\" lemma=\"" + x + "\" pos=\"a\">" + x + "</instance>");
                        j++;
                    } else {
                        semeval.println(x);
                    }
                }
                i++;
                semeval.println("</sentence>");
            }
        }
        semeval.println("</text>");
        semeval.println("</corpus>");
        semeval.flush();
        semeval.close();
    }
}