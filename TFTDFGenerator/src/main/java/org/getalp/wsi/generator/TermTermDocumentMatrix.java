package org.getalp.wsi.generator;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import org.apache.commons.collections.list.TreeList;
import org.getalp.wsi.CorpusIface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Mohammad on 17/03/2014.
 */

@SuppressWarnings("ALL")
public class TermTermDocumentMatrix {
    DoubleMatrix2D termTermMatrix;
    DoubleMatrix2D termTermMatrixView;
    List<List<String>> documents;
    List<WordDocumentPair> currentSentenceProcessed;
    private Map<Integer, WordDocumentPair> wordDocumentPairIndexView;
    //DefinitionNormalizer normalizer;
    private List<List<Integer>> wordDocumentIndex;
    private Map<Integer, WordDocumentPair> wordDocumentPairIndex;
    private Map<WordDocumentPair, Integer> inverseWordDocumentPairIndex;
    private Map<WordDocumentPair, Integer> inverseWordDocumentPairIndexView;
    private Map<String, Integer> wordIndex;
    private Map<Integer, String> wordInvertedIndex;
    private long corpusSize;
    private long cummulativeDocumentLength;
    private long numberOfDocuments;
    private int numberOfUniqueWords;
    private Runtime runtime;
    private int currentWordDocumentPairIndex = 0;
    private int mb = 1024 * 1024;
    private List<String> wordListFilter;
    private long wordListSize;

    public TermTermDocumentMatrix(/*BuilderLanguage language,*/) {
        super();
        documents = new TreeList();
        cummulativeDocumentLength = 0;
        this.numberOfDocuments = 0;
        numberOfUniqueWords = 0;
        wordIndex = new TreeMap<>();
        wordInvertedIndex = new TreeMap<>();
        inverseWordDocumentPairIndex = new TreeMap<>();
        wordDocumentIndex = new TreeList();
        wordDocumentPairIndex = new TreeMap<>();
        wordDocumentPairIndexView = new TreeMap<>();
        inverseWordDocumentPairIndexView = new TreeMap<>();
        runtime = Runtime.getRuntime();
    }

    public static void main(String[] args) throws SQLException, IOException {
        CorpusIface lccCorpus = new LineCorpus(args[0]);
        System.out.println("The corpus is loaded...");
        List<String> sts = lccCorpus.getAllSentences();
        TermTermDocumentMatrix at = new TermTermDocumentMatrix();
        if (args.length > 1) {
            at.loadWordList(args[1]);
        }
        at.processCorpus(sts);
    }

    public void loadWordList(String filename) throws IOException {
        LineCorpus lc = new LineCorpus(filename);
        wordListFilter = lc.getAllSentences();
        wordListSize = lc.getNumberOfSentences();
        System.out.println("List of words are loaded...");
    }

    private void allocateDataStructures() {
        termTermMatrix = new SparseDoubleMatrix2D(numberOfUniqueWords,
                inverseWordDocumentPairIndex.size());
        System.err.println(String.format("Allocating a %d by %d double matrix.", numberOfUniqueWords, inverseWordDocumentPairIndex.size()));
        termTermMatrix.assign(0);
    }

    public double getFrequency(String word1, String word2, int documentIndex) {
        int index1 = wordIndex.get(word1);
        int index2 = wordIndex.get(word2);
        return (Double) termTermMatrix.getQuick(index1, index2);
    }

