package com.example;

import au.com.bytecode.opencsv.CSVReader;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ScriptGen {
    public static void createDataSheetV2(String dataPath) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
            File newfile = new File("data.csv");
            if (newfile.createNewFile()) {
                System.out.println("Success");
            } else {
                System.out.println("Failed");
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data_saucedemo.csv"));
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(dataPath));
            StringBuilder content = new StringBuilder();
            NodeList testcases = document.getElementsByTagName("TestCase");
            List<Element> testCaseElements = new ArrayList<>();
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                if (testcase.getNodeType() == Node.ELEMENT_NODE)
                    testCaseElements.add((Element) testcase);
            }
            for (Element testcaseElement: testCaseElements) {
                //Get the outer logic of actions
                NodeList testCaseChildNodes = testcaseElement.getChildNodes();
                List<Element> expressionActionElements = new ArrayList<>();
                for (int i = 0; i < testCaseChildNodes.getLength(); i++) {
                    Node childNode = testCaseChildNodes.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && ((Element) childNode).getTagName().equals("LogicExpressionOfActions"))
                        expressionActionElements.add((Element) childNode);
                }
                for (Element expressionActionElement: expressionActionElements) {
                    String type = expressionActionElement.getElementsByTagName("type").item(0).getTextContent();
                    if (!type.equals("and") && !type.equals("or")) {
                        String locator = expressionActionElement.getElementsByTagName("locator").item(0).getTextContent();
                        if (content.indexOf(locator) == -1)
                            content.append(locator).append("\n");
                        if (expressionActionElement.getElementsByTagName("text").getLength() > 0) {
                            String text = expressionActionElement.getElementsByTagName("text").item(0).getTextContent();
                            if (content.indexOf(text) == -1)
                                content.append(text).append("\n");
                        }
                    } else {
                        List<List<Element>> dnfList = LogicParser.createDNFList(LogicParser.createAction(expressionActionElement));
                        List<String> locators = new ArrayList<>();
                        List<List<String>> texts = new ArrayList<>();
                        for (List<Element> elementList : dnfList) {
                            List<String> textList = new ArrayList<>();
                            for (Element element: elementList) {
                                String locator = element.getElementsByTagName("locator").item(0).getTextContent();
                                if (content.indexOf(locator) == -1) content.append(locator).append("\n");
                                if (element.getElementsByTagName("text").getLength() > 0)
                                    textList.add(element.getElementsByTagName("text").item(0).getTextContent());
                            }
//                            Collections.sort(textList);
                            texts.add(new ArrayList<>(textList));
                        }
                        int[] nums = new int[texts.size()];
                        for (int i = 0; i < nums.length; i++) nums[i] = i;
                        List<List<Integer>> subsets = Subset.subsets(nums);
                        for (List<Integer> subset : subsets) {
                            if (subset.isEmpty()) continue;
                            StringBuilder satisfy = new StringBuilder();
                            for (int index : subset) {
                                List<String> textList = texts.get(index);
                                for (String text : textList) {
                                    if (satisfy.indexOf(text) == -1) {
                                        if (satisfy.isEmpty()) satisfy.append("(").append(text);
                                        else satisfy.append(" & ").append(text);
                                    }
                                }
                            }
                            satisfy.append(")");
                            if (content.indexOf(satisfy.toString()) == -1)
                                content.append(new StringBuilder(satisfy)).append("\n");
                        }
                    }

                }

            }
            bufferedWriter.append(content);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
                if (testcase.getNodeType() != Node.ELEMENT_NODE) continue;
                Element testcaseElement = (Element) testcase;
                NodeList actions = testcaseElement.getElementsByTagName("Action");
                for (int j = 0; j < actions.getLength(); j++) {
                    Node action = actions.item(j);
                    if (action.getNodeType() != Node.ELEMENT_NODE) continue;
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
                variables.put(words[0], new ArrayList<>());
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

    public static void replace(String oldStr, String newStr, StringBuilder testscript) {
        int start = testscript.indexOf(oldStr);
        int end = start + oldStr.length();
        testscript.replace(start, end, newStr);
    }

    public static void backtrack(int n, Map<String, List<String>> data, List<String> vars, StringBuilder content, StringBuilder testScript, Map<String, List<String>> tuples, AtomicInteger countTest) {
        if (n > vars.size()) {
            testScript.insert(0, "Test " + countTest + "\n");
            countTest.incrementAndGet();
            content.append(testScript);
            return;
        }
        List<String> list = data.get(vars.get(n - 1));
        if (tuples.containsKey(vars.get(n - 1))) {
            for (int i = 0; i < list.size(); i++) {
                StringBuilder prev = new StringBuilder(testScript);
                replace(vars.get(n - 1), list.get(i), testScript);
                for (int j = 0; j < tuples.get(vars.get(n - 1)).size(); j++) {
                    replace(tuples.get(vars.get(n - 1)).get(j), data.get(tuples.get(vars.get(n - 1)).get(j)).get(i), testScript);
                }
                backtrack(n + 1, data, vars, content, new StringBuilder(testScript), tuples, countTest);
                testScript = new StringBuilder(prev);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                int start = testScript.indexOf(vars.get(n - 1));
                int end = start + vars.get(n - 1).length();
                testScript.replace(start, end, list.get(i));
                backtrack(n + 1, data, vars, content, new StringBuilder(testScript), tuples, countTest);
                int newEnd = start + list.get(i).length();
                testScript.replace(start, newEnd, vars.get(n - 1));
            }
        }

    }

    public static void fillData(int n, StringBuilder actionString, Map<String, List<String>> data, List<String> placeholders, List<StringBuilder> lineOptions, Map<String, List<String>> tuples) {
        if (n >= placeholders.size()) {
            lineOptions.add(new StringBuilder(actionString));
            return;
        } else {
            String placeholder = placeholders.get(n);
            List<String> options = data.get(placeholder);
            if (!tuples.containsKey(placeholder)) {
                for (String option : options) {
                    replace(placeholder, option, actionString);
                    fillData(n + 1, new StringBuilder(actionString), data, placeholders, lineOptions, tuples);
                    replace(option, placeholder, actionString);
                }
            } else {
                StringBuilder prev = new StringBuilder(actionString);
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i);
                    replace(placeholder, option, actionString);
                    for (String otherText : tuples.get(placeholder)) {
                        replace(otherText, data.get(otherText).get(i), actionString);
                    }
                    fillData(n + 1, new StringBuilder(actionString), data, placeholders, lineOptions, tuples);
                    actionString = new StringBuilder(prev);
                }
            }
        }
    }

    public static void createTestCase(int n, Map<Integer, List<StringBuilder>> lines, StringBuilder testScript, int stopFlag, StringBuilder content, AtomicInteger testCount) {
        if (n >= lines.size()) {
            content.append("Test ").append(testCount).append("\n").append(testScript);
            testCount.incrementAndGet();
            return;
        }
        List<StringBuilder> lineOptions = lines.get(n);
        for (StringBuilder lineOption : lineOptions) {
            int startIndex = testScript.length();
            testScript.append(lineOption);
            createTestCase(n + 1, lines, new StringBuilder(testScript), stopFlag, content, testCount);
            testScript.delete(startIndex, testScript.length());
        }

    }

    public static void createScriptV2(String outlinePath, String dataPath) {
        Map<String, List<String>> dataMap = createMap(dataPath);
        System.out.println(dataMap.get("test2").get(0).isBlank());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("test_saucedemo.robot"));
            DocumentBuilder bulider = factory.newDocumentBuilder();
            Document document = bulider.parse(new File(outlinePath));
            String url = document.getElementsByTagName("url").item(0).getTextContent();
            NodeList testcases = document.getElementsByTagName("TestCase");
            StringBuilder content = new StringBuilder();
            content.append("*** Setting ***\nLibrary\tSeleniumLibrary\n\n*** Test Cases ***\n");
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                StringBuilder testScript = new StringBuilder();
                testScript.append("\tOpen Browser\t").append(url).append("\tChrome\n");
