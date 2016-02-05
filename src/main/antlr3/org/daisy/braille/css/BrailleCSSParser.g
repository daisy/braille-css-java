parser grammar BrailleCSSParser;

options {
    output = AST;
    tokenVocab=BrailleCSSLexer;
    k = 2;
}

import CSSParser;

@header {package org.daisy.braille.css;}

@members {
    public void init() {
        gCSSParser.init();
    }
}

unknown_atrule
    : volume
    | ATKEYWORD S* LCURLY any* RCURLY -> INVALID_ATSTATEMENT
    ;

volume
    : VOLUME volume_pseudo? S* LCURLY S* declarations volume_area* RCURLY
      -> ^(VOLUME volume_pseudo? declarations ^(SET volume_area*))
    ;

volume_pseudo
    : pseudocolon^ ( IDENT | FUNCTION S!* NUMBER S!* RPAREN! )
    ;

volume_area
    : VOLUME_AREA S* LCURLY S* declarations RCURLY S*
      -> ^(VOLUME_AREA declarations)
    ;

pseudo
    : pseudocolon^ (MINUS? IDENT | FUNCTION S!* (IDENT | MINUS? NUMBER | MINUS? INDEX) S!* RPAREN!)
    ;
  catch [RecognitionException re] {
     retval.tree = gCSSParser.tnr.invalidFallback(INVALID_SELPART, "INVALID_SELPART", re);
  }

inlineset
    : (pseudo+ S*)?
      LCURLY S*
        declarations
      RCURLY S*
      -> ^(RULE pseudo* declarations)
    ;

/*
 * The COLON recognized as the start of an invalid property (which is
 * used in some nasty CSS hacks) conflicts with the COLON in the
 * possible pseudo class selector of inlineset. jStyleParser favors
 * the hack recovery over the support for pseudo elements in inline
 * stylesheets.
 */
noprop
    :
    ( CLASSKEYWORD -> CLASSKEYWORD
    | NUMBER -> NUMBER
    | COMMA -> COMMA
    | GREATER -> GREATER
    | LESS -> LESS
    | QUESTION -> QUESTION
    | PERCENT -> PERCENT
    | EQUALS -> EQUALS
    | SLASH -> SLASH
    | EXCLAMATION -> EXCLAMATION
    | PLUS -> PLUS
    | ASTERISK -> ASTERISK
    | DASHMATCH -> DASHMATCH
    | INCLUDES -> INCLUDES
 // | COLON -> COLON
    | STRING_CHAR -> STRING_CHAR
    | CTRL -> CTRL
    | INVALID_TOKEN -> INVALID_TOKEN
    ) !S*
    ;
