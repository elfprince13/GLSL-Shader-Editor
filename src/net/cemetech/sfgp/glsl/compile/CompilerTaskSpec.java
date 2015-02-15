package net.cemetech.sfgp.glsl.compile;

public class CompilerTaskSpec extends TaskSpec<CompilerTaskSpec> {
	int kind; 
	String src;
	public CompilerTaskSpec(int k, String s) {
		kind = k;
		src = s;
	}
	
	public int getKind(){ return kind; }
	public String getSrc(){ return src; }
	
	@Override
	public TaskResult<CompilerTaskSpec> call() {
		if(compiler == null){
			throw new IllegalStateException("A compiler must have been set to call this taskspec");
		} else {
			return compiler.compileShader(this);			
		}
	}
}
