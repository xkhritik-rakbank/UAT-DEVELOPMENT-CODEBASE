/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: STP (V1.0) 
Application				: STP Book Transaction Utility
Module					: STP Autobook  
File Name				: Validation.java
Author 					: Ajay Kumar
Date (DD/MM/YYYY)		: 29/06/2009
Description 			: Contains the basic Validation methods.
---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/

 
package com.newgen.TF_Firco;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Title: STP</p>
 * <p>Description: Contains the basic Validation methods.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Newgen Software Technologies Ltd.</p>
 * @author Ajay Kumar
 * @version 1.0
 */
public class MapXML
{
	private static Logger mLogger;
	static
	{
        mLogger = Logger.getLogger("mLogger");
    }
	public static String getTagValue(String xml,String tag) throws ParserConfigurationException, SAXException, IOException  
	{
		Document doc=getDocument(xml);
		NodeList nodeList = doc.getElementsByTagName(tag);
		
		int length = nodeList.getLength();
		
		if (length > 0) 
		{
			Node node =  nodeList.item(0);
			// System.out.println("Node : " + node);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				NodeList childNodes = node.getChildNodes();
				String value = "";
				int count = childNodes.getLength();
				for (int i = 0; i < count; i++) 
				{
					Node item = childNodes.item(i);
					if (item.getNodeType() == Node.TEXT_NODE) 
					{
						value += item.getNodeValue();
					}
				}
				return value;
			} 
			else if (node.getNodeType() == Node.TEXT_NODE) 
			{
				return node.getNodeValue();
			}
		}
		return "";
	}
	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException  
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		return doc;
	}
	public static NodeList getNodeListFromDocument(Document doc,String identifier)
	{
		NodeList records = doc.getElementsByTagName(identifier);
		return records;
	}
	public static Map getKeyValueMapFromNode(Node record,String [] keys)
	{
		Map map = new HashMap();
		NodeList columnList = record.getChildNodes();
		int columnLength = columnList.getLength();
		//System.out.println("columnLength "+columnLength);
		for (int col = 0, i = 0; col  < columnLength; ++col) 
		{
			Node columnItem = columnList.item(col);
			if (columnItem.getNodeType() == Node.ELEMENT_NODE ) 
			{
				if( columnItem.getTextContent()==null)
					map.put(keys[i++].trim(),"");
				else if( columnItem.getTextContent().equalsIgnoreCase("null"))
					map.put(keys[i++].trim(),"");
				else
					map.put(keys[i++].trim(), columnItem.getTextContent());
			}
		}
		return map;
	}
	
	public static String getFirstNodeValue(Node node,String tagFirst) throws ParserConfigurationException, SAXException, IOException  
	{ 
		String retValue="";
		Element element = (Element) node;
		NodeList firstNameElemntList = element.getElementsByTagName(tagFirst);
		Element firstNameElement = (Element) firstNameElemntList.item(0);
		NodeList firstName = firstNameElement.getChildNodes();
		retValue =((Node)firstName.item(0)).getNodeValue();
		return retValue;
	}
}