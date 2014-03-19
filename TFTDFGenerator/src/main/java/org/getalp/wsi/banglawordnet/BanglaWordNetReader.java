package org.getalp.wsi.banglawordnet;

import java.io.*;
import java.util.*;


public class BanglaWordNetReader {
    public Map<Integer, Integer> hMSensesFileSenseIDs = new HashMap<>();
    public Map<Integer, String> hMSensesFileSenses = new HashMap<>();
    public Map<Integer, String> hMSensesFileSensesPOSTags = new HashMap<>();
    public Map<Integer, Integer> hMSensesFileSynsetIDs = new HashMap<>();

    ArrayList<String> aLSensesFileSenses = new ArrayList<String>();
    public List<List<String>> aLSensesFileTopics = new ArrayList<>();

    public Map<Integer, Integer> hMGlossesFileSynsetIDs = new HashMap<>();
    public Map<Integer, String> hMGlossesFileDefinitions = new HashMap<>();
    public Map<Integer, String> hMGlossesFileExamples = new HashMap<>();

    public Map<Integer, String> hMTopics = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Syntax: ");
            System.exit(1);
        }
        String SensesFile = args[0];
        String GlossesFile = args[1];

        BanglaWordNetReader pco = new BanglaWordNetReader();

        pco.loadSenses(SensesFile);
        pco.loadGlosses(GlossesFile);
    }

    public void loadSenses(String fileName) throws IOException {
        BufferedReader bRSenses = new BufferedReader(new FileReader(fileName));
        String SensesLine = "";
        while (null != (SensesLine = bRSenses.readLine())) {
            String[] SensesEntry = SensesLine.split("\t");
            //System.out.println(ssl);

            Integer SenseIDs = Integer.parseInt(SensesEntry[0]);
            String Senses = SensesEntry[1];
            String SensePOSTags = SensesEntry[2];
            Integer SynsetIDs = Integer.parseInt(SensesEntry[3]);

            hMSensesFileSenseIDs.put(SenseIDs, SenseIDs);
            hMSensesFileSenses.put(SenseIDs, Senses);
            hMSensesFileSensesPOSTags.put(SenseIDs, SensePOSTags);
            hMSensesFileSynsetIDs.put(SenseIDs, SynsetIDs);

            aLSensesFileSenses.add(Senses);
        }
        bRSenses.close();

        List<String> uniqueList = new ArrayList<String>(new HashSet<String>(aLSensesFileSenses));
        aLSensesFileTopics.add(uniqueList);
        //System.out.println(uniqueList);

        //Creates "topics.txt" file.
        for (String topic : uniqueList) {
            System.out.println(topic);
        }
            //String sense = hMSensesFileTopics.get(topic);
            //if (sense != null) {
                //System.out.println(topic + "\t" + sense);
            //}
        //}

        //Creates "subTopics.txt" file.
        //Map<String, Integer> counts = new HashMap<>();
        //for(String currentWord : hMSensesFileSenses.) {
            //if(counts.containsKey(currentWord)){
                //counts.put(currentWord, 0);
            //}
            //counts.put(currentWord, counts.get(currentWord)+1);
        //}

        //for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            //System.out.println("Key = " + entry.getValue() + ", Value = " + entry.getKey());
        //}
    }

    public void loadGlosses(String fileName) throws IOException {
        BufferedReader bRGlosses = new BufferedReader(new FileReader(fileName));
        String bRGlossesLine;
        while (null != (bRGlossesLine = bRGlosses.readLine())) {
            String[] bRGlossesEntry = bRGlossesLine.split("\t");

            Integer SynsetIDs = Integer.valueOf(bRGlossesEntry[0]);
            String Definitions = bRGlossesEntry[1];
            String Examples = bRGlossesEntry[2];

            hMGlossesFileSynsetIDs.put(SynsetIDs, SynsetIDs);
            hMGlossesFileDefinitions.put(SynsetIDs, Definitions);
            hMGlossesFileExamples.put(SynsetIDs, Examples);
        }
        bRGlosses.close();
    }
}