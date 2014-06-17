package org.getalp.wsi.wikipediacleaner;

/**
 * Created by Mohammad on 20/05/2014.
 */

import org.getalp.wsi.CorpusIface;
import org.getalp.wsi.generator.LineCorpus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class WikipediaCleaner {
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The Wikipedia corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        WikipediaCleaner at = new WikipediaCleaner();
        String reTitle = "<title>[^:]+</title>";
        String reTextStart = "<text xml:space=\"preserve\">";
        String reTextEnd = "</text>";
        at.cleaningWikipedia(sts, reTitle, reTextStart, reTextEnd);
    }

    public void cleaningWikipedia(List<String> documents, String reTitle, String reTextStart, String reTextEnd) throws FileNotFoundException {
        PrintStream wikipedia = new PrintStream("WikipeadiaArticlesWithTitles.txt");
        Pattern pTitle = Pattern.compile(reTitle);
        Pattern pTextStart = Pattern.compile(reTitle);
        Pattern pTextEnd = Pattern.compile(reTitle);
        for (int i = 0; i < documents.size(); i++) {
            String sent = documents.get(i);
            Matcher mTitle = pTitle.matcher(sent);
            if (mTitle.find() && (i < documents.size() - 1)) {
                wikipedia.println(documents.get(i));
                //System.err.println(documents.get(i));
                for (int j = i + 1; j < documents.size() - 1; j++) {
                    Matcher mTextStart = pTextStart.matcher(sent);
                    boolean matched = false;
                    if (mTextStart.find() && (j < documents.size() - 1)) {
                        wikipedia.println(documents.get(j));
                        //System.err.println(documents.get(j));
                        matched = true;
                        for (int k = j + 1; k < documents.size() - 1; k++) {
                            wikipedia.println(documents.get(k));
                            //System.err.println(documents.get(k));
                            Matcher mTextEnd = pTextEnd.matcher(sent);
                            if (mTextEnd.find() && (k < documents.size() - 1)) {
                                wikipedia.println(documents.get(k));
                                //System.err.println(documents.get(k));
                                matched = false;
                            }
                        }
                    }
                }
            }
        }
        wikipedia.flush();
        wikipedia.close();
    }
}