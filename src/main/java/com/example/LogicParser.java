package com.example;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.rules.Rule;
import com.bpodgursky.jbool_expressions.rules.RuleList;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogicParser {
    public static Expression<Action> createAction(Element element) {
        String type = element.getElementsByTagName("type").item(0).getTextContent();

        if (!type.equals("and") && !type.equals("or")) {
            String text;
            String locator = element.getElementsByTagName("locator").item(0).getTextContent();
            if (type.equals("Input Text")) text = element.getElementsByTagName("text").item(0).getTextContent();
            else text = null;
            Action action = new Action(type, locator, text);
            return Variable.of(action);
        }
        if (type.equals("and")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();
            List<String> childString = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }
            for (int i = 0; i < childElements.size(); i++) {
                childString.add(childElements.get(i).getElementsByTagName("text").item(0).getTextContent());
            }
            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createAction(childElements.get(i));
            }

            Expression expression = And.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;

        } else if (type.equals("or")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();


            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }

            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createAction(childElements.get(i));
            }
            Expression expression = Or.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;
        }
        System.out.println("Wrong type of logic expression!");
        return null;
    }

    public static Expression createExpression(Element element) {

        String type = element.getElementsByTagName("type").item(0).getTextContent();
        if (!type.equals("and") && !type.equals("or")) return Variable.of(element);
        if (type.equals("and")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }
            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createExpression(childElements.get(i));
            }
            return And.of(expressionList);

        } else if (type.equals("or")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }
            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createExpression(childElements.get(i));
            }
            return Or.of(expressionList);
        }
        System.out.println("Wrong type of logic expression!");
        return null;
    }

    public static List<List<Element>> createDNFList(Expression expr) {
        System.out.println(expr);
        System.out.println("AFter call dnf");
        expr = RuleSet.toSop(expr);
        System.out.println(expr);
        List<List<Element>> list = new ArrayList<>();
        List<Expression> expressionList = expr.getChildren();
        for (Expression expression : expressionList) {
//            if (expression.getExprType().equals("variable"))
//                list.add(expression)
            list.add(expression.getAllK().stream().toList());
//            Collections.reverse(list);
        }
        return list;
    }

    public static void main(String[] args)  {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder bulider = factory.newDocumentBuilder();
            Document document = bulider.parse(new File("outline_saucedemo.xml"));
            NodeList logics = document.getElementsByTagName("LogicExpressionOfActions");
            Expression expression = LogicParser.createExpression((Element) document.getElementsByTagName("LogicExpressionOfActions").item(0));
            System.out.println(expression);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
