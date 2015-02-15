package net.cemetech.sfgp.glsl.compile;

import java.util.List;

public interface CompilerImpl {
	public TaskResult<CompilerTaskSpec> compileShader(CompilerTaskSpec spec);
	public TaskResult<LinkerTaskSpec> linkProgram(List<TaskResult<CompilerTaskSpec>> shaders);
}
