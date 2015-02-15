package net.cemetech.sfgp.glsl.compile;

import java.util.List;

public class LinkerTaskSpec implements TaskSpec<LinkerTaskSpec> {
	List<TaskResult<CompilerTaskSpec>> shaders;
	public LinkerTaskSpec(List<TaskResult<CompilerTaskSpec>> input){
		shaders = input;
	}
	@Override
	public TaskResult<LinkerTaskSpec> passToImpl(CompilerImpl impl) {
		return impl.linkProgram(shaders);
	}
}
