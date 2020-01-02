package com.example.simplecomplier.complier;

/**
 * Created by JJBOOM on 2019/12/29.
 */
public enum TokenType {
    Identifier, IntLiteral,
    GT, GE, //比较
    Int, //关键字
    Assignment, //赋值
    Plus,Minus,
    Star,Slash,
}
