package com.example;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Hello world!
 *
 */      
public class App 
{

    public static void main( String[] args )
    {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
            DocumentBuilder bulider = factory.newDocumentBuilder();
            Document document = bulider.parse(new File("C:\\Users\\quyenhoang03\\Desktop\\demo\\outline_saucedemo.xml"));
            NodeList testcases = document.getElementsByTagName("TestCase");
            StringBuilder testScript = new StringBuilder();
            for (int i = 0; i < testcases.getLength(); i++) {
               Node testcase = testcases.item(i);
               if (testcase.getNodeType() == Node.ELEMENT_NODE) {
                  List<String> variables = new ArrayList<>();
                  List<String> locators = new ArrayList<>();
                  Map<String, List<String>> tuples = new HashMap<>();
                  Element testcaseElement = (Element) testcase;
                  NodeList andOfActions = testcaseElement.getElementsByTagName("AndOfAction");
                  for (int j = 0; j < andOfActions.getLength(); j++) {
                     Node andOfAction = andOfActions.item(j);
                     if (andOfAction.getNodeType() == Node.ELEMENT_NODE) {
                        Element andOfActionElement = (Element) andOfAction;
                        NodeList actions = andOfActionElement.getElementsByTagName("Action");
                        if (actions.getLength() > 1) {
                           String key = ((Element) actions.item(0)).getElementsByTagName("text").item(0).getTextContent();
                           for (int k = 0; k < actions.getLength(); k++) {
                              StringBuilder actionString = new StringBuilder();
                              Element action = (Element) actions.item(k);
                              if (k == 0) {
                                 tuples.put(key, new ArrayList<String>());
                                 variables.add(key);
                              } else tuples.get(key).add(action.getElementsByTagName("text").item(0).getTextContent());
                              NodeList params = action.getChildNodes();
                              for (int index = 0; index < params.getLength(); index++) {
                                 Node param = params.item(index);
                                 if (param.getNodeType() == Node.ELEMENT_NODE) {
                                    Element paramElement = (Element) param;
                                    actionString.append("\t").append(paramElement.getTextContent());
                                    if (paramElement.getTagName().equals("locator")) locators.add(paramElement.getTextContent());
                                 }
                              }
                              actionString.append("\n");
                              testScript.append(actionString);
                              System.out.println(variables);
                              System.out.println(tuples);
                           }
                        } else {
                           Element action = (Element) actions.item(0);
                           StringBuilder actionString = new StringBuilder();
                           NodeList params = action.getChildNodes();
                           for (int index = 0; index < params.getLength(); index++) {
                              Node param = params.item(index);
                              if (param.getNodeType() == Node.ELEMENT_NODE) {
                                 Element paramElement = (Element) param;
                                 actionString.append("\t").append(paramElement.getTextContent());
                                 if (paramElement.getTagName().equals("locator")) locators.add(paramElement.getTextContent());
                              }
                           }
                           actionString.append("\n");
                           testScript.append(actionString);
                           System.out.println(variables);
                           System.out.println(tuples);
                           System.out.println(locators);
                        }
                     }
                  }
               }
            }
            System.out.println(testScript);
         } catch (Exception e) {
            e.printStackTrace();
         }
    }
}
