package com.example.simplecomplier.complier;

/**
 * Created by JJBOOM on 2019/12/29.
 */
public interface Token {
    TokenType getType();

    String getText();
}
