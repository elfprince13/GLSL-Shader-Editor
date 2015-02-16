package net.cemetech.sfgp.glsl.compile;

public interface CompilerImpl {
	public TaskResult<CompilerTaskSpec> compileShader(CompilerTaskSpec spec);
	public TaskResult<LinkerTaskSpec> linkProgram(LinkerTaskSpec spec);
	public void cleanCompileResult(TaskResult<CompilerTaskSpec> result);
	public void cleanLinkResult(TaskResult<LinkerTaskSpec> result);
}
