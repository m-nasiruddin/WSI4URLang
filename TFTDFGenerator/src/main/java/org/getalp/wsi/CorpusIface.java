package org.getalp.wsi;

import java.util.List;

public interface CorpusIface {

	public long getNumberOfSentences();

	public String getSentence(int sId);

	public List<String> getAllSentences();

}