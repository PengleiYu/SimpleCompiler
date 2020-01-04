package com.example.simplecomplier.complier;

import java.util.List;

/**
 * Created by JJBOOM on 2020/01/04.
 */
public interface ASTNode {
    /*父节点*/
    ASTNode getParent();

    /*子节点*/
    List<ASTNode> getChildren();

    /*节点类型*/
    ASTNodeType getType();

    /*文本值*/
    String getText();
}
