package net.cemetech.sfgp.glsl.compile;

import java.util.concurrent.Callable;

abstract class TaskSpec<T extends TaskSpec<T>> implements Callable<TaskResult<T>> {
	CompilerImpl compiler = null;
	public void setCompiler(CompilerImpl c){	compiler = c;	}
	public void clearCompiler(){	compiler = null;	}
	public abstract TaskResult<T> call();
}
