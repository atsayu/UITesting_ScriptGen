package com.example;

import au.com.bytecode.opencsv.CSVReader;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.bpodgursky.jbool_expressions.Expression;
import org.example.newSolve;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ScriptGen {
    public static void createDataSheetV2(String outline, String datasheetPath) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
            File newfile = new File(datasheetPath);
            if (newfile.createNewFile()) {
                System.out.println("Created " + datasheetPath + "!");
            } else {
                System.out.println("The file" + datasheetPath + "already exists!");
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(datasheetPath));
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(outline));
            StringBuilder content = new StringBuilder();
            String url = document.getElementsByTagName("url").item(0).getTextContent();
            NodeList testcases = document.getElementsByTagName("TestCase");
            List<Element> testCaseElements = new ArrayList<>();
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                if (testcase.getNodeType() == Node.ELEMENT_NODE)
                    testCaseElements.add((Element) testcase);
            }
            //This list will be passed to the finding locator API
            List<String> locators = new ArrayList<>();
            locators.add(url);
            for (Element testcaseElement: testCaseElements) {
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
                        if (!locators.contains(locator))
                            locators.add(locator);
                        if (expressionActionElement.getElementsByTagName("text").getLength() > 0) {
                            String text = expressionActionElement.getElementsByTagName("text").item(0).getTextContent();
                            if (content.indexOf(text) == -1)
                                content.append(text).append("\n");
                        }
                    } else {
                        List<List<Action>> dnfList = LogicParser.createDNFList(LogicParser.createAction(expressionActionElement));
                        List<List<String>> texts = new ArrayList<>();
                        for (List<Action> actionList : dnfList) {
                            List<String> textList = new ArrayList<>();
                            for (Action action : actionList) {
                                String locator = action.getLocator();
                                if (!locators.contains(locator))
                                    locators.add(locator);
                                if (action.getText() != null)
                                    textList.add(action.getText());
                            }
                            texts.add(new ArrayList<>(textList));
                        }
                        List<List<Integer>> subsets = Subset.subsets(texts.size());
                        for (List<Integer> subset : subsets) {
                            if (subset.isEmpty()) continue;
                            StringBuilder satisfy = new StringBuilder();
                            for (int index : subset) {
                                List<String> textList = texts.get(index);
                                for (String text : textList) {
                                    if (satisfy.indexOf(text) == -1) {
                                        if (satisfy.isEmpty()) satisfy.append(text);
                                        else satisfy.append(" & ").append(text);
                                    }
                                }
                            }
                            if (content.indexOf(satisfy.toString()) == -1)
                                content.append(new StringBuilder(satisfy)).append("\n");
                        }
                    }

                }

            }
            String[] test = new String[locators.size()];
            Vector<String> dataOfLocator = newSolve.getLocator(locators.toArray(test));
            for (String s: dataOfLocator) {
                int separator = s.indexOf(":");
                String locator = s.substring(0, separator);
                String xpath = s.substring(separator + 2);
                content.append(locator).append(",").append(xpath);
            }
            bufferedWriter.append(content);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Map<String, List<String>> createDataMap(String path) {
        Map<String, List<String>> variables = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
            String[] words = null;
            while ((words = csvReader.readNext()) != null) {
                variables.put(words[0], new ArrayList<>());
                for (int i = 1; i < words.length; i++) {
                    variables.get(words[0]).add(words[i]);
                }
            }
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

    public static void createTestCase(int n, Map<Integer, List<StringBuilder>> lines, StringBuilder testScript, StringBuilder content, StringBuilder validations, String testName, AtomicInteger testCount) {
        if (n >= lines.size()) {
            content.append("Test ").append(testName).append(" ").append(testCount).append("\n").append(testScript).append(validations);
            testCount.incrementAndGet();
            return;
        }
        List<StringBuilder> lineOptions = lines.get(n);
        for (StringBuilder lineOption : lineOptions) {
            int startIndex = testScript.length();
            testScript.append(lineOption);
            createTestCase(n + 1, lines, new StringBuilder(testScript), content, validations, testName, testCount);
            testScript.delete(startIndex, testScript.length());
        }

    }

    public static void createScriptV2(String outlinePath, String dataPath, String outputScriptPath) {
        Map<String, List<String>> dataMap = createDataMap(dataPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputScriptPath));
            DocumentBuilder bulider = factory.newDocumentBuilder();
            Document document = bulider.parse(new File(outlinePath));
            String url = document.getElementsByTagName("url").item(0).getTextContent();
            NodeList testcases = document.getElementsByTagName("TestCase");
            StringBuilder content = new StringBuilder();
            content.append("*** Setting ***\nLibrary\tSeleniumLibrary\n\n*** Test Cases ***\n");
            StringBuilder header = new StringBuilder("*** Variables ***\n");
            for (int i = 0; i < testcases.getLength(); i++) {
                Node testcase = testcases.item(i);
                StringBuilder testScript = new StringBuilder();
                testScript.append("\tOpen Browser\t").append(url).append("\tChrome\n");
                testScript.append("\tMaximize Browser Window\n");
                if (testcase.getNodeType() != Node.ELEMENT_NODE) continue;
                Element testcaseElement = (Element) testcase;
                String testName = testcaseElement.getElementsByTagName("Scenario").item(0).getTextContent();
                StringBuilder validations = new StringBuilder();
                NodeList validationNodes = testcaseElement.getElementsByTagName("Validation");
                for (int j = 0; j < validationNodes.getLength(); j++) {
                    Node validation = validationNodes.item(j);
                    if (validation.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element validationElement = (Element) validation;
                    String type = validationElement.getElementsByTagName("type").item(0).getTextContent();
                    switch (type) {
                        case "URLValidation":
                            String correctUrl = validationElement.getElementsByTagName("url").item(0).getTextContent();
                            validations.append("\tLocation Should Be\t").append(correctUrl).append("\n");
                            break;
                        case "PageContainValidation":
                            String text = validationElement.getElementsByTagName("text").item(0).getTextContent();
                            validations.append("\tPage Should Contain\t").append(text).append("\n");
                            break;
                        default:
                            break;
                    }
                }
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
                        List<StringBuilder> list = new ArrayList<>();
                        StringBuilder actionString = new StringBuilder();
                        String realLocator = cur.getElementsByTagName("locator").item(0).getTextContent();
                        String locator = "${" + realLocator + "}";
                        String text = null;
                        if (cur.getElementsByTagName("text").getLength() > 0) {
                            text = cur.getElementsByTagName("text").item(0).getTextContent();
                        }
                        actionString.append("\t").append(type).append("\t").append(locator);
                        if (text != null) {
                            actionString.append("\t").append(text).append("\n");
                        }
                        else {
                            actionString.append("\n");
                        }
                        if (text != null) {
                            for (String s: dataMap.get(text)) {
                                StringBuilder prev = new StringBuilder(actionString);
                                replace(text, s, actionString);
                                list.add(new StringBuilder(actionString));
                                actionString = new StringBuilder(prev);
                            }
                        } else {
                            list.add(actionString);
                        }
                        lines.put(j, list);
                        StringBuilder locatorAndXpath = new StringBuilder().append(locator).append("\t").append(dataMap.get(realLocator).get(0)).append("\n");
                        if (header.indexOf(locatorAndXpath.toString()) == -1)
                            header.append(locatorAndXpath);
                    } else {
                        List<List<Action>> elementList = LogicParser.createDNFList(LogicParser.createAction(cur));
                        List<StringBuilder> listOfChoices = new ArrayList<>();
                        List<List<String>> placeHoldersEachAction = new ArrayList<>();
                        List<List<StringBuilder>> actionStringList = new ArrayList<>();
                        for (List<Action> actions : elementList) {
                            List<String> locators = new ArrayList<>();
                            List<String> placeholders = new ArrayList<>();
                            StringBuilder actionString = new StringBuilder();
                            List<StringBuilder> temp = new ArrayList<>();
                            for (Action action: actions) {
                                actionString.append("\t").append(action.getType());
                                String realLocator = action.getLocator();
                                actionString.append("\t").append("${").append(realLocator).append("}");
                                StringBuilder locatorAndXpath = new StringBuilder().append("${").append(realLocator).append("}").append("\t").append(dataMap.get(realLocator).get(0)).append("\n");
                                if (header.indexOf(locatorAndXpath.toString()) == -1)
                                    header.append(locatorAndXpath);
                                locators.add(action.getLocator());
                                if (action.getText() != null) {
                                    placeholders.add(action.getText());
                                    actionString.append("\t").append(action.getText());
                                }
                                actionString.append("\n");
                                temp.add(actionString);
                                actionString = new StringBuilder();
                            }
                            placeHoldersEachAction.add(placeholders);
                            actionStringList.add(temp);
                        }
                        List<List<Integer>> subsets = Subset.subsets(actionStringList.size());
                        DisjointSet disjointSet = new DisjointSet(placeHoldersEachAction.size());
                        for (List<Integer> subset : subsets) {
                            if (subset.isEmpty()) continue;
                            StringBuilder subsetOfActions = new StringBuilder();
                            Map<String, String> mapChoseData = new HashMap<>();
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
                                    subsetOfActions.append(actionStringList.get(index));
                                    continue;
                                }
                                if (!mapChoseData.containsKey(placeHoldersEachAction.get(index).get(0))) {
                                    List<String> list = new ArrayList<>(placeHoldersEachAction.get(index));
                                    for (int k = index1 + 1; k < subset.size(); k++) {
                                        if (disjointSet.sameSet(index, subset.get(k))) {
                                            for (String placeholder : placeHoldersEachAction.get(subset.get(k))) {
                                                if (!list.contains(placeholder)) list.add(placeholder);
                                            }
                                        }
                                    }
                                    StringBuilder combinePlaceHolder = new StringBuilder(String.join(" & ", list));
                                    int randomIndex = new Random().nextInt(dataMap.get(combinePlaceHolder.toString()).size());
                                    String realDatas = dataMap.get(combinePlaceHolder.toString()).get(randomIndex);
                                    String[] datas = realDatas.split(" & ");
                                    for (int k = 0; k < list.size(); k++) {
                                        mapChoseData.put(list.get(k), datas[k]);
                                    }
                                    List<StringBuilder> actionString = actionStringList.get(index);
                                    for (int k = 0; k < placeHoldersEachAction.get(index).size(); k++) {
                                        StringBuilder temp = new StringBuilder(actionString.get(k));
                                        String placeholder = placeHoldersEachAction.get(index).get(k);
                                        replace(placeholder, mapChoseData.get(placeholder), temp);
                                        if (subsetOfActions.indexOf(temp.toString()) == -1)
                                            subsetOfActions.append(temp);
                                    }
                                } else {
                                    List<StringBuilder> actionString = actionStringList.get(index);
                                    for (int k = 0; k < placeHoldersEachAction.get(index).size(); k++) {
                                        StringBuilder temp = new StringBuilder(actionString.get(k));
                                        String placeholder = placeHoldersEachAction.get(index).get(k);
                                        replace(placeholder, mapChoseData.get(placeholder), temp);
                                        if (subsetOfActions.indexOf(temp.toString()) == -1)
                                            subsetOfActions.append(temp);
                                    }
                                }
                            }
                            listOfChoices.add(new StringBuilder(subsetOfActions));
                            disjointSet.makeSet();
                        }
                        lines.put(j, listOfChoices);
                    }
                }
                createTestCase(0, lines, testScript, content,validations, testName, new AtomicInteger(1));
            }
            content.insert(0, header.append("\n"));
            bufferedWriter.append(content);
            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
//        createDataSheetV2("outline_saucedemo.xml", "data_saucedemo.csv");
        createScriptV2("outline_saucedemo.xml", "data_saucedemo.csv", "test_saucedemo.robot");
//
//        createDataSheetV2("outline_demoqa.xml", "data_demoqa.csv");
//        createScriptV2("outline_demoqa.xml", "data_demoqa.csv", "test_demoqa.robot");
//
//        createDataSheetV2("outline_thinktester.xml", "data_thinktester.csv");
//        createScriptV2("outline_thinktester.xml", "data_thinktester.csv","thinktester.robot");


    }
}