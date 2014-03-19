package org.getalp.wsi.generator;

public class WordDocumentPair implements Comparable<WordDocumentPair>{
    String word;
    int documentIndex;

    public WordDocumentPair(String word, int documentIndex) {
        this.word = word;
        this.documentIndex = documentIndex;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getDocumentIndex() {
        return documentIndex;
    }

    public void setDocumentIndex(int documentIndex) {
        this.documentIndex = documentIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordDocumentPair wordEntry = (WordDocumentPair) o;

        if (documentIndex != wordEntry.documentIndex) return false;
        if (!word.equals(wordEntry.word)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + documentIndex;
        return result;
    }

    @Override
    public String toString() {
        return word+"#"+documentIndex;
    }

    @Override
    public int compareTo(WordDocumentPair o) {
        String a = o.toString();
        String b = toString();
        return a.compareTo(b);
    }
}
