package com.example.simplecomplier.complier;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的手写的词法分析器
 * <p>
 * 1，解析比较语句：a，定义各种状态，b，定义token初始化方法，c，定义状态机流转逻辑
 * 2, 解析赋值操作：a，需要解析关键字int，添加id_int*中间state，b，添加赋值状态
 * 3，解析算术表达式：添加加减乘除的state和type
 */
public class SimpleLexer {
    public static void main(String[] args) {
        String s1 = "age >= 45";
        String s2 = "int age = 40";
        String s3 = "2+3*5";

        SimpleLexer lexer = new SimpleLexer();

        TokenReader tokenReader = lexer.tokenize(s3);
        dumpTokens(tokenReader);
    }

    private static void dumpTokens(TokenReader tokenReader) {
        while (tokenReader.peek() != null) {
            Token token = tokenReader.read();
            System.out.println(token.getText() + "\t\t" + token.getType());
        }
    }

    private StringBuffer tokenText = new StringBuffer();
    private List<Token> tokens = new ArrayList<>();
    private SimpleToken token = new SimpleToken();

    /**
     * 状态机，根据当前所处的状态和读到的字符决定下一步走向
     *
     * @param code 代码字符串
     */
    public TokenReader tokenize(String code) {
        tokens = new ArrayList<>();
        tokenText = new StringBuffer();
        token = new SimpleToken();
        int ich;
        char ch = 0;
        DfaState state = DfaState.Initial;
        try (CharArrayReader reader = new CharArrayReader(code.toCharArray())) {
            while ((ich = reader.read()) != -1) {
                ch = (char) ich;
                switch (state) {
                    case Initial:
                        state = initToken(ch);//当前是初始状态，根据读到的字符决定走向哪种状态
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {//当前是Id状态，又读到了字母或数字，则保持当前状态
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {//n进入int的下一步
                            state = DfaState.Id_int2;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {//非n字符则进入id状态
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {//非t字符则进入id状态
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBland(ch)) {//int后跟空白字符，说明是int关键字
                            token.tokenType = TokenType.Int;
                            state = initToken(ch);
                        } else {//int后跟非空白字符，说明是id
                            state = DfaState.Id;
                            tokenText.append(ch);
                        }
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {//当前是int字面值状态，又读到了数字
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case GT:
                        if (ch == '=') {//当前是大于号，又读到了等于号，进入大于等于状态
                            state = DfaState.GE;
                            token.tokenType = TokenType.GE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case Semicolon:
                    case LeftParen:
                    case RightParen:
                        state = initToken(ch);//退出当前状态并保存token
                        break;
                }
            }
                /*末尾的-1，用于终结最后一个token*/
            if (tokenText.length() > 0) {
                initToken(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SimpleTokenReader(tokens);
    }

    private DfaState initToken(char ch) {
        /*进入初始化状态时清理其他状态*/
        if (tokenText.length() > 0) {
            token.text = tokenText.toString();
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }

        DfaState newState = DfaState.Initial;
        if (isAlpha(ch)) {
            if (ch == 'i') {
                newState = DfaState.Id_int1;
            } else {
                newState = DfaState.Id;
            }
            token.tokenType = TokenType.Identifier;
            tokenText.append(ch);
        } else if (isDigit(ch)) {
            newState = DfaState.IntLiteral;
            token.tokenType = TokenType.IntLiteral;
            tokenText.append(ch);
        } else {
            switch (ch) {
                case '>':
                    newState = DfaState.GT;
                    token.tokenType = TokenType.GT;
                    tokenText.append(ch);
                    break;
                case '=':
                    newState = DfaState.Assignment;
                    token.tokenType = TokenType.Assignment;
                    tokenText.append(ch);
                    break;
                case '+':
                    newState = DfaState.Plus;
                    token.tokenType = TokenType.Plus;
                    tokenText.append(ch);
                    break;
                case '-':
                    newState = DfaState.Minus;
                    token.tokenType = TokenType.Minus;
                    tokenText.append(ch);
                    break;
                case '*':
                    newState = DfaState.Star;
                    token.tokenType = TokenType.Star;
                    tokenText.append(ch);
                    break;
                case '/':
                    newState = DfaState.Slash;
                    token.tokenType = TokenType.Slash;
                    tokenText.append(ch);
                    break;
                case '(':
                    newState=DfaState.LeftParen;
                    token.tokenType=TokenType.LeftParen;
                    tokenText.append(ch);
                    break;
                case ')':
                    newState=DfaState.RightParen;
                    token.tokenType=TokenType.RightParen;
                    tokenText.append(ch);
                    break;
                case ';':
                    newState=DfaState.Semicolon;
                    token.tokenType=TokenType.Semicolon;
                    tokenText.append(ch);
                    break;
            }
        }
        return newState;
    }

    private boolean isAlpha(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isBland(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }


    private enum DfaState {
        Initial, IntLiteral,
        Id, Id_int1, Id_int2, Id_int3,
        GT, GE,
        Assignment,
        Plus, Minus,
        Star, Slash,

        Semicolon,//分号
        LeftParen,
        RightParen,
    }

    private static class SimpleToken implements Token {
        TokenType tokenType;
        String text;

        @Override
        public TokenType getType() {
            return tokenType;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return tokenType + " : " + text;
        }
    }

    private static class SimpleTokenReader implements TokenReader {
        private final List<Token> tokens;
        private int position;

        private SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
            position = 0;
        }

        @Override
        public Token read() {
            if (position < tokens.size()) {
                return tokens.get(position++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (position < tokens.size()) {
                return tokens.get(position);
            }
            return null;
        }

        @Override
        public void unread() {
            if (position > 0) {
                position--;
            }
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public void setPosition(int position) {
            if (0 <= position && position < tokens.size()) {
                this.position = position;
            }
        }
    }
}
