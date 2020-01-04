package com.example.simplecomplier.complier;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JJBOOM on 2020/01/04.
 */
public class SimpleCalculator {

    public SimpleASTNode intDeclare(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        Token token = reader.peek();
        if (token.getType() == TokenType.Int) {
            reader.read();
            token = reader.peek();
            if (token.getType() == TokenType.Identifier) {
                token = reader.read();
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = reader.peek();
                if (token.getType() == TokenType.Assignment) {
                    reader.read();
                    SimpleASTNode child = additive(reader);
                    if (child != null) {
                        node.addChild(child);
                    } else {
                        throw new Exception("非法的初始化，需要一个表达式");
                    }
                }

            }
            if (node != null) {
                token = reader.peek();
                if (token != null && token.getType() == TokenType.Semicolon) {
                    reader.read();
                } else {
                    throw new Exception("非法表达式，需要分号");
                }
            }
        }
        return node;
    }

    private SimpleASTNode additive(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        SimpleASTNode child1 = multiplicative(reader);
        if (child1 != null) {
            Token token = reader.peek();
            if (token != null && (token.getType() == TokenType.Minus || token.getType() == TokenType.Plus)) {
                token = reader.read();
                ASTNode child2 = additive(reader);
                if (child2 == null) {
                    throw new Exception("非法的加法表达式，需要右半部分");
                } else {
                    node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                }
            } else {
                node = child1;
            }
        }
        return node;
    }

    private SimpleASTNode multiplicative(TokenReader reader) throws Exception {
        SimpleASTNode node = null;
        SimpleASTNode child1 = primary(reader);
        if (child1 != null) {
            Token token = reader.peek();
            if (token != null &&
                    (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
                token = reader.read();
                SimpleASTNode child2 = multiplicative(reader);
                if (child2 == null) {
                    throw new Exception("非法乘法表达式，需要右半部分");
                } else {
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                }
            } else {
                node = child1;
            }
        }
        return node;
    }

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

    public int evaluate(String script) throws Exception {
        ASTNode tree = parse(script);
        dumpAST(tree, "");
        return evaluate(tree, "");
    }

    private int evaluate(ASTNode node, String indent) throws Exception {
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

    public void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode astNode : node.getChildren()) {
            dumpAST(astNode, indent + "\t");
        }
    }

    private ASTNode parse(String script) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        return program(tokens);
    }

    private ASTNode program(TokenReader reader) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "Calculator");
        SimpleASTNode child = additive(reader);
        if (child != null) {
            node.addChild(child);
        }
        return node;
    }

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