    public void computeCounts(/*int documentIndex*/) {

        DoubleMatrix2D termTermMatrix;
        Map<Integer, WordDocumentPair> wordDocumentPairIndex;
        Map<WordDocumentPair, Integer> inverseWordDocumentPairIndex;
        if (termTermMatrixView == null) {
            termTermMatrix = this.termTermMatrix;
            wordDocumentPairIndex = this.wordDocumentPairIndex;
            inverseWordDocumentPairIndex = this.inverseWordDocumentPairIndex;
        } else {
            termTermMatrix = termTermMatrixView;
            wordDocumentPairIndex = wordDocumentPairIndexView;
            inverseWordDocumentPairIndex = this.inverseWordDocumentPairIndexView;
        }
        currentSentenceProcessed = new ArrayList<WordDocumentPair>();
        int percentage = 0;
        long startTime = System.currentTimeMillis();
        for (int j = 0; j < termTermMatrix.columns(); j++) {
            System.out.print("Mem["
                    + ((runtime.totalMemory() - runtime.freeMemory()) / mb)
                    + "m / " + (runtime.totalMemory() / mb) + "m]  ");
            System.out.print("Instance " + j
                    + "                              \r");
            WordDocumentPair wp = wordDocumentPairIndex.get(j);
            String cw = wp.getWord();
            for (String s : documents.get(wp.documentIndex)) {
                if (wordListFilter.contains(cw)) {
                    int currentIndex = wordIndex.get(s);
                    double value = termTermMatrix.getQuick(currentIndex, inverseWordDocumentPairIndex.get(wp));
                    termTermMatrix.setQuick(currentIndex, inverseWordDocumentPairIndex.get(wp), ++value);
                }
            }
            if (documents.get(wp.documentIndex).contains(cw) && wordListFilter.contains(cw)) {
                double value = termTermMatrix.getQuick(wordIndex.get(cw), inverseWordDocumentPairIndex.get(wp));
                termTermMatrix.setQuick(wordIndex.get(cw), inverseWordDocumentPairIndex.get(wp), --value);
            }
            if ((((double) j / (double) termTermMatrix.columns()) * 100d > percentage + 1)) {

                System.err.println(String.format("%.2f", ((double) j / (double) termTermMatrix.columns()) * 100d) + "% - " + String.format("%d ms", System.currentTimeMillis() - startTime));
                percentage++;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public void buildIndexes(String s, int documentIndex) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isLetter(chars[i])) {
                if (Character.getType(chars[i]) != Character.COMBINING_SPACING_MARK
                        && Character.getType(chars[i]) != Character.NON_SPACING_MARK) {
                    chars[i] = ' ';
                }
            }
        }
        documents.add(new ArrayList<String>());
        String ss = new String(chars);
        ss = ss.toLowerCase();
        String[] tokens = ss.split(" ");
        long length = 0;
        for (String w : tokens) {
            w = w.replaceAll("\\p{Punct}", "");
            //System.out.println(w);
            if (!w.equals("")) {
                if (!wordIndex.containsKey(w)) {
                    wordIndex.put(w, numberOfUniqueWords);
                    wordInvertedIndex.put(numberOfUniqueWords, w);
                    numberOfUniqueWords++;
                }
                int windex = wordIndex.get(w);
                if (wordDocumentIndex.size() - 1 < windex) {
                    wordDocumentIndex.add(new ArrayList<Integer>());
                }
                wordDocumentIndex.get(windex).add(documentIndex);
                //if (wordListFilter.contains(w)) {
                documents.get(documents.size() - 1).add(w);
                WordDocumentPair wp = new WordDocumentPair(w, documentIndex);
                if (!inverseWordDocumentPairIndex.containsKey(wp)) {
                    wordDocumentPairIndex.put(currentWordDocumentPairIndex, wp);
                    inverseWordDocumentPairIndex.put(wp, currentWordDocumentPairIndex++);
                }
                length++;
                //  }
            }
        }
        cummulativeDocumentLength += length;
        numberOfDocuments++;
    }

    public double getAverageDocumentLength() {
        return (double) getCummulativeDocumentLength() / (double) getNumberOfDocuments();
    }

    public long getCummulativeDocumentLength() {
        return cummulativeDocumentLength;
    }

    public void setCummulativeDocumentLength(long cummulativeSentenceLength) {
        this.cummulativeDocumentLength = cummulativeSentenceLength;
    }

    public long getNumberOfDocuments() {
        return numberOfDocuments;
    }

    public void setNumberOfSentences(long numberofSentences) {
        this.numberOfDocuments = numberofSentences;
    }

    public long getNumberOfUniqueWords() {
        return numberOfUniqueWords;
    }

    public void setNumberOfUniqueWords(int numberOfUniqueWords) {
        this.numberOfUniqueWords = numberOfUniqueWords;
    }

