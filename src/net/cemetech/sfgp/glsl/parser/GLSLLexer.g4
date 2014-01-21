lexer grammar GLSLLexer;

fragment NONDIGIT : [a-zA-Z_];
fragment DIGIT : [0-9];
fragment NONZERO : [1-9];
fragment OCTAL : [0-7];
fragment HEX : [0-9a-fA-F];
fragment INTSUF : [uU];
fragment FLOATSUF : [fF];
fragment SIGN : [-+];
fragment EXP : [eE];
fragment HEXPREF : '0' [xX];

COMMENT : ('//' ~[\r\n]*) | ('/*' .*? '*/');
WS : [ \t] -> skip;
NL : ('\n' | ('\r' '\n'?)) -> skip;


PREPROCESSOR : '#' ~[\r\n]*;

TYPE : 'float' | 'int' | 'void' | 'bool' | 'mat2' | 'mat3' | 'mat4' | 'mat2x2' | 'mat2x3' | 'mat2x4' | 'mat3x2' | 'mat3x3' |
'mat3x4' | 'mat4x2' | 'mat4x3' | 'mat4x4' | 'vec2' | 'vec3' | 'vec4' | 'ivec2' | 
'ivec3' | 'ivec4' | 'bvec2' | 'bvec3' | 'bvec4' | 'uint' | 'uvec2' | 'uvec3' | 
'uvec4' | 'sampler1D' | 'sampler2D' | 
'sampler3D' | 'samplerCube' | 'sampler1DShadow' | 'sampler2DShadow' | 
'samplerCubeShadow' | 'sampler1DArray' | 'sampler2DArray' | 'sampler1DArrayShadow' | 
'sampler2DArrayShadow' | 'isampler1D' | 'isampler2D' | 'isampler3D' | 
'isamplerCube' | 'isampler1DArray' | 'isampler2DArray' | 'usampler1D' | 
'usampler2D' | 'usampler3D' | 'usamplerCube' | 'usampler1DArray' | 
'usampler2DArray' | 'sampler2DRect' | 'sampler2DRectShadow' | 'isampler2DRect' | 
'usampler2DRect' | 'samplerBuffer' | 'isamplerBuffer' | 'usamplerBuffer' | 
'sampler2DMS' | 'isampler2DMS' | 'usampler2DMS' | 'sampler2DMSArray' | 
'isampler2DMSArray' | 'usampler2DMSArray';

KEYWORD : 'attribute' | 'const' | 'uniform' | 'varying' | 'layout' | 'centroid' | 
'flat' | 'smooth' | 'noperspective' | 'break' | 'continue' | 'do' | 'for' | 'while' | 
'switch' | 'case' | 'default' | 'if' | 'else' | 'in' | 'out' | 'inout' | 'invariant' | 'discard' | 'return' | 
 'lowp' | 'mediump' | 'highp' | 'precision' | 'struct';
 
 RESERVED : 'common' | 'partition' | 'active' | 'asm' | 'class' | 'union' | 'enum' | 
 'typedef' | 'template' | 'this' | 'packed' | 'goto' | 'inline' | 'noinline' | 
 'volatile' | 'public' | 'static' | 'extern' | 'external' | 'interface' | 'long' | 
 'short' | 'double' | 'half' | 'fixed' | 'unsigned' | 'superp' | 'input' | 'output' |
 'hvec2' | 'hvec3' | 'hvec4' | 'dvec2' | 'dvec3' | 'dvec4' | 'fvec2' | 'fvec3' | 
 'fvec4' | 'sampler3DRect' | 'filter' | 'image1D' | 'image2D' | 'image3D' | 
 'imageCube' | 'iimage1D' | 'iimage2D' | 'iimage3D' | 'iimageCube' | 'uimage1D' |
 'uimage2D' | 'uimage3D' | 'uimageCube' | 'image1DArray' | 'image2DArray' | 
 'iimage1DArray' | 'iimage2DArray' | 'uimage1DArray' | 'uimage2DArray' | 
 'image1DShadow' | 'image2DShadow' | 'image1DArrayShadow' | 'image2DArrayShadow' | 
 'imageBuffer' | 'iimageBuffer' | 'uimageBuffer' | 'sizeof' | 'cast' | 'namespace' | 
 'using' | 'row_major' | (NONDIGIT? (DIGIT|NONDIGIT)* '__' (DIGIT|NONDIGIT)*);

INTEGER : ( (NONZERO DIGIT*) | ('0' OCTAL*) | (HEXPREF HEX+) ) INTSUF?;
FLOAT : ((DIGIT+ EXP SIGN? DIGIT+  ) | (((DIGIT* '.' DIGIT+) | (DIGIT+ '.' DIGIT*)) (EXP SIGN? DIGIT+)?)) FLOATSUF?;
BOOLEAN : 'true' | 'false';

IDENT : NONDIGIT (DIGIT|NONDIGIT)*;

STMT_SEP : '{' | '}' | ';';
OPERATOR : '(' | ')' | '[' | ']' | '.' | '++' | '--' | '+' | '-' | '~' | '!' | 
'*' | '/' | '%' | '<<' | '>>' | '<' | '>' | '<=' | '>=' | '==' | '!=' | '&' | '^' |
'|' | '&&' | '^^' | '||' | '?' | ':' | '=' | '+=' | '-=' | '*=' | '/=' | '%=' |
'<<=' | '>>=' | '&=' | '^=' | '|=' | ',';
