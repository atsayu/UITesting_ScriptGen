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
            Document document = bulider.parse(new File("C:\\Users\\quyenhoang03\\Desktop\\demo\\src\\outline.xml"));
            NodeList tests = document.getElementsByTagName("hehe");
            System.out.println(document.getElementsByTagName("url").item(0).hasAttributes() );
         } catch (Exception e) {
            e.printStackTrace();
         }
    
    }
}
