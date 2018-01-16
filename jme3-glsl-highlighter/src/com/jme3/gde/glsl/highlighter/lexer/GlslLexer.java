/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.glsl.highlighter.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author grizeldi
 */
public class GlslLexer implements Lexer<GlslTokenID>{
    private LexerInput lexerInput;
    private TokenFactory tokenFactory;

    public GlslLexer(LexerRestartInfo info) {
        lexerInput = info.input();
        tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<GlslTokenID> nextToken() {
        int c;
        while (true){
            c = lexerInput.read();
            if (isDigit(c)){
                while (true){
                    int next = lexerInput.read();
                    if (!isDigit(next)){
                        if (next == '.' || next == 'f' || next == 'F')
                            continue;
                        lexerInput.backup(1);
                        return token(GlslTokenID.NUMBER);
                    }
                }
            }
            switch (c){
                case '/':
                    if (lexerInput.read() == '/'){
                        //It's an inline comment
                        while (true){
                            int in = lexerInput.read();
                            if (in == '\n' || in == '\r'){
                                lexerInput.backup(1);
                                break;
                            }
                        }
                        return token(GlslTokenID.INLINE_COMMENT);
                    }else
                        lexerInput.backup(1);
                    break;
                case LexerInput.EOF:
                    return null;
                default:
                    return token(GlslTokenID.TEXT);
            }
        }
    }

    @Override
    public Object state() {
        return null;
    }

    //Honestly, I have no idea what is this.
    @Override
    public void release() {}
    
    private Token<GlslTokenID> token(GlslTokenID id){
        return tokenFactory.createToken(id);
    }
    
    private boolean isDigit(int c){
        switch (c){
            case'0':case'1':case'2':case'3':case'4':
            case'5':case'6':case'7':case'8':case'9':
                return true;
            default:
                return false;
        }
    }
}
