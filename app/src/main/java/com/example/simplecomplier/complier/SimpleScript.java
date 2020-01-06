package com.example.simplecomplier.complier;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 一个简单的REPL，使用{@link SimpleParser}实现
 */
public class SimpleScript {
    private final Map<String, Integer> variables = new HashMap<>();
    /*详细模式*/
    public static boolean verbose;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "-v".equals(args[0])) {
            verbose = true;
            System.out.println("verbose mode");
        }
        System.out.println("Simple script language!");

        SimpleParser parser = new SimpleParser();
        SimpleScript simpleScript = new SimpleScript();

        Scanner reader = new Scanner(System.in);
        StringBuilder scriptTxt = new StringBuilder();
        System.out.print(">");
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            if ("exit();".equals(line)) {
                return;
            }
            scriptTxt.append(line).append("\n");
            if (line.endsWith(";")) {//分号结尾才开始执行
                try {
                    ASTNode node = parser.parse(scriptTxt.toString());
                    if (verbose) {
                        parser.dumpAST(node, "");
                    }
                    Integer result = simpleScript.evaluate(node, "");
                    System.out.println(String.valueOf(result));
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                scriptTxt = new StringBuilder();
                System.out.print(">");
            }
        }
    }

    public Integer evaluate(ASTNode node, String indent) throws Exception {
        if (verbose) {
            System.out.println(indent + "evaluate:" + node.getType() + " " + node.getText());
        }

        Integer result = null;
        switch (node.getType()) {
            case Programm:
                for (ASTNode child : node.getChildren()) {
                    result = evaluate(child, indent + "\t");
                }
                break;
            case AssignmentStmt:
                String varName = node.getText();
                if (!variables.containsKey(varName)) {
                    throw new Exception("未知的变量名");
                }//继续向下
            case IntDeclaration:
                varName = node.getText();
                Integer varValue = null;
                if (!node.getChildren().isEmpty()) {
                    varValue = evaluate(node.getChildren().get(0), indent + "\t");
                }
                variables.put(varName, varValue);
                break;
            case Primary:
                result = Integer.parseInt(node.getText());
                break;
            case Multiplicative:
                ASTNode child1 = node.getChildren().get(0);
                ASTNode child2 = node.getChildren().get(1);
                Integer val1 = evaluate(child1, indent + "\t");
                Integer val2 = evaluate(child2, indent + "\t");
                if ("*".equals(node.getText())) {
                    result = val1 * val2;
                } else {
                    result = val1 / val2;
                }
                break;
            case Additive:
                child1 = node.getChildren().get(0);
                child2 = node.getChildren().get(1);
                val1 = evaluate(child1, indent + "\t");
                val2 = evaluate(child2, indent + "\t");
                if ("+".equals(node.getText())) {
                    result = val1 + val2;
                } else {
                    result = val1 - val2;
                }
                break;
            case Identifier:
                varName = node.getText();
                if (variables.containsKey(varName)) {
                    Integer value = variables.get(varName);
                    if (value != null) {
                        result = value;
                    } else {
                        throw new Exception("变量尚未初始化");
                    }
                } else {
                    throw new Exception("未知的变量:" + varName);
                }
                break;
            case IntLiteral:
                result = Integer.parseInt(node.getText());
                break;
        }

        if (verbose) {
            System.out.println(indent + "result:" + result);
        }
        return result;
    }
}