//                List<String> variables = new ArrayList<>();
//                List<String> locators = new ArrayList<>();
                if (testcase.getNodeType() != Node.ELEMENT_NODE) continue;
                Element testcaseElement = (Element) testcase;
                NodeList nodes = testcaseElement.getChildNodes();
                List<Element> parentLogicOfActions = new ArrayList<>();
                for (int j = 0; j < nodes.getLength(); j++) {
                    if (nodes.item(j).getNodeType() == Node.ELEMENT_NODE && ((Element) nodes.item(j)).getTagName().equals("LogicExpressionOfActions"))
                        parentLogicOfActions.add((Element) nodes.item(j));
                }
                Map<Integer, List<StringBuilder>> lines = new HashMap<>();
                for (int j = 0; j < parentLogicOfActions.size(); j++) {
                    Element cur = parentLogicOfActions.get(j);
                    String type = cur.getElementsByTagName("type").item(0).getTextContent();
                    if (!type.equals("or") && !type.equals("and")) {
                        Map<String, List<String>> tuples = new HashMap<>();
                        List<String> locators = new ArrayList<>();
                        List<String> placeholders = new ArrayList<>();
                        List<StringBuilder> list = new ArrayList<>();
                        StringBuilder actionString = new StringBuilder();
                        NodeList params = cur.getChildNodes();
                        for (int index = 0; index < params.getLength(); index++) {
                            Node param = params.item(index);
                            if (param.getNodeType() != Node.ELEMENT_NODE) continue;
                            Element paramElement = (Element) param;
                            actionString.append("\t").append(paramElement.getTextContent());
                            if (paramElement.getTagName().equals("locator"))
                                locators.add(paramElement.getTextContent());
                            if (paramElement.getTagName().equals("text"))
                                placeholders.add(paramElement.getTextContent());
                        }
                        actionString.append("\n");
                        for (String locator : locators) {
                            replace(locator, dataMap.get(locator).get(0), actionString);
                        }
                        //Backtracking to create all possible combination
                        fillData(0, actionString, dataMap, placeholders, list, tuples);
                        lines.put(j, list);
                    } else {
                        Expression expr = LogicParser.createExpression(cur);
                        List<List<Element>> elementList = LogicParser.createDNFList(expr);
                        System.out.println(elementList);
                        System.out.println(expr);
                        Map<Integer, List<StringBuilder>> mapChoices = new HashMap<>();
                        List<StringBuilder> listOfChoices = new ArrayList<>();
                        List<List<String>> placeHoldersEachAction = new ArrayList<>();
                        for (List<Element> actionElements : elementList) {
                            //A list for each action after fill data in a Disjunctive normal form
                            List<StringBuilder> listOfEachActions = new ArrayList<>();
                            Map<String, List<String>> tuples = new HashMap<>();
                            List<String> locators = new ArrayList<>();
                            List<String> placeholders = new ArrayList<>();
                            StringBuilder actionString = new StringBuilder();
                            if (actionElements.size() == 1) {
                                Element actionElement = actionElements.get(0);
                                NodeList params = actionElement.getChildNodes();
                                for (int index = 0; index < params.getLength(); index++) {
                                    Node param = params.item(index);
                                    if (param.getNodeType() == Node.ELEMENT_NODE) {
                                        Element paramElement = (Element) param;
                                        actionString.append("\t").append(paramElement.getTextContent());
                                        if (paramElement.getTagName().equals("locator"))
                                            locators.add(paramElement.getTextContent());
                                        if (paramElement.getTagName().equals("text"))
                                            placeholders.add(paramElement.getTextContent());
                                    }
                                }
                                actionString.append("\n");
                            } else {

                                for (Element actionElement : actionElements) {
                                    NodeList params = actionElement.getChildNodes();
                                    for (int index = 0; index < params.getLength(); index++) {
                                        Node param = params.item(index);
                                        if (param.getNodeType() != Node.ELEMENT_NODE) continue;
                                        Element paramElement = (Element) param;
                                        actionString.append("\t").append(paramElement.getTextContent());
                                        if (paramElement.getTagName().equals("locator"))
                                            locators.add(paramElement.getTextContent());
                                        if (paramElement.getTagName().equals("text")) {
                                            placeholders.add(paramElement.getTextContent());
                                        }
                                    }
                                    actionString.append("\n");
                                }
                            }
                            placeHoldersEachAction.add(placeholders);
                            for (String locator : locators) {
                                replace(locator, dataMap.get(locator).get(0), actionString);
                            }
//                            fillData(0, actionString, dataMap, placeholders, listOfEachActions, tuples);
                            //Replace real data into placeholders
                            if (!placeholders.isEmpty()) {
                                for (int index = 0; index < dataMap.get(placeholders.get(0)).size(); index++) {
                                    StringBuilder prev = new StringBuilder(actionString);
                                    for (String placeholder : placeholders) {
                                        replace(placeholder, dataMap.get(placeholder).get(index), actionString);
                                    }
                                    listOfEachActions.add(new StringBuilder(actionString));
                                    actionString = new StringBuilder(prev);
                                }
                            } else {
                                listOfEachActions.add(new StringBuilder(actionString));
                            }
                            mapChoices.put(mapChoices.size(), listOfEachActions);
                        }
                        int[] nums = new int[mapChoices.size()];
                        for (int index = 0; index < nums.length; index++) nums[index] = index;
                        List<List<Integer>> subsets = Subset.subsets(nums);
                        DisjointSet disjointSet = new DisjointSet(placeHoldersEachAction.size());
                        for (List<Integer> subset : subsets) {
                            if (subset.isEmpty()) continue;
                            StringBuilder subsetOfActions = new StringBuilder();
                            Map<String, Integer> mapChoseData = new HashMap<>();
                            for (int index1 = 0; index1 < subset.size() - 1; index1++) {
                                for (int index2 = index1 + 1; index2 < subset.size(); index2++) {
                                    List<String> placeHolders1 = placeHoldersEachAction.get(subset.get(index1));
                                    List<String> placeHolders2 = placeHoldersEachAction.get(subset.get(index2));
                                    Set<String> check = new HashSet<>(placeHolders1);
                                    check.retainAll(placeHolders2);
                                    if (!check.isEmpty()) disjointSet.union(subset.get(index1), subset.get(index2));
                                }
                            }
                            for (int index1 = 0; index1 < subset.size(); index1++) {
                                int index = subset.get(index1);
                                if (placeHoldersEachAction.get(index).isEmpty()) {
                                    subsetOfActions.append(mapChoices.get(index).get(0));
                                    continue;
                                }
                                if (!mapChoseData.containsKey(placeHoldersEachAction.get(index).get(0))) {
                                    Set<String> set = new HashSet<>(placeHoldersEachAction.get(index));
                                    for (int k = index1 + 1; k < subset.size(); k++) {
                                        if (disjointSet.sameSet(index, subset.get(k))) set.addAll(placeHoldersEachAction.get(subset.get(k)));
                                    }
                                    List<String> list = new ArrayList<>();
                                    list.addAll(set);
                                    for (int k = 0; k < dataMap.get(placeHoldersEachAction.get(index).get(0)).size(); k++) {
                                        boolean valid = true;
                                        for (String placeholder : list) {
                                            if (dataMap.get(placeholder).get(k).isEmpty()) {
                                                valid = false;
                                                break;
                                            }
                                        }
                                        if (valid) {
                                            for (String placeholder : list) {
                                                mapChoseData.put(placeholder, k);
                                            }
                                            subsetOfActions.append(mapChoices.get(index).get(k));
                                            break;
                                        }
                                    }
                                } else {
                                    int choseIndex = mapChoseData.get(placeHoldersEachAction.get(index).get(0));
                                    subsetOfActions.append(mapChoices.get(index).get(choseIndex));
                                }
                            }
                            listOfChoices.add(new StringBuilder(subsetOfActions));
                            disjointSet.makeSet();
                        }
                        lines.put(j, listOfChoices);
                    }
                }
                createTestCase(0, lines, testScript, parentLogicOfActions.size(), content, new AtomicInteger(1));
                System.out.println(content);
            }
            bufferedWriter.append(content);
            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

         createDataSheetV2("outline_saucedemo.xml");
        // createMap("C:\\Users\\quyenhoang03\\Desktop\\demo\\data1.csv");
//        createScriptV2("outline_saucedemo.xml", "data_saucedemo.csv");

    }
}