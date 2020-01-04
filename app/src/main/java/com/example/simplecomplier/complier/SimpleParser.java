package com.example.simplecomplier.complier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 简单的语法分析器。
 * 能够解析简单的表达式、变量声明（包括初始化语句）、赋值语句
 * <p>
 * 支持的语法规则如下：
 * program -> intDeclare | expressionStatement | assignmentStatement
 * intDeclare -> 'int' Id ( = additive) ';'
 * expressionStatement -> additive ';'
 * assignmentStatement -> Id = additive ';'
 * additive -> multiplicative ( (+ | -) multiplicative)* ';'
 * multiplicative -> primary ( (* | /) primary)* ';'
 * primary -> IntLiteral | Id | '('additive')'
 */
public class SimpleParser {

    public ASTNode parse(String script) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokenize = lexer.tokenize(script);
        return programme(tokenize);
    }

    public void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode astNode : node.getChildren()) {
            dumpAST(astNode, indent + "\t");
        }
    }

    //region 根据词法单元构建语法树

    /**
     * AST根节点，解析的入口
     */
    private SimpleASTNode programme(TokenReader reader) throws Exception {
        SimpleASTNode root = new SimpleASTNode(ASTNodeType.Programm, "pwc");
        while (reader.peek() != null) {
            SimpleASTNode child = intDeclare(reader);
            if (child == null) {
                child = expressionStatement(reader);
            }
            if (child == null) {
                child = assignmentStatement(reader);
            }
            if (child != null) {
                root.addChild(child);
            } else {
                throw new Exception("未知的语句");
            }
        }
        return root;
    }

    /**
     * 表达式语句
     * <p>
     * expressionStatement -> additive ';'
     */
    private SimpleASTNode expressionStatement(TokenReader reader) throws Exception {
        int position = reader.getPosition();
        SimpleASTNode node = additive(reader);
        if (node != null) {
            Token token = reader.peek();
            if (token != null && token.getType() == TokenType.Semicolon) {
                reader.read();
            } else {
                node = null;
                reader.setPosition(position);//表达式没有跟分号，回溯
            }
        }
        return node;
    }

    /**
     * 赋值语句
     * assignmentStatement -> Id = additive ';'
     */
    private SimpleASTNode assignmentStatement(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        Token token = reader.peek();
        if (token != null && token.getType() == TokenType.Identifier) {
            token = reader.read();
            node = new SimpleASTNode(ASTNodeType.AssignmentStmt, token.getText());
            token = reader.peek();
            if (token != null && token.getType() == TokenType.Assignment) {
                reader.read();
                SimpleASTNode child = additive(reader);
                if (child != null) {
                    node.addChild(child);
                    token = reader.peek();
                    if (token != null && token.getType() == TokenType.Semicolon) {
                        reader.read();
                    } else {
                        throw new Exception("非法的语句，需要分号");
                    }
                } else {
                    throw new Exception("非法的赋值语句，需要一个表达式");
                }
            } else {
                reader.unread();
                node = null;
            }
        }
        return node;
    }

    /**
     * int声明
     * <p>
     * intDeclare -> 'int' Id ( = additive) ';'
     */
    private SimpleASTNode intDeclare(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        Token token = reader.peek();
        if (token != null && token.getType() == TokenType.Int) {
            reader.read();
            token = reader.peek();
            if (token != null && token.getType() == TokenType.Identifier) {
                token = reader.read();
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = reader.peek();
                if (token != null && token.getType() == TokenType.Assignment) {
                    reader.read();
                    SimpleASTNode child = additive(reader);
                    if (child != null) {
                        node.addChild(child);
                    } else {
                        throw new Exception("非法的变量初始化，需要表达式");
                    }
                }
            } else {
                throw new Exception("需要变量名");
            }
        }
        if (node != null) {
            token = reader.peek();
            if (token != null && token.getType() == TokenType.Semicolon) {
                reader.read();
            } else {
                throw new Exception("非法语句，需要分号");
            }
        }
        return node;
    }
    //endregion

    //region 四则运算表达式求值

    /**
     * @param node   AST根节点,只接受四则运算表达式
     * @param indent 格式化间隔
     * @return 根据抽象语法树计算出的结果
     */
    public int evaluate(ASTNode node, String indent) throws Exception {
        int result;
        System.out.println(indent + "Calculating " + node.getType());
        switch (node.getType()) {
            case Programm:
                result = evaluate(node.getChildren().get(0), indent);
                break;
            case Multiplicative:
                ASTNode left = node.getChildren().get(0);
                ASTNode right = node.getChildren().get(1);
                int val1 = evaluate(left, indent + "\t");
                int val2 = evaluate(right, indent + "\t");
                if ("*".equals(node.getText())) {
                    result = val1 * val2;
                } else {
                    result = val1 / val2;
                }
                break;
            case Additive:
                left = node.getChildren().get(0);
                right = node.getChildren().get(1);
                val1 = evaluate(left, indent + "\t");
                val2 = evaluate(right, indent + "\t");
                if ("+".equals(node.getText())) {
                    result = val1 + val2;
                } else {
                    result = val1 - val2;
                }
                break;
            case IntLiteral:
                result = Integer.parseInt(node.getText());
                break;
            default:
                throw new Exception("非法类型");
        }
        System.out.println(indent + "Result: " + result);
        return result;
    }
    //endregion

    //region 语法分析

    /**
     * 解析加法表达式
     * 为回避左递归导致了右结合问题
     * <p>
     * additive -> multiplicative ( (+|-) multiplicative)* ';'
     */
    private SimpleASTNode additive(TokenReader reader) throws Exception {
        SimpleASTNode child1 = multiplicative(reader);
        SimpleASTNode node = child1;
        Token token;
        while ((token = reader.peek()) != null &&
                (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus)) {
            token = reader.read();
            SimpleASTNode child2 = multiplicative(reader);
            if (child2 != null) {
                node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                node.addChild(child1);
                node.addChild(child2);
                child1 = node;
            } else {
                throw new Exception("非法的加法表达式，需要右半部分");
            }
        }
        return node;
    }

    /**
     * 解析乘法表达式
     * 为回避左递归导致了右结合问题
     * <p>
     * multiplicative -> primary ( (*|/) primary)* ';'
     */
    private SimpleASTNode multiplicative(TokenReader reader) throws Exception {
        SimpleASTNode child1 = primary(reader);
        SimpleASTNode node = child1;
        Token token;
        while ((token = reader.peek()) != null &&
                (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
            token = reader.read();
            SimpleASTNode child2 = primary(reader);
            if (child2 != null) {
                node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                node.addChild(child1);
                node.addChild(child2);
                child1 = node;
            } else {
                throw new Exception("非法的乘法表达式，需要右半部分");
            }
        }
        return node;
    }

    /**
     * 解析原始类型数据
     * 包括int字面值和括号表达式
     * <p>
     * primary -> IntLiteral | Id | '('additive')'
     */
    private SimpleASTNode primary(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        Token token = reader.peek();
        if (token != null) {
            if (token.getType() == TokenType.IntLiteral) {
                token = reader.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            } else if (token.getType() == TokenType.Identifier) {
                token = reader.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            } else if (token.getType() == TokenType.LeftParen) {//左括号需要后继加法表达式和右括号
                reader.read();//消耗掉token
                node = additive(reader);
                if (node == null) {
                    throw new Exception("括号中需要一个加法表达式");
                } else {
                    token = reader.peek();
                    if (token != null && token.getType() == TokenType.RightParen) {
                        reader.read();
                    } else {
                        throw new Exception("需要右括号");
                    }
                }
            }
        }
        return node;
    }
    //endregion

    private static class SimpleASTNode implements ASTNode {
        private ASTNode parent;
        private List<ASTNode> children = new ArrayList<>();
        private List<ASTNode> readOnlyChildren = Collections.unmodifiableList(children);
        private final ASTNodeType nodeType;
        private final String text;

        private SimpleASTNode(ASTNodeType nodeType, String text) {
            this.nodeType = nodeType;
            this.text = text;
        }

        @Override
        public ASTNode getParent() {
            return parent;
        }

        @Override
        public List<ASTNode> getChildren() {
            return children;
        }

        @Override
        public ASTNodeType getType() {
            return nodeType;
        }

        @Override
        public String getText() {
            return text;
        }

        public void addChild(ASTNode child) {
            if (child != null) {
                children.add(child);
            }
        }
    }
}
