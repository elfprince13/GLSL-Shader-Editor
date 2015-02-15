package net.cemetech.sfgp.glsl.compile;

import java.util.List;

public class LinkerTaskSpec extends TaskSpec<LinkerTaskSpec> {
	List<TaskResult<CompilerTaskSpec>> shaders;
	public LinkerTaskSpec(List<TaskResult<CompilerTaskSpec>> input){
		shaders = input;
	}
	@Override
	public TaskResult<LinkerTaskSpec> call() {
		if(compiler == null){
			throw new IllegalStateException("A compiler must have been set to call this taskspec");
		} else {
			return compiler.linkProgram(this);			
		}
	}
}
