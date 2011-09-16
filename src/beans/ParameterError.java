package beans;

public class ParameterError{
	
	private String message  = "invalid parameters";
	
	private int code = 500; 
	

	public ParameterError(){}
	
	public ParameterError(String _mess, int _errCode) {
		message=_mess;
		code=_errCode;
	}

}
