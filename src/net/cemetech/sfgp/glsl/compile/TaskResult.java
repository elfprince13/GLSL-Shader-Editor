package net.cemetech.sfgp.glsl.compile;

public class TaskResult<T extends TaskSpec<T>> {
	int resultId;
	String loggingResults;
	public TaskResult(int id, String log) {
		resultId = id;
		loggingResults = log;
	}
	
	public int getResultId(){ return resultId; }
	public String getLoggingResults(){ return loggingResults; }
	
	public boolean useable() {
		return resultId != 0;
	}
}
