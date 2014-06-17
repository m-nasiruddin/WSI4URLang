package org.getalp.wsi.wikisenses;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class WikiSensesCollector {
    public Map<String, String> hMWikiTitles = new HashMap<>();
    public Map<String, String> hMWikiGlosses = new HashMap<>();
    public Map<String, String> hMWikiSenseTitles = new HashMap<>();
    public Map<String, String> hMWikiSenses = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The Wikipedia corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        WikiSensesCollector at = new WikiSensesCollector();
        String rex = "\\[\\[([^\\(\\)]*)(\\([^\\)]*\\))?\\]\\]";
        at.extractWikiGlosses(sts, rex);
        at.extractWikiSenses(sts, rex);
        at.createWikiNet();
    }

    public void extractWikiGlosses(List<String> documents, String rex) throws FileNotFoundException {
        PrintStream wikiGlosses = new PrintStream("WikiGlosses.txt");
        PrintStream withoutGlosses = new PrintStream("WordsWithoutGlosses.txt");
        Pattern p = Pattern.compile(rex);
        for (int i = 0; i < documents.size(); i++) {
            String sent = documents.get(i);
            Matcher m = p.matcher(sent);
            if (m.find() && (i < documents.size() - 1)) {
                String word = m.group(1);
                for (String w : word.split(" ")) {
                    if ((documents.get(i + 1).contains(w)) && (!documents.get(i + 1).startsWith("=")) && (!documents.get(i + 1).startsWith("*"))) {
                        wikiGlosses.println(sent);
                        //System.err.println(sent);
                        wikiGlosses.println(documents.get(i + 1));
                        //System.out.println(documents.get(i + 1));
                        hMWikiGlosses.put(documents.get(i + 1), sent);
                        hMWikiTitles.put(sent, documents.get(i + 1));
                        break;
                    } else
                        withoutGlosses.println(sent);
                    //System.out.println(sent);
                    break;
                }
            }
        }
        wikiGlosses.flush();
        withoutGlosses.flush();
        wikiGlosses.close();
        withoutGlosses.close();
    }

    public void extractWikiSenses(List<String> documents, String rex) throws FileNotFoundException {
        PrintStream wikiSenses = new PrintStream("WikiSenses.txt");
        Pattern p = Pattern.compile(rex);
        for (int i = 0; i < documents.size(); i++) {
            String sent = documents.get(i);
            Matcher m = p.matcher(sent);
            if (m.find() && (i < documents.size() - 1)) {
                if (documents.get(i + 1).startsWith("*") || documents.get(i + 2).startsWith("*")) {
                    if (documents.get(i + 1).startsWith("*")) {
                        wikiSenses.println(documents.get(i));
                        //System.err.println(documents.get(i));
                        wikiSenses.println(documents.get(i + 1));
                        //System.err.println(documents.get(i + 1));
                        hMWikiSenseTitles.put(documents.get(i), documents.get(i + 1));
                        hMWikiSenses.put(documents.get(i + 1), documents.get(i));
                        for (int j = i + 2; j < documents.size() - 1 && documents.get(j).startsWith("*"); j++) {
                            wikiSenses.println(documents.get(j));
                            //System.err.println(documents.get(j));
                            hMWikiSenseTitles.put(documents.get(i), documents.get(j));
                            hMWikiSenses.put(documents.get(j), documents.get(i));
                        }
                    } else {
                        wikiSenses.println(documents.get(i));
                        //System.err.println(documents.get(i));
                        wikiSenses.println(documents.get(i + 2));
                        //System.err.println(documents.get(i + 2));
                        hMWikiSenseTitles.put(documents.get(i), documents.get(i + 2));
                        hMWikiSenses.put(documents.get(i + 2), documents.get(i));
                        for (int j = i + 3; j < documents.size() - 1 && documents.get(j).startsWith("*"); j++) {
                            wikiSenses.println(documents.get(j));
                            //System.err.println(documents.get(j));
                            hMWikiSenseTitles.put(documents.get(i), documents.get(j));
                            hMWikiSenses.put(documents.get(j), documents.get(i));
                        }
                    }
                }
            }
        }
        wikiSenses.flush();
        wikiSenses.close();
    }

    public void createWikiNet() throws FileNotFoundException {
        PrintStream wikiNet = new PrintStream("WikiNet.txt");
        for (String title : hMWikiSenses.keySet()) {
            wikiNet.println(title);
            System.err.println(title);
        }
        wikiNet.flush();
        wikiNet.close();
    }
}