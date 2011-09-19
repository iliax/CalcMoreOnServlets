package beans;

public class Poster {

	private String nickId;
	
	private long statusUpdate;
	
	private STATUS status;
	
	public Poster(){};
	
	public Poster(String nick){
		nickId=nick;
		status=STATUS.IN_PROGRESS;
		statusUpdate=System.currentTimeMillis();
	}
	
	public static enum STATUS {
		FORGOTTEN,
		IN_PROGRESS,
		DONE
	}
	
}
