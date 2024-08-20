package com.newgen.DCC.SystemIntegration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class ResponseParser
  {
 
    private static NGEjbClient ngEjbClientConnection;

    static
      {
        try
          {
            ngEjbClientConnection = NGEjbClient.getSharedInstance();
          }
        catch (NGException e)
          {
            e.printStackTrace();
          }
      }
    public static String getOutputXMLValues(String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String prod,
      String subprod, String cifId, String cust_type)

      {
    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside getOutputXMLValues");
        String outputXMLHead = "";
        //String outputXMLMsg = "";
        //String returnDesc = "";
        String returnCode = "";
        //String response = "";
        String returnType = "";
        String result_str = "";
        //String MsgFormat = "";
        //String CompanyCIF = "Corporate_CIF";
        try
          {

            /*
             * String squery_comp =
             * "select case when COUNT(CompanyCIF)>0 then  ISNULL(CompanyCIF,'') else '' end as CompanyCIF  from NG_RLOS_GR_CompanyDetails where comp_winame ='"
             * +wi_name+"' and applicantCategory='Business' group by CompanyCIF"; String
             * strInputXml_comp = ExecuteQuery_APSelect(squery_comp,cabinetName,sessionId); String
             * strOutputXml_comp = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort,
             * appServerType, strInputXml_comp);
             * 
             * DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Out put XML of company : "+strOutputXml_comp);
             * if(!"".equalsIgnoreCase(strOutputXml_comp)){ String row_count_str_comp =
             * strOutputXml_comp.substring(strOutputXml_comp.indexOf("<TotalRetrieved>")+16,
             * strOutputXml_comp.indexOf("</TotalRetrieved>")); int result_count_comp =
             * Integer.parseInt(row_count_str_comp); if (result_count_comp>0){
             * CompanyCIF=strOutputXml_comp.substring(strOutputXml_comp.indexOf("<CompanyCIF>")+12,
             * strOutputXml_comp.indexOf("</CompanyCIF>")); } else{ CompanyCIF=""; } }
             */
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in custexpose_output_PL company : " + e.getMessage());
            e.printStackTrace();

          }
        try
          {

            if (parseXml.indexOf("<EE_EAI_HEADER>") > -1)
              {
                outputXMLHead = parseXml.substring(parseXml.indexOf("<EE_EAI_HEADER>"), parseXml.indexOf("</EE_EAI_HEADER>") + 16);
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("RLOSCommon valueSetCustomer"+ outputXMLHead);
              }
            if (outputXMLHead.indexOf("<MsgFormat>") > -1)
              {
                //response = outputXMLHead.substring(outputXMLHead.indexOf("<MsgFormat>") + 11, outputXMLHead.indexOf("</MsgFormat>"));
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$response "+response);
              }
            if (outputXMLHead.indexOf("<ReturnDesc>") > -1)
              {
                //returnDesc = outputXMLHead.substring(outputXMLHead.indexOf("<ReturnDesc>") + 12, outputXMLHead.indexOf("</ReturnDesc>"));
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$returnDesc "+returnDesc);
              }
            if (outputXMLHead.indexOf("<ReturnCode>") > -1)
              {
                returnCode = outputXMLHead.substring(outputXMLHead.indexOf("<ReturnCode>") + 12, outputXMLHead.indexOf("</ReturnCode>"));
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$returnCode "+returnCode);
              }
            //Deepak changes done for Commented for PCSP-526
            if (parseXml.indexOf("<RequestType>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<RequestType>") + 13, parseXml.indexOf("</RequestType>"));

                if ("0000".equalsIgnoreCase(returnCode) || ("ExternalExposure".equalsIgnoreCase(returnType) && ("B003".equalsIgnoreCase(returnCode) || "B005".equalsIgnoreCase(returnCode))))
                  {
                    if ("InternalExposure".equalsIgnoreCase(returnType))
                      {
                        result_str = parseInternalExposure(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                    else if ("ExternalExposure".equalsIgnoreCase(returnType))
                      {
                         result_str = parseExternalExposure(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                    else if ("CollectionsSummary".equalsIgnoreCase(returnType))
                      {
                        result_str = parseCollectionSummary(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                  }
                
              }

            //added
            if (parseXml.indexOf("<MsgFormat>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<MsgFormat>") + 11, parseXml.indexOf("</MsgFormat>"));
              }
            //ended


            if (parseXml.indexOf("<OperationType>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<OperationType>") + 15, parseXml.indexOf("</OperationType>"));
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$returnType "+returnType);
                if (returnType.equalsIgnoreCase("TRANSUM"))
                  {
                    result_str = parseTRANSUM(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("AVGBALDET"))
                  {
                    result_str = parseAVGBALDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("RETURNDET"))
                  {
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Hi");
                    result_str = parseRETURNDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("LIENDET"))
                  {
                    result_str = parseLIENDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("SIDET"))
                  {
                    result_str = parseSIDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("SALDET"))
                  {
                    result_str = parseSALDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }

              }
            returnType = parseXml.substring(parseXml.indexOf("<MsgFormat>") + 11, parseXml.indexOf("</MsgFormat>"));
            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$returnType result_strresult_strresult_str"+returnType);
            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$MsgFormat "+returnType);
            if (returnType.equalsIgnoreCase("FINANCIAL_SUMMARY") && (result_str.equalsIgnoreCase("")))
              {
                result_str = returnCode;
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("$$result_str result_strresult_strresult_str"+result_str);
              }
            //ended

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in getOutputXMLValues: " + e.getMessage());
            e.printStackTrace();
            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in getOutputXMLValues method:  "+e.getMessage());
            result_str = "Failure";
          }
        return (result_str);
      }

    public static String parseInternalExposure(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
      {
        String flag1 = "";
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        //String result = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parseInternalExposure");
        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName("CustomerExposureResponse");


            for (int i = 0; i < nList_loan.getLength(); i++)
              {
                Node node = nList_loan.item(i);
                Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                DOMImplementationLS abc = (DOMImplementationLS) newXmlDocument.getImplementation();
                LSSerializer lsSerializer = abc.createLSSerializer();

                Element root = newXmlDocument.createElement("root");
                newXmlDocument.appendChild(root);
                root.appendChild(newXmlDocument.importNode(node, true));
                String n_parseXml = lsSerializer.writeToString(newXmlDocument);
                n_parseXml = n_parseXml.substring(n_parseXml.indexOf("<root>") + 6, n_parseXml.indexOf("</root>"));
                cifId =
                  (n_parseXml.contains("<CustIdValue>")) ? n_parseXml.substring(n_parseXml.indexOf("<CustIdValue>") + "</CustIdValue>".length() - 1, n_parseXml.indexOf("</CustIdValue>")) : cifId;

               
                tagName = "LoanDetails";
                subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                sTableName = "USR_0_iRBL_InternalExpo_LoanDetails";
                subtag_single = "";
                flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
                   cust_type, subtag_single);

                if (flag1.equalsIgnoreCase("true"))
                  {
                    tagName = "CardDetails";
                    subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                    sTableName = "USR_0_iRBL_InternalExpo_CardDetails";
                    subtag_single = "";
                    flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId, cust_type, subtag_single);

                    if (flag1.equalsIgnoreCase("true"))
                      {
                        tagName = "InvestmentDetails";
                        subTagName = "AmountDtls";
                        sTableName = "USR_0_iRBL_InternalExpo_InvestmentDetails";
                        subtag_single = "";
                        flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                          cifId, cust_type, subtag_single);

                        if (flag1.equalsIgnoreCase("true"))
                          {
                            tagName = "AcctDetails";
                            subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                            sTableName = "USR_0_iRBL_InternalExpo_AcctDetails";
                            subtag_single = "ODDetails";
                            flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                              cifId, cust_type, subtag_single);
                            if (flag1.equalsIgnoreCase("true"))
                              {
                                tagName = "Derived";
                                subTagName = "";
                                sTableName = "USR_0_iRBL_InternalExpo_Derived";
                                subtag_single = "";
                                flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
                                  subprod, cifId, cust_type, subtag_single);
                                if (flag1.equalsIgnoreCase("true"))
                                  {
                                    tagName = "RecordDestribution";
                                    subTagName = "";
                                    sTableName = "USR_0_iRBL_InternalExpo_RecordDestribution";
                                    subtag_single = "";
                                    flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
                                      subprod, cifId, cust_type, subtag_single);
                                    //Deepak 22 july 2019 new condition added to save custinfo
                                    if (flag1.equalsIgnoreCase("true"))
                                      {
                                        tagName = "CustInfo";
                                        subTagName = "";
                                        sTableName = "USR_0_iRBL_InternalExpo_CustInfo";
                                        subtag_single = "";
                                        flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName,
                                          prod, subprod, cifId, cust_type, subtag_single);
                                      }
                                    else
                                      {
                                        flag1 = "false";
                                      }
                                  }
                                else
                                  {
                                    flag1 = "false";
                                  }
                              }
                            else
                              {
                                flag1 = "false";
                              }
                          }
                        else
                          {
                            flag1 = "false";
                          }
                      }
                    else
                      {
                        flag1 = "false";
                      }
                  }
                else
                  {
                    flag1 = "false";
                  }
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return flag1;
    }


    public static String parseExternalExposure(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
    {
    	String flag1 = "";
    	try
    	{
    		String sTableName = "";
    		String subtag_single = "AdditionalAccountInfo";
    		String tagName = "ChequeDetails";
    		String subTagName = "";
    		sTableName = "ng_dcc_cust_extexpo_ChequeDetails";
    		flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId, cust_type, subtag_single);
    		//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted"+flag1);

    		if (flag1.equalsIgnoreCase("true"))
    		{
    			tagName="LoanDetails"; 
    			subTagName = "KeyDt,AmountDtls";
    			sTableName = "ng_dcc_cust_extexpo_LoanDetails";
    			flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,cust_type, subtag_single);
    			//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted1"+flag1);

    			if (flag1.equalsIgnoreCase("true"))
    			{
    				tagName="CardDetails"; 
    				subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
    				sTableName = "ng_dcc_cust_extexpo_CardDetails";
    				flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,cust_type, subtag_single);

    				//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted2"+flag1);
    				if (flag1.equalsIgnoreCase("true"))
    				{
    					tagName = "Derived";
    					subTagName = "";
    					sTableName = "ng_dcc_cust_extexpo_Derived";
    					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Hi");
    					flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
    							cust_type, subtag_single);
    					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
    					if (flag1.equalsIgnoreCase("true"))
    					{
    						tagName = "RecordDestribution";
    						subTagName = "";
    						sTableName = "ng_dcc_cust_extexpo_RecordDestribution";
    						flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
    								cifId, cust_type, subtag_single);
    						if (flag1.equalsIgnoreCase("true"))
    						{
    							tagName = "AcctDetails";
    							subTagName = "KeyDt,AmountDtls";
    							sTableName = "ng_dcc_cust_extexpo_AccountDetails";
    							flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
    									cifId, cust_type, subtag_single);
    							if (flag1.equalsIgnoreCase("true"))
    							{
    								tagName = "ServicesDetails";
    								subTagName = "KeyDt,AmountDtls";
    								sTableName = "ng_dcc_cust_extexpo_ServicesDetails";
    								flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
    										subprod, cifId, cust_type, subtag_single);

    								// Added By Pooja
    								if(flag1.equalsIgnoreCase("true")){
    									tagName="CaseDetails";
    									subTagName = "";
    									sTableName="ng_dcc_cust_extexpo_CaseDetails";
    									flag1 = commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,subTagName,prod,subprod,cifId,cust_type,subtag_single);

    									//Kamran23012023
    									if (flag1.equalsIgnoreCase("true"))
    									{
    										tagName="Ratios"; 
    										subTagName = "";
    										sTableName = "ng_dcc_cust_extexpo_ExpToSalRatio";
    										subtag_single = "ExpenseToSalaryRatio";
    										DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("ExpenseToSalaryRatio");
    										flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
    												cust_type, subtag_single);


    										if (flag1.equalsIgnoreCase("true"))
    										{
    											tagName="SalaryCreditDetails"; 
    											subTagName ="";
    											sTableName = "ng_dcc_cust_extexpo_SalCreditDtls";
    											//	subtag_single = "CBAccountNo,DPAccountNo,ProviderNo,AccountType,Phase,IBAN,StartDate,DateOfLastUpdate";
    											DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SalaryCreditDetails");
    											flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
    													cust_type, subtag_single);
    										}
    										else
    										{
    											flag1 = "false";
    										}
    									}
    									else
    									{
    										flag1 = "false";
    									}
    								}
    								else
    								{
    									flag1 = "false";
    								}
    							}
    							else
    							{
    								flag1 = "false";
    								//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
    							}
    						}
    						else
    						{
    							flag1 = "false";
    							//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse"+flag1);
    						}


    					}
    					else
    					{
    						flag1 = "false";
    						//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
    					}

    				}
    				else
    				{
    					flag1 = "false";
    					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
    				}

    			}
    			else
    			{
    				flag1 = "false";
    				//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
    			}

    		}
    		else
    		{
    			flag1 = "false";
    			//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
    		}

    	}

    	catch (Exception e)
    	{
    		DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
    		e.printStackTrace();
    		flag1 = "false";
    	}

    	//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted final value"+flag1);
    	return flag1;
    }

    public static String parseCollectionSummary(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
      {
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        //String result = "";
        String flag1 = "";
        try
          {

            //Deepak code commented method changed with new subtag_single param 23jan2018
            String subtag_single = "";

            tagName = "LoanDetails";
            subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
            sTableName = "USR_0_iRBL_InternalExpo_LoanDetails";
            flag1 = commonParseProduct_collection(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
              subtag_single, cust_type);

            if (flag1.equalsIgnoreCase("true"))
              {
                tagName = "CardDetails";
                subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                sTableName = "USR_0_iRBL_InternalExpo_CardDetails";
                flag1 = commonParseProduct_collection(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                  cifId, subtag_single, cust_type);
                if (flag1.equalsIgnoreCase("true"))
                  {
                    tagName = "Derived";
                    subTagName = "";
                    sTableName = "USR_0_iRBL_InternalExpo_Derived";
                    flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
                      cust_type, subtag_single);

                  }
                else
                  {
                    flag1 = "false";
                  }
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }

        return flag1;
      }

    public static String parseCardInstallmentsDetails(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
       String prod, String subprod, String cifId)
      {

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        //String result = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "TransactionDetailsRec";
            subTagName = "";
            sTableName = "USR_0_iRBL_InternalExpo_CardInstallmentDetails";
            flag1 = commonParseFinance_CardInstallment(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName,
             subtag_single);

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }

        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: CardInstallmentDetailsResponse: "+flag1);


        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: CardInstallmentDetailsResponse final value: "+flag1);
        return flag1;
      }


    public static String parseTRANSUM(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "TxnSummaryDtls";
            subTagName = "";
            sTableName = "USR_0_iRBL_FinancialSummary_TxnSummary";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseAVGBALDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: parseAVGBALDET: "+wrapperIP);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperPort jsp: parseAVGBALDET: "+wrapperPort);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sessionId jsp: parseAVGBALDET: "+sessionId);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cabinetName jsp: parseAVGBALDET: "+cabinetName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wi_name jsp: parseAVGBALDET: "+wi_name);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("appServerType jsp: parseAVGBALDET: "+appServerType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("parseXml jsp: parseAVGBALDET: "+parseXml);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: parseAVGBALDET: "+returnType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseAVGBALDET: "+cifId);

        String flag1 = "";
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";

        try
          {
            tagName = "FinancialSummaryRes";
            subTagName = "AvgBalanceDtls";
            sTableName = "USR_0_iRBL_FinancialSummary_AvgBalanceDtls";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseRETURNDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: parseRETURNDET: "+wrapperIP);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperPort jsp: parseRETURNDET: "+wrapperPort);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sessionId jsp: parseRETURNDET: "+sessionId);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cabinetName jsp: parseRETURNDET: "+cabinetName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wi_name jsp: parseRETURNDET: "+wi_name);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("appServerType jsp: parseRETURNDET: "+appServerType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("parseXml jsp: parseRETURNDET: "+parseXml);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: parseRETURNDET: "+returnType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseRETURNDET: "+cifId);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "ReturnsDtls";
            subTagName = "";
            sTableName = "USR_0_iRBL_FinancialSummary_ReturnsDtls";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;

      }

    public static String parseLIENDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: parseLIENDET: "+wrapperIP);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperPort jsp: parseLIENDET: "+wrapperPort);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sessionId jsp: parseLIENDET: "+sessionId);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cabinetName jsp: parseLIENDET: "+cabinetName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wi_name jsp: parseLIENDET: "+wi_name);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("appServerType jsp: parseLIENDET: "+appServerType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("parseXml jsp: parseLIENDET: "+parseXml);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: parseLIENDET: "+returnType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseLIENDET: "+cifId);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        tagName = "LienDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_LienDetails";
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseSIDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String cifId)
      {
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside parseSIDET: ");


        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";

        tagName = "SIDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_SiDtls";
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;

      }

    public static String parseSALDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String cifId)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperIP jsp: parseSALDET: "+wrapperIP);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wrapperPort jsp: parseSALDET: "+wrapperPort);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sessionId jsp: parseSALDET: "+sessionId);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cabinetName jsp: parseSALDET: "+cabinetName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("wi_name jsp: parseSALDET: "+wi_name);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("appServerType jsp: parseSALDET: "+appServerType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("parseXml jsp: parseSALDET: "+parseXml);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: parseSALDET: "+returnType);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("cifId jsp: parseSALDET: "+cifId);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        //String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        tagName = "SalDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_SalTxnDetails";

        if (parseXml.indexOf("<AcctId>") > -1)
          {
            String acc_no = parseXml.substring(parseXml.indexOf("<AcctId>") + "</AcctId>".length() - 1, parseXml.indexOf("</AcctId>"));
            String sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = '" + acc_no + "'";
            String strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strInputXml delete returndtls " + strInputXml);
            try
              {
                //String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                String strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml delete SalDetails: " + strOutputXml);
              }
            catch (NGException e)
              {
                e.printStackTrace();
              }
            catch (Exception ex)
              {
                ex.printStackTrace();
              }


          }
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("return flag1 jsp: parseSALDET: "+flag1);
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String commonParseProduct(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String prod, String subprod, String cifId, String cust_type, String subtag_single)
      {
        String retVal = "";

        try
          {
            if (!parseXml.contains(tagName))
              {
                return "true";
              }
            else
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside commonParseProduct for: "+sTableName);
                String [] valueArr= null;
    			String strInputXml="";
    			String strOutputXml="";
    			String columnName = "";
    			String columnValues = "";
    			String tagNameU = "";
    			String subTagNameU = "";
    			String subTagNameU_2 = "";
    			String mainCode = "";
    			String sWhere = "";
    			String row_updated = "";
    			String ReportUrl = "";
    			String FullNm = "";
    			String TotalOutstanding = "";
    			String TotalOverdue = "";
    			String NoOfContracts = "";
    			//String ECRN = "";
    			//String BorrowingCustomer = "";
    			String 	sQry="";
    			String selectdata="";
    			String companyUpdateQuery="";
    			String companiestobeUpdated = "";
    			String referenceNo = "";
    			String scoreInfo = "";
    			String Aecb_Score = "";
    			String range = "";
    			String EnquiryDate = "";

                boolean stopIndividualToInsert = false;
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("tagName jsp: commonParse: "+tagName);
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("subTagName jsp: commonParse: "+subTagName);
                //Parsing AECB score, range and Reference No. for 2.1 start, Added by Shivang

                referenceNo = (parseXml.contains("<ReferenceNumber>")) ? parseXml.substring(parseXml.indexOf("<ReferenceNumber>") + "</ReferenceNumber>".length() - 1, parseXml.indexOf("</ReferenceNumber>")) : "";
                if (parseXml.contains("<ScoreInfo>"))
                  {
                    scoreInfo = parseXml.substring(parseXml.indexOf("<ScoreInfo>") + "</ScoreInfo>".length() - 1, parseXml.indexOf("</ScoreInfo>"));
                    Aecb_Score = (scoreInfo.contains("<Value>")) ? scoreInfo.substring(scoreInfo.indexOf("<Value>") + "</Value>".length() - 1, scoreInfo.indexOf("</Value>")) : "";
                    range = (scoreInfo.contains("<Range>")) ? scoreInfo.substring(scoreInfo.indexOf("<Range>") + "</Range>".length() - 1, scoreInfo.indexOf("</Range>")) : "";
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("parsexml jsp: commonParse: AECB Score: " + Aecb_Score + " Range: " + range);
                  }

                //Parsing AECB score, range and Reference No. for 2.1 end, Added by Shivang
                //Deepak 23 Dec changes done to save updated Rerport URL in DB
                ReportUrl = (parseXml.contains("<ReportUrl>")) ? parseXml.substring(parseXml.indexOf("<ReportUrl>") + "</ReportUrl>".length() - 1, parseXml.indexOf("</ReportUrl>")) : "";
                //cifId=(parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>")+"</CustIdValue>".length()-1,parseXml.indexOf("</CustIdValue>")):"";
                FullNm = (parseXml.contains("<FullNm>")) ? parseXml.substring(parseXml.indexOf("<FullNm>") + "</FullNm>".length() - 1, parseXml.indexOf("</FullNm>")) : "";
                TotalOutstanding = (parseXml.contains("<TotalOutstanding>"))
                  ? parseXml.substring(parseXml.indexOf("<TotalOutstanding>") + "</TotalOutstanding>".length() - 1, parseXml.indexOf("</TotalOutstanding>")) : "";
                TotalOverdue =
                  (parseXml.contains("<TotalOverdue>")) ? parseXml.substring(parseXml.indexOf("<TotalOverdue>") + "</TotalOverdue>".length() - 1, parseXml.indexOf("</TotalOverdue>")) : "";
                NoOfContracts =
                  (parseXml.contains("<NoOfContracts>")) ? parseXml.substring(parseXml.indexOf("<NoOfContracts>") + "</NoOfContracts>".length() - 1, parseXml.indexOf("</NoOfContracts>")) : "";
                //ECRN = (parseXml.contains("<ECRN>")) ? parseXml.substring(parseXml.indexOf("<ECRN>") + "</ECRN>".length() - 1, parseXml.indexOf("</ECRN>")) : "";
                //BorrowingCustomer = (parseXml.contains("<BorrowingCustomer>")) ? parseXml.substring(parseXml.indexOf("<BorrowingCustomer>") + "</BorrowingCustomer>".length() - 1, parseXml.indexOf("</BorrowingCustomer>")) : "";
                EnquiryDate = (parseXml.contains("<EnquiryDate>")) ? parseXml.substring(parseXml.indexOf("<EnquiryDate>")+"</EnquiryDate>".length()-1,parseXml.indexOf("</EnquiryDate>")):"";

                Map<String, String> tagValuesMap = getTagDataParent_deep_new(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                // String colValue="";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values" + entry.getValue());

                    //columnValues = valueArr[1].spilt(",");
                    // columnValues=columnValues+",'"+getCellData(SheetName1, rCnt, cCnt)+"'";
                    //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
                    columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                    columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";



                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnName commonParse" + columnName);
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnValues commonParse" + columnValues);
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                      {
                        columnName = valueArr[0] + ",Liability_type,Request_Type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + cifId + "','" + wi_name + "'";
                        sWhere = "CardEmbossNum = '" + entry.getKey() + "' AND wi_name='" + wi_name + "' And Liability_type ='" + cust_type + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Individual_CIF' ";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "sQry sQry" + sQry);
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Card", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                      {
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,CardType,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        columnName = columnName.replace("OutStandingAmt", "TotalOutStandingAmt");
                        sWhere = "AgreementId = '" + entry.getKey() + "' AND wi_name='" + wi_name + "' And Liability_type ='" + cust_type + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And  AgreementId = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "sQry  loan sQry" + sQry);
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Loan", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_ChequeDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "Wi_Name='" + wi_name + "' AND Number = '" + entry.getKey() + "'";
                      }
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_LoanDetails"))
                      {
                        String History = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<History>", "</History>");
                        History = History.replace("\n", "").replace("\r", "");
                        String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<Utilizations24Months>", "</Utilizations24Months>");
                        Utilization = Utilization.replace("\n", "").replace("\r", "");
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside parseHistoryUtilization" + History);
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside parseHistoryUtilization" + Utilization);
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,CardType,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("LoanType".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);

                              }
                            if ("History".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + History + "'");

                              }
                            if ("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], "'" + Utilization + "'");

                              }
                          }
                        columnName = columnName.replace("OutStanding Balance", "OutStanding_Balance");
                        columnName = columnName.replace("LastUpdateDate", "datelastupdated");
                        columnName = columnName.replace("Total Amount", "Total_Amount");
                        columnName = columnName.replace("Payments Amount", "Payments_Amount");
                        columnName = columnName.replace("Overdue Amount", "Overdue_Amount");
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside parseHistoryUtilization" + columnName);
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside parseHistoryUtilization" + columnValues);
                        //sWhere="Wi_Name='"+parentWiName+"' AND AgreementId = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
                        sWhere = "Wi_Name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "'";
           
                      }
                    
                    //Kamran 23012023
                   
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_ExpToSalRatio"))
                    {
                    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside ng_dcc_cust_extexpo_ExpToSalRatio table condition " );
                    	String ExpToSalRatio = (parseXml.contains("<Ratios>")) ? parseXml.substring(parseXml.indexOf("<ExpenseToSalaryRatio>") + "</ExpenseToSalaryRatio>".length() - 1, parseXml.indexOf("</ExpenseToSalaryRatio>")) : "";
                    	columnName = "Wi_Name,CifID,ExpenseToSalaryRatio";
                        columnValues = "'" + wi_name + "','" + cifId + "','" + ExpToSalRatio + "'";
                        sWhere="Wi_Name='"+wi_name+"'";
                    }
                    
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_SalCreditDtls"))
                    {
                    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside ng_dcc_cust_extexpo_SalCreditDtls table condition " );
                    	String SalCreditHistory = parseSalaryCreditHistory(parseXml,entry.getKey(), "SalaryCreditDetails", "<SalaryCreditHistory>", "</SalaryCreditHistory>");
                        SalCreditHistory = SalCreditHistory.replace("\n", "").replace("\r", "");
                        Map<String, String> tagValuesMap_salhistory = getTagDataParent_deep_new("<SalaryCreditDetails>"+SalCreditHistory+"</SalaryCreditDetails>", "SalaryCreditHistory", subTagName, subtag_single);
                        String DPAccountNo="";
                        DPAccountNo= get_tagValue(columnName,columnValues,"DPAccountNo");
                        Save_SalaryCreditHistory(tagValuesMap_salhistory, wi_name,entry.getKey(),DPAccountNo, "ng_dcc_cust_extexpo_SalCreditHis", wrapperIP, wrapperPort, sessionId, cabinetName);
                        columnName = valueArr[0] + ",CifId,Wi_Name";
                        columnValues = valueArr[1]  + ",'"+ cifId + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                        	if ("SalaryCreditHistory".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                        		columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + SalCreditHistory + "'");
                              }
                          }
                        sWhere = "Wi_Name='" + wi_name +"' AND CBAccountNo = '" + entry.getKey() +"'";
                        
                    }
                                        
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_CardDetails"))
                      {
                    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside ng_dcc_cust_extexpo_CardDetails table condition deep" );
                        String History = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<History>", "</History>");
                        History = History.replace("\n", "").replace("\r", "");
                        String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<Utilizations24Months>", "</Utilizations24Months>");
                        Utilization = Utilization.replace("\n", "").replace("\r", "");
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,sub_product_type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("CardType".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);

                              }
                            if ("History".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + History + "'");

                              }
                            if ("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + Utilization + "'");

                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "'";
                        //sWhere="Wi_Name='"+parentWiName+"' AND CardEmbossNum = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
                      }
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_Derived"))
                      {
                        //Deepak 23 Dec changes done to save updated Report URL in DB.
                    	columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,FullNm,TotalOutstanding,TotalOverdue,NoOfContracts,ReportURL,ReferenceNo,AECB_Score,Range,EnquiryDate";
						columnValues = valueArr[1]+",'"+wi_name+"','"+returnType+"','"+cifId+"','"+FullNm+"','"+TotalOutstanding+"','"+TotalOverdue+"','"+NoOfContracts+"','"+ReportUrl+"','"+referenceNo+"','"+Aecb_Score+"','"+range+"','"+EnquiryDate+"'";
						sWhere="Wi_Name='"+wi_name+"' AND Request_Type = '"+returnType+"' and CifId='"+cifId+"'";
                      }
                    //Changes Done to save data in NG_RLOS_CUSTEXPOSE_RecordDestribution table on 14th sept by Aman
                    //Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_RecordDestribution"))
                      {
                        columnName = valueArr[0] + ",Request_Type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "Wi_Name='" + wi_name + "' AND ContractType = '" + entry.getKey() + "' AND CifId='" + cifId + "'";
                      }
                    //Changes Done to save data in NG_RLOS_CUSTEXPOSE_RecordDestribution table on 14th sept by Aman
                    //Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_AccountDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("AcctType".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
                                break;
                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND AcctId = '" + entry.getKey() + "'";//Cif_id removed
                      }
                    //Deepak changes done for Service details
                    else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_ServicesDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name  + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");

                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("ServiceName".equalsIgnoreCase(columnName_arr[arrlen].trim()))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
                                break;
                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND ServiceID = '" + entry.getKey() + "'";
                      }
                    //below changes Done to save AccountType in ng_RLOS_CUSTEXPOSE_AcctDetails table on 29th Dec by Disha
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_AcctDetails")) //TODO 1626
                      {
                        String CreditGrade =
                          (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>") + "</CreditGrade>".length() - 1, parseXml.indexOf("</CreditGrade>")) : "";
                        //PCASP-2833 
                        String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>") + "</IsDirect>".length() - 1, parseXml.indexOf("</IsDirect>")) : "";
                        columnName = valueArr[0] + ",isDirect,Request_Type,CifId,CreditGrade,Account_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + isDirect + "','" + returnType + "','" + cifId + "','" + CreditGrade + "','" + cust_type + "','" +  wi_name + "'";
                        sWhere = "Request_Type='" + returnType + "' AND AcctId = '" + entry.getKey() + "' AND wi_name='" + wi_name + "'";// AND Account_Type = '" + cust_type + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        String LimitSactionDate = "";
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("LimitSactionDate".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside LimitSactionDate tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside LimitSactionDate value" + columnValues_arr[arrlen]);
                                LimitSactionDate = columnValues_arr[arrlen];
                              }
                            if ("MonthsOnBook".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside MonthsOnBook tag name" + columnName_arr[arrlen]);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside MonthsOnBook value" + columnValues_arr[arrlen]);
                                if (!LimitSactionDate.equals(""))
                                  {
                                    String MOB = get_Mob_forOD(LimitSactionDate);
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside MonthsOnBook value" + MOB);
                                    if (!MOB.equalsIgnoreCase("Invalid"))
                                      {
                                        columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + MOB + "'");
                                      }
                                  }

                              }
                          }
                        //change by saurabh on 24th Feb for skipping employer accounts to save.
                        sQry = "Select count(*) as selectdata from NG_RLOS_ALOC_OFFLINE_DATA where CIF_ID ='Nikhil123'";
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Account", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "sQry  loan sQry" + sQry);    
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_InvestmentDetails"))
                      {

                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name  + "'";
                        sWhere = "Request_Type='" + returnType + "' AND wi_name='" + wi_name + "' and InvestmentID='" + entry.getKey() + "'";

                      }
                    //above changes Done to save AccountType in ng_RLOS_CUSTEXPOSE_AcctDetails table on 29th Dec by Disha
                    //Deepak 22 july 2019 new condition added to save custinfo
                  //Deepak 22 july 2019 new condition added to save custinfo
  				  else if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_CustInfo")){
  				  String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
  					  columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,isDirect";
  						columnValues = valueArr[1]+",'"+wi_name+"','"+returnType+"','"+cifId+"','"+isDirect+"'";
  					   sWhere="Wi_Name='"+wi_name+"' AND Request_Type = '"+returnType+"' AND CifId = '"+cifId+"'";	  
  					  }
  				  //above changes Done to save AccountType in ng_RLOS_CUSTEXPOSE_AcctDetails table on 29th Dec by Disha
  				  //Added by Pooja 
					else if (sTableName.equalsIgnoreCase("ng_dcc_cust_extexpo_CaseDetails")) {

						columnName = valueArr[0] + ",Wi_Name,Request_Type,CifId";
						columnValues = valueArr[1] + ",'" + wi_name + "','" + returnType + "','" + cifId + "'";
						sWhere = "Wi_Name='" + wi_name + "' AND CifId = '" + cifId + "'";
					}
  					//ended here

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml update for "+sTableName+" table: " + strInputXml);
                    try
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Hi");
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);;

                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strOutputXml update for "+sTableName+" table: "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception update for "+sTableName+" table: " + e.getMessage());
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception update for "+sTableName+" table: " + ex.getMessage());
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("maincode update for "+sTableName+" table:  --> "+mainCode);
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("row updated update for "+sTableName+" table: --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {   
                    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sQry sQry sQry");
                        if (!sQry.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(sQry, cabinetName, sessionId);
                            try
                              {
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                              }
                            catch (NGException e)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception select for "+sTableName+" table sQry sQry sQry: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception select for "+sTableName+" table sQry sQry sQry: " + ex.getMessage());
                                ex.printStackTrace();
                              }
                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("maincode select for "+sTableName+" table sQry sQry sQry --> "+mainCode);
                            selectdata = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("selectdata select for "+sTableName+" table sQry sQry sQry--> "+selectdata);
                          }
                        if (!companyUpdateQuery.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(companyUpdateQuery, cabinetName, sessionId);
                            try
                              {
                            	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("companyUpdateQuery select for "+sTableName+" table: "+strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug(" companyUpdateQuery select for "+sTableName+" table: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companyUpdateQuery select for "+sTableName+" table: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companyUpdateQuery select for "+sTableName+" table: " + ex.getMessage());
                                ex.printStackTrace();
                              }

                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("maincode companyUpdateQuery select for "+sTableName+" table --> "+mainCode);

                            companiestobeUpdated = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("selectdata companyUpdateQuery select for "+sTableName+" table--> "+companiestobeUpdated);

                            if (Integer.parseInt(companiestobeUpdated) > 0)
                              {
                                sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml companiestobeUpdated update for "+sTableName+" table: " + strInputXml);
                                try
                                  {
                                    
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strOutputXml companiestobeUpdated update for "+sTableName+" table: "+strOutputXml);
                                  }
                                catch (NGException e)
                                  {
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companiestobeUpdated update for "+sTableName+" table: " + e.getMessage());
                                    e.printStackTrace();
                                  }
                                catch (Exception ex)
                                  {
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companiestobeUpdated update for "+sTableName+" table: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }

                                tagNameU = "APUpdate_Output";
                                subTagNameU = "MainCode";
                                subTagNameU_2 = "Output";
                                mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                                //row_updated = getTagValue(strOutputXml,tagNameU,subTagNameU_2);
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
                                stopIndividualToInsert = true;
                              }
                          }

                        if (sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert))
                          {
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for cif --> "+cifId);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for table --> "+sTableName);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for cust_type --> "+cust_type);
                            strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml final insert for "+sTableName+" table:" + strInputXml);
                            try
                              {
                                
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strOutputXml final insert for "+sTableName+" table: "+strOutputXml);
                                mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("mainCode"+mainCode);
                                if (!mainCode.equalsIgnoreCase("0"))
                                  {
                                    retVal = "false";
                                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:false "+retVal);
                                  }
                                else
                                  {
                                    retVal = "true";
                                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:true "+retVal);
                                  }
                              }
                            catch (NGException e)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + ex.getMessage());
                                ex.printStackTrace();
                              }
                          }
                        else
                          {
                            retVal = "true";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                      }

                  }
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("return for "+sTableName+" table:finalValue: "+retVal);
                return retVal;
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        return retVal;
      }


    private static String get_tagValue(String columnName, String columnValues, String tag_name) {
		// TODO Auto-generated method stub
    	try{
    		if(columnName.contains(tag_name)){
    			String[] colName = columnName.split(",");
    			for(int i=0; i<=colName.length;i++){
    				System.out.println(colName[i]);
    				if(tag_name.equalsIgnoreCase(colName[i].trim())){
    					String[] colval = columnValues.split(",");
    					return colval[i].replaceAll("'", "");
    				}   				
    			}    			
    		}
    		else{
    			return "";
    		}
    	}
    	catch(Exception e){
    		
    	}
		return null;
	}

	public static String commonParseProduct_collection(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String prod, String subprod, String cifId, String subtag_single, String cust_type)
      {
        String retVal = "";
        try
          {
            if (!parseXml.contains(tagName))
              {
                return "true";
              }
            else
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside commonParseProduct_collection ");

                String[] valueArr = null;
                String strInputXml = "";
                String strOutputXml = "";
                String columnName = "";
                String columnValues = "";
                String tagNameU = "";
                String subTagNameU = "";
                String subTagNameU_2 = "";
                String mainCode = "";
                String sWhere = "";
                String row_updated = "";
                String sQry = "";
                String selectdata = "";
                String companyUpdateQuery = "";
                String companiestobeUpdated = "";
                boolean stopIndividualToInsert = false;
                cifId = (parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>") + "</CustIdValue>".length() - 1, parseXml.indexOf("</CustIdValue>")) : "";
                
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Cifid jsp: ReportUrl: "+cifId);
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("tagName jsp: commonParse: "+tagName);
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("subTagName jsp: commonParse: "+subTagName);


                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                //  String colValue="";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values" + entry.getValue());


                    //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
                    columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                    columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";



                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnName commonParse" + columnName);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnValues commonParse" + columnValues);
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                      {

                        columnName = valueArr[0] + ",CifId,Request_Type,wi_name";
                        columnName = columnName.replaceAll("Card_approve_date", "ApplicationCreationDate");
                        columnName = columnName.replaceAll("Outstanding_balance", "OutstandingAmt");
                        columnName = columnName.replaceAll("Credit_limit", "CreditLimit");
                        columnName = columnName.replaceAll("Overdue_amount", "OverdueAmt");
                        columnName = columnName.replaceAll("GeneralStatus", "General_Status");
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery = "Select count(*) as selectdata from " + sTableName + " where  CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }

                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";
                        columnName = columnName.replaceAll("OutstandingAmt", "TotalOutstandingAmt");
                        columnName = columnName.replaceAll("Loan_close_date", "LoanMaturityDate");
                        columnName = columnName.replaceAll("GeneralStatus", "General_Status");//Deepak code added to save value in General_Status for PCAS-1264 as it was mising in PL & CC And same was there in RLOS
                        sWhere = "wi_name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "'";
                        sQry = "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_AcctDetails"))
                      {
                        String CreditGrade =
                          (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>") + "</CreditGrade>".length() - 1, parseXml.indexOf("</CreditGrade>")) : "";
                        //PCASP-2833 
                        //String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
                        columnName = valueArr[0] + ",Request_Type,CifId,CreditGrade,wi_name";
                        columnValues = valueArr[1] + ",'" + CreditGrade + "','" + returnType + "','" + cifId + "','" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND AcctId = '" + entry.getKey() + "'";
                        sQry = "Select count(*) as selectdata from " + sTableName + " where Wi_Name='" + wi_name + "' And AcctId = '" + entry.getKey() + "' And Account_Type ='Individual_CIF'";
                      }
                    else
                      {
                        sWhere = "Wi_Name='" + wi_name + "' AND Request_Type='" + returnType + "'";
                      }
                    
                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "collection summary update "+sTableName+" input:  " + strInputXml);
                    try
                      {
                        
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "collection summary update "+sTableName+" output:  " + strOutputXml);
                      }
                    catch (NGException e)
                      {
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception collection summary update "+sTableName+" output: " + e.getMessage());
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception collection summary update "+sTableName+" output: " + ex.getMessage());
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {   //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sQry sQry sQry --> "+sQry);
                        if (!sQry.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(sQry, cabinetName, sessionId);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                ex.printStackTrace();
                              }
                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                            selectdata = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select selectdata --> "+selectdata);
                          }

                        if (!companyUpdateQuery.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(companyUpdateQuery, cabinetName, sessionId);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + ex.getMessage());
                                ex.printStackTrace();
                              }



                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);

                            companiestobeUpdated = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select companiestobeUpdated --> "+companiestobeUpdated);

                            if (Integer.parseInt(companiestobeUpdated) > 0)
                              {
                                if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                                  {
                                    sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                  }
                                else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                                  {
                                    sWhere = "wi_name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                  }
                                strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "companiestobeUpdated collection summary update "+sTableName+" input: " + strInputXml);
                                try
                                  {
                                    
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("companiestobeUpdated collection summary update "+sTableName+" output: "+strOutputXml);
                                  }
                                catch (NGException e)
                                  {
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companiestobeUpdated collection summary "+sTableName+" update: " + e.getMessage());
                                    e.printStackTrace();
                                  }
                                catch (Exception ex)
                                  {
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception companiestobeUpdated collection summary "+sTableName+" update: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }

                                tagNameU = "APUpdate_Output";
                                subTagNameU = "MainCode";
                                subTagNameU_2 = "Output";
                                mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                                //row_updated = getTagValue(strOutputXml,tagNameU,subTagNameU_2);
                                ////DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
                                ////DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
                                stopIndividualToInsert = true;
                              }


                          }

                        if (sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert))
                          {
                            strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml final collection summary "+sTableName+" update: " + strInputXml);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strInputXml final collection summary "+sTableName+" update: "+strOutputXml);
                                mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                                if (!mainCode.equalsIgnoreCase("0"))
                                  {
                                    retVal = "false";
                                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: ApINsertfalse for collection summary: "+retVal);
                                  }
                                else
                                  {
                                    retVal = "true";
                                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: ApINserttrue for collection summary: "+retVal);
                                  }
                              }
                            catch (NGException e)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct_collection for "+sTableName+" : " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct_collection for "+sTableName+" : " + ex.getMessage());
                                ex.printStackTrace();
                              }
                          }
                        //change by saurabh for company call if its not able to overwrite individual data but call was successful so at frontend it should be successfull. Change on 2nd feb.
                        else
                          {
                            retVal = "true";
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: ApUpdatetrue for collection summary: "+retVal);
                      }
                  }

                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("final value for collection summary: "+retVal);
                return retVal;
              }

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        return retVal;
      }

    public static String commonParseFinance_CardInstallment(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort,
      String sessionId, String cabinetName, String subTagName, String subtag_single)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("commonParseFinance jsp: inside: ");
        String retVal = "";
        String[] valueArr = null;
        String strInputXml = "";
        String strOutputXml = "";
        String columnName = "";
        String columnValues = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String mainCode = "";
        String sWhere = "";
        String row_updated = "";
        String txnNum = "";
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("tagName jsp: commonParseFinance: "+tagName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("subTagName jsp: commonParseFinance: "+subTagName);
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sTableName jsp: commonParseFinance: "+sTableName);
        try
          {

            if ((returnType.equalsIgnoreCase("CARD_INSTALLMENT_DETAILS") && parseXml.contains("TransactionDetailsRec")))
              {

                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: commonParseFinance: "+returnType);
                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                String colValue = "";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    for (int i = 0; i < valueArr.length; i++)
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values:12345 " +valueArr[i]);
                      }
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values: " + entry.getValue());
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Key values: " + entry.getKey());

                    colValue = "'" + valueArr[1].replaceAll("[,]", "','") + "'";


                    //added
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardInstallmentDetails"))
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for USR_0_iRBL_InternalExpo_CardInstallmentDetails");
                        String header_info = getTagDataParent_cardInstallment_header(parseXml, "CardInstallmentDetailsResponse",
                          "CIFID,CardCRNNumber,CardSerialNumber,OTBAmount,TotalExposureAmount,TotalRepaymentAmount,InstallmentAccountStatus");

                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside commonParseFinance for USR_0_iRBL_InternalExpo_CardInstallmentDetails header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");

                        columnName = valueArr[0] +","+ header_info_arr[0]+ ",Request_Type,Wi_Name" ;
                        columnValues = valueArr[1] + ",'" + header_info_arr[1] + "','CARD_INSTALLMENT_DETAILS'," + wi_name;
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        columnValues = "";
                        for (int i = 0; i < columnName_arr.length; i++)
                          {
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside Card Installment for loop to remove I:"+columnName_arr[i]);
                            if (columnName_arr[i].equalsIgnoreCase("CardNumber"))
                              {
                                columnValues_arr[i] = columnValues_arr[i].replace("I", "");
                              }
                            if (i == 0)
                              {
                                columnValues = columnValues_arr[i];
                              }
                            else
                              {
                                columnValues = columnValues + "," + columnValues_arr[i];
                              }
                          }

                        txnNum = columnValues_arr[Arrays.asList(columnName_arr).indexOf("TxnSerialNum")];
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside Cardinstallment: columnName after merging:"+columnValues);

                        // sWhere="Wi_Name='"+wi_name+"' AND Request_Type='"+returnType+"' AND TxnSerialNum = '"+entry.getKey()+"' ";
                        sWhere = "Wi_Name='" + wi_name + "' AND Request_Type='" + returnType + "' AND TxnSerialNum = " + txnNum + "";

                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sWhere of cardinstallmentDetails"+sWhere);
                      }
                    //ended

                    else
                      {
                        columnName = valueArr[0] + ",Request_Type,Wi_Name";
                        columnValues = colValue + ",'" + returnType + "','" + wi_name + "'";
                      }


                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnName commonParse123" + columnName);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnValues commonParse456" + columnValues);

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml update for finance " + strInputXml);
                    try
                      {
                        //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml update:123 "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode123 --> "+mainCode);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode123 --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {
                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml123Installment insert Query:" + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:1234 "+strOutputXml);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:mainCode value "+mainCode);
                            mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:mainCode value1234 "+mainCode);
                            if (!mainCode.equalsIgnoreCase("0"))
                              {
                                retVal = "false";
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:false "+retVal);
                              }
                            else
                              {
                                retVal = "true";
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:true "+retVal);
                              }
                          }
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                      }
                  }

              }
            else
              {
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: commonParseFinance Empty tag : "+returnType+" Wi_Name: "+wi_name);
              }
            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: final value for financial summary "+retVal);

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseFinance_CardInstallment: " + e.getMessage());
            e.printStackTrace();
            retVal = "";
          }
        return retVal;
      }



    public static String commonParseFinance(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String subtag_single)
      {
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("commonParseFinance jsp: inside: ");
        String retVal = "";
        String[] valueArr = null;
        String strInputXml = "";
        String strOutputXml = "";
        String columnName = "";
        String columnValues = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String mainCode = "";
        String sWhere = "";
        String row_updated = "";
        String id = "";
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("tagName jsp: commonParseFinance: " + tagName);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("subTagName jsp: commonParseFinance: " + subTagName);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sTableName jsp: commonParseFinance: " + sTableName);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("sTableName jsp: commonParseFinance: " + parseXml);
        try
          {
            if ((returnType.equalsIgnoreCase("RETURNDET") && parseXml.contains("ReturnsDtls")) 
            		|| (returnType.equalsIgnoreCase("AVGBALDET") && parseXml.contains("AcctId"))
                    || (returnType.equalsIgnoreCase("LIENDET") && parseXml.contains("LienDetails")) 
                    || (returnType.equalsIgnoreCase("SIDET") && parseXml.contains("SIDetails"))
                    || (returnType.equalsIgnoreCase("TRANSUM") && parseXml.contains("TxnSummary")) 
              		|| (returnType.equalsIgnoreCase("SALDET") && parseXml.contains("SalDetails")))
              {

                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: commonParseFinance: "+returnType);
                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                String colValue = "";


                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values:1234 " +valueArr);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values: " + entry.getValue());

                    colValue = "'" + valueArr[1].replaceAll("[,]", "','") + "'";
                    if (returnType.equalsIgnoreCase("AVGBALDET") && valueArr[0].contains("AcctId"))
                      {
                        String columnName_arr[] = valueArr[0].split(",");
                        String columnValues_arr[] = valueArr[1].split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                      }

                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_AvgBalanceDtls"))
                      {
                        columnName = valueArr[0] + ",Wi_Name";
                        columnValues = valueArr[1] + ",'" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                      }
                    //modified by akshay on 6/2/18  
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_ReturnsDtls"))
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside Return Details-->columnValues: "+columnValues);
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        if (returnType.equalsIgnoreCase("RETURNDET") && valueArr[0].contains("ReturnNumber"))
                          {
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("ReturnNumber")];
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND ReturnNumber = " + id;
                          }
                        else
                          {
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_LienDetails"))
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];

                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String leinId = columnValues_arr[Arrays.asList(columnName_arr).indexOf("LienId")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and LienId = " + leinId;
                        strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml delete returndtls " + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml delete returndtls: "+strOutputXml);
                          }
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_TxnSummary"))
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",Wi_Name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String Month = columnValues_arr[Arrays.asList(columnName_arr).indexOf("Month")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and Month = " + Month + "";
                        strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_SalTxnDetails"))
                      {
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_SalTxnDetails");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CifId,AcctId,OperationType");
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];

                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String SalCreditDate = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SalCreditDate")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and 1=2 and SalCreditDate = " + SalCreditDate + "";

                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_SiDtls"))
                      {
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside commonParseFinance: ng_rlos_FinancialSummary_SiDtls ");
                        try
                          {
                            String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CifId,AcctId,OperationType");
                            String[] header_info_arr = header_info.split(":");
                            columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                            columnValues = valueArr[1] + ",'" + wi_name +  "'," + header_info_arr[1];
                            String columnName_arr[] = columnName.split(",");
                            String columnValues_arr[] = columnValues.split(",");
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                            String SINumber = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SINumber")];
                            columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                            columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                            //String sWhere_delete = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " And SINumber=" + SINumber;
                            //strInputXml = ExecuteQuery_APdelete(sTableName,sWhere_delete,cabinetName,sessionId);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml delete ng_rlos_FinancialSummary_SiDtls " + strInputXml);
                            /*
                             * try { strOutputXml =
                             * NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort,
                             * appServerType, strInputXml);
                             * 
                             * System.out.
                             * println("CustExpose_Output jsp: strOutputXml delete returndtls: "
                             * +strOutputXml); } catch (NGException e) { e.printStackTrace();
                             */
                          }
                        catch (Exception ex)
                          {
                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in ng_rlos_FinancialSummary_SiDtls: " + ex.getMessage());
                          }
                      }
                    else
                      {
                        columnName = valueArr[0] + ",Request_Type,Wi_Name";
                        columnValues = colValue + ",'" + returnType +  "','" +  wi_name + "'";
                      }


                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnName commonParse" + columnName);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "columnValues commonParse" + columnValues);

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strInputXml update " + strInputXml);
                    try
                      {
                        //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml update: "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        ex.printStackTrace();
                      }
                    //changed by akshay on 2/5/18 for proc 8964
                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                    //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("getTagValue select mainCode --> "+row_updated);
                    if (!(mainCode.equalsIgnoreCase("0")) || row_updated.equalsIgnoreCase("0"))
                      {
                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strInputXml" + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                            tagNameU = "APInsert_Output";
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml: "+strOutputXml);
                            mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                            //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "mainCode value is: " +mainCode );
                            if (!mainCode.equalsIgnoreCase("0"))
                              {
                                retVal = "false";
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: ApINsertfalse for financial summary: "+retVal);
                              }
                            else
                              {
                                retVal = "true";
                                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: ApINserttrue for financial summary: "+retVal);
                              }
                          }
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                      }
                  }
              }
            else
              {
                retVal = "true";
                //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("returnType jsp: commonParseFinance Empty tag : "+returnType+" Wi_Name: "+wi_name);
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseFinance: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: final value for financial summary "+retVal);
        return retVal;
      }


    public static String ExecuteQuery_APInsert(String tableName, String columnName, String strValues, String cabinetName, String sessionId)
      {
        StringBuffer ipXMLBuffer = new StringBuffer();

        ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
        ipXMLBuffer.append("<APInsertExtd_Input>\n");
        ipXMLBuffer.append("<Option>APInsert</Option>");
        ipXMLBuffer.append("<TableName>");
        ipXMLBuffer.append(tableName);
        ipXMLBuffer.append("</TableName>");
        ipXMLBuffer.append("<ColName>");
        ipXMLBuffer.append(columnName);
        ipXMLBuffer.append("</ColName>\n");
        ipXMLBuffer.append("<Values>");
        ipXMLBuffer.append(strValues);
        ipXMLBuffer.append("</Values>\n");
        ipXMLBuffer.append("<EngineName>");
        ipXMLBuffer.append(cabinetName);
        ipXMLBuffer.append("</EngineName>\n");
        ipXMLBuffer.append("<SessionId>");
        ipXMLBuffer.append(sessionId);
        ipXMLBuffer.append("</SessionId>\n");
        ipXMLBuffer.append("</APInsertExtd_Input>");

        return ipXMLBuffer.toString();
      }

    public static String ExecuteQuery_APdelete(String tableName, String sWhere, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version=\"1.0\"?>" + "<APDelete_Input><Option>APDelete</Option>" + "<TableName>" + tableName + "</TableName>" + "<WhereClause>" + sWhere + "</WhereClause>"
          + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionId>" + sessionId + "</SessionId>" + "</APDelete_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APUpdate(String tableName, String columnName, String strValues, String sWhere, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version=\"1.0\"?>" + "<APUpdate_Input><Option>APUpdate</Option>" + "<TableName>" + tableName + "</TableName>" + "<ColName>" + columnName + "</ColName>" + "<Values>"
          + strValues + "</Values>" + "<WhereClause>" + sWhere + "</WhereClause>" + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionId>" + sessionId + "</SessionId>" + "</APUpdate_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APSelectwithparam(String sQry, String params, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version='1.0'?><APSelectWithNamedParam_Input>" + "<option>APSelectWithNamedParam</option>" + "<Query>" + sQry + "</Query>" + "<Params>" + params + "</Params>"
          + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionID>" + sessionId + "</SessionID>" + "</APSelectWithNamedParam_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APSelect(String sQry, String cabinetName, String sessionId)
      {

        StringBuffer ipXMLBuffer = new StringBuffer();

        ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
        ipXMLBuffer.append("<APSelect_Input>\n");
        ipXMLBuffer.append("<Option>APSelectWithColumnNames</Option>\n");
        ipXMLBuffer.append("<Query>");
        ipXMLBuffer.append(sQry);
        ipXMLBuffer.append("</Query>\n");
        ipXMLBuffer.append("<EngineName>");
        ipXMLBuffer.append(cabinetName);
        ipXMLBuffer.append("</EngineName>\n");
        ipXMLBuffer.append("<SessionId>");
        ipXMLBuffer.append(sessionId);
        ipXMLBuffer.append("</SessionId>\n");
        ipXMLBuffer.append("</APSelect_Input>");

        return ipXMLBuffer.toString();

      }

    public static String getTagValue(String parseXml, String tagName, String subTagName)
      {
        //WriteLog("getTagValue jsp: inside: ");
        String[] valueArr = null;
        String mainCodeValue = "";

        //WriteLog("tagName jsp: getTagValue: "+tagName);
        //WriteLog("subTagName jsp: getTagValue: "+subTagName);

        try
          {
            Map<Integer, String> tagValuesMap = new LinkedHashMap<Integer, String>();
            tagValuesMap = getTagDataParent(parseXml, tagName, subTagName);

            Map<Integer, String> map = tagValuesMap;
            for (Map.Entry<Integer, String> entry : map.entrySet())
              {
                valueArr = entry.getValue().split("~");
                //WriteLog( "tag values" + entry.getValue());
                mainCodeValue = valueArr[1];
                //WriteLog( "mainCodeValue" + mainCodeValue);
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured getTagValue: " + e.getMessage());
            e.printStackTrace();
          }
        return mainCodeValue;
      }

    public static void updateQuery(String sTableName, String columnName, String colValue, String sWhere, String cabinetName, String sessionId, String returnType, String wrapperIP, String wrapperPort,
      String appServerType, String cifId, String wi_name)
      {
        String strInputXml = "";
        String strOutputXml = "";
        String mainCode = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String columnValues = "";
        String row_updated = "";
        try
          {
            strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, colValue, sWhere, cabinetName, sessionId);
            //WriteLog( "strInputXml update " + strInputXml);
            try
              {
                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                //WriteLog("CustExpose_Output jsp: strOutputXml update: "+strOutputXml);
              }
            catch (NGException e)
              {
                e.printStackTrace();
              }
            catch (Exception ex)
              {
                ex.printStackTrace();
              }

            tagNameU = "APUpdate_Output";
            subTagNameU = "MainCode";
            subTagNameU_2 = "Output";
            mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
            row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
            //WriteLog("getTagValue select mainCode --> "+mainCode);
            if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
              {
                //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";

                columnName = columnName + ",Request_Type,CifId,Wi_Name";
                columnValues = colValue + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";

                strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                //WriteLog( "strInputXml insert " + strInputXml);
                try
                  {
                    //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                    //WriteLog("CustExpose_Output jsp: strOutputXml insert: "+strOutputXml);
                  }
                catch (NGException e)
                  {
                    e.printStackTrace();
                  }
                catch (Exception ex)
                  {
                    ex.printStackTrace();
                  }
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured updateQuery: " + e.getMessage());
            e.printStackTrace();
          }
      }

    public static Map<String, String> getTagDataParent_deep(String parseXml,String tagName,String sub_tag,String subtag_single){
		  Map<String, String> tagValuesMap= new LinkedHashMap<String, String>(); 
		   InputStream is = new ByteArrayInputStream(parseXml.getBytes());
		  try {
				//WriteLog("getTagDataParent_deep jsp: parseXml: "+parseXml);
				//WriteLog("getTagDataParent_deep jsp: tagName: "+tagName);
				//WriteLog("getTagDataParent_deep jsp: subTagName: "+sub_tag);
				String Operationtype="";
				String id="";
				String tag_notused = "BankId,OperationDesc,TxnSummary,#text";
				String subtag_notused = "MaxOverdueAmountDate"; //Deepak New variabled added to avoid subtag
				if(parseXml.indexOf("<OperationType>")>-1)
					{
						Operationtype= parseXml.substring(parseXml.indexOf("<OperationType>")+15,parseXml.indexOf("</OperationType>"));
						//WriteLog("$$Operationtype "+Operationtype);
					}
				
			   	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(is);
				doc.getDocumentElement().normalize();
				NodeList nList_loan = doc.getElementsByTagName(tagName);
				//WriteLog("getTagDataParent_deep jsp: nList_loan: "+nList_loan);
				for(int i = 0 ; i<nList_loan.getLength();i++){
					String col_name = "";
					String col_val ="";
					NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
					////WriteLog("getTagDataParent_deep jsp: ch_nodeList: "+ch_nodeList);
					if (Operationtype.equalsIgnoreCase("SALDET")){
						id = ch_nodeList.item(1).getTextContent();
					}
					else if (Operationtype.equalsIgnoreCase("RETURNDET")){
						int id_num = 0;
						for(int ch_len = 0 ;ch_len< ch_nodeList.getLength(); ch_len++){
							if(ch_nodeList.item(ch_len).getNodeName().equalsIgnoreCase("ReturnNumber")){
								id_num = ch_len;
							}
						}
						id = ch_nodeList.item(id_num).getTextContent();
					}
					else if("SalDetails".equalsIgnoreCase(tagName)){
						id = ch_nodeList.item(0).getTextContent()+i;
					}
					else if("ServicesDetails".equalsIgnoreCase(tagName)){
						id = ch_nodeList.item(1).getTextContent();
					}
					else if("ChequeDetails".equalsIgnoreCase(tagName)){
						id = ch_nodeList.item(1).getTextContent();
					}
					else {
						id = ch_nodeList.item(0).getTextContent();
					}
					for(int ch_len = 0 ;ch_len< ch_nodeList.getLength(); ch_len++){
						if(sub_tag.contains(ch_nodeList.item(ch_len).getNodeName())){
							NodeList sub_ch_nodeList =  ch_nodeList.item(ch_len).getChildNodes();
							if(!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text")){
								if(!(subtag_notused.toUpperCase()).contains(sub_ch_nodeList.item(0).getTextContent().toUpperCase())){//new condition added by Deepak to bypass subtag_notused MaxOverdueAmountDate
									if(col_name.equalsIgnoreCase("")){
										col_name = sub_ch_nodeList.item(0).getTextContent();
										col_val = "'"+sub_ch_nodeList.item(1).getTextContent()+"'";
									}
									else if(!col_name.contains(sub_ch_nodeList.item(0).getTextContent())){
										col_name = col_name+","+sub_ch_nodeList.item(0).getTextContent();
										col_val = col_val+",'"+sub_ch_nodeList.item(1).getTextContent()+"'";
									}
								}
							}
						}
						else if(tag_notused.contains(ch_nodeList.item(ch_len).getNodeName())){
							//WriteLog("this tag not to be passed: "+ch_nodeList.item(ch_len).getNodeName());
						}
						else if(subtag_single.contains(ch_nodeList.item(ch_len).getNodeName())){
							NodeList sub_ch_nodeList =  ch_nodeList.item(ch_len).getChildNodes();
							if(!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text")){
								for(int sub_chd_len=0;sub_chd_len<sub_ch_nodeList.getLength();sub_chd_len++){
									if(col_name.equalsIgnoreCase("")){
										col_name = sub_ch_nodeList.item(sub_chd_len).getNodeName();
										col_val = "'"+sub_ch_nodeList.item(sub_chd_len).getTextContent()+"'";
									}
									else if(!col_name.contains(sub_ch_nodeList.item(sub_chd_len).getNodeName())){
										col_name = col_name+","+sub_ch_nodeList.item(sub_chd_len).getNodeName();
										col_val = col_val+",'"+sub_ch_nodeList.item(sub_chd_len).getTextContent()+"'";
									}
								}
							}
						}
						else{
							if(col_name.equalsIgnoreCase("")){
								////WriteLog("inside else if");
								col_name = ch_nodeList.item(ch_len).getNodeName();
								col_val = "'"+ch_nodeList.item(ch_len).getTextContent()+"'";
							}
							else if(!col_name.contains(ch_nodeList.item(ch_len).getNodeName())){
							//WriteLog("inside else else if"+ch_nodeList.item(ch_len).getNodeName());
							//WriteLog("inside else else if"+ch_nodeList.item(ch_len).getTextContent());
											col_name = col_name+","+ch_nodeList.item(ch_len).getNodeName();
											//WriteLog("inside else col_name"+col_name);
											col_val = col_val+",'"+ch_nodeList.item(ch_len).getTextContent()+"'";
											//WriteLog("inside else col_name"+col_val);
										}
						}
					}
					if(!col_name.equalsIgnoreCase(""))
						//WriteLog("inside else col_name "+col_name+" id "+id+" col_val "+col_val);
						//Deepak to handle ~
						if(col_val.contains("~")){
							col_val = col_val.replaceAll("~","-");
						}
						tagValuesMap.put(id, col_name+"~"+col_val);	
						//WriteLog("tagValuesMap inside loop"+tagValuesMap);
				}
			    } catch (Exception e) {
			    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Exception occured in getTagDataParent_deep method: "+ e.getMessage());
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Exception occured in is close:  "+e.getMessage());
					e.printStackTrace();
				//WriteLog("Exception occured in getTagDataParent method:  "+e.getMessage());
			    }
				finally
			{
				try{
			    		if(is!=null)
			    		{
			    		is.close();
			    		is=null;
			    		}
			    	}
			    	catch(Exception e){
			    		DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Exception occured in is close:  "+e.getMessage());
			    	}
			}
			    return tagValuesMap;
	  }
    
    /**added by ravindra on 19-10-2022**/
    public static Map<String, String> getTagDataParent_deep_new(String parseXml, String tagName, String sub_tag, String subtag_single) {
		Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
		InputStream is = new ByteArrayInputStream(parseXml.getBytes());
		try {

			List<String> tag_notused = new LinkedList<String>();
			tag_notused.add("BankId");
			tag_notused.add("OperationDesc");
			tag_notused.add("TxnSummary");
			tag_notused.add("#text");

			List<String> subtag_notused = new LinkedList<String>();
			subtag_notused.add("MaxOverdueAmountDate");

			String Operationtype = "";
			String id = "";
			if (parseXml.indexOf("<OperationType>") > -1) {
				Operationtype = parseXml.substring(parseXml.indexOf("<OperationType>") + 15, parseXml.indexOf("</OperationType>"));
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nList_loan = doc.getElementsByTagName(tagName);
			for (int i = 0; i < nList_loan.getLength(); i++) {
				List<String> columns = new LinkedList<String>();
				String values = "";
				NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
				if (Operationtype.equalsIgnoreCase("SALDET")) {
					id = ch_nodeList.item(1).getTextContent();
				} else if (Operationtype.equalsIgnoreCase("RETURNDET")) {
					int id_num = 0;
					for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++) {
						if (ch_nodeList.item(ch_len).getNodeName().equalsIgnoreCase("ReturnNumber")) {
							id_num = ch_len;
						}
					}
					id = ch_nodeList.item(id_num).getTextContent();
				} else if ("SalDetails".equalsIgnoreCase(tagName)) {
					id = ch_nodeList.item(0).getTextContent() + i;
				} else if ("ServicesDetails".equalsIgnoreCase(tagName)) {
					id = ch_nodeList.item(1).getTextContent();
				} else if ("ChequeDetails".equalsIgnoreCase(tagName)) {
					id = ch_nodeList.item(1).getTextContent();
				} 
				//Added by kamran 25012023 
				else if ("SalaryCreditDetails".equalsIgnoreCase(tagName)) {
					id = ch_nodeList.item(3).getTextContent();
				} 
							
				//End 25012023
				else {
					id = ch_nodeList.item(0).getTextContent();
				}
				for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++) {
					if (sub_tag.contains(ch_nodeList.item(ch_len).getNodeName())) {
						NodeList sub_ch_nodeList = ch_nodeList.item(ch_len).getChildNodes();
						if (!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text")) {
							if (!subtag_notused.contains(sub_ch_nodeList.item(0).getTextContent())) {// new condition added by Deepak to bypass subtag_notused MaxOverdueAmountDate
								if (!columns.contains(sub_ch_nodeList.item(0).getTextContent())) {
									columns.add(sub_ch_nodeList.item(0).getTextContent());
									if (values.equals(""))
										values = "'" + sub_ch_nodeList.item(1).getTextContent() + "'";
									else
										values += ",'" + sub_ch_nodeList.item(1).getTextContent() + "'";
								}
							}
						}
					} else if (tag_notused.contains(ch_nodeList.item(ch_len).getNodeName())) {

					} else if (subtag_single.contains(ch_nodeList.item(ch_len).getNodeName())) {
						NodeList sub_ch_nodeList = ch_nodeList.item(ch_len).getChildNodes();
						if (!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text")) {
							for (int sub_chd_len = 0; sub_chd_len < sub_ch_nodeList.getLength(); sub_chd_len++) {
								if (!columns.contains(sub_ch_nodeList.item(sub_chd_len).getNodeName())) {
									columns.add(sub_ch_nodeList.item(sub_chd_len).getNodeName());
									if (values.equals(""))
										values = "'" + sub_ch_nodeList.item(sub_chd_len).getTextContent() + "'";
									else
										values += ",'" + sub_ch_nodeList.item(sub_chd_len).getTextContent() + "'";
								}
							}
						}
					} else if (!columns.contains(ch_nodeList.item(ch_len).getNodeName())) {
						columns.add(ch_nodeList.item(ch_len).getNodeName());
						if (values.equals(""))
							values = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
						else
							values += ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
					}
				}
				if (columns.size() > 0) {
					if (!values.equals("") && values.contains("~")) {
						values = values.replaceAll("~", "-");
					}
					tagValuesMap.put(id, columns.toString().replace("[", "").replace("]", "") + "~" + values);
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.info("Columns : " + columns.toString().replace("[", "").replace("]", ""));
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.info("Values : " + values);
				}
			}
		} catch (Exception e) {
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Exception occured in getTagDataParent_deep method: " + e.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (Exception e) {
				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Exception occured : " + e.getMessage());
			}
		}
		return tagValuesMap;
	}
    
    public static void parseLinkedCif(String Xml, String TableName, String Main_CIF,String Wi_name, String Agreement_id, String Cust_Type, String Liability_type, String cabinetName,
      String sessionId, String wrapperIP, String wrapperPort)
      {
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF");
        //DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: Input_XMl" + Xml);

        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
            doc.getDocumentElement().normalize();
            String Liabilityid = "";
            //String ParentTag= doc.getDocumentElement().getNodeName();
            NodeList nList;
            if ("Account".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("AcctDetails");
                Liabilityid = "AcctId";
              }
            else if ("Loan".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("LoanDetails");
                Liabilityid = "AgreementId";
              }
            else
              {
                nList = doc.getElementsByTagName("CardDetails");
                Liabilityid = "CardEmbossNum";
              }

            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());
            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                //  DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {

                    Element eElement = (Element) nNode;
                    String Liability_ID = eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: AcctId" + Liability_ID);
                    if (Liability_ID.equalsIgnoreCase(Agreement_id))
                      {

                        NodeList Linked_CIF = eElement.getElementsByTagName("LinkedCIFs");
                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: Linked_CIF.getLength()" + Linked_CIF.getLength());
                        for (int temp1 = 0; temp1 < Linked_CIF.getLength(); temp1++)
                          {
                            Node node1 = Linked_CIF.item(temp1);
                            if (node1.getNodeType() == Node.ELEMENT_NODE)
                              {
                                Element eElement1 = (Element) node1;
                                String Linked_CIF1 = eElement1.getElementsByTagName("CIFId").item(0).getTextContent();
                                String Relation1 = eElement1.getElementsByTagName("RelationType").item(0).getTextContent();
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: Linked_CIF" + Linked_CIF1);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: Relation" + Relation1);

                                /*
                                 * String Linked_CIF=
                                 * eElement.getElementsByTagName("CIFId").item(0).getTextContent();
                                 * String Relation =
                                 * eElement.getElementsByTagName("RelationType").item(0).
                                 * getTextContent();
                                 */
                                String SQuery = "select count(wi_name) as Select_Count from USR_0_iRBL_InternalExpo_LinkedICF where Linked_CIFs='" + Linked_CIF1 + "' and Relation='" + Relation1
                                  + "' and wi_name='" + Wi_name + "' and Main_Cif='" + Main_CIF + "' and AgreementId='" + Agreement_id + "'";
                                String strInputXml = ExecuteQuery_APSelect(SQuery, cabinetName, sessionId);
                                String strOutputXml = "";
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: ExecuteQuery_APSelect" + strInputXml);
                                try
                                  {
                                    //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    //WriteLog("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: ExecuteQuery_APSelect output" + strOutputXml);
                                  }
                                catch (Exception ex)
                                  {
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in commonParseProduct: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }
                                String mainCode = (strOutputXml.contains("<MainCode>"))
                                  ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                                //WriteLog("getTagValue select mainCode --> "+mainCode);
                                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF select mainCode --> " + mainCode);
                                if ("0".equalsIgnoreCase(mainCode))
                                  {
                                    String selectdata = (strOutputXml.contains("<Select_Count>"))
                                      ? strOutputXml.substring(strOutputXml.indexOf("<Select_Count>") + "</Select_Count>".length() - 1, strOutputXml.indexOf("</Select_Count>")) : "";
                                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF select selectdata --> " + selectdata);
                                    int totalretrieved = Integer.parseInt(selectdata);
                                    if (totalretrieved == 0)
                                      {
                                        //need to change by prabhakar
                                        String sTableName = "USR_0_iRBL_InternalExpo_LinkedICF";
                                        String columnName = "Wi_name,Linked_CIFs,Relation,AgreementId,Main_Cif,Liability_Type,Cust_Type";
                                        String columnValues =
                                          "'" + Wi_name + "','" + Linked_CIF1 + "','" + Relation1 + "','" + Agreement_id + "','" + Main_CIF + "','" + Liability_type + "','" + Cust_Type + "'";
                                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                                        //WriteLog( "strInputXml" + strInputXml);
                                        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug(" Parse linked cif  strInputXml" + strInputXml);
                                        try
                                          {
                                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug(" Parse linked cif  strOutputXml" + strOutputXml);
                                            //WriteLog("CustExpose_Output jsp: strOutputXml: "+strOutputXml);
                                            mainCode = getTagValue(strOutputXml, "APInsert_Output", "MainCode");


                                          }
                                        catch (Exception ex)
                                          {
                                            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parseCIF: " + ex.getMessage());
                                            ex.printStackTrace();
                                          }
                                      }
                                  }
                                //WriteLog("getTagValue select selectdata --> "+selectdata);
                              }
                          }
                      }
                  }
              }
          }
        catch (Exception ex)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parse linked cif : " + ex.getMessage());
            ex.printStackTrace();
          }
      }
    
    //Kamran24012023
	public static String parseSalaryCreditHistory(String Xml, String Agreement_id, String Liability_type,
			String StartType, String EndType) {
		String Output_desired = "";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
			doc.getDocumentElement().normalize();
			String Salaryid = "";
			NodeList nList;
			nList = doc.getElementsByTagName("SalaryCreditDetails");
			Salaryid = "CBAccountNo";
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());
			 for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String Salary_ID = eElement.getElementsByTagName(Salaryid).item(0).getTextContent();
					if (Salary_ID.equalsIgnoreCase(Agreement_id)){
						DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: CBAccountNo" + Salary_ID);
						String Salaryid_aggregate = nodeToString(nNode);
						Output_desired = Salaryid_aggregate.substring(Salaryid_aggregate.indexOf(StartType),
								Salaryid_aggregate.lastIndexOf(EndType) + EndType.length());
					}
				}
			}
		}
		
		catch (Exception e) {
			  DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parse Salary Credit Details : " + e.getMessage());
		}
		return Output_desired;
	}
    //End 24012023
	//Deepak 27012023 
	public static String Save_SalaryCreditHistory(Map<String, String> SalaryCreditHistory_map,String Wi_name,String account_Id,String DPAccountNo, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
			String cabinetName) {
		String Output_desired = "";
		try {
			Map<String, String> map = SalaryCreditHistory_map;
			String [] valueArr= null;
			String strInputXml="";
			String strOutputXml="";
			String columnName = "";
			String columnValues = "";
			String retVal="";
			// String colValue="";
			for (Map.Entry<String, String> entry : map.entrySet())
			{
				valueArr = entry.getValue().split("~");
				//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "tag values" + entry.getValue());

				//columnValues = valueArr[1].spilt(",");
				// columnValues=columnValues+",'"+getCellData(SheetName1, rCnt, cCnt)+"'";
				//colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
				columnName = valueArr[0] + ", CBAccountNo, DPAccountNo, Wi_Name";
				columnValues = valueArr[1] + ",'" + account_Id + "', '"+DPAccountNo+"', '" + Wi_name + "'";
				String sWhere = "Wi_Name='" + Wi_name +"' AND CBAccountNo = '" + account_Id +"' AND referencedate = '" + entry.getKey()+"'";

				strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml update for "+sTableName+" table: " + strInputXml);
				try
				{
					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Hi");
					strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);;

					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strOutputXml update for "+sTableName+" table: "+strOutputXml);
				}
				catch (NGException e)
				{
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception update for "+sTableName+" table: " + e.getMessage());
					e.printStackTrace();
				}
				catch (Exception ex)
				{
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception update for "+sTableName+" table: " + ex.getMessage());
					ex.printStackTrace();
				}

				String tagNameU = "APUpdate_Output";
				String subTagNameU = "MainCode";
				String subTagNameU_2 = "Output";
				String mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
				String row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("maincode update for "+sTableName+" table:  --> "+mainCode);
				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("row updated update for "+sTableName+" table: --> "+row_updated);
				if (mainCode.equalsIgnoreCase("0") && row_updated.equalsIgnoreCase("0")){
					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for cif --> "+cifId);
					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for table --> "+sTableName);
					//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("calling APInsert for cust_type --> "+cust_type);
					strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug( "strInputXml final insert for "+sTableName+" table:" + strInputXml);
					try
					{

						strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

						DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("strOutputXml final insert for "+sTableName+" table: "+strOutputXml);
						mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
						DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("mainCode"+mainCode);
						if (!mainCode.equalsIgnoreCase("0"))
						{
							retVal = "false";
							//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:false "+retVal);
						}
						else
						{
							retVal = "true";
							//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:true "+retVal);
						}
					}
					catch (NGException e)
					{
						DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + e.getMessage());
						e.printStackTrace();
					}
					catch (Exception ex)
					{
						DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + ex.getMessage());
						ex.printStackTrace();
					}
				}
				else{
					retVal = "false";
				}
			}

		}

		catch (Exception e) {
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parse Salary Credit Details : " + e.getMessage());
		}
		return Output_desired;
	}
    public static String parseHistoryUtilization(String Xml, String Agreement_id, String Liability_type, String StartType, String EndType)
      {
        //WriteLog("Inside parse CIF");
        //WriteLog("Inside parse CIF:: Input_XMl" + Xml);
        String Output_desired = "";

        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
            doc.getDocumentElement().normalize();
            String Liabilityid = "";
            //String ParentTag= doc.getDocumentElement().getNodeName();
            NodeList nList;
            if ("LoanDetails".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("LoanDetails");
                Liabilityid = "AgreementId";
              }
            else
              {
                nList = doc.getElementsByTagName("CardDetails");
                Liabilityid = "CardEmbossNum";
              }

            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());

            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {

                    Element eElement = (Element) nNode;

                    String Liability_ID = eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside parse CIF:: AcctId" + Liability_ID);
                    if (Liability_ID.equalsIgnoreCase(Agreement_id))
                      {
                        //  DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("\nCurrent Element :" + nNode.getNodeName());
                        //  WriteLog("Inside parse CIF:: ExecuteQuery_APSelect" + nodeToString(nNode));
                        String Liability_aggregate = nodeToString(nNode);
                        Output_desired = Liability_aggregate.substring(Liability_aggregate.indexOf(StartType), Liability_aggregate.lastIndexOf(EndType) + EndType.length());

                      }
                  }
              }

          }
        catch (Exception ex)
          {
            
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in parse history Utilitixation cif : " + ex.getMessage());
            //ex.printStackTrace();
          }
        return Output_desired;
      }

    public static String get_loanDesc(String loan_code, String cabinetName, String sessionId, String wrapperIP, String wrapperPort)
      {
        String loan_desc = "";
        try
          {
            String str_Loandesc = "select Description from NG_MASTER_contract_type with(nolock) where code = '"+loan_code.replace("'", "")+"'";
            //String params = "code==" + loan_code.replace("'", "");
            String strInputXml = ExecuteQuery_APSelect(str_Loandesc, cabinetName, sessionId);//(str_Loandesc, params, cabinetName, sessionId);
            //String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
            String strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("inside get_loanDesc strOutputXml:  " + strOutputXml);
            String Maincode = strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>"));
            if ("0".equalsIgnoreCase(Maincode))
              {
                loan_desc = strOutputXml.substring(strOutputXml.indexOf("<Description>") + "</Description>".length() - 1, strOutputXml.indexOf("</Description>"));
              }
            else
              {
                loan_desc = loan_code;
              }
          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in get_loanDesc:  " + e.getMessage());
            loan_desc = loan_code;
          }
        return "'" + loan_desc + "'";
      }

    public static String get_Mob_forOD(String LimitSactionDate)
      {
        try
          {
            LimitSactionDate = LimitSactionDate.replaceAll("'", "");
            Date Current_date = new Date();
            Date Old_Date = new SimpleDateFormat("yyyy-MM-dd").parse(LimitSactionDate);
            int yy = Current_date.getYear() - Old_Date.getYear();
            int mm = Current_date.getMonth() - Old_Date.getMonth();
            if (mm < 0)
              {
                yy--;
                mm = 12 - Old_Date.getMonth() + Current_date.getMonth();
                if (Current_date.getDate() < Old_Date.getDate())
                  {
                    mm--;
                  }
              }
            else if (mm == 0 && Current_date.getDate() < Old_Date.getDate())
              {
                yy--;
                mm = 11 - Old_Date.getMonth() + Current_date.getMonth();
              }
            else if (mm > 0 && Current_date.getDate() < Old_Date.getDate())
              {
                mm--;
              }
            else if (Current_date.getDate() - Old_Date.getDate() != 0)
              {
                if (mm == 12)
                  {
                    yy++;
                    mm = 0;
                  }
              }

            return String.valueOf((yy * 12) + mm);
          }
        catch (Exception ex)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in get_Mob_forOD: " + ex.getMessage());
            ex.printStackTrace();
            return "Invalid";
          }

      }

    public static String getTagDataParent_cardInstallment_header(String parseXml, String tagName, String sub_tag)
      {
        String col_name = "";
        String col_val = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent_cardInstallment_header jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent_cardInstallment_header jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent_cardInstallment_header jsp: subTagName: "+sub_tag);

            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent_cardInstallment_header jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nList_loan.getLength(); i++)
              {

                NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
                //String id = ch_nodeList.item(0).getTextContent();
                for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++)
                  {
                    if (sub_tag.toUpperCase().contains(ch_nodeList.item(ch_len).getNodeName().toUpperCase()))
                      {
                        if (col_name.equalsIgnoreCase(""))
                          {
                            col_name = ch_nodeList.item(ch_len).getNodeName();
                            col_val = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                        else
                          {
                            col_name = col_name + "," + ch_nodeList.item(ch_len).getNodeName();
                            col_val = col_val + ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                      }
                  }
                //WriteLog("insert/update getTagDataParent_cardInstallment_header for id: "+id);
                //WriteLog("insert/update getTagDataParent_cardInstallment_header cal_name: "+col_name);
                //WriteLog("insert/update getTagDataParent_cardInstallment_header col_val: "+col_val);

              }

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in getTagDataParent_cardInstallment_header: " + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent_cardInstallment_header method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return col_name + ":" + col_val;
      }

    public static String getTagDataParent_financ_header(String parseXml, String tagName, String sub_tag)
      {
        String col_name = "";
        String col_val = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent_financ_header jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent_financ_header jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent_financ_header jsp: subTagName: "+sub_tag);

            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent_financ_header jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nList_loan.getLength(); i++)
              {

                NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
                //String id = ch_nodeList.item(0).getTextContent();
                for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++)
                  {
                    if (sub_tag.toUpperCase().contains(ch_nodeList.item(ch_len).getNodeName().toUpperCase()))
                      {
                        if (col_name.equalsIgnoreCase(""))
                          {
                            col_name = ch_nodeList.item(ch_len).getNodeName();
                            col_val = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                        else
                          {
                            col_name = col_name + "," + ch_nodeList.item(ch_len).getNodeName();
                            col_val = col_val + ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                      }
                  }
                //WriteLog("insert/update getTagDataParent_financ_header for id: "+id);
                //WriteLog("insert/update getTagDataParent_financ_header cal_name: "+col_name);
                //WriteLog("insert/update getTagDataParent_financ_header col_val: "+col_val);

              }

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in getTagDataParent_financ_header: " + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent_financ_header method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return col_name + ":" + col_val;
      }

    public static Map<Integer, String> getTagDataParent(String parseXml, String tagName, String subTagName)
      {
        Map<Integer, String> tagValuesMap = new LinkedHashMap<Integer, String>();
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent jsp: subTagName: "+subTagName);
            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(tagName);

            String[] values = subTagName.split(",");
            String value = "";
            String subTagDerivedvalue = "";
            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {
                    Element eElement = (Element) nNode;
                    Node uNode = eElement.getParentNode();

                    for (int j = 0; j < values.length; j++)
                      {
                        if (eElement.getElementsByTagName(values[j]).item(0) != null)
                          {
                            value = value + "," + eElement.getElementsByTagName(values[j]).item(0).getTextContent();
                            subTagDerivedvalue = subTagDerivedvalue + "," + values[j];
                          }

                      }
                    value = value.substring(1, value.length());
                    subTagDerivedvalue = subTagDerivedvalue.substring(1, subTagDerivedvalue.length());

                    Node nNode_c = doc.getElementsByTagName(uNode.getNodeName()).item(temp);
                    Element eElement_agg = (Element) nNode_c;
                    String id_val = "";
                    if (uNode.getNodeName().equalsIgnoreCase("LoanDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("AgreementId").item(0).getTextContent();
                      }
                    else if (uNode.getNodeName().equalsIgnoreCase("CardDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("CardEmbossNum").item(0).getTextContent();
                      }
                    else if (uNode.getNodeName().equalsIgnoreCase("AcctDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("AcctId").item(0).getTextContent();
                      }
                    else
                      {
                        id_val = "";
                      }

                    tagValuesMap.put(temp + 1, subTagDerivedvalue + "~" + value + "~" + uNode.getNodeName() + "~" + id_val);
                    value = "";
                    subTagDerivedvalue = "";
                  }
              }

          }
        catch (Exception e)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in getTagDataParent" + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return tagValuesMap;
      }

    public static String nodeToString(Node node)
      {
        StringWriter sw = new StringWriter();
        try
          {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
          }
        catch (TransformerException te)
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("nodeToString Transformer Exception");
          }
        return sw.toString();
      }

    protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag) throws IOException, Exception
      {
        //ConnectionLogger.debug("In WF NG Execute : " + serverPort);
        try
          {
            if (serverPort.startsWith("33"))
              return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
            else
              return ngEjbClientConnection.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
          }
        catch (Exception e)
          {
            //ConnectionLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
            e.printStackTrace();
            return "Error";
          }
      }
  }
