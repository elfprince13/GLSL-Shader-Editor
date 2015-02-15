package net.cemetech.sfgp.glsl.compile;

interface TaskSpec<T extends TaskSpec<T>> {
	public TaskResult<T> passToImpl(CompilerImpl impl);
}
