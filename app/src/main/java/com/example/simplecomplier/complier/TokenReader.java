package com.example.simplecomplier.complier;

/**
 * Created by JJBOOM on 2020/01/04.
 */
public interface TokenReader {
    Token read();

    Token peek();

    void unread();

    int getPosition();

    void setPosition(int position);
}
