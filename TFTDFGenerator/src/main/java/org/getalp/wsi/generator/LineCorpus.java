package org.getalp.wsi.generator;

import org.getalp.wsi.CorpusIface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LineCorpus implements CorpusIface {


    BufferedReader reader;
    List<String> sentences;
    {
        sentences = new ArrayList<>();
    }

    public LineCorpus(String fileName) throws IOException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line ="";
        while(null!=(line = reader.readLine())){
            sentences.add(line);
        }
    }

    @Override
    public long getNumberOfSentences() {
        return sentences.size();
    }

    @Override
    public String getSentence(int sId) {
        return sentences.get(sId);
    }

    @Override
    public List<String> getAllSentences() {
        return sentences;
    }
}
