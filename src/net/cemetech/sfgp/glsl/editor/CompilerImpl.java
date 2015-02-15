package net.cemetech.sfgp.glsl.editor;

import java.util.List;

public interface CompilerImpl {
	public int compileShader(int kind, String src);
	public int linkProgram(List<Integer> shaders);
}
