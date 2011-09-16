package beans;

public class JsonWordCountPair {

	private String word;
	private long count;
	
	public JsonWordCountPair(String word, long count) {
		this.word=word;
		this.count=count;
	}	
	
	public String getWord() {
		return word;
	}
	
	
	public long getCount() {
		return count;
	}
	
	public JsonWordCountPair() {
	}
}