    public DoubleMatrix2D computeView(int wordNumber) {
        DoubleMatrix2D termTermMatrixViewNew;
        List<Integer> indexes = new ArrayList<>();
        List<Integer> docs;
        Integer wi = wordNumber;
        if (wi != null) {
            docs = wordDocumentIndex.get(wi);
            for (int docindex : docs) {
                WordDocumentPair wp = new WordDocumentPair(wordInvertedIndex.get(wi), docindex);
                Integer cIndex = inverseWordDocumentPairIndex.get(wp);
                if (cIndex != null && !indexes.contains(cIndex)) {
                    indexes.add(cIndex);
                }
            }
        }
        Collections.sort(indexes);
        int[] indexarray = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            wordDocumentPairIndexView.put(i, wordDocumentPairIndex.get(indexes.get(i)));
            inverseWordDocumentPairIndexView.put(wordDocumentPairIndex.get(indexes.get(i)), i);
            indexarray[i] = indexes.get(i);
        }
        System.err.println(indexarray.length);
        termTermMatrixViewNew = termTermMatrix.viewSelection(null, indexarray);
        return termTermMatrixViewNew;
    }

    public void printThesaurusWeka(PrintWriter os, boolean printHeader, DoubleMatrix2D matrix) {
        DoubleMatrix2D termTermMatrix;
        Map<Integer, WordDocumentPair> wordDocumentPairIndex;
        termTermMatrix = matrix;
        wordDocumentPairIndex = wordDocumentPairIndexView;

        List<Integer> ignored = new ArrayList<>();
        for (int i = 0; i < termTermMatrix.rows(); i++) {
            StringBuilder col = new StringBuilder();
            int zeroCount = 0;
            for (int j = 0; j < termTermMatrix.columns(); j++) {
                double value = termTermMatrix.getQuick(i, j);
                if (value == 0) {
                    zeroCount++;
                }
            }
            if (zeroCount == termTermMatrix.columns()) {
                ignored.add(i);
            }
        }


        System.out.println("Writing Term Term Document matrix to disk...");
        int percentage = 0;
        os.println("@relation wordsenses\n");
        StringBuilder dataBuilder = new StringBuilder();
        for (int i = 0; i < termTermMatrix.columns(); i++) {
            StringBuilder col = new StringBuilder();
            col.append(wordDocumentPairIndex.get(i) + ",");
            int zeroCount = 0;
            for (int j = 0; j < termTermMatrix.rows(); j++) {
                if (!ignored.contains(j)) {
                    if (j < termTermMatrix.rows() - 1) {
                        col.append(String.format("%d,", (int) termTermMatrix.getQuick(j, i)));
                    } else {
                        col.append(String.format("%d", (int) termTermMatrix.getQuick(j, i)));
                    }
                }
            }
            String colstr = col.toString();
            colstr = colstr.trim();
            if (colstr.endsWith(",")) {
                colstr = colstr.substring(0, colstr.length() - 1);
            }
            if (zeroCount != termTermMatrix.columns()) {
                dataBuilder.append(colstr + "\n");
            } else {
                ignored.add(i);
            }
            if (((double) i / (double) termTermMatrix.rows()) * 100 > (double) (percentage + 1)) {
                System.out.print("Mem["
                        + ((runtime.totalMemory() - runtime.freeMemory()) / mb)
                        + "m / " + (runtime.totalMemory() / mb) + "m]  ");
                System.out
                        .print((int) (((double) i / (double) termTermMatrix.rows()) * 100) + "%                         \r");
                percentage++;
            }
        }
        String worattr = "@attribute instances {";
        for (int i = 0; i < termTermMatrix.columns(); i++) {

            if (i < termTermMatrix.columns() - 1) {
                worattr += String.format("%s, ", wordDocumentPairIndex.get(i));
            } else {
                worattr += String.format("%s", wordDocumentPairIndex.get(i));
            }
        }
        worattr += "}";
        os.println(worattr);
        for (int j = 0; j < termTermMatrix.rows(); j++) {
            if (!ignored.contains(j)) {
                os.println("@attribute " + wordInvertedIndex.get(j) + " real");
            }
        }
        os.println();
        os.println("\n@data");
        os.print(dataBuilder.toString());
        os.flush();
        os.close();
        System.out.println("\ndone.");
    }

    public void processCorpus(List<String> documents) throws FileNotFoundException {
        System.out.print(((runtime.totalMemory() - runtime.freeMemory()) / mb)
                + "m / " + (runtime.totalMemory() / mb) + "m\n");
        System.out.print("Building indexes...\n");
        int percentage = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < documents.size(); i++) {
            buildIndexes(documents.get(i), i);
            if ((((double) i / (double) documents.size()) * 100d > percentage + 1)) {
                System.err.println(String.format("%.2f", ((double) i / (double) documents.size()) * 100d) + "% - " + String.format("%d ms", System.currentTimeMillis() - startTime));
                percentage++;
                startTime = System.currentTimeMillis();
            }
        }
        System.out.println("[" + numberOfUniqueWords + "]. done.");
        System.out.println("Computing term frequencies...");
        allocateDataStructures();
        computeCounts();
        PrintStream ps = new PrintStream("WordsNotPresent.txt");
        PrintStream nfps = new PrintStream("WordsPresent.txt");
        int wn = 1;
        for (String w : wordListFilter) {
            if (wordIndex.containsKey(w)) {
                DoubleMatrix2D d = computeView(wordIndex.get(w));
                try {
                    PrintWriter pwcurrent = new PrintWriter(wn + "_cols.arff");
                    printThesaurusWeka(pwcurrent, true, d);
                    pwcurrent.flush();
                    pwcurrent.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                wn++;
                nfps.println(w);
            } else {
                ps.println(w);
            }
        }
        ps.flush();
        nfps.flush();
        ps.close();
        nfps.close();
        System.out.print(((runtime.totalMemory() - runtime.freeMemory()) / mb) + "m / " + (runtime.totalMemory() / mb) + "m                   \n");
    }
}