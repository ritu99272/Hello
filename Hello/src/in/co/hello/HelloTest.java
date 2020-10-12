package in.co.hello;

public class HelloTest {
	
	public static void main(String[] args) {
		System.out.println("Testing Git");
	}

}
package com.sbi.lcpc.extracts;

/*     */import java.io.File;
/*     */
import java.io.FileInputStream;
/*     */
import java.io.FileNotFoundException;
/*     */
import java.io.IOException;
/*     */
import java.sql.Connection;
/*     */
import java.sql.SQLException;
/*     */
import java.util.ArrayList;
/*     */
import java.util.Date;
/*     */
import java.util.Properties;
/*     */
import java.util.StringTokenizer;
/*     */
import java.util.Vector;

import com.sbi.lcpc.extracts.manager.AccountOpeningExtractManager;
import com.tcs.infrastructure.extractutil.CSVExtract;
import com.tcs.infrastructure.extractutil.DBUtils;
import com.tcs.infrastructure.extractutil.ErrorMessageConstants;
import com.tcs.infrastructure.extractutil.ExtractProperties;
import com.tcs.infrastructure.extractutil.Logger;
import com.tcs.infrastructure.extractutil.MightyWorkflowBean;
import com.tcs.infrastructure.extractutil.SMS_EMAIL_Process;
import com.tcs.workflow.util.BusinessKeyVO;

public class AccountOpeningExtract {

	public static String Exception_Flag = "N";
	public static int Exception_Count = 0;

