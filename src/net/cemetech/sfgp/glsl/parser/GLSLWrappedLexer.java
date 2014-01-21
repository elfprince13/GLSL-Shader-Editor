package net.cemetech.sfgp.glsl.parser;

import java.io.IOException;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import jsyntaxpane.Lexer;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

public class GLSLWrappedLexer implements Lexer {
	CommonTokenStream tokens = null;
	org.antlr.v4.runtime.Token yytok = null;

	@Override
	public char yycharat(int arg0) {
		return yytext().charAt(arg0);
	}

	@Override
	public int yylength() {
		return 1 + yytok.getStopIndex() - yytok.getStartIndex();
	}
	
	private TokenType identifyType(org.antlr.v4.runtime.Token at) {
		switch(at.getType()) {
		case GLSLLexer.COMMENT:
			return TokenType.COMMENT;
		case GLSLLexer.FLOAT:
		case GLSLLexer.INTEGER:
		case GLSLLexer.BOOLEAN:
			return TokenType.NUMBER;
		case GLSLLexer.TYPE:
			return TokenType.TYPE;
		case GLSLLexer.KEYWORD:
			return TokenType.KEYWORD;
		case GLSLLexer.RESERVED:
			return TokenType.WARNING;
		case GLSLLexer.PREPROCESSOR:
			return TokenType.KEYWORD2;
		case GLSLLexer.OPERATOR:
			return TokenType.OPERATOR;
		case GLSLLexer.IDENT:
			return TokenType.IDENTIFIER;
		default:
			return TokenType.DEFAULT;
		}
	}

	@Override
	public Token yylex() throws IOException {
		Token outToken = null;
		yytok = null;
		if (tokens == null) throw new IOException("Can't lex from null stream");
		else if(tokens.index() != org.antlr.v4.runtime.Token.EOF){
			org.antlr.v4.runtime.Token aToken = tokens.get(tokens.index());
			yytok = aToken;
			if (aToken.getType() == org.antlr.v4.runtime.Token.EOF ) {
				outToken = null;
			} else {
				tokens.consume();
				outToken = new Token(identifyType(aToken), aToken.getStartIndex(), yylength());
			}
		}
		return outToken;
	}

	@Override
	public void yyreset(Reader arg0) {
		yytok = null;
		try{
			ANTLRInputStream ais = new ANTLRInputStream(arg0);
			GLSLLexer lexer = new GLSLLexer(ais);
			tokens = new CommonTokenStream(lexer);
			tokens.reset();
		} catch (IOException e) {
			e.printStackTrace();
			tokens = null;
		}

	}

	@Override
	public String yytext() {
		return yytok.getText();
	}

}
