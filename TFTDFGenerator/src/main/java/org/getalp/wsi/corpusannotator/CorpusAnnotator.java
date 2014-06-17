package org.getalp.wsi.corpusannotator;

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
public class CorpusAnnotator {
    public Map<String, String> hMAnnotations = new HashMap<>();
    public Map<String, String> hMUnderAnnotations = new HashMap<>();
    List<List<String>> documents;
    private List<String> wordListFilter;
    private long wordListSize;

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        CorpusAnnotator at = new CorpusAnnotator();
        if (args.length > 1) {
            at.loadAnnotations(args[1]);
            at.processCorpus(sts);
        }
    }

    public void loadAnnotations(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        String UnderAnnotation = "";
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of annotations are loaded...");
        for (String annotation : lc.getAllSentences()) {
            hMAnnotations.put(annotation, annotation);
            UnderAnnotation = annotation.replaceAll(" ", "_");
            hMUnderAnnotations.put(annotation, UnderAnnotation);
        }
    }

    public void processCorpus(List<String> documents) throws FileNotFoundException {
        PrintStream annotatedCorpus = new PrintStream("AnnotatedCorpus.txt");
        PrintStream annotatedWords = new PrintStream("AnnotatedWords.txt");
        PrintStream unannotatedWords = new PrintStream("UnannotatedWords.txt");
        String replace = "";
        String annotated = "";
        for (String sent : documents) {
            for (String anno : hMAnnotations.values()) {
                if (sent.contains(anno)) {
                    if (!annotated.contains(anno)) {
                        annotated += anno + " ";
                        annotatedWords.println(anno);
                    }
                    String toAnnotate;
                    if (replace.isEmpty()) toAnnotate = sent;
                    else
                        toAnnotate = replace;
                    replace = toAnnotate.replaceAll(anno, hMUnderAnnotations.get(anno));
                }
            }
            if (replace.isEmpty()) {
                annotatedCorpus.println(sent);
            } else {
                annotatedCorpus.println(replace);
                replace = "";
            }
        }
        annotatedCorpus.flush();
        annotatedWords.flush();
        unannotatedWords.flush();
        annotatedCorpus.close();
        annotatedWords.close();
        unannotatedWords.close();
    }
}