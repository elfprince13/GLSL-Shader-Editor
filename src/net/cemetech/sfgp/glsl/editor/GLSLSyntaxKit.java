package net.cemetech.sfgp.glsl.editor;

import net.cemetech.sfgp.glsl.parser.GLSLWrappedLexer;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.Lexer;

public class GLSLSyntaxKit extends DefaultSyntaxKit {
	public GLSLSyntaxKit() {
		super(new GLSLWrappedLexer());
	}
	
	public static void initKit() {
		DefaultSyntaxKit.initKit();
		registerContentType("text/glsl", GLSLSyntaxKit.class.getCanonicalName());
	}
	
	GLSLSyntaxKit(Lexer lexer) {
		super(lexer);
	}
}
