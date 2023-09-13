package com.example;

import au.com.bytecode.opencsv.CSVReader;

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

public class ScriptGen {
    public static void createDataSheet(String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            File newfile = new File("data.csv");
            if (newfile.createNewFile()) {
                System.out.println("Success");
            } else {
                System.out.println("Failed");
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data_saucedemo.csv"));
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(path));
            NodeList testcases = document.getElementsByTagName("TestCase");
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                if (testcase.getNodeType() == Node.ELEMENT_NODE) {
                    Element testcaseElement = (Element) testcase;
                    NodeList actions = testcaseElement.getElementsByTagName("Action");
                    for (int j = 0; j < actions.getLength(); j++) {
                        Node action = actions.item(j);
                        if (action.getNodeType() == Node.ELEMENT_NODE) {
                            Element actionElement = (Element) action;
                            String type = actionElement.getElementsByTagName("type").item(0).getTextContent();
                            if (!type.equals("Maximize")) {
                                String locator = actionElement.getElementsByTagName("locator").item(0).getTextContent();
                                if (content.indexOf(locator) == -1) content.append(locator).append("\n");
                                if (actionElement.getElementsByTagName("text").getLength() > 0) {
                                    String text = actionElement.getElementsByTagName("text").item(0).getTextContent();
                                    if (content.indexOf(text) == -1) content.append(text).append("\n");
                                }
                            }
                        }
                    }
                }
            }
            bufferedWriter.append(content.toString());
            bufferedWriter.close();
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static Map<String, List<String>> createMap(String path) {
        Map<String, List<String>> variables = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
            String[] words = null;
            while ((words = csvReader.readNext()) != null) {
                variables.put(words[0], new ArrayList<String>());
                for (int i = 1; i < words.length; i++) {
                    variables.get(words[0]).add(words[i]);
                }
            }
            System.out.println(variables);
            return variables;
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return variables;
    }
    
    public static void backtrack(int n, Map<String, List<String>> map, List<String> vars, StringBuilder content, StringBuilder testScript, AtomicInteger countTest, String scenario) {
        if (n > vars.size()) {
            testScript.insert(0, scenario + " " + countTest + "\n");
            content.append(testScript);
            countTest.incrementAndGet();
            return;
        }
        List<String> list = map.get(vars.get(n - 1));
        for (int i = 0; i < list.size(); i++) {
            int start = testScript.indexOf(vars.get(n - 1));
            int end = start + vars.get(n - 1).length();
            testScript.replace(start, end, list.get(i));
            backtrack(n + 1, map, vars, content, new StringBuilder(testScript), countTest, scenario);
            int newEnd = start + list.get(i).length();
            testScript.replace(start, newEnd, vars.get(n - 1));
        }
    }
    public static void createScript(String outlinePath, String dataPath) {
        Map<String, List<String>> variables = createMap(dataPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            File newFile = new File("test1.robot");
            if (newFile.createNewFile()) {
                System.out.println("Success");
            } else {
                System.out.println("Failed");
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("test_saucedemo.robot"));
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(outlinePath));
            String url = document.getElementsByTagName("url").item(0).getTextContent();
            NodeList testcases = document.getElementsByTagName("TestCase");
            StringBuilder content = new StringBuilder();
            content.append("*** Setting ***\nLibrary\tSeleniumLibrary\n\n*** Test Cases ***\n");
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                if (testcase.getNodeType() == Node.ELEMENT_NODE) {
                    Element testcaseElement = (Element) testcase;
                    String scenario = testcaseElement.getElementsByTagName("Scenario").item(0).getTextContent();
                    AtomicInteger countTest = new AtomicInteger(1);
                    StringBuilder testScript = new StringBuilder();
                    testScript.append("\tOpen Browser\t").append(url).append("\tChrome\n");
                    NodeList actions = testcaseElement.getElementsByTagName("Action");
                    List<String> vars = new ArrayList<>();
                    for (int j = 0; j < actions.getLength(); j++) {
                        Node action = actions.item(j);
                        if (action.getNodeType() == Node.ELEMENT_NODE) {
                            Element actionElement = (Element) action;
                            String type = actionElement.getElementsByTagName("type").item(0).getTextContent();
                            if (type.equals("Maximize")) testScript.append("\tMaximize Browser Window");
                            else {
                                NodeList params = actionElement.getChildNodes();
                                for (int index = 0; index < params.getLength(); index++) {
                                    Node param = params.item(index);
                                    if (param.getNodeType() == Node.ELEMENT_NODE) {
                                        Element paramElement = (Element) param;
                                        if (paramElement.getTagName().equals("type")) testScript.append("\t").append(type);
                                        else {
                                            testScript.append("\t").append(paramElement.getTextContent());
                                            vars.add(paramElement.getTextContent());
                                        }

                                    }
                                }
                            }
                            testScript.append("\n");
                        }
                    }
                    NodeList validations = testcaseElement.getElementsByTagName("Validation");
                    for (int j = 0; j < validations.getLength(); j++) {
                        Node validation = validations.item(j);
                        if (validation.getNodeType() == Node.ELEMENT_NODE) {
                            Element validationElement = (Element) validation;
                            String type = validationElement.getElementsByTagName("type").item(0).getTextContent();
                            switch (type) {
                                case "PageContainValiton": 
                                    String text = validationElement.getElementsByTagName("text").item(0).getTextContent();
                                    testScript.append("\tPage should contain\t").append(text).append("\n");
                                    break;
                                case "URLValidation": 
                                    String correctURL = validationElement.getElementsByTagName("url").item(0).getTextContent();
                                    testScript.append("\tLocation should be\t").append(correctURL).append("\n");
                            }
                        }
                    }
                    System.out.println(testScript.toString());
                    System.out.println(vars);
                    backtrack(1, variables, vars, content, testScript, countTest, scenario);
                    System.out.println(content);
                    
                }
            }
            bufferedWriter.append(content.toString());
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // createDataSheet("C:\\Users\\quyenhoang03\\Desktop\\demo\\outline_saucedemo.xml");
        // createMap("C:\\Users\\quyenhoang03\\Desktop\\demo\\data1.csv");
        createScript("C:\\Users\\quyenhoang03\\Desktop\\demo\\outline_saucedemo.xml", 
        "C:\\Users\\quyenhoang03\\Desktop\\demo\\data_saucedemo.csv");

    }
}
