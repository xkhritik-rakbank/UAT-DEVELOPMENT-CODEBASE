package com.newgen.DCC.DECTECHIntegration;

public class TestMQService {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sample_request = "<APMQPUTGET_Input>"+"\n"
				+"		<SessionId>192843758</SessionId>                                                                                                     "+"\n"
				+"		<EngineName>rakcas</EngineName>                                                                                                      "+"\n"
				+"		<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>                                                                    "+"\n"
				+"		<WI_NAME>CC-000000000030004008-process</WI_NAME>                                                                                     "+"\n"
				+"		<WS_NAME>CAD_Analyst1</WS_NAME>                                                                                                      "+"\n"
				+"		<USER_NAME>BATCA6</USER_NAME>                                                                                                        "+"\n"
				+"		<MQ_REQUEST_XML>                                                                                                                     "+"\n"
				+"			<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>"
				+"				<soap:Header>                                                                                                                "+"\n"
				+"					<ServiceId>CallProcessManager</ServiceId>                                                                                "+"\n"
				+"					<ServiceType>ProductEligibility</ServiceType>                                                                            "+"\n"
				+"					<ServiceProviderId>DECTECH</ServiceProviderId>                                                                           "+"\n"
				+"					<ServiceChannelId>CAS</ServiceChannelId>                                                                                 "+"\n"
				+"					<RequestID>CASTEST</RequestID>                                                                                           "+"\n"
				+"					<TimeStampyyyymmddhhmmsss>2010-10-10T10:10:00.000</TimeStampyyyymmddhhmmsss>                                             "+"\n"
				+"					<RequestLifeCycleStage>CallProcessManagerRequest</RequestLifeCycleStage>                                                 "+"\n"
				+"					<MessageStatus>Success</MessageStatus>                                                                                   "+"\n"
				+"				</soap:Header>                                                                                                               "+"\n"
				+"				<soap:Body>                                                                                                                  "+"\n"
				+"					<CallProcessManager xmlns='http://tempuri.org/'>                                                                         "+"\n"
				+"						<applicationXML><![CDATA["
				+"<ProcessManagerRequest>                                                                               "+"\n"+
			"	<Application>                                                                                      "+"\n"+
			"		<Channel>DCC</Channel>                                                                          "+"\n"+
			"		<CallType>PM</CallType>                                                                        "+"\n"+
			"		<ApplicationNumber>Str_ApplicationNumber</ApplicationNumber>                           "+"\n"+
			"	</Application>                                                                                     "+"\n"+
			"	<ApplicationDetails>                                                                               "+"\n"+
			"		<full_eligibility_availed>Select</full_eligibility_availed>                                    "+"\n"+
			"		<product_type>CON</product_type>                                                               "+"\n"+
			"		<app_category>BAU</app_category>                                                               "+"\n"+
			"		<requested_product>DCC</requested_product>                                                      "+"\n"+
			"		<requested_limit>5000</requested_limit>                                                        "+"\n"+
			"		<sub_product>BTC</sub_product>                                                                 "+"\n"+
			"		<requested_card_product>SME-TITANIUM-SEC</requested_card_product>                              "+"\n"+
			"		<application_type>NTB</application_type>                                                       "+"\n"+
			"		<interest_rate>0.00</interest_rate>                                                            "+"\n"+
			"		<customer_type>Existing</customer_type>                                                        "+"\n"+
			"		<final_limit>25000</final_limit>                                                               "+"\n"+
			"		<emi>0.0</emi>                                                                                 "+"\n"+
			"		<manual_deviation>N</manual_deviation>                                                         "+"\n"+
			"		<application_date>2022-05-30T13:26:04</application_date>                                       "+"\n"+
			"	</ApplicationDetails>                                                                              "+"\n"+
			"	<ApplicantDetails>                                                                                 "+"\n"+
			"		<applicant_id>2909099</applicant_id>                                                           "+"\n"+
			"		<primary_cif>2909099</primary_cif>                                                             "+"\n"+
			"		<ref_no>CAS-0000073617-PROCESS</ref_no>                                                        "+"\n"+
			"		<wi_name>CC-000000000030004008-process</wi_name>                                               "+"\n"+
			"		<cust_name>VARUN GANESH</cust_name>                                                            "+"\n"+
			"		<emp_type>SE</emp_type>                                                                        "+"\n"+
			"		<dob>1988-01-01</dob>                                                                          "+"\n"+
			"		<age>34.04</age>                                                                               "+"\n"+
			"		<dbr>0.00</dbr>                                                                                "+"\n"+
			"		<tai>0.00</tai>                                                                                "+"\n"+
			"		<nationality>AE</nationality>                                                                  "+"\n"+
			"		<resident_flag>Y</resident_flag>                                                               "+"\n"+
			"		<world_check>Negative</world_check>                                                            "+"\n"+
			"		<blacklist_cust_type>I</blacklist_cust_type>                                                   "+"\n"+
			"		<negative_cust_type>I</negative_cust_type>                                                     "+"\n"+
			"		<no_of_cheque_bounce_int_3mon_Ind>2</no_of_cheque_bounce_int_3mon_Ind>                         "+"\n"+
			"		<no_of_DDS_return_int_3mon_Ind>0</no_of_DDS_return_int_3mon_Ind>                               "+"\n"+
			"		<lob>2.00</lob>                                                                                "+"\n"+
			"		<external_blacklist_flag>I</external_blacklist_flag>                                           "+"\n"+
			"		<target_segment_code>BTCNTB</target_segment_code>                                              "+"\n"+
			"		<avg_credit_turnover_3>66666.67</avg_credit_turnover_3>                                        "+"\n"+
			"		<avg_bal_3>223145.2</avg_bal_3>                                                                "+"\n"+
			"		<current_emp_catogery>CN</current_emp_catogery>                                                "+"\n"+
			"		<year_in_uae>04.00</year_in_uae>                                                               "+"\n"+
			"		<ref_relationship>PAR</ref_relationship>                                                       "+"\n"+
			"		<passport_expiry_date>2025-12-31</passport_expiry_date>                                        "+"\n"+
			"		<gender>M</gender>                                                                             "+"\n"+
			"		<cust_mobile_no>971562909099</cust_mobile_no>                                                  "+"\n"+
			"		<salary_with_rakbank>Y</salary_with_rakbank>                                                   "+"\n"+
			"		<emirates_of_residence>DXB</emirates_of_residence>                                             "+"\n"+
			"		<emp_name>SHAFIKUL PAINTING AND PLASTERING</emp_name>                                          "+"\n"+
			"		<NegatedDetails>                                                                               "+"\n"+
			"			<negative_cust_type>I</negative_cust_type>                                                 "+"\n"+
			"			<internal_negative_flag>N</internal_negative_flag>                                         "+"\n"+
			"		</NegatedDetails>                                                                              "+"\n"+
			"		<NegatedDetails>                                                                               "+"\n"+
			"			<negative_cust_type>C</negative_cust_type>                                                 "+"\n"+
			"			<internal_negative_flag>N</internal_negative_flag>                                         "+"\n"+
			"		</NegatedDetails>                                                                              "+"\n"+
			"		<BlacklistDetails>                                                                             "+"\n"+
			"			<blacklist_cust_type>I</blacklist_cust_type>                                               "+"\n"+
			"			<internal_blacklist>N</internal_blacklist>                                                 "+"\n"+
			"		</BlacklistDetails>                                                                            "+"\n"+
			"		<BlacklistDetails>                                                                             "+"\n"+
			"			<blacklist_cust_type>C</blacklist_cust_type>                                               "+"\n"+
			"			<internal_blacklist>N</internal_blacklist>                                                 "+"\n"+
			"		</BlacklistDetails>                                                                            "+"\n"+
			"		<cust_type>I</cust_type>                                                                       "+"\n"+
			"		<bank_no_borrowing_relation_individual>0</bank_no_borrowing_relation_individual>               "+"\n"+
			"		<bank_no_borrowing_relation_company>0</bank_no_borrowing_relation_company>                     "+"\n"+
			"		<AccountDetails>                                                                               "+"\n"+
			"			<type_of_account>RAK STARTER ACCOUNT</type_of_account>                                     "+"\n"+
			"			<role>Auth Sign.</role>                                                                    "+"\n"+
			"			<account_number>0032898562001</account_number>                                             "+"\n"+
			"			<acct_open_date>2021-04-20</acct_open_date>                                                "+"\n"+
			"			<acct_status>ACTIVE</acct_status>                                                          "+"\n"+
			"			<account_segment>PBD</account_segment>                                                     "+"\n"+
			"			<account_sub_segment>PSL</account_sub_segment>                                             "+"\n"+
			"			<credit_grade_code_individual>P2</credit_grade_code_individual>                            "+"\n"+
			"			<credit_grade_code_company>P2</credit_grade_code_company>                                  "+"\n"+
			"			<cust_type>Auth Sign.</cust_type>                                                          "+"\n"+
			"		</AccountDetails>                                                                              "+"\n"+
			"		<AccountDetails>                                                                               "+"\n"+
			"			<type_of_account>RAK STARTER ACCOUNT</type_of_account>                                     "+"\n"+
			"			<role>Main</role>                                                                          "+"\n"+
			"			<account_number>0032898562001</account_number>                                             "+"\n"+
			"			<acct_open_date>2021-04-20</acct_open_date>                                                "+"\n"+
			"			<acct_status>ACTIVE</acct_status>                                                          "+"\n"+
			"			<account_segment>PBD</account_segment>                                                     "+"\n"+
			"			<account_sub_segment>PSL</account_sub_segment>                                             "+"\n"+
			"			<credit_grade_code_individual>P2</credit_grade_code_individual>                            "+"\n"+
			"			<credit_grade_code_company>P2</credit_grade_code_company>                                  "+"\n"+
			"			<cust_type>Main</cust_type>                                                                "+"\n"+
			"		</AccountDetails>                                                                              "+"\n"+
			"		<industry_sector>SERVICES</industry_sector>                                                    "+"\n"+
			"		<industry_macro>ELECTRONICS</industry_macro>                                                   "+"\n"+
			"		<industry_micro>REPAIRS</industry_micro>                                                       "+"\n"+
			"		<no_bank_other_statement_provided>3</no_bank_other_statement_provided>                         "+"\n"+
			"		<aggregate_exposed>25000.00</aggregate_exposed>                                                "+"\n"+
			"		<bvr>N</bvr>                                                                                   "+"\n"+
			"		<cc_employer_status>CN</cc_employer_status>                                                    "+"\n"+
			"		<pl_employer_status>CN</pl_employer_status>                                                    "+"\n"+
			"		<marketing_code>BAU</marketing_code>                                                           "+"\n"+
			"		<nmf_flag>N</nmf_flag>                                                                         "+"\n"+
			"		<eff_date_estba>2020-06-28</eff_date_estba>                                                    "+"\n"+
			"		<eff_lob>2.05</eff_lob>                                                                        "+"\n"+
			"		<tlc_issue_date>2020-06-28</tlc_issue_date>                                                    "+"\n"+
			"		<no_bank_statement>3</no_bank_statement>                                                       "+"\n"+
			"		<no_of_partners>1</no_of_partners>                                                             "+"\n"+
			"		<standing_instruction>N</standing_instruction>                                                 "+"\n"+
			"		<country_of_residence>AE</country_of_residence>                                                "+"\n"+
			"		<vip_flag>N</vip_flag>                                                                         "+"\n"+
			"		<title>MR.</title>                                                                             "+"\n"+
			"		<gcc_national>Y</gcc_national>                                                                 "+"\n"+
			"		<customer_category>5</customer_category>                                                       "+"\n"+
			"	</ApplicantDetails>                                                                                "+"\n"+
			"	<InternalBureauData>                                                                               "+"\n"+
			"		<InternalBureau>                                                                               "+"\n"+
			"			<applicant_id>2909099</applicant_id>                                                       "+"\n"+
			"			<full_name>VARUN GANESH</full_name>                                                        "+"\n"+
			"			<total_out_bal>0</total_out_bal>                                                           "+"\n"+
			"			<total_overdue>0</total_overdue>                                                           "+"\n"+
			"			<cheque_return_3mon>0</cheque_return_3mon>                                                 "+"\n"+
			"			<dds_return_3mon>0</dds_return_3mon>                                                       "+"\n"+
			"			<cheque_return_6mon>1</cheque_return_6mon>                                                 "+"\n"+
			"			<dds_return_6mon>0</dds_return_6mon>                                                       "+"\n"+
			"			<internal_charge_off>N</internal_charge_off>                                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"		</InternalBureau>                                                                              "+"\n"+
			"		<InternalBouncedCheques>                                                                       "+"\n"+
			"			<applicant_id>2898562</applicant_id>                                                       "+"\n"+
			"			<internal_bounced_cheques_id>0032898562001</internal_bounced_cheques_id>                   "+"\n"+
			"			<bounced_cheque>ICCS</bounced_cheque>                                                      "+"\n"+
			"			<cheque_no>000007</cheque_no>                                                              "+"\n"+
			"			<amount>7350</amount>                                                                      "+"\n"+
			"			<reason>U</reason>                                                                         "+"\n"+
			"			<return_date>2021-12-04</return_date>                                                      "+"\n"+
			"			<bounced_cheque_dds>ICCS</bounced_cheque_dds>                                              "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"		</InternalBouncedCheques>                                                                      "+"\n"+
			"		<InternalBouncedCheques>                                                                       "+"\n"+
			"			<applicant_id>2898562</applicant_id>                                                       "+"\n"+
			"			<internal_bounced_cheques_id>0032898562001</internal_bounced_cheques_id>                   "+"\n"+
			"			<bounced_cheque>ICCS</bounced_cheque>                                                      "+"\n"+
			"			<cheque_no>000008</cheque_no>                                                              "+"\n"+
			"			<amount>7350</amount>                                                                      "+"\n"+
			"			<reason>K</reason>                                                                         "+"\n"+
			"			<return_date>2022-01-06</return_date>                                                      "+"\n"+
			"			<bounced_cheque_dds>ICCS</bounced_cheque_dds>                                              "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"		</InternalBouncedCheques>                                                                      "+"\n"+
			"		<InternalBureauDBRTAICalc>                                                                     "+"\n"+
			"			<accomodation_provided>N</accomodation_provided>                                           "+"\n"+
			"			<net_salary_3mon_ave>0.0</net_salary_3mon_ave>                                             "+"\n"+
			"			<salary_flag>Y</salary_flag>                                                               "+"\n"+
			"			<is_tenancy_contract_custname>Select</is_tenancy_contract_custname>                        "+"\n"+
			"		</InternalBureauDBRTAICalc>                                                                    "+"\n"+
			"	</InternalBureauData>                                                                              "+"\n"+
			"	<ExternalBureauData>                                                                               "+"\n"+
			"		<ExternalBureau>                                                                               "+"\n"+
			"			<applicant_id>2909099</applicant_id>                                                       "+"\n"+
			"			<bureauone_ref_no>394589</bureauone_ref_no>                                                "+"\n"+
			"			<full_name>SHAFIKUL PAINTING AND PLASTERING</full_name>                                    "+"\n"+
			"			<total_out_bal>2627.00</total_out_bal>                                                     "+"\n"+
			"			<total_overdue>390.00</total_overdue>                                                      "+"\n"+
			"			<no_default_contract>0</no_default_contract>                                               "+"\n"+
			"			<total_exposure>0</total_exposure>                                                         "+"\n"+
			"			<worst_curr_pay>0</worst_curr_pay>                                                         "+"\n"+
			"			<worst_curr_pay_24>0</worst_curr_pay_24>                                                   "+"\n"+
			"			<worst_status_24>U</worst_status_24>                                                       "+"\n"+
			"			<no_of_rec>2</no_of_rec>                                                                   "+"\n"+
			"			<cheque_return_3mon>0</cheque_return_3mon>                                                 "+"\n"+
			"			<dds_return_3mon>0</dds_return_3mon>                                                       "+"\n"+
			"			<no_months_aecb_history>0</no_months_aecb_history>                                         "+"\n"+
			"			<aecb_score>710</aecb_score>                                                               "+"\n"+
			"			<range>4</range>                                                                           "+"\n"+
			"			<AECB_Enquiry_date>10-05-2022</AECB_Enquiry_date>                                          "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<dispute_alert>N</dispute_alert>                                                           "+"\n"+
			"		</ExternalBureau>                                                                              "+"\n"+
			"		<ExternalBureau>                                                                               "+"\n"+
			"			<applicant_id>2909099</applicant_id>                                                       "+"\n"+
			"			<bureauone_ref_no>394589</bureauone_ref_no>                                                "+"\n"+
			"			<full_name>VARUN GANESH</full_name>                                                        "+"\n"+
			"			<total_out_bal>0</total_out_bal>                                                           "+"\n"+
			"			<total_overdue>0.00</total_overdue>                                                        "+"\n"+
			"			<no_default_contract>0</no_default_contract>                                               "+"\n"+
			"			<total_exposure>0</total_exposure>                                                         "+"\n"+
			"			<worst_curr_pay>0</worst_curr_pay>                                                         "+"\n"+
			"			<worst_curr_pay_24>0</worst_curr_pay_24>                                                   "+"\n"+
			"			<no_of_rec>0</no_of_rec>                                                                   "+"\n"+
			"			<cheque_return_3mon>0</cheque_return_3mon>                                                 "+"\n"+
			"			<dds_return_3mon>0</dds_return_3mon>                                                       "+"\n"+
			"			<no_months_aecb_history>0</no_months_aecb_history>                                         "+"\n"+
			"			<aecb_score>710</aecb_score>                                                               "+"\n"+
			"			<range>4</range>                                                                           "+"\n"+
			"			<AECB_Enquiry_date>30-05-2022</AECB_Enquiry_date>                                          "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<dispute_alert>N</dispute_alert>                                                           "+"\n"+
			"		</ExternalBureau>                                                                              "+"\n"+
			"		<CourtCase>                                                                                    "+"\n"+
			"			<CodOrganization>RAK Courts</CodOrganization>                                              "+"\n"+
			"			<ProviderCaseNo>600000020594-9000507331-1000290361</ProviderCaseNo>                        "+"\n"+
			"			<ReferenceDate>30062020</ReferenceDate>                                                    "+"\n"+
			"			<CaseCategoryCode>1</CaseCategoryCode>                                                     "+"\n"+
			"			<OpenDate>29062020</OpenDate>                                                              "+"\n"+
			"			<CaseStatusCode>55</CaseStatusCode>                                                        "+"\n"+
			"			<InitialTotalClaimAmount>461826.10</InitialTotalClaimAmount>                               "+"\n"+
			"		</CourtCase>                                                                                   "+"\n"+
			"		<ExternalBureauIndividualProducts>                                                             "+"\n"+
			"			<applicant_id>2898562</applicant_id>                                                       "+"\n"+
			"			<external_bureau_individual_products_id>F05506631</external_bureau_individual_products_id> "+"\n"+
			"			<contract_type>85</contract_type>                                                          "+"\n"+
			"			<provider_no>T02</provider_no>                                                             "+"\n"+
			"			<phase>A</phase>                                                                           "+"\n"+
			"			<role_of_customer>A</role_of_customer>                                                     "+"\n"+
			"			<start_date>2019-08-01</start_date>                                                        "+"\n"+
			"			<worst_status>U</worst_status>                                                             "+"\n"+
			"			<worst_status_date>2020-05-31</worst_status_date>                                          "+"\n"+
			"			<no_of_days_payment_delay>60</no_of_days_payment_delay>                                    "+"\n"+
			"			<mob>33.00</mob>                                                                           "+"\n"+
			"			<currently_current>N</currently_current>                                                   "+"\n"+
			"			<current_utilization>0</current_utilization>                                               "+"\n"+
			"			<dpd_60p_in_last_12_mon>0</dpd_60p_in_last_12_mon>                                         "+"\n"+
			"			<dpd_5_in_last_12_mon>0</dpd_5_in_last_12_mon>                                             "+"\n"+
			"			<no_months_aecb_history>0</no_months_aecb_history>                                         "+"\n"+
			"			<delinquent_in_last_3months>0</delinquent_in_last_3months>                                 "+"\n"+
			"			<company_flag>Y</company_flag>                                                             "+"\n"+
			"			<consider_for_obligation>Y</consider_for_obligation>                                       "+"\n"+
			"			<duplicate_flag>N</duplicate_flag>                                                         "+"\n"+
			"		</ExternalBureauIndividualProducts>                                                            "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>005504750</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>105</ppl_type_of_contract>                                           "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-01-11</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_total_amount>1000000</ppl_total_amount>                                               "+"\n"+
			"			<ppl_no_of_instalments>48</ppl_no_of_instalments>                                          "+"\n"+
			"			<ppl_no_of_days_in_pipeline>484</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>405504801</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>105</ppl_type_of_contract>                                           "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-01-11</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_total_amount>1000000</ppl_total_amount>                                               "+"\n"+
			"			<ppl_no_of_instalments>48</ppl_no_of_instalments>                                          "+"\n"+
			"			<ppl_no_of_days_in_pipeline>484</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>A05507638</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>00</ppl_type_of_contract>                                            "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-01-12</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_no_of_instalments>0</ppl_no_of_instalments>                                           "+"\n"+
			"			<ppl_credit_limit>9000</ppl_credit_limit>                                                  "+"\n"+
			"			<ppl_no_of_days_in_pipeline>483</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>A05508679</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>00</ppl_type_of_contract>                                            "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-04-15</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_no_of_instalments>0</ppl_no_of_instalments>                                           "+"\n"+
			"			<ppl_credit_limit>5000</ppl_credit_limit>                                                  "+"\n"+
			"			<ppl_no_of_days_in_pipeline>390</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>C05508890</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>00</ppl_type_of_contract>                                            "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-04-15</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_no_of_instalments>0</ppl_no_of_instalments>                                           "+"\n"+
			"			<ppl_credit_limit>5000</ppl_credit_limit>                                                  "+"\n"+
			"			<ppl_no_of_days_in_pipeline>390</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>J05509221</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>00</ppl_type_of_contract>                                            "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-04-28</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_no_of_instalments>0</ppl_no_of_instalments>                                           "+"\n"+
			"			<ppl_credit_limit>15000</ppl_credit_limit>                                                 "+"\n"+
			"			<ppl_no_of_days_in_pipeline>377</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"		<ExternalBureauPipelineProducts>                                                               "+"\n"+
			"			<applicant_ID>2909099</applicant_ID>                                                       "+"\n"+
			"			<external_bureau_pipeline_products_id>N05508537</external_bureau_pipeline_products_id>     "+"\n"+
			"			<ppl_provider_no>B01</ppl_provider_no>                                                     "+"\n"+
			"			<ppl_type_of_contract>00</ppl_type_of_contract>                                            "+"\n"+
			"			<ppl_type_of_product>Requested</ppl_type_of_product>                                       "+"\n"+
			"			<ppl_phase>PIPELINE</ppl_phase>                                                            "+"\n"+
			"			<ppl_role>A</ppl_role>                                                                     "+"\n"+
			"			<ppl_date_of_last_update>2021-01-31</ppl_date_of_last_update>                              "+"\n"+
			"			<ppl_no_of_instalments>0</ppl_no_of_instalments>                                           "+"\n"+
			"			<ppl_credit_limit>9000</ppl_credit_limit>                                                  "+"\n"+
			"			<ppl_no_of_days_in_pipeline>464</ppl_no_of_days_in_pipeline>                               "+"\n"+
			"			<company_flag>N</company_flag>                                                             "+"\n"+
			"			<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>                               "+"\n"+
			"			<ppl_duplicate_flag>N</ppl_duplicate_flag>                                                 "+"\n"+
			"		</ExternalBureauPipelineProducts>                                                              "+"\n"+
			"	</ExternalBureauData>                                                                              "+"\n"+
			"</ProcessManagerRequest>                                                                              "
			
