package net.cemetech.sfgp.glsl.compile;

public class CompilerTaskSpec implements TaskSpec<CompilerTaskSpec> {
	int kind; 
	String src;
	public CompilerTaskSpec(int k, String s) {
		kind = k;
		src = s;
	}
	@Override
	public TaskResult<CompilerTaskSpec> passToImpl(CompilerImpl impl) {
		return impl.compileShader(this);
	}
}
