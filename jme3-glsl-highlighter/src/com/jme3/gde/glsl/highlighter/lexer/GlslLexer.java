/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.glsl.highlighter.lexer;

import java.util.logging.Logger;
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
    private Logger log = Logger.getLogger(this.getClass().getCanonicalName());
    
    private int oneCBack = -22222;

    public GlslLexer(LexerRestartInfo info) {
        lexerInput = info.input();
        tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<GlslTokenID> nextToken() {
        int c;
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
                    readTillNewLine();
                    return token(GlslTokenID.INLINE_COMMENT);
                }else
                    lexerInput.backup(1);
                break;
            case '\"':
            case '\'':
                //String starts here
                int previous = c, starter = c;
                while (true){
                    int now = lexerInput.read();

                    if (now == starter && previous != '\\')
                        break;
                    previous = now;
                }
                return token(GlslTokenID.STRING);
            case '#':
                log.info("One c back was: " + (char) oneCBack);
                if (oneCBack == '\n' || oneCBack == '\r' || oneCBack == -22222){
                    //Preprocessor code
                    readTillNewLine();
                    return token(GlslTokenID.PREPROCESSOR);
                }
                break;
            case '\n':
            case '\r':
                oneCBack = c;
                return token(GlslTokenID.NEW_LINE);
            case LexerInput.EOF:
                return null;
        }
        oneCBack = c;
        return token(GlslTokenID.TEXT);
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
    
    private void readTillNewLine(){
        while (true){
            int in = lexerInput.read();
            if (in == '\n' || in == '\r' || in == LexerInput.EOF){
                lexerInput.backup(1);
                break;
            }
        }
    }
}