			+ "]]></applicationXML>                                                                        "+"\n"
			+"					</CallProcessManager>                                                                                                    "+"\n"
			+"				</soap:Body>                                                                                                                 "+"\n"
			+"			</soap:Envelope>                                                                                                                 "+"\n"
			+"		</MQ_REQUEST_XML>                                                                                                                    "+"\n"
			+"	</APMQPUTGET_Input>                                                                                                                      ";
	}

	
	public static String inputxml = 
		"<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>"+"\n"
			+"<soap:Header>"+"\n"
				+"<ServiceId>CallProcessManager</ServiceId>"+"\n"
				+"<ServiceType>ProductEligibility</ServiceType>"+"\n"
				+"<ServiceProviderId>DECTECH</ServiceProviderId>"+"\n"
				+"<ServiceChannelId>CAS</ServiceChannelId>"+"\n"
				+"<RequestID>CASTEST</RequestID>"+"\n"
				+"<TimeStampyyyymmddhhmmsss>2010-10-10T10:10:00.000</TimeStampyyyymmddhhmmsss>"+"\n"
				+"<RequestLifeCycleStage>CallProcessManagerRequest</RequestLifeCycleStage>"+"\n"
				+"<MessageStatus>Success</MessageStatus>"+"\n"
			+"</soap:Header>"+"\n"
			+"<soap:Body>"+"\n"
				+"<CallProcessManager xmlns='http://tempuri.org/'>"+"\n"
					+"<applicationXML><![CDATA[<ProcessManagerRequest><Application><Channel>DCC</Channel><CallType>PM</CallType><ApplicationNumber>DCC-0000000049-process</ApplicationNumber></Application><ApplicationDetails><full_eligibility_availed>Select</full_eligibility_availed><product_type>CON</product_type><app_category>BAU</app_category><requested_product>CC</requested_product><requested_limit>5000</requested_limit><sub_product>SAL</sub_product><requested_card_product>MCBWE-UAE</requested_card_product><application_type>NEWE</application_type><interest_rate>0.00</interest_rate><customer_type>NTB</customer_type><final_limit>25000</final_limit><emi>0.0</emi><manual_deviation>N</manual_deviation><application_date>2022-06-28T14:49:08</application_date></ApplicationDetails><ApplicantDetails><applicant_id>2967485</applicant_id><ref_no>DCC-0000000049-PROCESS</ref_no><wi_name>DCC-0000000049-process</wi_name><cust_name>YASMIN YACOUB</cust_name><emp_type>S</emp_type><dob>1989-06-30</dob><age>32.11</age><dbr>4.00</dbr><tai>25000.00</tai><nationality>AE</nationality><resident_flag>Y</resident_flag><world_check>Negative</world_check><blacklist_cust_type>I</blacklist_cust_type><negative_cust_type>I</negative_cust_type><no_of_cheque_bounce_int_3mon_Ind>0</no_of_cheque_bounce_int_3mon_Ind><no_of_DDS_return_int_3mon_Ind>0</no_of_DDS_return_int_3mon_Ind><external_blacklist_flag>I</external_blacklist_flag><los>3.03</los><confirmed_in_job>N</confirmed_in_job><target_segment_code>ALOC</target_segment_code><current_emp_catogery>CA</current_emp_catogery><year_in_uae>04.00</year_in_uae><ref_relationship>Friend</ref_relationship><passport_expiry_date>2030-08-02</passport_expiry_date><emirates_work>DXB</emirates_work><gender>F</gender><cust_mobile_no>00971540580922</cust_mobile_no><salary_with_rakbank>Y</salary_with_rakbank><designation>ACCT</designation><emirates_of_residence>DXB</emirates_of_residence><emp_name>MINISTRY OF EDUCATION</emp_name><cust_type>I</cust_type><industry_sector>SERVICES</industry_sector><industry_macro>EDUCATIONAL</industry_macro><aggregate_exposed>25000.00</aggregate_exposed><cc_employer_status>LO</cc_employer_status><pl_employer_status>OP</pl_employer_status><included_pl_aloc>Y</included_pl_aloc><included_cc_aloc>Y</included_cc_aloc><los_prev>00.00</los_prev><marketing_code>BAU</marketing_code><payroll_flag>N</payroll_flag><nmf_flag>N</nmf_flag><visa_sponsor>Binacle</visa_sponsor><no_of_partners>0</no_of_partners><standing_instruction>N</standing_instruction><country_of_residence>AE</country_of_residence><vip_flag>N</vip_flag><title>MR.</title><gcc_national>Y</gcc_national><customer_category>5</customer_category><employer_type>P</employer_type></ApplicantDetails><InternalBureauData><InternalBureau><applicant_id>2967485</applicant_id><full_name>YASMIN YACOUB</full_name><total_out_bal>0</total_out_bal><total_overdue>0</total_overdue><cheque_return_3mon>0</cheque_return_3mon><dds_return_3mon>0</dds_return_3mon><cheque_return_6mon>0</cheque_return_6mon><dds_return_6mon>0</dds_return_6mon><internal_charge_off>N</internal_charge_off><company_flag>N</company_flag></InternalBureau><InternalBureauDBRTAICalc><basic>25000.00</basic><gross_salary>25000.00</gross_salary><accomodation_provided>N</accomodation_provided><net_salary_mon1>25000.00</net_salary_mon1><net_salary_mon2>25000.00</net_salary_mon2><net_salary_mon3>25000.00</net_salary_mon3><net_salary_3mon_ave>25000.00</net_salary_3mon_ave><salary_flag>Y</salary_flag><is_tenancy_contract_custname>Select</is_tenancy_contract_custname></InternalBureauDBRTAICalc></InternalBureauData><ExternalBureauData><ExternalBureau><applicant_id>2967485</applicant_id><bureauone_ref_no>395275</bureauone_ref_no><full_name>YASMINE YACOUB</full_name><total_out_bal>0</total_out_bal><total_overdue>0.00</total_overdue><no_default_contract>0</no_default_contract><total_exposure>0</total_exposure><worst_curr_pay>0</worst_curr_pay><worst_curr_pay_24>0</worst_curr_pay_24><no_of_rec>11</no_of_rec><cheque_return_3mon>0</cheque_return_3mon><dds_return_3mon>0</dds_return_3mon><no_months_aecb_history>0</no_months_aecb_history><aecb_score>586</aecb_score><range>A2</range><AECB_Enquiry_date>16-06-2022</AECB_Enquiry_date><company_flag>N</company_flag><dispute_alert>N</dispute_alert></ExternalBureau><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>8805477899021999</cheque_no><amount>30000</amount><reason>Insufficient Funds</reason><return_date>2021-01-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>8805000086602</cheque_no><amount>10000</amount><reason>Insufficient Funds</reason><return_date>2021-05-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>19968555586</cheque_no><amount>15000</amount><reason>Insufficient Funds</reason><return_date>2021-01-01</return_date><provider_no>B02</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>12300000045222200</cheque_no><amount>10000</amount><reason>Insufficient Funds</reason><return_date>2021-05-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>880547789902256123</cheque_no><amount>30000</amount><reason>Insufficient Funds</reason><return_date>2021-01-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>10000044833</cheque_no><amount>5000</amount><reason>Insufficient Funds</reason><return_date>2020-01-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>660547789900660000</cheque_no><amount>20000</amount><reason>Insufficient Funds</reason><return_date>2021-01-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><ExternalBouncedCheques><applicant_id>2967485</applicant_id><bounced_cheque>Bounced Cheques</bounced_cheque><cheque_no>8805477899004785560</cheque_no><amount>30000</amount><reason>Insufficient Funds</reason><return_date>2021-01-01</return_date><provider_no>C12</provider_no><company_flag>N</company_flag></ExternalBouncedCheques><Utilization24months><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>11-2020</Month><OutstandingBalance>10395343</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>10-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>09-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>08-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>07-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>06-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>05-2020</Month><OutstandingBalance>16449945</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>04-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>03-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>02-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>01-2020</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>12-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>11-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>10-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>09-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>08-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>07-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>06-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>05-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>04-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>03-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>02-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>01-2019</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization><Month_Utilization><CB_application_id>C05506962</CB_application_id><Month>12-2018</Month><OutstandingBalance>0</OutstandingBalance></Month_Utilization></Utilization24months><History_24months><History><CB_application_id>C05506962</CB_application_id><monthyear>11-2020</monthyear><Status>120</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>10-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>09-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>08-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>07-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>06-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>05-2020</monthyear><Status>5</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>04-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>03-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>02-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>01-2020</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>12-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>11-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>10-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>09-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>08-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>07-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>06-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>05-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>04-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>03-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>02-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>01-2019</monthyear><Status>N/A</Status></History><History><CB_application_id>C05506962</CB_application_id><monthyear>12-2018</monthyear><Status>N/A</Status></History></History_24months><CourtCase><CodOrganization>DXB Courts</CodOrganization><ProviderCaseNo>PC200122_02</ProviderCaseNo><ReferenceDate>30102020</ReferenceDate><CaseCategoryCode>2</CaseCategoryCode><OpenDate>22062020</OpenDate><CloseDate>20122020</CloseDate><CaseStatusCode>90</CaseStatusCode><InitialTotalClaimAmount>30000</InitialTotalClaimAmount></CourtCase><ExternalBureauIndividualProducts><applicant_id>2967485</applicant_id><external_bureau_individual_products_id>C05506962</external_bureau_individual_products_id><contract_type>40</contract_type><provider_no>B02</provider_no><phase>C</phase><role_of_customer>G</role_of_customer><start_date>2019-03-05</start_date><close_date>2020-11-30</close_date><outstanding_balance>10395343</outstanding_balance><total_amount>183904</total_amount><payments_amount>10217</payments_amount><total_no_of_instalments>18</total_no_of_instalments><no_of_remaining_instalments>0</no_of_remaining_instalments><worst_status>E</worst_status><worst_status_date>2020-11-30</worst_status_date><overdue_amount>1163181</overdue_amount><no_of_days_payment_delay>120</no_of_days_payment_delay><mob>39.00</mob><last_repayment_date>2020-11-01</last_repayment_date><currently_current>N</currently_current><dpd_30_last_6_mon>0</dpd_30_last_6_mon><dpd_60p_in_last_12_mon>0</dpd_60p_in_last_12_mon><dpd_5_in_last_12_mon>0</dpd_5_in_last_12_mon><maximum_overdue_amount>1070947</maximum_overdue_amount><delinquent_in_last_3months>0</delinquent_in_last_3months><company_flag>N</company_flag><take_over_indicator>N</take_over_indicator><consider_for_obligation>N</consider_for_obligation><duplicate_flag>N</duplicate_flag><payment_frequency>monthly instalments-30 days</payment_frequency><maximum_overdue_date>31/05/2020</maximum_overdue_date></ExternalBureauIndividualProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>205506644</external_bureau_pipeline_products_id><ppl_provider_no>B02</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>G</ppl_role><ppl_date_of_last_update>2021-06-28</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>5000</ppl_credit_limit><ppl_no_of_days_in_pipeline>353</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>305514798</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-06-01</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>5000</ppl_credit_limit><ppl_no_of_days_in_pipeline>15</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>605515535</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-06-01</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>5000</ppl_credit_limit><ppl_no_of_days_in_pipeline>15</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>805510365</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-21</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>146</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>905515329</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>22</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>G</ppl_role><ppl_date_of_last_update>2022-06-15</ppl_date_of_last_update><ppl_total_amount>500000</ppl_total_amount><ppl_no_of_instalments>24</ppl_no_of_instalments><ppl_no_of_days_in_pipeline>1</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>A05518075</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-06-01</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>5000</ppl_credit_limit><ppl_no_of_days_in_pipeline>15</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>C05513279</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Not Taken Up</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-02-11</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>125</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>E05517317</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-06-01</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>5000</ppl_credit_limit><ppl_no_of_days_in_pipeline>15</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>H05517534</external_bureau_pipeline_products_id><ppl_provider_no>B01</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-05-31</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>9000</ppl_credit_limit><ppl_no_of_days_in_pipeline>16</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>I05513368</external_bureau_pipeline_products_id><ppl_provider_no>B02</ppl_provider_no><ppl_type_of_contract>00</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-04</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1000</ppl_credit_limit><ppl_no_of_days_in_pipeline>163</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>I05513379</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Rejected</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-06</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>161</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>L05513305</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Not Taken Up</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-02-11</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>125</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>L05513315</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-07</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>15000</ppl_credit_limit><ppl_no_of_days_in_pipeline>160</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>M05513991</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Not Taken Up</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-06</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>161</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>P05513664</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-06</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>1</ppl_credit_limit><ppl_no_of_days_in_pipeline>161</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts><ExternalBureauPipelineProducts><applicant_ID>2967485</applicant_ID><external_bureau_pipeline_products_id>P05513692</external_bureau_pipeline_products_id><ppl_provider_no>B09</ppl_provider_no><ppl_type_of_contract>01</ppl_type_of_contract><ppl_type_of_product>Requested</ppl_type_of_product><ppl_phase>PIPELINE</ppl_phase><ppl_role>A</ppl_role><ppl_date_of_last_update>2022-01-07</ppl_date_of_last_update><ppl_no_of_instalments>0</ppl_no_of_instalments><ppl_credit_limit>30000</ppl_credit_limit><ppl_no_of_days_in_pipeline>160</ppl_no_of_days_in_pipeline><company_flag>N</company_flag><ppl_consider_for_obligation>Y</ppl_consider_for_obligation><ppl_duplicate_flag>N</ppl_duplicate_flag></ExternalBureauPipelineProducts></ExternalBureauData></ProcessManagerRequest>]]></applicationXML>" + "\n"
				+"</CallProcessManager>"+"\n"
			+"</soap:Body>"+"\n"
		+"</soap:Envelope>"+"\n";

}
