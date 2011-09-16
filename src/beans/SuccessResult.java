package beans;

public class SuccessResult {

	private String message = "success";
	
	private int code = 200;
	
	public SuccessResult(String _mess, int _errCode) {
		message=_mess;
		code=_errCode;
	}
	
	public SuccessResult() {
	}
}
