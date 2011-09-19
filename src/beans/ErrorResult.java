package beans;

public class ErrorResult{
	
	private String message  = "invalid parameters";
	
	private int code = 500; 
	

	public ErrorResult(){}
	
	public ErrorResult(String _mess, int _errCode) {
		message=_mess;
		code=_errCode;
	}
	
	public ErrorResult(int _errCode) {
		code=_errCode;
	}

}