	// private static int arrLength=0;
	// public static String dtExtractDate="";
	public static void main(String[] args) throws SQLException {
		
		  /*args=new String[3]; args[0]="";*/
		  args[1]="D:\\ahamad\\AccountOpeningEAR\\props\\lcpc.properties";
		  args[2]=".dat";
		 
		String dtExtractDate = "";
		Logger.onError("Inside main of AccountOpeningExtract");
		Logger.createLogger();
		Logger.onError("" + new Date());
		Logger.onError("Starting to upload the extract....");
		Logger.onError("Extract Start Time " + System.currentTimeMillis());
		try {
			// Get number of files to be updated
			int arrLength = CSVExtract.getNoOfFiles(args,
					"LCPC_ACCOUNT_OPENING_EXTRACT_LOC");
			Logger.onError("Number of extract to be updated  = " + arrLength);
			int bigCounter = 0;
			// If no files found, terminate
			if (arrLength <= 0) {
				Logger.onError("NO FILES TO BE PROCESSED..");
				System.out.println("NO FILES TO BE PROCESSED..");
				return;
			}

			String fileName = null;
			String lcpcCode = null;
			CSVExtract extract[] = new CSVExtract[arrLength];
			String[] fileNameArr = null;
			// String fileTemp = null;
			Connection sbicon = null;
			ArrayList branchList = new ArrayList();
			boolean condition = false;
			
			//Start of IR 18060136
			
			boolean conditionGNC = false;
			
			//End of IR 18060136
			
			
			try {
				if (sbicon == null || sbicon.isClosed()) {
					sbicon = DBUtils.getConnection();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				Logger.onError("Exception occured while getting connection "
						+ e);
				Logger.onError("***************  CountCheck_Function dbcONNECTION CATCH DETAIL BEGINS  ********************");
				Exception_Flag = "Y";
				Exception_Count = SMS_EMAIL_Process
						.CountCheck_Function(Exception_Flag);
				Logger.onError("***************  CountCheck_Function dbcONNECTION CATCH ENDS Exception_Count::"
						+ Exception_Count);
			}

			/************** List of Active Branches ****************/
			AccountOpeningExtractManager manager = new AccountOpeningExtractManager();
			ArrayList listOfActBranches = manager
					.getActiveListOfBranches(sbicon);
			Logger.onError("-- > ActivelistOfBranches :"
					+ listOfActBranches.toString());

			/************** For every extract file *************/
			for (int index = 0; index < extract.length; index++) {
				Logger.onError("Start Time of Extract :: " + new Date());
				extract[index] = new CSVExtract(args[1],
						"LCPC_ACCOUNT_OPENING_EXTRACT_LOC",
						CSVExtract.STRING_DELIMITTED, args[2]);
				File extractFile = extract[index].getFileExtract();
				ArrayList file = extract[index].readFile("\\^"); // Pass the
																	// delimiter
				Logger.onError("Inside Branch List :: " + branchList.size()
						+ " Loop count :: " + index);
				// for the second extract branchList is set to null.
				if (0 != branchList.size()) {
					Logger.onError("Inside Branch List 1:: "
							+ branchList.size() + " Loop count 1:: " + index);
					branchList = new ArrayList();
					Logger.onError("Inside Branch List 1:: "
							+ branchList.size() + " Loop count 1:: " + index);
				}

				if (file == null) {
					Logger.onError("Cannot read the file.");
				}
				fileName = extract[index].getFileName();

				try {
					fileNameArr = fileName.split("_");
				} catch (Exception e) {
					Logger.onError(ErrorMessageConstants.ERR00016);
					StringTokenizer st = new StringTokenizer(fileName, "_");
					Vector fileVect = new Vector();
					while (st.hasMoreTokens()) {
						fileVect.add(st.nextToken());
					}
					fileVect.toArray(fileNameArr);
					Logger.onError("Filename = " + fileName);
					Logger.onError("***************  CountCheck_Function FileNameException CATCH DETAIL BEGINS  ********************");
					Exception_Flag = "Y";
					Exception_Count = SMS_EMAIL_Process
							.CountCheck_Function(Exception_Flag);
					Logger.onError("***************  CountCheck_Function FileNameException CATCH ENDS Exception_Count::"
							+ Exception_Count);
				}

				/************* Process the extract *************/
				int exmnPk = 0;
				int counter = 0;
				String hdrStr = "hh";
				String detail = "dd";
				String ftrStr = "ff";
				String dtOfExtr = null;
				String timeofExtr = null;
				String accPrcDate = null;
				boolean success = false;
				Properties recProp = new Properties();

				try {
					String connPropFile = ExtractProperties.getInstance()
							.getExtractProperty("CONN_PROP_FILE");
					recProp.load(new FileInputStream(new File(connPropFile)));
				} catch (FileNotFoundException e) {
					Logger.onError(ErrorMessageConstants.FERR00002 + e);
					Logger.onError(e.getMessage());
					Logger.onError("***************  CountCheck_Function propertyFile FileNotFoundException CATCH DETAIL BEGINS  ********************");
					Exception_Flag = "Y";
					Exception_Count = SMS_EMAIL_Process
							.CountCheck_Function(Exception_Flag);
					Logger.onError("***************  CountCheck_Function propertyFile FileNotFoundException CATCH ENDS Exception_Count::"
							+ Exception_Count);
					return;
				} catch (IOException e) {
					Logger.onError(ErrorMessageConstants.FERR00002 + e);
					Logger.onError(e.getMessage());
					Logger.onError("***************  CountCheck_Function propertyFile IOException CATCH DETAIL BEGINS  ********************");
					Exception_Flag = "Y";
					Exception_Count = SMS_EMAIL_Process
							.CountCheck_Function(Exception_Flag);
					Logger.onError("***************  CountCheck_Function propertyFile IOException CATCH ENDS Exception_Count::"
							+ Exception_Count);
					return;
				}

				String strGenForLtrNo = "";
				String accountType = "";
				String oldAccountType = "";
				ArrayList productCode = new ArrayList();
				// Get the records present in each extract file
				
				//Start of IR 18060136
				
				ArrayList GNCproductCode = new ArrayList();
				
				GNCproductCode = manager.getGNCProductCode(sbicon);
				
				//End of IR 18060136
				
				for (int i = 0; i < file.size(); i++) {
					String[] fields = (String[]) file.get(i);
					Logger.onError("Number of rows : " + fields.length);

					// If header
					if (fields[0].equals(hdrStr) && i == 0) {
						dtOfExtr = fields[4].trim();// modified on 090908
						dtExtractDate = fields[4].trim();// modified on 180908
						Logger.onError("date is :" + dtOfExtr);

						// Check date format
						if (extract[index].checkDateFormat(dtOfExtr)) {
							Logger.onError("Date format is correct can use it ahead. : "
									+ dtOfExtr);
						} else {
							file = null;
							success = false;
							Logger.onError(ErrorMessageConstants.ERR00002
									+ dtOfExtr, extractFile,
									ErrorMessageConstants.FAILEXTN);
							Logger.onError("***************  CountCheck_Function Date formate Exception CATCH DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function Date formate Exception CATCH ENDS Exception_Count::"
									+ Exception_Count);
							break;
						}

						timeofExtr = fields[2].trim();

						// Check time format
						if (extract[index].checkTimeFormat(timeofExtr)) {
							Logger.onError("Time format is correct can use it ahead. : "
									+ timeofExtr);
						} else {
							file = null;
							success = false;
							Logger.onError(ErrorMessageConstants.ERR00003
									+ timeofExtr, extractFile,
									ErrorMessageConstants.FAILEXTN);
							Logger.onError("***************  CountCheck_Function Time formate Exception CATCH DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function Time formate Exception CATCH ENDS Exception_Count::"
									+ Exception_Count);
							break;
						}

						lcpcCode = fields[3].trim();
						if (lcpcCode.equals(null) || lcpcCode.equals("")) {
							file = null;
							success = false;
							Logger.onError("lcpcCode inside else part : "
									+ lcpcCode);
							Logger.onError("***************  CountCheck_Function Lcpc value Exception CATCH DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function Lcpc value Exception CATCH ENDS Exception_Count::"
									+ Exception_Count);
							break;
						} else {
							lcpcCode = fields[3].trim();
							Logger.onError("lcpcCode : " + lcpcCode);
						}

						// Check date format
						accPrcDate = fields[4].trim();

						if (extract[index].checkDateFormat(accPrcDate)) {
							Logger.onError("Date format is correct can use it ahead. : "
									+ dtOfExtr);
						} else {
							file = null;
							success = false;
							Logger.onError(ErrorMessageConstants.ERR00004
									+ accPrcDate, extractFile,
									ErrorMessageConstants.FAILEXTN);
							Logger.onError("***************  CountCheck_Function accountPrecreation date in incorrect DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function accountPrecreation date in incorrect ends Exception_Count::"
									+ Exception_Count);
							break;
						}

						try {
							exmnPk = manager.executeSequence(sbicon);
							Logger.onError(" exmnPk :: " + exmnPk);
							manager.insertIntoExtractMn(exmnPk, fileName,
									dtOfExtr, lcpcCode, sbicon);

						} catch (Exception e) {
							Logger.onError("EXCEPTION IN EXCEUTING SEQUENCE....:"
									+ e);
							Logger.onError("***************  CountCheck_Function exmnPk CATCH DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function exmnPk CATCH ENDS Exception_Count::"
									+ Exception_Count);
						}

					} else if (fields[0].equals(detail)) { // If detail
						// TODO: Loop each record check for branch code and the
						// bulk flag.
						// If Normal Account Opening generate the Forward
						// Sequence Number and initiate the records with that
						// number.
						// If Bulk Accounts generate new Forwarding sequence
						// number and initiate the records with the same number.
						// If TDR/RD/STDR omit the record. by initaing
						bigCounter++;
						String branchCode = fields[1];
						Logger.onError("--> branchCode to be processed:"
								+ branchCode);
						while (branchCode.trim().length() < 5) {
							branchCode += "0" + branchCode;
						}
						Logger.onError("Coming for times = " + bigCounter);
						// Added by 938900
						if (listOfActBranches.contains(branchCode)) {

							Logger.onError("branchCode in arrayList : "
									+ branchCode);
							Logger.onError("field value check of last : "+fields[fields.length - 1].trim());
							if ("B".equalsIgnoreCase(fields[fields.length - 1]
									.trim())) {
								oldAccountType = accountType;
								accountType = "BULK";
							} else {
								oldAccountType = accountType;
								accountType = "";
							}
							Logger.onError("account type is : "+accountType);
							Logger.onError(" Before Condition ");
							productCode = manager
									.getDiscardedProductCode(sbicon);

							for (int m = 0; m < productCode.size(); ++m) {
								if (fields[3].startsWith(productCode.get(m)
										.toString(), 0)) {
									condition = true;
									break;
								}

								condition = false;
							}

							// Start of IR 18060136

							

							boolean mintenanceAvailable = false;

							

							for (int m = 0; m < GNCproductCode.size(); ++m) {
								if (fields[3].equals(GNCproductCode.get(m)
										.toString())) {
									String account_no = fields[2];
									String branch_code = fields[1];
									String product_code = fields[3];
									String customer_name = fields[5];
									String transBranch = fields[12];
									
									mintenanceAvailable = manager
											.insertGNCAccount(account_no,
													branch_code, product_code,
													customer_name, accPrcDate,lcpcCode,transBranch,
													sbicon);
									/*condition = true;*/
									conditionGNC = true;
									break;
								}

								/*condition = false;*/
								conditionGNC = false;
							}

							// End of IR 18060136

							Logger.onError("Product Code is::" + fields[3]);
							Logger.onError("Condition value before next condition"
									+ condition);
							if (!branchList.contains(branchCode)
									&& (!condition)) {
								Logger.onError(" After Condition branchList.contains(branchCode)");
								branchList.add(branchCode);
								Logger.onError(" After Addition to branchList size of branchList :: "
										+ branchList.size());
								if ((condition)
										&& (!"B".equalsIgnoreCase(fields[fields.length - 1]
												.trim()))) {
									continue;
								} else {
									try {
										strGenForLtrNo = generateForwardingLetter(
												branchCode, exmnPk,
												dtExtractDate, accountType,
												sbicon);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										Logger.onError("Exception" + e);
										Logger.onError("***************  CountCheck_Function generateForwardingLetter BEGINS  ********************");
										Exception_Flag = "Y";
										Exception_Count = SMS_EMAIL_Process
												.CountCheck_Function(Exception_Flag);
										Logger.onError("***************  CountCheck_Function generateForwardingLetter ENDS Exception_Count::"
												+ Exception_Count);
									}
								}

								// To be processed for N no of records
								// // Modified by Manu for IR 30834
								// if(processData(exmnPk, fields, lcpcCode,
								// strGenForLtrNo ,sbicon)){
								
								//Start of IR 18060136
								
								/*if (processData(exmnPk, fields, lcpcCode,
										strGenForLtrNo, dtOfExtr, sbicon,
										condition)) {
									Logger.onError(" Record initiated successfully in Workflow ");
								} else {
									Logger.onError(" Unable to initiate the record in Workflow ");
								}*/
								
								if (processData(exmnPk, fields, lcpcCode,
										strGenForLtrNo, dtOfExtr, sbicon,
										condition,conditionGNC)) {
									Logger.onError(" Record initiated successfully in Workflow ");
								} else {
									Logger.onError(" Unable to initiate the record in Workflow ");
								}
								
								//End of IR 18060136
								
								System.out.println("branch code at last : "+fields[12]
												.trim());
								
							} else {
								if ((condition)
										&& (!"B".equalsIgnoreCase(fields[fields.length - 1]
												.trim()))) {
									continue;
								} else {
									if (!accountType
											.equalsIgnoreCase(oldAccountType)) {
										try {
											strGenForLtrNo = generateForwardingLetter(
													branchCode, exmnPk,
													dtExtractDate, accountType,
													sbicon);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
								// To be processed for N no of records
								// // Modified by Manu for IR 30834
								// if(processData(exmnPk, fields, lcpcCode,
								// strGenForLtrNo ,sbicon)){
								
								//Start of IR 18060136
								
								/*if (processData(exmnPk, fields, lcpcCode,
										strGenForLtrNo, dtOfExtr, sbicon,
										condition)) {
									Logger.onError(" Record initiated successfully in Workflow ");
								} else {
									Logger.onError(" Unable to initiate the record in Workflow ");
								}*/
								
								if (processData(exmnPk, fields, lcpcCode,
										strGenForLtrNo, dtOfExtr, sbicon,
										condition,conditionGNC)) {
									Logger.onError(" Record initiated successfully in Workflow ");
								} else {
									Logger.onError(" Unable to initiate the record in Workflow ");
								}
								
								//End of IR 18060136
							}
							Logger.onError("branchCode in arrayList : "
									+ branchList.get(0) + " : "
									+ branchList.get(branchList.size() - 1));
							Logger.onError("Before updating counter , counter : "
									+ (++counter));
						} else {
							Logger.onError("-- >The processing branch code :"
									+ branchCode + " is not active");
							Logger.onError("***************  CountCheck Problem in branch code DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function  branch code format ENDS Exception_Count::"
									+ Exception_Count);
						}
					} else if (fields[0].equals(ftrStr) && i == file.size() - 1) { // if
																					// footer
																					// at
																					// the
																					// end
																					// of
																					// file
						Logger.onError("Inside else **** footer check");
						if (Long.parseLong(fields[1].trim()) == counter) {
							success = true;
							Logger.onError(ErrorMessageConstants.successMsg,
									extractFile, ErrorMessageConstants.SUCCEXTN);
							file = null;
							break;
						} else {
							success = false;
							Logger.onError(ErrorMessageConstants.ERR00015,
									extractFile, ErrorMessageConstants.FAILEXTN);
							file = null;
							Logger.onError("***************  CountCheck Problem with footer count DETAIL BEGINS  ********************");
							Exception_Flag = "Y";
							Exception_Count = SMS_EMAIL_Process
									.CountCheck_Function(Exception_Flag);
							Logger.onError("***************  CountCheck_Function footer count ENDS Exception_Count::"
									+ Exception_Count);
							break;
						}
					} else {
						success = false;
						Logger.onError(ErrorMessageConstants.FERR00001,
								extractFile, ErrorMessageConstants.FAILEXTN);
						file = null;
						Logger.onError("***************  CountCheck Problem with Header or Detail or Footer format DETAIL BEGINS  ********************");
						Exception_Flag = "Y";
						Exception_Count = SMS_EMAIL_Process
								.CountCheck_Function(Exception_Flag);
						Logger.onError("***************  CountCheck_Function  Header or Detail or Footer format ENDS Exception_Count::"
								+ Exception_Count);
						break;
					}

				}
				// Commented for taking letter no generation to start
				/*
				 * if(branchList != null && !(branchList.isEmpty())){ try {
				 * Logger.onError("branchList size : "+branchList.size());
				 * generateForwardingLetter(branchList,exmnPk); } catch
				 * (Exception e) { e.printStackTrace(); } }
				 */
				if (success) {
					/******************** For Rename of the File ******************/
					Logger.onError("Extract updated successfully.");
					Logger.onError(ErrorMessageConstants.successMsg);
				} else {
					/******************** For Rename of the File ******************/
					Logger.onError("Extract updation failed.");
					Logger.onError(ErrorMessageConstants.FERR00002);
				}
				Logger.onError("End Time of Extract :: "
						+ System.currentTimeMillis());
			}

			Logger.onError("Extract Exit Time " + System.currentTimeMillis());// outer
																				// for
																				// loop..
																				// scanning
																				// thru
																				// the
																				// folder.
		} catch (Exception e) {
			e.printStackTrace();
			Logger.onError("Print exception :" + e);
			Logger.onError("***************  CountCheck_Function getDataFromDB DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function getDataFromDB DETAIL ENDS Exception_Count::"
					+ Exception_Count);

		} finally {
			Logger.onError("***************  SMS_EMAIL_Function DETAIL BEGINS  ********************");
			SMS_EMAIL_Process.SMS_EMAIL_Function(Exception_Flag,
					Exception_Count);
			Logger.onError("***************  SMS_EMAIL_Function DETAIL ENDS  ********************");
			// DBUtils.freeConnection(sbicon);
		}
	}

	// Modified by Manu for IR 30834
	// public static boolean processData(int exmnFk, final String[] records,
	// String lcpcCode, String strGenForLtrNo,Connection sbicon){
	
	//Start of IR 18060136
	
	/*	public static boolean processData(int exmnFk, final String[] records,
			String lcpcCode, String strGenForLtrNo, String dtOfExtr,
			Connection sbicon, boolean condition) {*/
	
	public static boolean processData(int exmnFk, final String[] records,
			String lcpcCode, String strGenForLtrNo, String dtOfExtr,
			Connection sbicon, boolean condition, boolean conditionGNC ) {
		
	//End of IR 18060136	

		int accountPk = 0;
		int detailPk = 0;
		boolean status = false;
		boolean check = false;
		boolean insert = false;
		// Start of IR30917
		boolean maintenanceAvailable = false;
		boolean maintenanceInserted = false;
		// End of IR 30917
		String productCode = null;
		String dtextr = null;
		// Connection sbicon = null;
		boolean initFlag;
		AccountOpeningExtractManager manager = new AccountOpeningExtractManager();
		accountPk = manager.executeSequence(sbicon);
		Logger.onError(" accountPk :: " + accountPk + " exmnFk :: " + exmnFk
				+ ", strGenForLtrNo :: " + strGenForLtrNo);
		try {
			String cdmPk = strGenForLtrNo.substring(strGenForLtrNo
					.lastIndexOf("-") + 1);
			Logger.onError("cdmPk" + cdmPk);
			String cifNum1 = records[4].trim();
			String cifNum2 = records[6].trim();
			String cifNum3 = records[7].trim();
			String opnBrchCode = records[records.length-1];
			if (Long.parseLong(cifNum1) == 0) {
				cifNum1 = "";
			}
			if (Long.parseLong(cifNum2) == 0) {
				cifNum2 = "";
			}
			if (Long.parseLong(cifNum3) == 0) {
				cifNum3 = "";
			}

			if ((condition)
					&& (!"B".equalsIgnoreCase(records[records.length - 1]
							.trim()))) {
				return true;
			} else {
				String accountType = "NEW";
				if ("B".equalsIgnoreCase(records[records.length - 1].trim())) {
					productCode = records[3].trim();
					accountType = "BULK";
				} else {
					productCode = records[3].trim();
				}

				if (!records[2].trim().equals("00000000000000000")) {

					String accountNumber = records[2];
					String branchCode = records[1].trim();
					// Start Of IR 30917
					maintenanceAvailable = manager.checkaccountMaintenance(
							accountNumber, sbicon);
					if (maintenanceAvailable) {
						maintenanceInserted = manager.insertMaintenanceAccount(
								accountNumber, sbicon);
						Logger.onError("maintenanceInserted::"
								+ maintenanceInserted);
						// return false;
					} else {
						// End OF IR 30917
						
						//Start of IR 18060136
						
						/*status = manager.insertAccountOpeningMn(accountPk,
								exmnFk, accountNumber, cifNum1, cifNum2,
								cifNum3, branchCode, accountType, lcpcCode,
								cdmPk, sbicon);*/
						
						if(!conditionGNC == true)
						 {
							 status = manager.insertAccountOpeningMn(accountPk,
										exmnFk, accountNumber, cifNum1, cifNum2,
										cifNum3, branchCode, accountType, lcpcCode,
										cdmPk,opnBrchCode, sbicon);

						 }
						
						//End of IR 18060136

						if (status) {
							detailPk = manager.executeSequence(sbicon);
							String customerName = handleSpecialCharacters(records[5]
									.trim());
							// // Modified by Manu for IR 30834
							// check = manager.insertAccountDetailMn(detailPk,
							// accountPk, customerName, productCode , sbicon);
							check = manager.insertAccountDetailMn(detailPk,
									accountPk, customerName, productCode,
									dtOfExtr, sbicon);

							if (check) {
								insert = manager.insertCustomerDetailMn(
										accountNumber, cifNum1, cifNum2,
										cifNum3, customerName, productCode,
										sbicon);
								MightyWorkflowBean BO = new MightyWorkflowBean();
								BO.setAccountPk(accountPk + "");
								String acctNum = removeLeadingZeros(records[2]
										.trim());
								BO.setAccountNumber(acctNum);
								Logger.onError("accountNum : " + acctNum);
								BO.setAccountCategory(accountType);
								BO.setBranchCode(records[1]);
								BO.setProductCode(records[3]);
								// Added by apparao while tuning the code.
								BO.setExtractPk(String.valueOf(exmnFk));
								BO.setCourierPk(String.valueOf(cdmPk));
								BO.setAccountPk(String.valueOf(accountPk));
								BO.setForwardingSequenceNumber(strGenForLtrNo);
								BO.setCpcCode(lcpcCode);
								BO.setBranchName(manager.getBranchName(
										records[1], sbicon));
								// Added to handle customer name in LCPC Account
								// Opening New Process 24-Sept-2009
								BO.setCustomerName(handleSpecialCharacters(records[5]
										.trim()));

								Logger.onError("Branch code before Initiating the workflow : "
										+ records[1]);
								// added by neha

								com.tcs.workflow.util.HandleExtractWorkflow it = new com.tcs.workflow.util.HandleExtractWorkflow();
								com.tcs.workflow.util.WorkflowTransactionVO wt = new com.tcs.workflow.util.WorkflowTransactionVO();
								com.tcs.workflow.util.BusinessKeyVO bk = new com.tcs.workflow.util.BusinessKeyVO();
								// BusinessKeyConvertor bkConv = new
								// BusinessKeyConvertor();
								// BusinessKeyVO businessKeyVO = null;
								bk = ConvertFromLCPCMightyBeanToBusinessVO(BO);
								wt.setBusinessKey(bk);
								wt.setComments("Initiating");
								// wt.setTransactionName(Constants.RACPC_MAINT_SOFTCOL_TXN_NAME);
								wt.setTransactionID("2872");
								// wt.setCompletedTxnStep("RACPC_W01_CM_MT_START");
								wt.setCompletedStepId("3040");
								// wt.setCompletedStepId("111");
								wt.setSourceUserId("1");
								// wt.setSourceRole("ADMIN");
								wt.setSourceRole("1");
								// wt.setPendingSteps(new
								// String[]{"RACPC_W02_CM_MT_CO_ALLOC"});
								wt.setPendingSteps(new String[] { "3043" });
								wt.setTargetUsers(new String[] { null });
								// wt.setClaimableRoles(new
								// String[][]{{"RACPC_Chief_Manager_(Maintenance)"}});
								wt.setClaimableRoles(new String[][] {
										{ "55616" }, { "55615" } });
								wt.setWeightage(new Float(2.0));
								long t1 = System.currentTimeMillis();
								if (sbicon == null || sbicon.isClosed()) {
									sbicon = DBUtils.getConnection();
								}
								long entryTime = System.currentTimeMillis();
								initFlag = it.insertWorkItem(wt, sbicon);
								Logger.onError("Time taken for insertWorkItem"
										+ (System.currentTimeMillis() - entryTime));

								if (initFlag) {
									sbicon.commit();
								}

								long t2 = System.currentTimeMillis();
								Logger.onError("Total time taken for insertWorkItem ::"
										+ (t2 - t1));

							} else {
								Logger.onError("Error in inserting this record : "
										+ records[11].trim());
								Logger.onError("***************  CountCheck_Function insertWorkItem error CATCH DETAIL BEGINS  ********************");
								Exception_Flag = "Y";
								Exception_Count = SMS_EMAIL_Process
										.CountCheck_Function(Exception_Flag);
								Logger.onError("***************  CountCheck_Function insertWorkItem error CATCH ENDS Exception_Count::"
										+ Exception_Count);
								return false;
							}
						} else {
							return false;
						}
						// Start OF IR 30917
					}
					// End OF IR 30917
				} else {
					Logger.onError("Exception: Received invalid account no: 00000000000000000");
					Logger.onError("***************  CountCheck_Function AccountNumber error CATCH DETAIL BEGINS  ********************");
					Exception_Flag = "Y";
					Exception_Count = SMS_EMAIL_Process
							.CountCheck_Function(Exception_Flag);
					Logger.onError("***************  CountCheck_Function AccountNumber error CATCH ENDS Exception_Count::"
							+ Exception_Count);
				}

			}

		} catch (Exception e) {
			Logger.onError("EXCEPTION IN EXCEUTING insQry....:" + e);
			Logger.onError("***************  CountCheck_Function ProcessData CATCH DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function ProcessData CATCH ENDS Exception_Count::"
					+ Exception_Count);
			return false;
		}

		return true;
	}

	/**
	 * @deprecated
	 * 
	 * @param branchList
	 * @param exmnPk
	 * @return
	 * @throws Exception
	 */
	public static int generateForwardingLetter(ArrayList branchList,
			int exmnPk, String dtExtractDate) throws Exception {
		Logger.onError("Inside generateForwardingLetter ");
		int inserted = 0;
		/*
		 * AccountOpeningExtractManager manager = new
		 * AccountOpeningExtractManager(); inserted =
		 * manager.generateForwardingLetter(branchList, exmnPk, dtExtractDate);
		 */
		return inserted;
	}

	/**
	 * 
	 * @param branchCode
	 * @param exmnPk
	 * @return
	 * @throws Exception
	 */
	public static String generateForwardingLetter(String branchCode,
			int exmnPk, String dtExtractDate, String accountType,
			Connection sbicon) throws Exception {

		Logger.onError("Inside generateForwardingLetter ");
		String strForLtr = "";
		AccountOpeningExtractManager manager = new AccountOpeningExtractManager();
		strForLtr = manager.generateForwardingLetter(branchCode, exmnPk,
				dtExtractDate, accountType, sbicon);

		return strForLtr;
	}

	public static BusinessKeyVO ConvertFromLCPCMightyBeanToBusinessVO(
			MightyWorkflowBean BO) throws Exception {
		BusinessKeyVO businessKeyVO = new BusinessKeyVO();
		try {

			if (BO.getAccountPk() != null) {
				businessKeyVO.setKey1(BO.getAccountPk());
			}
			if (BO.getAccountNumber() != null) {
				businessKeyVO.setKey2(BO.getAccountNumber());
			}
			if (BO.getBranchCode() != null) {
				businessKeyVO.setKey3(BO.getBranchCode());
			}
			if (BO.getCpcCode() != null) {
				businessKeyVO.setKey5(BO.getCpcCode());
			}
			if (BO.getBranchName() != null) {
				businessKeyVO.setKey6(BO.getBranchName());
			}
			if (BO.getAccountCategory() != null) {
				businessKeyVO.setKey7(BO.getAccountCategory());
			}
			if (BO.getProductCode() != null) {
				businessKeyVO.setKey8(BO.getProductCode());
			}
			if (BO.getCourierPk() != null) {
				businessKeyVO.setKey9(BO.getCourierPk());
			}
			if (BO.getForwardingSequenceNumber() != null) {
				businessKeyVO.setKey10(BO.getForwardingSequenceNumber());
			}
			if (BO.getCustomerName() != null) {
				businessKeyVO.setKey30(BO.getCustomerName());
			}
			if (BO.getExtractPk() != null) {
				businessKeyVO.setKey29(BO.getExtractPk());
			}

		} catch (Exception e) {
			Logger.onError("Exception:" + e);
			throw new Exception(e);
		}
		return businessKeyVO;
	}

	/**
	 * @deprecated
	 * 
	 * @param TransactionName
	 * @param RoutingCriterion
	 * @param workListBean
	 * @param sourceUser
	 */
	public static void initiateWorkFlow(String TransactionName,
			String RoutingCriterion, MightyWorkflowBean workListBean,
			String sourceUser) {

		/*
		 * try { Logger.onError("Mighty workflow bean : "+workListBean);
		 * WorkflowTrigger trigger = new WorkflowTrigger(); int status=0; try {
		 * status =
		 * trigger.initiateWorkFlow(TransactionName,RoutingCriterion,workListBean
		 * ,sourceUser,true); Logger.onError("Status = "+status); if(status==1)
		 * { Logger.onError("Workflow triggered successfully."); }else {
		 * Logger.onError("Workflow NOT triggered successfully."); } } catch
		 * (Exception e) { e.printStackTrace(); }
		 * 
		 * } catch (Exception e){ Logger.onError("There was an exception " +
		 * e.getMessage()); e.printStackTrace(); }
		 */
	}

	public static String removeLeadingZeros(String str) {
		Logger.onError("-->removeLeadingZeros :: " + str);
		long t1 = System.currentTimeMillis();

		if (str == null) {
			return "";
		}
		char[] chars = str.toCharArray();
		int index = 0;
		for (; index < str.length(); index++) {
			if (chars[index] != '0') {
				break;
			}
		}
		long t2 = System.currentTimeMillis();
		Logger.onError("Time Taken for removeLeadingZeros :: " + (t2 - t1));
		Logger.onError("<--removeLeadingZeros :: "
				+ ((index == 0) ? str : str.substring(index)));
		return (index == 0) ? str : str.substring(index);
	}

	private static String handleSpecialCharacters(String Value) {
		Logger.onError("-->handleSpecialCharacters :: " + Value);
		String finalValue = "";
		int flag = 0;
		boolean tokenStart = false;
		long t1 = System.currentTimeMillis();
		StringTokenizer stk = new StringTokenizer(Value, "'");
		while (stk.hasMoreTokens()) {
			flag = 1;
			if (!tokenStart) {
				finalValue = finalValue + stk.nextToken();
				tokenStart = true;
			} else {
				finalValue = finalValue + "''" + stk.nextToken();
			}
		}
		long t2 = System.currentTimeMillis();
		Logger.onError("Time Taken for handleSpecialCharacters :: " + (t2 - t1));
		if (flag == 1) {
			Logger.onError("<--handleSpecialCharacters :: " + finalValue);
			return finalValue;
		}

		else {

			Logger.onError("<--handleSpecialCharacters :: " + Value);
			return Value;
		}

	}

}
