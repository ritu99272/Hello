package in.co.hello;

public class HelloTest {
	
	public static void main(String[] args) {
		System.out.println("Testing Git");
	}

}

=============
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
		
		   args=new String[3]; args[0]="";
		  args[1]="D:/ritu1/AccountOpeningEAR/props/lcpc.properties";
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
					"database.filepath");
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

---------------AccountOpeningExtractManager-------------
	
	package com.sbi.lcpc.extracts.manager;

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
import java.sql.PreparedStatement;
/*     */
import java.sql.ResultSet;
/*     */
import java.sql.SQLException;
/*     */
import java.util.ArrayList;
/*     */
import java.util.Properties;

import com.tcs.infrastructure.extractutil.DBUtils;
import com.tcs.infrastructure.extractutil.ErrorMessageConstants;
import com.tcs.infrastructure.extractutil.ExtractProperties;
import com.tcs.infrastructure.extractutil.Logger;
import com.tcs.infrastructure.extractutil.MightyWorkflowBean;
import com.tcs.infrastructure.extractutil.SMS_EMAIL_Process;
import com.tcs.infrastructure.extractutil.SQLQuery;

public class AccountOpeningExtractManager {
	public static String Exception_Flag = "N";
	public static int Exception_Count = 0;

	public ArrayList getActiveListOfBranches(Connection sbicon) {

		ArrayList listOfBranches = new ArrayList();
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {
			String strQuery = SQLQuery.getActiveListOfBranchesQuery();
			pstmt = sbicon.prepareStatement(strQuery);
			aoResult = pstmt.executeQuery();
			while (aoResult.next()) {
				listOfBranches.add(aoResult.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in getActiveListOfBranches.." + e);
			Logger.onError("***************  CountCheck_Function getDataFromDB DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function getDataFromDB DETAIL ENDS Exception_Count::"
					+ Exception_Count);
		} finally {
			DBUtils.freeLocalResources(aoResult, pstmt);
		}

		return listOfBranches;
	}

	/**
	 * 
	 * 
	 */
	public boolean insertIntoExtractMn(int exmnPk, String fileName,
			String dtOfExtr, String lcpcCode, Connection sbicon) {
		int updated = 0;
		boolean status = false;
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {

			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }
			long t1 = System.currentTimeMillis();
			// Logger.onError("Inside insertIntoExtractMn before insertExtractMn timings"
			// + System.currentTimeMillis());
			String strQuery = SQLQuery.insertExtractMn();
			Logger.onError("Time taken for insertIntoExtractMn :: "
					+ (System.currentTimeMillis() - t1));

			pstmt = sbicon.prepareStatement(strQuery);
			pstmt.setInt(1, exmnPk);
			pstmt.setString(2, fileName);
			pstmt.setString(3, dtOfExtr);
			pstmt.setString(4, lcpcCode.trim());
			updated = pstmt.executeUpdate();

			if (updated > 0) {
				status = true;
			} else {
				status = false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in insertIntoExtractMn.." + e);
			Logger.onError("***************  CountCheck_Function insertIntoExtractMn BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function insertIntoExtractMn Exception_Count::"
					+ Exception_Count);
		} finally {
			if (status) {
				try {
					sbicon.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			DBUtils.freeLocalResources(aoResult, pstmt);
		}
		return status;
	}

	/**
	 * This method is used for executing Sequence
	 * 
	 * @param sequence
	 *            The sequence that is to be executed
	 * @return
	 * @throws SQLException
	 */
	public int executeSequence() {
		int retVal = 0;
		Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {
			if (sbicon == null || sbicon.isClosed()) {
				sbicon = DBUtils.getConnection();
			}
			long t1 = System.currentTimeMillis();
			String strSequence = SQLQuery.executeSequence();
			pstmt = sbicon.prepareStatement(strSequence);
			aoResult = pstmt.executeQuery(strSequence);
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for executeSequence :: " + (t2 - t1));
			aoResult.next();
			retVal = aoResult.getInt(1);

		} catch (SQLException e) {
			Logger.onError("Exception occured in Executing Sequence.."
					+ e.getMessage());
		} finally {
			DBUtils.freeLocalResources(aoResult, pstmt);
			DBUtils.freeConnection(sbicon);
		}

		return retVal;
	}

	public int executeSequence(Connection sbicon) {
		int retVal = 0;
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {
			if (sbicon == null || sbicon.isClosed()) {
				sbicon = DBUtils.getConnection();
			}
			long t1 = System.currentTimeMillis();
			String strSequence = SQLQuery.executeSequence();
			pstmt = sbicon.prepareStatement(strSequence);
			aoResult = pstmt.executeQuery(strSequence);
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for executeSequence :: " + (t2 - t1));
			aoResult.next();
			retVal = aoResult.getInt(1);

		} catch (SQLException e) {
			Logger.onError("Exception occured in Executing Sequence.."
					+ e.getMessage());
			Logger.onError("***************  CountCheck_Function executeSequence exception DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function executeSequence exception ends Exception_Count::"
					+ Exception_Count);
		} finally {
			DBUtils.freeLocalResources(aoResult, pstmt);
			// DBUtils.freeConnection(sbicon);
		}

		return retVal;
	}

	public boolean insertAccountOpeningMn(int accountPk, int exmnFk,
			final String accountNumber, String cifNum1, String cifNum2,
			String cifNum3, final String branchCode, String accountType,
			String lcpcCode, String cdmPk,String opnBrchCode, Connection sbicon) {

		int updated = 0;
		boolean status = false;
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {

			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }
			long t1 = System.currentTimeMillis();
			String strQuery = SQLQuery.insertAccountOpeningMn();
			pstmt = sbicon.prepareStatement(strQuery);
			pstmt.setInt(1, accountPk);
			pstmt.setInt(2, exmnFk);
			pstmt.setString(3, accountNumber);
			pstmt.setString(4, cifNum1);
			pstmt.setString(5, cifNum2);
			pstmt.setString(6, cifNum3);
			pstmt.setString(7, branchCode);
			pstmt.setString(8, accountType);
			pstmt.setString(9, lcpcCode);
			pstmt.setString(10, cdmPk);
			pstmt.setString(11, opnBrchCode);
			updated = pstmt.executeUpdate();
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for insertAccountOpeningMn :: "
					+ (t2 - t1));

			if (updated > 0) {
				status = true;
			} else {
				status = false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in insertAccountOpeningMn.." + e);
			Logger.onError("***************  CountCheck_Function insertAccountOpeningMn exception DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function insertAccountOpeningMn exception ends Exception_Count::"
					+ Exception_Count);
		} finally {
			if (status) {
				try {
					sbicon.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			DBUtils.freeLocalResources(aoResult, pstmt);
		}
		return status;
	}

	// Modified by Manu for IR 30834
	// public boolean insertAccountDetailMn(int detailPk, int accountPk, String
	// customerName, String productCode,Connection sbicon){
	public boolean insertAccountDetailMn(int detailPk, int accountPk,
			String customerName, String productCode, String dtOfExtr,
			Connection sbicon) {

		int updated = 0;
		boolean status = false;
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {

			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }
			long t1 = System.currentTimeMillis();
			/*
			 * String strSelectQuery=SQLQuery.getExtractDate(); pstmt
			 * =sbicon.prepareStatement(strSelectQuery); pstmt.setInt(1,
			 * accountPk); aoResult=pstmt.executeQuery(); Date date=
			 * aoResult.getDate(1);
			 */

			String strQuery = SQLQuery.insertAccountDetailMn();
			pstmt = sbicon.prepareStatement(strQuery);
			pstmt.setInt(1, detailPk);
			pstmt.setInt(2, accountPk);
			pstmt.setString(3, customerName);
			pstmt.setString(4, productCode);
			pstmt.setString(5, dtOfExtr); // Modified by Manu for IR 30834

			updated = pstmt.executeUpdate();
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for insertAccountDetailMn :: "
					+ (t2 - t1));
			if (updated > 0) {
				status = true;
			} else {
				status = false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in getActiveListOfBranches.." + e);
		} finally {
			if (status) {
				try {
					sbicon.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			DBUtils.freeLocalResources(aoResult, pstmt);
		}
		return status;
	}

	// Added on 17-july-2010 for Customer Detail table
	public boolean insertCustomerDetailMn(final String accountNumber,
			String cifNum1, String cifNum2, String cifNum3,
			String customerName, String productCode, Connection sbicon) {
		Logger.onError("inside  insertCustomerDetailMn");
		int updated = 0;
		boolean status = false;
		// Connection sbicon = null;
		ResultSet aoResult = null;
		PreparedStatement pstmt = null;
		try {

			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }
			long t1 = System.currentTimeMillis();
			String strQuery = SQLQuery.insertCustomerDetailMN();
			pstmt = sbicon.prepareStatement(strQuery);
			// pstmt.setInt(1, accountPk);
			pstmt.setString(1, accountNumber);
			pstmt.setString(2, cifNum1);
			pstmt.setString(3, cifNum2);
			pstmt.setString(4, cifNum3);
			pstmt.setString(5, customerName);
			pstmt.setString(6, productCode);
			updated = pstmt.executeUpdate();
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for insertCustomerDetailMN :: "
					+ (t2 - t1));
			if (updated > 0) {
				status = true;
			} else {
				status = false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in insertCustomerDetailMN.." + e);
		} finally {
			if (status) {
				try {
					sbicon.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			DBUtils.freeLocalResources(aoResult, pstmt);
		}
		return status;
	}

	/**
	 * @deprecated
	 * 
	 * @param branchList
	 * @param exmnPk
	 * @param strDate
	 * @return
	 * @throws SQLException
	 */
	public int generateForwardingLetter(ArrayList branchList, int exmnPk,
			String strDate) throws SQLException {

		Logger.onError("Inside generateForwardingLetter ");
		int inserted = 0;
		/*
		 * final int QUERY_SIZE = 2; String userId = "SYSTEM"; String branchCode
		 * = null; int branchListSize = 0; // int updated = 0; boolean status =
		 * false;
		 * 
		 * Connection sbicon = null; ResultSet aoResult = null;
		 * PreparedStatement pstmtBulk = null; PreparedStatement pstmtSelect =
		 * null;
		 * 
		 * // ResultSet aoResultBrName = null; // PreparedStatement pstmtBrName
		 * = null; // PreparedStatement pstmtInsert = null; // PreparedStatement
		 * pstmtUpdate = null;
		 * 
		 * String strBulkQuery = SQLQuery.bulkQuery(); String strCommonQuery =
		 * SQLQuery.commonQuery();
		 * 
		 * // String strInsertQuery = SQLQuery.insertQuery(); // String
		 * strUpdateQuery = SQLQuery.updateQuery(); // String strBrNameQuery =
		 * SQLQuery.cpcBranchNameQuery(); // int seqValue = executeSequence();
		 * 
		 * 
		 * try{
		 * 
		 * 
		 * if(sbicon==null || sbicon.isClosed()){ sbicon =
		 * DBUtils.getConnection(); }
		 * 
		 * Properties recProp = new Properties(); try { String connPropFile =
		 * ExtractProperties.getInstance().getExtractProperty("CONN_PROP_FILE");
		 * recProp.load(new FileInputStream(new File(connPropFile))); } catch
		 * (FileNotFoundException e) {
		 * Logger.onError(ErrorMessageConstants.FERR00002+e);
		 * Logger.onError("MAJOR ERROR PLEASE SEE THE LOGS."); } catch
		 * (IOException e) { Logger.onError(ErrorMessageConstants.FERR00002+e);
		 * Logger.onError("MAJOR ERROR PLEASE SEE THE LOGS."); }
		 * 
		 * if(branchList != null){ branchListSize = branchList.size(); }
		 * 
		 * for(int j=0; j<QUERY_SIZE; j++){ Logger.onError("Value of j :"+j);
		 * if(j == 0){ pstmtSelect = sbicon.prepareStatement(strBulkQuery);
		 * }else if(j == 1){ pstmtSelect =
		 * sbicon.prepareStatement(strCommonQuery); }else{ break; }
		 * 
		 * // For all the branches present in the branchList for(int i=0;
		 * i<branchListSize; i++){
		 * 
		 * int counter = 0; // Number of rows in aoResult
		 * 
		 * // Get the branchCode from list branchCode =
		 * branchList.get(i).toString();
		 * Logger.onError("In generateForwardingLetter, branchCode : "
		 * +branchCode); while(branchCode.trim().length() < 5){ branchCode +=
		 * "0" + branchCode; } long cdmPk = 0; String fwdSeqNum = null;
		 * 
		 * // Find out all the rows with first branch and first account type.
		 * 
		 * pstmtSelect.setString(1, branchCode); pstmtSelect.setLong(2, exmnPk);
		 * aoResult = pstmtSelect.executeQuery();
		 * 
		 * 
		 * while(aoResult.next()){ // The rows are present
		 * 
		 * if(counter == 0) {
		 * 
		 * // Get CdmPk for WCDM table cdmPk = executeSequence();
		 * Logger.onError("cdmPk : "+cdmPk); if(j == 0){ fwdSeqNum = branchCode
		 * +"-"+strDate+"-BULK-"+cdmPk; }else if(j == 1){ fwdSeqNum = branchCode
		 * +"-"+strDate+"-"+cdmPk; }else{ break; }
		 * Logger.onError("fwdSeqNum : "+fwdSeqNum);
		 * 
		 * 
		 * // Set the corresponding values and insert the rows
		 * 
		 * insertCourierDispatchMn(cdmPk, fwdSeqNum, branchCode, userId);
		 * 
		 * // pstmtInsert = sbicon.prepareStatement(strInsertQuery); // //
		 * pstmtInsert.setLong(1, cdmPk); // pstmtInsert.setString(2,
		 * fwdSeqNum); // pstmtInsert.setString(3, branchCode); //
		 * pstmtInsert.setString(4, branchCode); // pstmtInsert.setString(5,
		 * "WCDM_DISP_TO_CPC"); // pstmtInsert.setString(6, userId); //
		 * pstmtInsert.setString(7, userId); // pstmtInsert.executeUpdate(); //
		 * Logger.onError(strInsertQuery);
		 * 
		 * ++counter;
		 * 
		 * }
		 * 
		 * 
		 * // Get the individual AccountPk from the result set and update
		 * LCPC_ACCOUNT_OPENING_MN long accountPk =aoResult.getLong(1);
		 * 
		 * updateAccountOpeningMn(cdmPk, accountPk);
		 * 
		 * // pstmtUpdate = sbicon.prepareStatement(strUpdateQuery); //
		 * pstmtUpdate.setLong(1, cdmPk); // pstmtUpdate.setLong(2, accountPk);
		 * // updated = pstmtUpdate.executeUpdate();
		 * 
		 * // if(updated>0){ // status =true; // }else{ // status = false; // }
		 * 
		 * 
		 * updateBusinessKeyObject(cdmPk, accountPk, branchCode, fwdSeqNum);
		 * 
		 * // Update the details into the BusinessObject // //
		 * MightyWorkflowBean mightyBean = new MightyWorkflowBean(); //
		 * mightyBean.setCourierPk(String.valueOf(cdmPk)); //
		 * mightyBean.setAccountPk(String.valueOf(accountPk)); //
		 * mightyBean.setBranchCode(branchCode); //
		 * mightyBean.setForwardingSequenceNumber(fwdSeqNum); // //
		 * if(branchCode != null){ // pstmtBrName =
		 * sbicon.prepareStatement(strBrNameQuery); // pstmtBrName.setString(1,
		 * branchCode); // aoResultBrName = pstmtBrName.executeQuery(); //
		 * if(aoResultBrName.next()){ //
		 * mightyBean.setCpcCode(aoResultBrName.getString("WFL_CPC_CODE")); //
		 * mightyBean.setBranchName(aoResultBrName.getString("BRANCH_NAME")); //
		 * } // } // // updateBusinessObjects(mightyBean);
		 * 
		 * } } } }catch(SQLException sqle){
		 * Logger.onError("Error Code:-"+sqle.getErrorCode());
		 * sqle.printStackTrace(); }finally{ if(status){ try{ sbicon.commit();
		 * }catch(SQLException ex){ ex.printStackTrace(); } }
		 * DBUtils.freeLocalResources(aoResult, pstmtBulk);
		 * DBUtils.freeLocalResources(null, pstmtSelect);
		 * DBUtils.freeConnection(sbicon);
		 * 
		 * // DBUtils.freeLocalResources(null, pstmtInsert); //
		 * DBUtils.freeLocalResources(null, pstmtUpdate); //
		 * DBUtils.freeLocalResources(aoResultBrName, pstmtBrName); }
		 */
		return inserted;
	}

	public String generateForwardingLetter(String branchCode, int exmnPk,
			String strExtractDate, String strSeqType, Connection sbicon)
			throws SQLException {

		Logger.onError("Inside generateForwardingLetter ");
		String userId = "SYSTEM";
		boolean status = false;
		String fwdSeqNum = null;
		/*
		 * Connection sbicon = null; ResultSet aoResult = null;
		 */

		try {
			/*
			 * if(sbicon==null || sbicon.isClosed()){ sbicon =
			 * DBUtils.getConnection(); }
			 */

			Properties recProp = new Properties();
			try {
				String connPropFile = ExtractProperties.getInstance()
						.getExtractProperty("CONN_PROP_FILE");
				recProp.load(new FileInputStream(new File(connPropFile)));
			} catch (FileNotFoundException e) {
				Logger.onError(ErrorMessageConstants.FERR00002 + e);
				Logger.onError("MAJOR ERROR PLEASE SEE THE LOGS.");
				Logger.onError("***************  CountCheck_Function FileNotFoundException CATCH DETAIL BEGINS  ********************");
				Exception_Flag = "Y";
				Exception_Count = SMS_EMAIL_Process
						.CountCheck_Function(Exception_Flag);
				Logger.onError("***************  CountCheck_Function FileNotFoundException CATCH ENDS Exception_Count::"
						+ Exception_Count);
			} catch (IOException e) {
				Logger.onError(ErrorMessageConstants.FERR00002 + e);
				Logger.onError("MAJOR ERROR PLEASE SEE THE LOGS.");
				Logger.onError("***************  CountCheck_Function IOException CATCH DETAIL BEGINS  ********************");
				Exception_Flag = "Y";
				Exception_Count = SMS_EMAIL_Process
						.CountCheck_Function(Exception_Flag);
				Logger.onError("***************  CountCheck_Function IOExceptions CATCH ENDS Exception_Count::"
						+ Exception_Count);
			}

			// Get CdmPk for WCDM table
			long cdmPk = executeSequence(sbicon);

			Logger.onError("cdmPk : " + cdmPk);
			if ("BULK".equalsIgnoreCase(strSeqType)) {
				fwdSeqNum = branchCode + "-" + strExtractDate + "-BULK-"
						+ cdmPk;
			} else {
				fwdSeqNum = branchCode + "-" + strExtractDate + "-" + cdmPk;
			}
			Logger.onError("fwdSeqNum : " + fwdSeqNum);

			// Set the corresponding values and insert the rows

			insertCourierDispatchMn(cdmPk, fwdSeqNum, branchCode, userId,
					sbicon);

		} catch (Exception e) {
			Logger.onError("Error Message:-" + e.getMessage());
			e.printStackTrace();
			Logger.onError("***************  CountCheck_Function insertCourierDispatchMn CATCH DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function insertCourierDispatchMn CATCH ENDS Exception_Count::"
					+ Exception_Count);
		}/*
		 * finally{ if(status){ try{ sbicon.commit(); }catch(SQLException ex){
		 * ex.printStackTrace(); } } DBUtils.freeLocalResources(aoResult, null);
		 * DBUtils.freeConnection(sbicon);
		 * 
		 * }
		 */
		return fwdSeqNum;

	}

	/**
	 * @deprecated
	 * @param workListBean
	 */

	private static void updateBusinessObjects(MightyWorkflowBean workListBean) {
		/*
		 * try { Logger.onError("Mighty workflow bean : "+workListBean);
		 * WorkflowTrigger trigger = new WorkflowTrigger(); int status=0; try {
		 * BusinessKeyVO businessKeyVO = new BusinessKeyVO();
		 * businessKeyVO.setKey1(workListBean.getAccountPk());
		 * businessKeyVO.setKey3(workListBean.getBranchCode()); ArrayList
		 * tranIdList = trigger.getAcopWorkList(businessKeyVO); if(tranIdList ==
		 * null || tranIdList.isEmpty()){ return; }
		 * businessKeyVO.setKey6(workListBean.getBranchName());
		 * businessKeyVO.setKey5(workListBean.getCpcCode());
		 * businessKeyVO.setKey9(workListBean.getCourierPk());
		 * businessKeyVO.setKey10(workListBean.getForwardingSequenceNumber());
		 * 
		 * status = trigger.updateWF(tranIdList, businessKeyVO);
		 * 
		 * Logger.onError("Status = "+status); if(status > 0) {
		 * Logger.onError("Workflow triggered successfully."); }else {
		 * Logger.onError("Workflow NOT triggered successfully."); } } catch
		 * (Exception e) { e.printStackTrace(); }
		 * 
		 * } catch (Exception e){ Logger.onError("There was an exception " +
		 * e.getMessage()); e.printStackTrace(); }
		 */
	}

	private static void insertCourierDispatchMn(long cdmPk, String fwdSeqNum,
			String branchCode, String userId, Connection sbicon) {

		Logger.onError(" Entering into insertCourierDispatchMn method :: "
				+ " cdmPk :: " + cdmPk + " fwdSeqNum :: " + fwdSeqNum
				+ " branchCode :: " + branchCode + " userId :: " + userId);
		int iUpdate = 0;
		boolean status = false;
		// Connection sbicon = null;
		PreparedStatement pstmtInsert = null;
		try {
			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }
			long t1 = System.currentTimeMillis();
			String strInsertQuery = SQLQuery.insertQuery();
			pstmtInsert = sbicon.prepareStatement(strInsertQuery);

			pstmtInsert.setLong(1, cdmPk);
			pstmtInsert.setString(2, fwdSeqNum);
			pstmtInsert.setString(3, branchCode);
			pstmtInsert.setString(4, branchCode);
			pstmtInsert.setString(5, "WCDM_DISP_TO_CPC");
			pstmtInsert.setString(6, userId);
			pstmtInsert.setString(7, userId);
			iUpdate = pstmtInsert.executeUpdate();
			Logger.onError(strInsertQuery);
			long t2 = System.currentTimeMillis();
			Logger.onError("Time taken for insertCourierDispatchMn :: "
					+ (t2 - t1));

			if (iUpdate > 0) {
				status = true;
			} else {
				status = false;
			}

		} catch (SQLException sqle) {
			Logger.onError("Error Code:-" + sqle.getErrorCode());
			sqle.printStackTrace();
			Logger.onError("***************  CountCheck_Function insertCourierDispatchMn CATCH DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function insertCourierDispatchMn CATCH ENDS Exception_Count::"
					+ Exception_Count);
		} finally {
			if (status) {
				try {
					sbicon.commit();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			DBUtils.freeLocalResources(null, pstmtInsert);
		}
		Logger.onError(" Exiting from insertCourierDispatchMn method status :: "
				+ status);
	}

	/**
	 * @deprecated
	 * 
	 * @param cdmPk
	 * @param accountPk
	 */
	private static void updateAccountOpeningMn(long cdmPk, long accountPk) {
		/*
		 * Logger.onError(" Entering into updateAccountOpeningMn method :: "+
		 * " cdmPk :: "+cdmPk+" accountPk :: "+accountPk); int updated=0;
		 * boolean status =false; Connection sbicon = null; PreparedStatement
		 * pstmtUpdate = null; try{ if(sbicon==null || sbicon.isClosed()){
		 * sbicon = DBUtils.getConnection(); }
		 * 
		 * String strUpdateQuery = SQLQuery.updateQuery(); pstmtUpdate =
		 * sbicon.prepareStatement(strUpdateQuery); pstmtUpdate.setLong(1,
		 * cdmPk); pstmtUpdate.setLong(2, accountPk); updated =
		 * pstmtUpdate.executeUpdate();
		 * 
		 * if(updated>0){ status =true; }else{ status = false; }
		 * }catch(SQLException sqle){
		 * Logger.onError("Error Code:-"+sqle.getErrorCode());
		 * sqle.printStackTrace(); }finally{ if(status){ try{ sbicon.commit();
		 * }catch(SQLException ex){ ex.printStackTrace(); } }
		 * DBUtils.freeLocalResources(null, pstmtUpdate);
		 * DBUtils.freeConnection(sbicon); }
		 * Logger.onError(" Exiting from updateAccountOpeningMn method"+
		 * "status :: "+status);
		 */
	}

	/**
	 * @deprecated
	 * @param cdmPk
	 * @param accountPk
	 * @param branchCode
	 * @param fwdSeqNum
	 */
	private static void updateBusinessKeyObject(long cdmPk, long accountPk,
			String branchCode, String fwdSeqNum) {
		/*
		 * Logger.onError(" Entering into updateBusinessKeyObject method :: "+
		 * " cdmPk :: "
		 * +cdmPk+" accountPk :: "+accountPk+" fwdSeqNum :: "+fwdSeqNum
		 * +" branchCode :: "+branchCode); Connection sbicon = null; ResultSet
		 * aoResultBrName = null; PreparedStatement pstmtBrName = null; try{
		 * if(sbicon==null || sbicon.isClosed()){ sbicon =
		 * DBUtils.getConnection(); }
		 * 
		 * String strBrNameQuery = SQLQuery.cpcBranchNameQuery();
		 * 
		 * MightyWorkflowBean mightyBean = new MightyWorkflowBean();
		 * mightyBean.setCourierPk(String.valueOf(cdmPk));
		 * mightyBean.setAccountPk(String.valueOf(accountPk));
		 * mightyBean.setBranchCode(branchCode);
		 * mightyBean.setForwardingSequenceNumber(fwdSeqNum);
		 * 
		 * if(branchCode != null){ pstmtBrName =
		 * sbicon.prepareStatement(strBrNameQuery); pstmtBrName.setString(1,
		 * branchCode); aoResultBrName = pstmtBrName.executeQuery();
		 * if(aoResultBrName.next()){
		 * mightyBean.setCpcCode(aoResultBrName.getString("WFL_CPC_CODE"));
		 * mightyBean.setBranchName(aoResultBrName.getString("BRANCH_NAME")); }
		 * }
		 * 
		 * updateBusinessObjects(mightyBean); }catch(SQLException sqle){
		 * Logger.onError("Error Code:-"+sqle.getErrorCode());
		 * sqle.printStackTrace(); }finally{
		 * DBUtils.freeLocalResources(aoResultBrName, pstmtBrName);
		 * DBUtils.freeConnection(sbicon); }
		 * Logger.onError(" Exiting from updateBusinessKeyObject method ");
		 */

	}

	public String getBranchName(String strBranchCode, Connection sbicon) {
		// Connection sbicon = null;
		ResultSet aoResultBrName = null;
		PreparedStatement pstmtBrName = null;
		String strBranchName = "";
		try {
			// if(sbicon==null || sbicon.isClosed()){
			// sbicon = DBUtils.getConnection();
			// }

			String strBrNameQuery = SQLQuery.getBranchNameQuery();
			if (strBranchCode != null) {
				pstmtBrName = sbicon.prepareStatement(strBrNameQuery);
				pstmtBrName.setString(1, strBranchCode);
				aoResultBrName = pstmtBrName.executeQuery();
				if (aoResultBrName.next()) {
					strBranchName = aoResultBrName.getString("WFL_BRANCH_NAME");
				}
			}
		} catch (SQLException sqle) {
			Logger.onError("Error Code:-" + sqle.getErrorCode());
			sqle.printStackTrace();
			Logger.onError("Exception" + sqle);
			Logger.onError("***************  CountCheck_Function getBranchName DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function getBranchName DETAIL ENDS Exception_Count::"
					+ Exception_Count);
		} finally {
			DBUtils.freeLocalResources(aoResultBrName, pstmtBrName);
			// DBUtils.freeConnection(sbicon);
		}
		Logger.onError(" Exiting from getBranchName method ");
		return strBranchName;
	}

	public ArrayList getDiscardedProductCode(Connection sbicon) {
		ArrayList productCode = new ArrayList();
		ResultSet aoResultPc = null;
		PreparedStatement pstmtPc = null;
		try {
			String strProductQuery = SQLQuery.getProdcutCodeQuery();
			if (strProductQuery != null) {
				pstmtPc = sbicon.prepareStatement(strProductQuery);

				aoResultPc = pstmtPc.executeQuery();
				while (aoResultPc.next()) {
					System.out.println("productCode" + aoResultPc.getString(1));
					productCode.add(aoResultPc.getString("PRODUCT_CODE"));
				}
			}
		} catch (SQLException sqle) {
			Logger.onError("Error Code:-" + sqle.getErrorCode());
			sqle.printStackTrace();
			Logger.onError("Exception" + sqle);
			Logger.onError("***************  CountCheck_Function getDiscardedProductCode  BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function getDiscardedProductCode  ENDS Exception_Count::"
					+ Exception_Count);
		} finally {
			DBUtils.freeLocalResources(aoResultPc, pstmtPc);
		}

		Logger.onError(" Exiting from getBranchName method ");
		return productCode;
	}

	// Start of IR 30917
	public boolean checkaccountMaintenance(String account_number,
			Connection sbicon) {
		boolean maintenanceAvailable = false;

		ResultSet aoResult1 = null;
		ResultSet aoResult2 = null;
		ResultSet aoResult3 = null;
		ResultSet aoResult4 = null;

		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;
		try {
			long t1 = System.currentTimeMillis();

			Logger.onError("account_number for checking for maintenance:: "
					+ account_number);

			Logger.onError("Time taken for insertIntoExtractMn :: "
					+ (System.currentTimeMillis() - t1));

			String query1 = "select * from LCPC_ACCOUNT_OPENING_MN mn where mn.lcpc_aom_acc_num=? ";
			String query2 = "select mn1.lcpc_aom_pk from LCPC_ACCOUNT_OPENING_MN mn1 where mn1.lcpc_aom_acc_num=? ";
			String query3 = " select count(*) as count_row from lcpc_ao_dataentry_signupload d where d.lcpc_aodm_acct_fk=? ";
			String query4 = " select ds.lcpc_aodm_current_acct_status,ds.lcpc_aodm_acct_fk,ds.lcpc_aodm_account_number from  "
					+ " LCPC_ACCOUNT_OPENING_MN mn, lcpc_ao_dataentry_signupload ds "
					+ " where mn.lcpc_aom_acc_num=ds.lcpc_aodm_account_number and mn.lcpc_aom_pk=ds.lcpc_aodm_acct_fk and mn.lcpc_aom_acc_num=? ";

			Logger.onError("checkaccountMaintenance query1 :: " + query1);

			pstmt1 = sbicon.prepareStatement(query1);
			pstmt1.setString(1,
					removeLeadingZerosforMaintenance(account_number.trim()));

			aoResult1 = pstmt1.executeQuery();

			if (aoResult1.next()) {
				// Account Number available in LCPC_ACCOUNT_OPENING_MN
				Logger.onError("Account Number available in  LCPC_ACCOUNT_OPENING_MN");
				Logger.onError("checkaccountMaintenance query2 :: " + query2);

				pstmt2 = sbicon.prepareStatement(query2);
				pstmt2.setString(1,
						removeLeadingZerosforMaintenance(account_number.trim()));
				aoResult2 = pstmt2.executeQuery();
				boolean IdFlag = false;
				while (aoResult2.next()) {
					// Fetch ID from LCPC_ACCOUNT_OPENING_MN and check whether
					// for each id corresponding row is present in
					// lcpc_ao_dataentry_signupload
					Logger.onError("After Fetching ids for Account Number in  LCPC_ACCOUNT_OPENING_MN");
					String id = aoResult2.getString("lcpc_aom_pk");
					Logger.onError("checkaccountMaintenance query3 :: "
							+ query3);

					pstmt3 = sbicon.prepareStatement(query3);
					pstmt3.setString(1, id);
					aoResult3 = pstmt3.executeQuery();
					if (aoResult3.next()) {
						Logger.onError("Checking for ids for Account Number in  lcpc_ao_dataentry_signupload");
						if (aoResult3.getInt("count_row") == 0) {
							Logger.onError("Id for Account Number not present in lcpc_ao_dataentry_signupload");
							// if any row corresponding to id of
							// LCPC_ACCOUNT_OPENING_MN not present in
							// lcpc_ao_dataentry_signupload then set flag=true
							IdFlag = true;
						}
					}
				}

				if (IdFlag) {
					// it means that for that particular id DeSU is not started
					// thus it is pending and new acc cannot be opened
					Logger.onError("Id for Account Number not present in lcpc_ao_dataentry_signupload  Flag avaliable");
					maintenanceAvailable = true;
					// break;
				} else {
					// for all id entry is presend in DESU thus chcking for
					// status corresponding to each id
					Logger.onError("for all id entry is presend in DESU thus chcking for status corresponding to each id ");
					Logger.onError("checkaccountMaintenance query4 :: "
							+ query4);
					pstmt4 = sbicon.prepareStatement(query4);
					pstmt4.setString(1,
							removeLeadingZerosforMaintenance(account_number
									.trim()));

					aoResult4 = pstmt4.executeQuery();
					int storageFlag = 0;
					int statusFlag = 0;

					while (aoResult4.next()) {

						String account_num = aoResult4
								.getString("lcpc_aodm_account_number");
						String id = aoResult4.getString("lcpc_aodm_acct_fk");
						String status = aoResult4
								.getString("lcpc_aodm_current_acct_status");

						if (status.equalsIgnoreCase("S")) {
							storageFlag++;
						} else {
							statusFlag++;
						}
					}
					Logger.onError("storageFlag::" + storageFlag);
					Logger.onError("statusFlag::" + statusFlag);
					if (storageFlag > 0) {
						if (statusFlag > 0) {
							maintenanceAvailable = true;
						} else {
							maintenanceAvailable = false;
						}
					} else {
						if (statusFlag > 0) {
							maintenanceAvailable = true;
						} else {
							maintenanceAvailable = false;
						}
					}

				}

			}

			else {
				Logger.onError("Account number not available in  LCPC_ACCOUNT_OPENING_MN ");
				maintenanceAvailable = false;

			}

			Logger.onError("Last maintenanceAvailable :: "
					+ maintenanceAvailable);

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.onError("Exception occured in insertIntoExtractMn.." + e);
			Logger.onError("***************  CountCheck_Function insertIntoExtractMn BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function insertIntoExtractMn Exception_Count::"
					+ Exception_Count);
		} finally {

			DBUtils.freeLocalResources(aoResult1, pstmt1);
			DBUtils.freeLocalResources(aoResult2, pstmt2);
			DBUtils.freeLocalResources(aoResult3, pstmt3);
			DBUtils.freeLocalResources(aoResult4, pstmt4);

		}
		return maintenanceAvailable;
	}

	public static String removeLeadingZerosforMaintenance(String str) {
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

	public boolean insertMaintenanceAccount(String account_number,
			Connection sbicon) throws SQLException {
		boolean mintenanceAvailable = false;
		int inserted = 0;
		PreparedStatement pstmt = null;
		long t1 = System.currentTimeMillis();
		String strQuery = SQLQuery.insertAccForAmended();
		Logger.onError("account_number for inserting for maintenance:: "
				+ account_number);
		Logger.onError("insertaccountMaintenance Query :: " + strQuery);
		Logger.onError("Time taken for insertIntoExtractMn :: "
				+ (System.currentTimeMillis() - t1));
		pstmt = sbicon.prepareStatement(strQuery);
		pstmt.setString(1,
				removeLeadingZerosforMaintenance(account_number.trim()));
		inserted = pstmt.executeUpdate();
		if (inserted > 0) {
			sbicon.commit();
			Logger.onError("inserted :: " + inserted);
		}
		if (pstmt != null) {
			pstmt.close();
			mintenanceAvailable = true;
		}
		return mintenanceAvailable;
	}

	// End of IR 30917

	// Start of IR 18060136

	public ArrayList getGNCProductCode(Connection sbicon) {
		ArrayList productCode = new ArrayList();  
		ResultSet aoResultPc = null;
		PreparedStatement pstmtPc = null;
		try {
			String strProductQuery = SQLQuery.getGNCProdcutCodeQuery();
			if (strProductQuery != null) {
				pstmtPc = sbicon.prepareStatement(strProductQuery);

				aoResultPc = pstmtPc.executeQuery();
				while (aoResultPc.next()) {
					System.out.println("productCode" + aoResultPc.getString(1));
					productCode.add(aoResultPc.getString("PRODUCT_CODE"));
				}
			}
		} catch (SQLException sqle) {
			Logger.onError("Error Code:-" + sqle.getErrorCode());
			sqle.printStackTrace();
			Logger.onError("Exception" + sqle);
			Logger.onError("***************  CountCheck_Function getGNCProductCode  BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process
					.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function getGNCProductCode  ENDS Exception_Count::"
					+ Exception_Count);
		} finally {
			DBUtils.freeLocalResources(aoResultPc, pstmtPc);
		}

		Logger.onError(" Exiting from getBranchName method ");
		return productCode;
	}

	public boolean insertGNCAccount(String account_no, String branch_code,
			String product_code, String customer_name, String accPrcDate,String lcpcCode,String transBranch,
			Connection sbicon) throws SQLException {
		boolean mintenanceAvailable = false;
		int inserted = 0;
		int inserted1 = 0;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;

		String strQuery = SQLQuery.insertAccInGncBaseTable();
		Logger.onError("account_number for inserting for GncBaseTable:: "
				+ account_no);
		Logger.onError("insertAccInGncBaseTable Query :: " + strQuery);

		pstmt = sbicon.prepareStatement(strQuery);
		pstmt.setString(1, account_no);
		pstmt.setString(2, customer_name);
		pstmt.setString(3, "10");
		pstmt.setString(4, accPrcDate);
		pstmt.setString(5, "");
		pstmt.setString(6, branch_code);
		pstmt.setString(7, product_code);
		pstmt.setString(8, "N");
		pstmt.setString(9, "");
		pstmt.setString(10, "");
		pstmt.setString(11, "");
		pstmt.setString(12, "N");
		pstmt.setString(13, lcpcCode);
		pstmt.setString(14, transBranch);

		inserted = pstmt.executeUpdate();

		if (inserted > 0) {
			sbicon.commit();

			String strQuery1 = SQLQuery.insertAccInGncDashRecords();
			Logger.onError("account_number for inserting for GncDashRecords:: "
					+ account_no);
			Logger.onError("insertAccInGncDashRecords Query :: " + strQuery1);

			pstmt1 = sbicon.prepareStatement(strQuery1);
			pstmt1.setString(1, branch_code);
			pstmt1.setString(2, account_no);
			pstmt1.setString(3, "Y");
			// pstmt1.setString(4,"");
			pstmt1.setString(4, "");
			pstmt1.setString(5, "");
			pstmt1.setString(6, "");
			pstmt1.setString(7, "");
			pstmt1.setString(8, transBranch);
			inserted1 = pstmt1.executeUpdate();

			if (inserted1 > 0) {
				sbicon.commit();
			}
			if (pstmt1 != null) {
				pstmt1.close();
				mintenanceAvailable = true;
			}

			Logger.onError("inserted :: " + inserted);
		}
		if (pstmt != null) {
			pstmt.close();
			mintenanceAvailable = true;
		}

		return mintenanceAvailable;
	}

	// End of IR 18060136
}
-----------------------------package com.tcs.infrastructure.extractutil;-----------
	
	package com.tcs.infrastructure.extractutil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/*import org.apache.log4j.Logger;*/

/* This class is used fetch key-value pairs of all properties. The property file contains configration 
 * properties for the APPROVALS system */
 
public class ConfigManager
{
	
	private static Properties acopConfiguration = null;
	private static ConfigManager configManager = new ConfigManager();
	
	private ConfigManager() {
		super();
		System.out.println("com.tcs.infrastructure.extractutil.ConfigManager");
		try{
			//Get EXTRACT Configuration Properties
			acopConfiguration = new Properties();
			String connPropFile = ExtractProperties.getInstance().getExtractProperty("CONN_PROP_FILE");
			acopConfiguration.load(new FileInputStream(new File(connPropFile)));
			
			System.out.println("ConfigManager.ConfigManager(): "+ExtractProperties.getInstance().getExtractProperty("CONN_PROP_FILE"));
			Logger.onError("ConfigManager.ConfigManager(): "+ExtractProperties.getInstance().getExtractProperty("CONN_PROP_FILE"));
			
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
		System.out.println("Exiting ConfigManager");
		Logger.onError("Exiting ConfigManager");
	}
	
	public static ConfigManager getInstance(){
		return configManager;
	}
	
	public String getAccountConfig(String key){
		String value = acopConfiguration.getProperty(key);
		Logger.onError("key::"+key);
		Logger.onError("value::"+value);
		
		if(null==key){
			throw new RuntimeException("Could not get key value for Account Opening Extract using key '"+key+"'");
		}
		return value;
	}
    /*public Logger getLogger(Object obj)
	{
		return Logger.getLogger(obj.getClass().getName());
	}
    
    public Logger getLogger(String className){
    	return Logger.getLogger(className);
    }*/
    





}

--------------CSVExtract------CLASS--
	
package com.tcs.infrastructure.extractutil;

/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
import java.util.Date;
/*     */ import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * This is a utility class for all types of extracts. There are two primary
 * types of extracts considered here. 1. STRING DELIMITTED 2. NON STRING
 * DELIMITED Call the getNoOfFiles(String[] args, String property) method first
 * before calling the constructor to decide how many files are there in the
 * folder to be processed.
 * 
 * Utilities available: 1. Read the text file. 2. Parse it based on the type of
 * file 3. Connect to the database and fire queries 4. Checking of formats of
 * date and time when they are passed as raw strings.
 * 
 * @author Jiddvish Rawal
 * @version 1.0.0
 */
public final class CSVExtract {

	private Properties recProp = null;
	private String path = null;
	private String fileName = null;
	private File extractFileBeingRead = null;
	private int fileType = 0;
	public final static int STRING_DELIMITTED = 1;
	public final static int NON_STRING_DELIMITTED = 2;
	private int[] recordLength = null;

	// private RandomAccessFile raf=null;
	// private FileInputStream fis=null;
	// private Properties prop=null;
	// private int capacity;
	// private File fil=null;
	// private String line=null;
	// private long fcap=0;
	// int index=0;
	// long successfulRecords=0L;
	// long unSuccessfulRecords=0L;

	/**
	 * Call this method first to detect how many files are to be processed from
	 * the folder.
	 * 
	 * @param args
	 *            Send the arg[] from the main()
	 * @param property
	 *            Send the property string that has the location of the
	 *            respective extracts.
	 * @return Returns an int type indicating how many files are required.
	 * 
	 */
	public static int getNoOfFiles(String[] args, String property) {
		Logger.onError("Inside getNoOfFiles");
		int arrLength = 0;
		String propFileAddr = args[1];
		String extension = args[2];
		Logger.onError("propFileAddr" + propFileAddr);
		Logger.onError("extension" + extension);
		System.out.println("propFileAddr" + propFileAddr);
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File(propFileAddr)));
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}
		String path = prop.getProperty(property);
		Logger.onError("path" + path);
		System.out.println("path" + path);
		File folder = new File(path);
		arrLength = (folder.listFiles()).length;
		Logger.onError(path + "\tcontains\t" + arrLength + "\tfiles");
		System.out.println(path + "\tcontains\t" + arrLength + "\tfiles");
		File[] far = new File[arrLength];
		Logger.onError("After file initialization");
		far = folder.listFiles();
		arrLength = 0;
		for (int i = 0; i < far.length; i++) { // JIDDVISH 2
			Logger.onError("file name " + i + "" + far[i].getName());
			String fileName = far[i].getName();
			Logger.onError("file name " + i + "" + far[i].getName());

			if (fileName.substring(fileName.lastIndexOf('.')).equalsIgnoreCase(extension)) {
				Logger.onError("arrLength 1" + arrLength);
				arrLength++;
			}
		}
		far = null;
		folder = null;
		Logger.onError("arrLength" + arrLength);
		return arrLength;

	}

	/**
	 * 
	 * @param propertyFileAddr
	 *            Pass the file Address of the property file that will be used
	 *            for storing the location of your extracts
	 * @param property
	 *            Pass the property name used in your file
	 * @param fileType
	 *            Pass the file type whether it is String Delimitted or
	 *            Non-String Delimitted. Passing any other value will make the
	 *            file non-string delimitted.
	 * 
	 */

	public CSVExtract(String propertyFileAddr, String property, int fileType, String extension) {
		super();
		Logger.createLogger();
		int capacity;
		File fil = null;
		File[] far = null;
		Properties prop = null;
		try {
			if (fileType != STRING_DELIMITTED && fileType != NON_STRING_DELIMITTED) {
				this.fileType = NON_STRING_DELIMITTED;
			} else {
				this.fileType = fileType;
			}
			// extract[index] = new
			// CSVExtract(args[1],"LCPC_ACCOUNT_OPENING_EXTRACT_LOC",CSVExtract.STRING_DELIMITTED,args[2]);
			prop = new Properties();
			prop.load(new FileInputStream(new File(propertyFileAddr)));
			path = prop.getProperty(property);
			fil = new File(path);
			capacity = fil.listFiles().length;
			far = new File[capacity];
			far = fil.listFiles();

			for (int i = 0; i < far.length; i++) { // JIDDVISH 2
				String fileName = far[i].getName();
				if (fileName.substring(fileName.lastIndexOf('.')).equalsIgnoreCase(extension)) {
					extractFileBeingRead = far[i];
					Logger.onError("The file that is being read is : " + extractFileBeingRead);// +"
																								// time
																								// :
																								// "+extractFileBeingRead[i].lastModified());
					break;
				}
			}

		} catch (Exception e) {
			Logger.onError("ERRCON: Error in Constructor.. Following is the stack trace. " + e);
		}
	}

	// public int getCapacity(){
	// return capacity;
	// }

	/**
	 * This method is used for Creating new property instance for all the
	 * records that are stored in the property file, whose address is passed as
	 * parameter
	 * 
	 * @param propFileAddr
	 *            LOCATION of the file that is storing the record properties.
	 */
	public boolean readRecordProperties(String propFileAddr) {
		recProp = new Properties();
		try {
			recProp.load(new FileInputStream(new File(propFileAddr)));
			return true;
		} catch (FileNotFoundException e) {
			Logger.onError("ERR006: ERROR IN READING RECORDS : " + e);
			return false;
		} catch (IOException e) {
			Logger.onError("ERR007: ERROR IN READING RECORDS : " + e);
			return false;
		}
	}

	/*
	 * This method is used for reading the property file and returning the total
	 * number of records present in the fist field.
	 */
	private int getTotalRecords() {
		int retVal = 0;
		try {
			String recNum = recProp.getProperty("RECNUM");
			retVal = Integer.parseInt(recNum);
		} catch (Exception e) {
			Logger.onError("ERR008: ERROR IN LOADING TOTAL NUMBER OF RECORDS.... : " + e);
		}
		return retVal;
	}

	/**
	 * This method is used for reading all the lengths of all the fields from
	 * the Property file.
	 *
	 */
	public void readRecordLength() {

		int recLength = getTotalRecords();
		recordLength = new int[recLength];

		int i = 0;
		try {
			for (i = 0; i < recLength; i++) {
				try {
					recordLength[i] = Integer.parseInt(recProp.getProperty("FIELD" + i));
				} catch (NumberFormatException nfe) {
					recordLength[i] = 0;
					Logger.onError("Exception in setting record length : " + nfe);
				}
			}
		} catch (Exception e) {
			Logger.onError("ERR009: ERROR IN READING THE RECORD LENGTH..." + e);
		}
	}

	public boolean isHeaderFooterAvailable() {
		boolean evaluation = false;

		String data = recProp.getProperty("HHDDFF");

		if (data.equalsIgnoreCase("Yes") || data.equalsIgnoreCase("true"))
			evaluation = true;
		else
			evaluation = false;

		return evaluation;
	}

	public boolean processHeader(String[] hdr) {
		boolean success = false;
		return success;
	}

	public boolean processFooter(String[] ftr, int counter) {
		boolean success = false;
		return success;
	}

	/**
	 * This method is used for reading the file when filetype is
	 * NON_STRING_DELIMITTED
	 * 
	 * @return The entire file is parsed and put into a Vector that is returned
	 *         from here.
	 */
	public ArrayList readFile() {
		return readFile("");
	}

	/**
	 * This method is used for reading the file when filetype is
	 * STRING_DELIMITTED
	 * 
	 * @param delimitter
	 *            The delimitter that will be used for parsing. Be careful with
	 *            delimters like '^', '&', '*', etc.. that have a meaningful
	 *            implication. If any error is encountered, then send the
	 *            delimitter as "\\^"
	 * @return The entire file is parsed and put into a Vector that is returned
	 *         from here.
	 */
	public ArrayList readFile(String delimitter) {
		// long fcap=0;
		String line = null;
		// RandomAccessFile raf=null;
		// FileInputStream fis=null;
		ArrayList tempList = new ArrayList();
		// String st;

		if (extractFileBeingRead == null) {
			Logger.onError("There are no files to be processed at this point of time. Processing ended.");
			return null;
		}
		String tempFileName = extractFileBeingRead.toString();
		try {
			Logger.onError("inside readfile");
			BufferedReader br = new BufferedReader(new FileReader(tempFileName));
			// raf=new RandomAccessFile(tempFileName,"r");
			// fis=new FileInputStream(tempFileName);
			extractFileNameFromPath(tempFileName);
			// fcap=fis.available();
			String st;
			while ((st = br.readLine()) != null) {
				Logger.onError("inside reading line");
				if (0 != st.length()) {
					Logger.onError("inside checking the line length");
					if (fileType == STRING_DELIMITTED) {
						tempList.add(processData(st, delimitter));
						Logger.onError("LINE in fileType==STRING_DELIMITTED = " + st);
					} else {
						tempList.add(processData(st));
						Logger.onError("LINE in fileType==STRING_DELIMITTED = " + st);
					}
				}
			}

		} catch (FileNotFoundException e1) {
			Logger.onError("ERR002: ERROR IN CREATING RANDOMACCESSFILE/FILEINPUTSTREAM.. : " + e1);
		} catch (IOException e1) {
			Logger.onError("ERR003: EERROR IN CREATING RANDOMACCESSFILE/FILEINPUTSTREAM.. : " + e1);
		}
		/*
		 * try { while(null!=(st = br.readLine())){ if(0!=line.length()){
		 * if(fileType==STRING_DELIMITTED){
		 * tempList.add(processData(line,delimitter));
		 * Logger.onError("LINE in fileType==STRING_DELIMITTED = "+line); }else{
		 * tempList.add(processData(line));
		 * Logger.onError("LINE in fileType==STRING_DELIMITTED = "+line); } } }
		 * } catch (IOException e) {
		 * Logger.onError("ERR004: ERROR IN READING LINE : "+e); }
		 */
		return tempList;
	}

	public java.util.Date getCreationDate() {
		return new Date(extractFileBeingRead.lastModified());
	}

	public String getFileName() {
		return fileName;
	}

	private void extractFileNameFromPath(String pathName) {
		// String pathSeparator = File.separator;
		// if(!pathSeparator.equalsIgnoreCase("/"))
		// {
		// *********** For UNIX System ************************
		fileName = pathName.substring(pathName.lastIndexOf('/') + 1, pathName.length());
		// }else{
		// *********** For Windows System ************************
		// fileName =
		// pathName.substring(pathName.lastIndexOf('\\')+1,pathName.length());
		// }
		Logger.onError("FileName = " + fileName);
	}

	private String[] processData(String data) {
		int startIndex = 0, endIndex = 0;
		String[] records = new String[recordLength.length];
		int arrData = 0;
		for (int i = 0; i < recordLength.length; i++) {
			arrData = recordLength[i];
			endIndex = arrData + startIndex;
			String temprec = null;
			if (arrData != 0) {
				try {
					temprec = data.substring(startIndex, endIndex);
				} catch (StringIndexOutOfBoundsException e) {
					temprec = data.substring(startIndex, data.length());
				}
			} else {
				temprec = data.substring(startIndex, data.length());
			}
			records[i] = temprec;

			startIndex += arrData;
		}
		return records;
	}

	private String[] processData(String data, String delimitter) {
		String[] records = data.split(delimitter);
		// for(int i=0;i<records.length;i++){
		// String record = records[i];
		// Logger.onError((i+1)+"th Record = "+record+" : "+record.length());
		// }
		return records;
	}

	public File getFileExtract() {
		return extractFileBeingRead;
	}

	public void onError(String errorMsg, boolean closeDB) {
		try {

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Logs\\ErorLog.log", true));
				PrintWriter writer = new PrintWriter(out);
				java.util.Date date = new Date(System.currentTimeMillis());

				Calendar cal = new GregorianCalendar();
				if (closeDB)
					writer.print(date.toString() + " : " + cal.get(Calendar.HOUR_OF_DAY) + ":"
							+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + " " + cal.get(Calendar.AM_PM)
							+ " : " + extractFileBeingRead.toString() + " was not processed completely because "
							+ errorMsg);
				else
					writer.print(
							date.toString() + " : " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
									+ ":" + cal.get(Calendar.SECOND) + " " + cal.get(GregorianCalendar.PM) + " : "
									+ extractFileBeingRead.toString() + " : " + errorMsg);

				writer.println();
				out.close();
				writer.close();
				out = null;
				writer = null;
			} catch (IOException e) {
				Logger.onError("Exception occured while writing to a file onError() " + e);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.onError("Exception in inError method : " + e);
		}
	}

	/**
	 * Use this method only if the output is a single row.
	 * 
	 * @param dataList
	 * @return
	 */
	public String[] getStrArrFrmArrList(ArrayList dataList) {

		if (dataList == null) {
			Logger.onError("ArrayList that is passed is null.");
			return null;
		}
		String[] temp = null;

		StringBuffer recordsBfr = new StringBuffer(0);

		for (int i1 = 0; i1 < dataList.size(); i1++) {
			recordsBfr = recordsBfr.append((StringBuffer) dataList.get(i1));
		}
		temp = recordsBfr.toString().split("~");

		return temp;
	}

	/**
	 * This method is used for checking the date format. The parameter that is
	 * expected is the date in a string format. For Example : 27072008
	 * 
	 * @param sent_on
	 *            Date without any special characters in a raw string format as
	 *            shown in the example above
	 * @return Returns a boolean whether the datwe was in appropriate format or
	 *         not.
	 */

	public boolean checkDateFormat(String sent_on) {

		if (sent_on.length() != 8) {
			Logger.onError("DATE FORMAT NOT CORRECT(DDMMYYYY)");
			return false;
		} else {
			// Logger.onError("Date length perfect...Now checking the data");
			try {
				Integer.parseInt(sent_on);
			} catch (Exception e) {
				Logger.onError("Date format is non-numeric.. REJECTED : " + e);
				return false;
			}
		}
		if (Integer.parseInt(sent_on.substring(2, 4)) > 0 && Integer.parseInt(sent_on.substring(2, 4)) <= 12) {
			// Logger.onError("MONTH FORMAT CHECKED");
		} else {
			Logger.onError("MONTH NOT CORRECT IN SENT DATE IN HEADER");
			return false;
		}
		if (Integer.parseInt(sent_on.substring(2, 4)) == 1 || Integer.parseInt(sent_on.substring(2, 4)) == 3
				|| Integer.parseInt(sent_on.substring(2, 4)) == 5 || Integer.parseInt(sent_on.substring(2, 4)) == 7
				|| Integer.parseInt(sent_on.substring(2, 4)) == 8 || Integer.parseInt(sent_on.substring(2, 4)) == 10
				|| Integer.parseInt(sent_on.substring(2, 4)) == 12) {
			if (Integer.parseInt(sent_on.substring(0, 2)) > 0 && Integer.parseInt(sent_on.substring(0, 2)) <= 31) {
				// Logger.onError("DATE CORRECT FOR THAT MONTH print0");
			} else {
				Logger.onError("DATE NOT CORRECT FOR MONTH: " + sent_on.substring(2, 4));
				return false;
			}
		}
		if (Integer.parseInt(sent_on.substring(2, 4)) == 4 || Integer.parseInt(sent_on.substring(2, 4)) == 6
				|| Integer.parseInt(sent_on.substring(2, 4)) == 9 || Integer.parseInt(sent_on.substring(2, 4)) == 11) {
			if (Integer.parseInt(sent_on.substring(0, 2)) > 0 && Integer.parseInt(sent_on.substring(0, 2)) <= 30) {
				// Logger.onError("DATE CORRECT FOR THAT MONTH print1");
			} else {
				Logger.onError("DATE NOT CORRECT FOR MONTH: " + sent_on.substring(2, 4));
				return false;
			}
		}
		if ((Integer.parseInt(sent_on.substring(sent_on.length() - 4, sent_on.length()))) % 4 == 0) {
			if (Integer.parseInt(sent_on.substring(2, 4)) == 2) {
				if (Integer.parseInt(sent_on.substring(0, 2)) > 0 && Integer.parseInt(sent_on.substring(0, 2)) <= 29) {
					// Logger.onError("DATE CORRECT FOR THAT MONTH print2");
				} else {
					Logger.onError("DATE NOT CORRECT FOR MONTH: " + sent_on.substring(2, 4));
					return false;
				}
			}
		} else {
			if (Integer.parseInt(sent_on.substring(2, 4)) == 2) {
				if (Integer.parseInt(sent_on.substring(0, 2)) > 0 && Integer.parseInt(sent_on.substring(0, 2)) <= 28) {
					// Logger.onError("DATE CORRECT FOR THAT MONTH print3");
				} else {
					Logger.onError("DATE NOT CORRECT FOR MONTH: " + sent_on.substring(2, 4));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method is used for checking the time format. The time format
	 * expected is in 24 Hr format without any seperator in between. For example
	 * 202020 or 2020 You can give seconds if required and not if not required.
	 * 
	 * @param timeStr
	 *            Time to be sent as a raw string
	 * @return
	 */

	public boolean checkTimeFormat(String timeStr) {

		if (timeStr.length() == 6 || timeStr.length() == 4) {

			String hh = timeStr.substring(0, 2);
			String mm = timeStr.substring(2, 4);
			String sec = null;
			if (timeStr.length() > 4)
				sec = timeStr.substring(4, timeStr.length());

			int hours = 0;
			int minutes = 0;
			int seconds = 0;

			try {
				hours = Integer.parseInt(hh);
				minutes = Integer.parseInt(mm);

				if (hours < 0 || hours > 23) {
					Logger.onError("HOURS FORMAT IS WRONG : " + hh + " hours is not possible");
					return false;
				}
				if (minutes < 0 || minutes > 59) {
					Logger.onError("MINUTES FORMAT IS WRONG : " + mm + " minutes is not possible");
					return false;
				}
				if (timeStr.length() > 4) {
					seconds = Integer.parseInt(sec);
					if (seconds < 0 || seconds > 59) {
						Logger.onError("SECONDS FORMAT IS WRONG : " + sec + " seconds is not possible");
						return false;
					}
				}
			} catch (Exception e) {
				if (timeStr.length() > 4)
					Logger.onError(hh + ":" + mm + ":" + sec + " TIME FORMAT INVALID : " + e);
				else
					Logger.onError(hh + ":" + mm + " TIME FORMAT INVALID : " + e);
				return false;
			}
			if (timeStr.length() > 4)
				Logger.onError(hh + ":" + mm + ":" + sec + " TIME FORMAT IS VALID");
			else
				Logger.onError(hh + ":" + mm + ":" + " TIME FORMAT IS VALID");
			return true;
		} else {
			Logger.onError("ERROR IN TIME FORMAT.. IT CAN BE 4 OR 6 CHARACTERS ONLY.");
			return false;
		}
	}

}
----------------DBUtils-CLASS----
	
	package com.tcs.infrastructure.extractutil;

/*     */ import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.Hashtable;
import java.util.Properties;

/*     */ import javax.naming.Context;
/*     */ import javax.naming.InitialContext;
/*     */ import javax.naming.NamingException;
/*     */ import javax.sql.DataSource;

public class DBUtils {
	// private static Logger
	// logger=ConfigManager.getInstance().getLogger(DBUtils.class);
	private static DBUtils instance = new DBUtils();
	public static String Exception_Flag = "N";
	public static int Exception_Count = 0;

	private DBUtils() {

	}

	public static DBUtils getInstance() {
		return instance;
	}

	public static Connection getConnection() {
		// logger.debug("Get DB Conneciton");
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
		Connection conn = null;
		try {
			/*
			 * Context ctx = new InitialContext(env);
			 * 
			 * DataSource ds = (DataSource)
			 * ctx.lookup("java:comp/env/jdbc/SBI");
			 * 
			 * String dbUid =
			 * ConfigManager.getInstance().getAccountConfig("ORA_UID");
			 * 
			 * String dbPwd =
			 * ConfigManager.getInstance().getAccountConfig("ORA_PWD");
			 * 
			 * String decPassword=PasswordUtils.decrypt(dbPwd);
			 */

			/*String filePath = ConfigManager.getInstance().getAccountConfig("database.filepath");
			Logger.onError("filePath" + filePath);

			Properties prop = new Properties();
			InputStream in = new FileInputStream(filePath);

			prop.load(in);*/
			System.out.println("DB PROP file loaded");

			String connectionURL = ConfigManager.getInstance().getAccountConfig("database.url");
			String connectionClass = ConfigManager.getInstance().getAccountConfig("database.cls");
			String userName = ConfigManager.getInstance().getAccountConfig("database.username");
			String password = ConfigManager.getInstance().getAccountConfig("database.pwd");

			//userName = Decryption.decrypt(userName);
			Logger.onError("userName::" + userName);
			System.out.println("userName::" + userName);
			password = Decryption.decrypt(password);
			Logger.onError("password::" + password);
			System.out.println("password::" + password);

			//in.close();

			Class.forName(connectionClass);
			conn = DriverManager.getConnection(connectionURL, userName, password);
			Logger.onError("after getting connection");

		} catch (SQLException ex) {
			ex.printStackTrace();
			Logger.onError("Exception occured while getting connection " + ex);
			Logger.onError(
					"***************  CountCheck_Function dbcONNECTION SQLException CATCH DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process.CountCheck_Function(Exception_Flag);
			Logger.onError("***************  CountCheck_Function dbcONNECTION SQLException CATCH ENDS Exception_Count::"
					+ Exception_Count);
		}
		/*
		 * catch (NamingException e){ e.printStackTrace();
		 * Logger.onError("Exception occured while getting connection "+e);
		 * Logger.
		 * onError("***************  CountCheck_Function dbcONNECTION NamingException CATCH DETAIL BEGINS  ********************"
		 * ); Exception_Flag="Y";
		 * Exception_Count=SMS_EMAIL_Process.CountCheck_Function(Exception_Flag)
		 * ; Logger.
		 * onError("***************  CountCheck_Function dbcONNECTION NamingException CATCH ENDS Exception_Count::"
		 * +Exception_Count); }
		 */
		catch (Exception e) {
			e.printStackTrace();
			Logger.onError("Exception occured while getting connection " + e);
			Logger.onError(
					"***************  CountCheck_Function dbcONNECTION NamingException CATCH DETAIL BEGINS  ********************");
			Exception_Flag = "Y";
			Exception_Count = SMS_EMAIL_Process.CountCheck_Function(Exception_Flag);
			Logger.onError(
					"***************  CountCheck_Function dbcONNECTION NamingException CATCH ENDS Exception_Count::"
							+ Exception_Count);
		}
		return conn;
	}
	/*
	 * public static Connection getConnection (){ Connection connection=null;
	 * try{ Class.forName("oracle.jdbc.driver.OracleDriver"); connection =
	 * DriverManager.getConnection("jdbc:oracle:thin:@01HW138885:1521:SBI",
	 * "sbiadmin","sbiadmin"); } catch (Exception e) { e.printStackTrace();
	 * Logger.onError("Exception occured while getting connection "+e); Logger.
	 * onError("***************  CountCheck_Function dbcONNECTION CATCH DETAIL BEGINS  ********************"
	 * ); Exception_Flag="Y";
	 * Exception_Count=SMS_EMAIL_Process.CountCheck_Function(Exception_Flag);
	 * Logger.
	 * onError("***************  CountCheck_Function dbcONNECTION CATCH ENDS Exception_Count::"
	 * +Exception_Count); }
	 * 
	 * return connection;
	 * 
	 * }
	 */

	public static void freeLocalResources(ResultSet rs, Statement stmt) {
		if (null != rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (null != stmt) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static void freeConnection(Connection conn) {
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
---------------Decryption-CLASS-
	
	package com.tcs.infrastructure.extractutil;


import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class Decryption {

	private static String key="0U56YT#$^B89E3RT";
	private static String initVector="9JGY#$56S@Q!3g/M";
	public static String decrypt(String accNum) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		/*String key = "0123456789123456";
		String iv = "0123456789123456";*/
		byte[] text =Base64.decodeBase64(accNum.getBytes());
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec sck = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		c.init(Cipher.DECRYPT_MODE, sck, new IvParameterSpec(initVector.getBytes("UTF-8")));
		//System.out.println(new String(c.doFinal(text),"UTF-8"));
		return new String(c.doFinal(text),"UTF-8");
		
	}
}
---------------ErrorMessageConstantsCLASS---------
	
	
	/*
 * Created on Aug 10, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.tcs.infrastructure.extractutil;

/**
 * @author 194238
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//interface ErrorMessageConstants
public class ErrorMessageConstants
{

	public final static String ERR00001="ERR00001: LCPCCode format is incorrect, LCPC Code = ";
	public final static String ERR00002="ERR00002: Date format in the header is is incorrect, Date of extract creation in the file= ";
	public final static String ERR00003="ERR00003: Time format in the header is incorrect, Time Of Extract in the file = ";
	public final static String ERR00004="ERR00004: Acc Pre-creation Date format in the header is incorrect, Date of Acc Pre-creation in the file = ";
	public final static String ERR00005="ERR00005: Footer counter and records processed donot match. Footer in the file reads : ";
	public final static String ERR00005A=" while no. of records processed : ";
	public final static String ERR00006="ERR00006: Problem in CIFNumber : ";
	public final static String ERR00007="ERR00007: Problem in coreAccNumber : ";
	public final static String ERR00008="ERR00008: Footer counter is not a Numeric value. The value should have been numeric whereas it is : ";
	public final static String ERR00009="ERR00009: Range not available in WKRT Tlable for the Account Number : ";
	public final static String ERR00010="ERR00010: The Record for Non-Personalized Welcome Kit is not found in the Cheque Book Table. The Record Number is : ";
	public final static String ERR00011="ERR00011: Home Branch code format is incorrect, Home Branch code = ";
	public final static String ERR00012="ERR00012: Start Welcome Kit Number format is incorrect, Home Branch code = ";
	public final static String ERR00013="ERR00013: Insert/Update Query was not updated successfully. Encountered the exception : ";
	public final static String ERR00014="ERR00014: ACCOUNT NUMBER NOT AVAILABLE IN CB EXTRACT : ";
	public final static String ERR00015="ERR00015: FILE WAS NOT PROCESSED BECAUSE COUNT DOES NOT MAP";
	public final static String ERR00016="ERR00016: split() NOT AVAILABLE";
	public final static String SUCCEXTN="SUCCESS";
	public final static String FAILEXTN="FAILED";
	public final static String FERR00001="FATAL ERROR 00001: ************ERROR IN FILE FORMAT**********. There is something wrong with Header, Details and Footer prefixes. Please see the file.";
	public final static String FERR00002="FATAL ERROR 00002: ************ERROR IN FILE FORMAT**********. There is some major error. Please see logs for further details";
	
	public final static String successMsg="FILE PROCESSED SUCCESSFULLY..";
	public final static String partialFail="Either all or some records were not updated. Therefore, the file was partially erroneous.";
}
===========CLASS-ExtractProperties------
	
	/*
 * Created on Jun 9, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.tcs.infrastructure.extractutil;

/*    */ import java.io.BufferedInputStream;
/*    */ import java.util.Properties;

/**
 * @author 142331
 *
 */
public class ExtractProperties {
	private static final String EXTRACT_PROPERTIES_FILE = "/com/tcs/infrastructure/properties/Extract.properties";
	private static ExtractProperties instance = new ExtractProperties();
	private String propertyValue;
	Properties extractProp;	
	
	private ExtractProperties(){
		try {
			loadProperties();
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void loadProperties(){
		BufferedInputStream bisExtract = new BufferedInputStream(getClass().getResourceAsStream(EXTRACT_PROPERTIES_FILE));
		extractProp = new Properties();
		try {
			extractProp.load(bisExtract);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public String getExtractProperty(String key){
		propertyValue = extractProp.getProperty(key);
		return propertyValue;
	}
	
	public static ExtractProperties getInstance() {
		if (instance == null){
			instance = new ExtractProperties();
		}
		return instance;
	}	
}
=============LcpcAccountOpeningWorkflowBean=========
	
	package com.tcs.infrastructure.extractutil;


/*     */ import java.util.ArrayList;
public class LcpcAccountOpeningWorkflowBean {
	
	/*
	 * Non - keys
	 */
	String transactionId = null;
	String transactionName = null;

	/*
	 * Keys in BusinessVO
	 */
	String accountPk = null;					// Key1 
	String accountNumber = null;				// Key2
	String accountCategory = null;				// Key3
	String amendReasons = null;					// Key4
	String branchCode = null;					// Key5
	String branchName = null;					// Key6
	String cpcCode = null;						// Key7
	String productCode = null;					// Key8
	String courierPk = null;					// Key9
	String forwardingSequenceNumber = null;		// Key10
	String consignmentNumber = null;			// Key11
	String courierCode = null;					// Key12
	String remarks = null;						// Key13
	String dispatchDate = null;					// Key14
	String forwardingLetterDate = null;			// Key15
	String receiptDate = null;					// Key16
	String verificationStatus = null;			// Key17
	String documentReturned = null;				// Key18
	String standardReasons = null;				// Key19 
	String otherReasons = null;					// Key20
	//These attributes are used for verification of documents purpose.
	ArrayList acceptList = null;
	ArrayList rejectList = null;
	ArrayList rejectAccsList = null;
	ArrayList onHoldList = null;
	ArrayList onHoldAccsList = null;
	ArrayList acceptTranIdList = null;
	ArrayList rejectTranIdList = null;
	ArrayList onHoldTranIdList = null;
	
	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	/**
	 * @return the transactionName
	 */
	public String getTransactionName() {
		return transactionName;
	}
	/**
	 * @param transactionName the transactionName to set
	 */
	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountPk() {
		return accountPk;
	}
	public void setAccountPk(String accountPk) {
		this.accountPk = accountPk;
	}
	public String getAmendReasons() {
		return amendReasons;
	}
	public void setAmendReasons(String amendReasons) {
		this.amendReasons = amendReasons;
	}
	public String getBranchCode() {
		return branchCode;
	}
	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}
	public String getCpcCode() {
		return cpcCode;
	}
	public void setCpcCode(String cpcCode) {
		this.cpcCode = cpcCode;
	}
	/**
	 * @return the accountCategory
	 */
	public String getAccountCategory() {
		return accountCategory;
	}
	/**
	 * @param accountCategory the accountCategory to set
	 */
	public void setAccountCategory(String accountCategory) {
		this.accountCategory = accountCategory;
	}
	/**
	 * @return the branchName
	 */
	public String getBranchName() {
		return branchName;
	}
	/**
	 * @param branchName the branchName to set
	 */
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	/**
	 * @return the consignmentNumber
	 */
	public String getConsignmentNumber() {
		return consignmentNumber;
	}
	/**
	 * @param consignmentNumber the consignmentNumber to set
	 */
	public void setConsignmentNumber(String consignmentNumber) {
		this.consignmentNumber = consignmentNumber;
	}
	/**
	 * @return the courierCode
	 */
	public String getCourierCode() {
		return courierCode;
	}
	/**
	 * @param courierCode the courierCode to set
	 */
	public void setCourierCode(String courierCode) {
		this.courierCode = courierCode;
	}
	/**
	 * @return the courierPk
	 */
	public String getCourierPk() {
		return courierPk;
	}
	/**
	 * @param courierPk the courierPk to set
	 */
	public void setCourierPk(String courierPk) {
		this.courierPk = courierPk;
	}
	/**
	 * @return the dispatchDate
	 */
	public String getDispatchDate() {
		return dispatchDate;
	}
	/**
	 * @param dispatchDate the dispatchDate to set
	 */
	public void setDispatchDate(String dispatchDate) {
		this.dispatchDate = dispatchDate;
	}
	/**
	 * @return the documentReturned
	 */
	public String getDocumentReturned() {
		return documentReturned;
	}
	/**
	 * @param documentReturned the documentReturned to set
	 */
	public void setDocumentReturned(String documentReturned) {
		this.documentReturned = documentReturned;
	}
	/**
	 * @return the forwardingLetterDate
	 */
	public String getForwardingLetterDate() {
		return forwardingLetterDate;
	}
	/**
	 * @param forwardingLetterDate the forwardingLetterDate to set
	 */
	public void setForwardingLetterDate(String forwardingLetterDate) {
		this.forwardingLetterDate = forwardingLetterDate;
	}
	
	/**
	 * @return the forwardingSequenceNumber
	 */
	public String getForwardingSequenceNumber() {
		return forwardingSequenceNumber;
	}
	/**
	 * @param forwardingSequenceNumber the forwardingSequenceNumber to set
	 */
	public void setForwardingSequenceNumber(String forwardingSequenceNumber) {
		this.forwardingSequenceNumber = forwardingSequenceNumber;
	}
	/**
	 * @return the productCode
	 */
	public String getProductCode() {
		return productCode;
	}
	/**
	 * @param productCode the productCode to set
	 */
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	/**
	 * @return the receiptDate
	 */
	public String getReceiptDate() {
		return receiptDate;
	}
	/**
	 * @param receiptDate the receiptDate to set
	 */
	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}
	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}
	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	/**
	 * @return the verificationStatus
	 */
	public String getVerificationStatus() {
		return verificationStatus;
	}
	/**
	 * @param verificationStatus the verificationStatus to set
	 */
	public void setVerificationStatus(String verificationStatus) {
		this.verificationStatus = verificationStatus;
	}
	/**
	 * @return the otherReasons
	 */
	public String getOtherReasons() {
		return otherReasons;
	}
	/**
	 * @param otherReasons the otherReasons to set
	 */
	public void setOtherReasons(String otherReasons) {
		this.otherReasons = otherReasons;
	}
	/**
	 * @return the standardReasons
	 */
	public String getStandardReasons() {
		return standardReasons;
	}
	/**
	 * @param standardReasons the standardReasons to set
	 */
	public void setStandardReasons(String standardReasons) {
		this.standardReasons = standardReasons;
	}
	/**
	 * @return the acceptList
	 */
	public ArrayList getAcceptList() {
		return acceptList;
	}
	/**
	 * @param acceptList the acceptList to set
	 */
	public void setAcceptList(ArrayList acceptList) {
		this.acceptList = acceptList;
	}
	/**
	 * @return the acceptTranIdList
	 */
	public ArrayList getAcceptTranIdList() {
		return acceptTranIdList;
	}
	/**
	 * @param acceptTranIdList the acceptTranIdList to set
	 */
	public void setAcceptTranIdList(ArrayList acceptTranIdList) {
		this.acceptTranIdList = acceptTranIdList;
	}
	/**
	 * @return the onHoldAccsList
	 */
	public ArrayList getOnHoldAccsList() {
		return onHoldAccsList;
	}
	/**
	 * @param onHoldAccsList the onHoldAccsList to set
	 */
	public void setOnHoldAccsList(ArrayList onHoldAccsList) {
		this.onHoldAccsList = onHoldAccsList;
	}
	/**
	 * @return the onHoldList
	 */
	public ArrayList getOnHoldList() {
		return onHoldList;
	}
	/**
	 * @param onHoldList the onHoldList to set
	 */
	public void setOnHoldList(ArrayList onHoldList) {
		this.onHoldList = onHoldList;
	}
	/**
	 * @return the onHoldTranIdList
	 */
	public ArrayList getOnHoldTranIdList() {
		return onHoldTranIdList;
	}
	/**
	 * @param onHoldTranIdList the onHoldTranIdList to set
	 */
	public void setOnHoldTranIdList(ArrayList onHoldTranIdList) {
		this.onHoldTranIdList = onHoldTranIdList;
	}
	/**
	 * @return the rejectAccsList
	 */
	public ArrayList getRejectAccsList() {
		return rejectAccsList;
	}
	/**
	 * @param rejectAccsList the rejectAccsList to set
	 */
	public void setRejectAccsList(ArrayList rejectAccsList) {
		this.rejectAccsList = rejectAccsList;
	}
	/**
	 * @return the rejectList
	 */
	public ArrayList getRejectList() {
		return rejectList;
	}
	/**
	 * @param rejectList the rejectList to set
	 */
	public void setRejectList(ArrayList rejectList) {
		this.rejectList = rejectList;
	}
	/**
	 * @return the rejectTranIdList
	 */
	public ArrayList getRejectTranIdList() {
		return rejectTranIdList;
	}
	/**
	 * @param rejectTranIdList the rejectTranIdList to set
	 */
	public void setRejectTranIdList(ArrayList rejectTranIdList) {
		this.rejectTranIdList = rejectTranIdList;
	}
	
	
}
================
	
	Logger===============
	
	/*
 * Created on Aug 16, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.tcs.infrastructure.extractutil;

/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/**
 * @author 194238
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class Logger {
	
	private static File file=null,report=null;
	  
	public static void createLogger(){
		try{

			String LogPath = ExtractProperties.getInstance().getExtractProperty("AO_LOG_PATH");
			String RepPath = ExtractProperties.getInstance().getExtractProperty("AO_REP_PATH");
			File parentDirectory = (new File(System.getProperty("user.dir")+LogPath));
			File parentDirectory1 = (new File(System.getProperty("user.dir")+RepPath));
			if(!parentDirectory.exists())
				parentDirectory.mkdir();
			if(!parentDirectory1.exists())
				parentDirectory1.mkdir();
			
			Calendar cal = Calendar.getInstance();
			file = new File(parentDirectory,"LCPC_ACOP_Extract_"+cal.get(Calendar.DAY_OF_MONTH)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.YEAR)+".log");
			report = new File(parentDirectory1,"Report_"+cal.get(Calendar.DAY_OF_MONTH)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.YEAR)+".rpt");
			try {
				if(!file.exists())
					file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void onError(String errorMsg){
		onError(errorMsg,null,null);
	}
	
	public static void reportLogger(String rptMsg){

		
		try{
			
		 	BufferedWriter out = new BufferedWriter(new FileWriter(report,true));
	        PrintWriter writer = new PrintWriter(out);
	        java.util.Date date = new Date(System.currentTimeMillis());
	        
	        writer.print("["+date.toString()+"] : "+rptMsg);
	        writer.println();
	        out.close();
	        writer.close();
	        out=null;
	        writer=null;
	      
	    } catch (IOException e) {
	    	System.out.println("Exception occured while writing the report "+e);
	    }
	 
	}
	
	public static void onError(String errorMsg, File extractFileBeingRead, String extension){
		try {
			
			try{
			 	BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
		        PrintWriter writer = new PrintWriter(out);
		        java.util.Date date = new Date(System.currentTimeMillis());
		        
		        /*Calendar cal = new GregorianCalendar();
		        cal.setTimeInMillis(System.currentTimeMillis());
		        cal.getTime().toString()*/
		        writer.print("["+System.currentTimeMillis()+"] : "+errorMsg);
		        writer.println();
		        /*out=null;
		        writer=null;*/
		        out.close();
		        writer.close();
		        if(extractFileBeingRead != null && extension!=null){
			        System.out.println("COMMING INTO ONERROR FOR RENAMING ");
		        	System.out.println("extractFileBeingRead.getName() :: "+extractFileBeingRead.getName());
		        	System.out.println("extractFileBeingRead.getAbsolutePath() :: "+extractFileBeingRead.getAbsolutePath());
		        	System.out.println("extractFileBeingRead.getPath() :: "+extractFileBeingRead.getPath());
		        	if(extension==null) 
		        		extension=".txt";
		        	String newDate = date.toString().replace(' ','_');
		        	newDate = newDate.replace(':','_');
		        	renameFileName(extractFileBeingRead.getPath(),extractFileBeingRead.getPath()+"_"+extension+"_"+newDate);
//		        	System.out.println(newDate);
		        	/*File destFile = new File(extractFileBeingRead.getAbsolutePath(),extractFileBeingRead.getName()+"_"+extension+"_"+newDate);
		        	System.out.println("extractFileBeingRead.getName()_extension_+newDate :: "+extractFileBeingRead.getName()+"_"+extension+"_"+newDate);
		        	extractFileBeingRead.renameTo(destFile);*/
//		        	System.out.println(extractFileBeingRead.getName());
		        }
		        
		    } catch (IOException e) {
		    	System.out.println("Exception occured while writing to a file onError() "+e);
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception in inError method "+e.getMessage());
		}
	}
	
	/*public static void rename(File extractFileBeingRead){
		System.out.println("File :: "+extractFileBeingRead);
		if(extractFileBeingRead != null){// && extension!=null){
        	String newDate = (new Date(System.currentTimeMillis())).toString().replace(' ','_');
        	newDate = newDate.replace(':','_');
        	File destFile = new File(extractFileBeingRead.getParent(),extractFileBeingRead.getName()+"_.txt_"+newDate);
//        	extractFileBeingRead.renameTo(destFile);
        }
	}*/
	
	public static void renameFileName(String originalFileName,	String renamedFileName) {
		try {

			File oldfile = new File(originalFileName);

			if (!oldfile.exists()) {

				System.out.println("File or directory does not exist.");
			} else {
	
				File newfile = new File(renamedFileName);
	
				System.out.println("Old File or directory name : " + oldfile);
				System.out.println("New File or directory name : " + newfile);
				boolean Rename = oldfile.renameTo(newfile);
	
				if (!Rename) {
	
					System.out.println("File or directory does not rename successfully.");
	
				} else {
	
					System.out.println("File or directory rename is successfully.");
	
				}
			}

		} catch (Exception e) {
			System.out
					.println("Exception occured while writing to a file onError() "
							+ e);
		}
	}
		
}
=============MightyWorkflowBean==========
	/*
 * Created on Oct 4, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.tcs.infrastructure.extractutil;

/*     */ import java.util.ArrayList;
/**
 * @author 194238
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MightyWorkflowBean {
	
	/*
	 * Non - keys
	 */
	String transactionId = null;
	String transactionName = null;

	/*
	 * Keys in BusinessVO
	 */
	String accountPk = null;					// Key1 
	String accountNumber = null;				// Key2
	String accountCategory = null;				// Key3
	String amendReasons = null;					// Key4
	String branchCode = null;					// Key5
	String branchName = null;					// Key6
	String cpcCode = null;						// Key7
	String productCode = null;					// Key8
	String courierPk = null;					// Key9
	String forwardingSequenceNumber = null;		// Key10
	String consignmentNumber = null;			// Key11
	String courierCode = null;					// Key12
	String remarks = null;						// Key13
	String dispatchDate = null;					// Key14
	String forwardingLetterDate = null;			// Key15
	String receiptDate = null;					// Key16
	String verificationStatus = null;			// Key17
	String documentReturned = null;				// Key18
	String standardReasons = null;				// Key19 
	String otherReasons = null;					// Key20
	//Added to handle customer name in the business key 24-Sept-2009 LCPC Account Opening New Process
	String customerName = null;					// Key30
	String extractPk = null;					// Key29
	//These attributes are used for verification of documents purpose.
	ArrayList acceptList = null;
	ArrayList rejectList = null;
	ArrayList rejectAccsList = null;
	ArrayList onHoldList = null;
	ArrayList onHoldAccsList = null;
	ArrayList acceptTranIdList = null;
	ArrayList rejectTranIdList = null;
	ArrayList onHoldTranIdList = null;
	
	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	/**
	 * @return the transactionName
	 */
	public String getTransactionName() {
		return transactionName;
	}
	/**
	 * @param transactionName the transactionName to set
	 */
	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountPk() {
		return accountPk;
	}
	public void setAccountPk(String accountPk) {
		this.accountPk = accountPk;
	}
	public String getAmendReasons() {
		return amendReasons;
	}
	public void setAmendReasons(String amendReasons) {
		this.amendReasons = amendReasons;
	}
	public String getBranchCode() {
		return branchCode;
	}
	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}
	public String getCpcCode() {
		return cpcCode;
	}
	public void setCpcCode(String cpcCode) {
		this.cpcCode = cpcCode;
	}
	/**
	 * @return the accountCategory
	 */
	public String getAccountCategory() {
		return accountCategory;
	}
	/**
	 * @param accountCategory the accountCategory to set
	 */
	public void setAccountCategory(String accountCategory) {
		this.accountCategory = accountCategory;
	}
	/**
	 * @return the branchName
	 */
	public String getBranchName() {
		return branchName;
	}
	/**
	 * @param branchName the branchName to set
	 */
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	/**
	 * @return the consignmentNumber
	 */
	public String getConsignmentNumber() {
		return consignmentNumber;
	}
	/**
	 * @param consignmentNumber the consignmentNumber to set
	 */
	public void setConsignmentNumber(String consignmentNumber) {
		this.consignmentNumber = consignmentNumber;
	}
	/**
	 * @return the courierCode
	 */
	public String getCourierCode() {
		return courierCode;
	}
	/**
	 * @param courierCode the courierCode to set
	 */
	public void setCourierCode(String courierCode) {
		this.courierCode = courierCode;
	}
	/**
	 * @return the courierPk
	 */
	public String getCourierPk() {
		return courierPk;
	}
	/**
	 * @param courierPk the courierPk to set
	 */
	public void setCourierPk(String courierPk) {
		this.courierPk = courierPk;
	}
	/**
	 * @return the dispatchDate
	 */
	public String getDispatchDate() {
		return dispatchDate;
	}
	/**
	 * @param dispatchDate the dispatchDate to set
	 */
	public void setDispatchDate(String dispatchDate) {
		this.dispatchDate = dispatchDate;
	}
	/**
	 * @return the documentReturned
	 */
	public String getDocumentReturned() {
		return documentReturned;
	}
	/**
	 * @param documentReturned the documentReturned to set
	 */
	public void setDocumentReturned(String documentReturned) {
		this.documentReturned = documentReturned;
	}
	/**
	 * @return the forwardingLetterDate
	 */
	public String getForwardingLetterDate() {
		return forwardingLetterDate;
	}
	/**
	 * @param forwardingLetterDate the forwardingLetterDate to set
	 */
	public void setForwardingLetterDate(String forwardingLetterDate) {
		this.forwardingLetterDate = forwardingLetterDate;
	}
	
	/**
	 * @return the forwardingSequenceNumber
	 */
	public String getForwardingSequenceNumber() {
		return forwardingSequenceNumber;
	}
	/**
	 * @param forwardingSequenceNumber the forwardingSequenceNumber to set
	 */
	public void setForwardingSequenceNumber(String forwardingSequenceNumber) {
		this.forwardingSequenceNumber = forwardingSequenceNumber;
	}
	/**
	 * @return the productCode
	 */
	public String getProductCode() {
		return productCode;
	}
	/**
	 * @param productCode the productCode to set
	 */
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	/**
	 * @return the receiptDate
	 */
	public String getReceiptDate() {
		return receiptDate;
	}
	/**
	 * @param receiptDate the receiptDate to set
	 */
	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}
	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}
	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	/**
	 * @return the verificationStatus
	 */
	public String getVerificationStatus() {
		return verificationStatus;
	}
	/**
	 * @param verificationStatus the verificationStatus to set
	 */
	public void setVerificationStatus(String verificationStatus) {
		this.verificationStatus = verificationStatus;
	}
	/**
	 * @return the otherReasons
	 */
	public String getOtherReasons() {
		return otherReasons;
	}
	/**
	 * @param otherReasons the otherReasons to set
	 */
	public void setOtherReasons(String otherReasons) {
		this.otherReasons = otherReasons;
	}
	/**
	 * @return the standardReasons
	 */
	public String getStandardReasons() {
		return standardReasons;
	}
	/**
	 * @param standardReasons the standardReasons to set
	 */
	public void setStandardReasons(String standardReasons) {
		this.standardReasons = standardReasons;
	}
	/**
	 * @return the acceptList
	 */
	public ArrayList getAcceptList() {
		return acceptList;
	}
	/**
	 * @param acceptList the acceptList to set
	 */
	public void setAcceptList(ArrayList acceptList) {
		this.acceptList = acceptList;
	}
	/**
	 * @return the acceptTranIdList
	 */
	public ArrayList getAcceptTranIdList() {
		return acceptTranIdList;
	}
	/**
	 * @param acceptTranIdList the acceptTranIdList to set
	 */
	public void setAcceptTranIdList(ArrayList acceptTranIdList) {
		this.acceptTranIdList = acceptTranIdList;
	}
	/**
	 * @return the onHoldAccsList
	 */
	public ArrayList getOnHoldAccsList() {
		return onHoldAccsList;
	}
	/**
	 * @param onHoldAccsList the onHoldAccsList to set
	 */
	public void setOnHoldAccsList(ArrayList onHoldAccsList) {
		this.onHoldAccsList = onHoldAccsList;
	}
	/**
	 * @return the onHoldList
	 */
	public ArrayList getOnHoldList() {
		return onHoldList;
	}
	/**
	 * @param onHoldList the onHoldList to set
	 */
	public void setOnHoldList(ArrayList onHoldList) {
		this.onHoldList = onHoldList;
	}
	/**
	 * @return the onHoldTranIdList
	 */
	public ArrayList getOnHoldTranIdList() {
		return onHoldTranIdList;
	}
	/**
	 * @param onHoldTranIdList the onHoldTranIdList to set
	 */
	public void setOnHoldTranIdList(ArrayList onHoldTranIdList) {
		this.onHoldTranIdList = onHoldTranIdList;
	}
	/**
	 * @return the rejectAccsList
	 */
	public ArrayList getRejectAccsList() {
		return rejectAccsList;
	}
	/**
	 * @param rejectAccsList the rejectAccsList to set
	 */
	public void setRejectAccsList(ArrayList rejectAccsList) {
		this.rejectAccsList = rejectAccsList;
	}
	/**
	 * @return the rejectList
	 */
	public ArrayList getRejectList() {
		return rejectList;
	}
	/**
	 * @param rejectList the rejectList to set
	 */
	public void setRejectList(ArrayList rejectList) {
		this.rejectList = rejectList;
	}
	/**
	 * @return the rejectTranIdList
	 */
	public ArrayList getRejectTranIdList() {
		return rejectTranIdList;
	}
	/**
	 * @param rejectTranIdList the rejectTranIdList to set
	 */
	public void setRejectTranIdList(ArrayList rejectTranIdList) {
		this.rejectTranIdList = rejectTranIdList;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" getAccountNumber="+getAccountNumber());
		sb.append(" ,getAccountPk="+getAccountPk());
		sb.append(" ,getAmendReasons="+getAmendReasons());
		sb.append(" ,getBranchCode="+getBranchCode());
		sb.append(" ,getBranchName="+getBranchName());
		sb.append(" ,getCpcCode="+getCpcCode());
		sb.append(" ,getCustomerName="+getCustomerName());
		sb.append(" ,getReceiptDate="+getReceiptDate());
		
		
		return sb.toString();
	}
	public String getExtractPk() {
		return extractPk;
	}
	public void setExtractPk(String extractPk) {
		this.extractPk = extractPk;
	}
	
}
=========PASSWORDuTIL


package com.tcs.infrastructure.extractutil;
/*    */ import java.io.IOException;
/*    */ import java.security.GeneralSecurityException;

/*    */ import javax.crypto.Cipher;
/*    */ import javax.crypto.SecretKey;
/*    */ import javax.crypto.SecretKeyFactory;
/*    */ import javax.crypto.spec.PBEKeySpec;
/*    */ import javax.crypto.spec.PBEParameterSpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
public class PasswordUtils {

	private static final char[] PASSWORD = "sbiworkflow".toCharArray();

	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

	public static String encrypt(String property)
			throws GeneralSecurityException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher
				.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property.getBytes()));
	}

	private static String base64Encode(byte[] bytes) {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Encoder().encode(bytes);
	}

	public static String decrypt(String property)
			throws GeneralSecurityException, IOException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher
				.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(base64Decode(property)));
	}

	private static byte[] base64Decode(String property) throws IOException {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Decoder().decodeBuffer(property);
	}
}
===================SMS_EMAIL_Process========
	
	
	package com.tcs.infrastructure.extractutil;
import com.sbi.lcpc.extracts.AccountOpeningExtract;



/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.SQLException;
public class SMS_EMAIL_Process {
	/**
	 * Added by akta on 07/02/2012 for sms and email
	 */
	
		public static final String MODULE_NAME="LCPC";
		public static final String CONTACT_TYPE_SMS="SMS";
		public static final String CONTACT_TYPE_EMAIL="EMAIL";
	
		public static final String MESSAGE_ID="LCPC_ACC_OPEN_EXTRACT_UTILITY";
		public static final String PROCESS_NAME="LCPC_ACC_OPEN_EXTRACT";
	//	private static final Logger logger = Logger.getLogger(SMS_EMAIL_Process.class.getName());
	
	public static int CountCheck_Function (String Exception_Flag)
	{
	
		Logger.onError(" CountCheck_Function START ====> :: "+ Exception_Flag+" Exception_Count ::"+AccountOpeningExtract.Exception_Count);
		if (Exception_Flag.equalsIgnoreCase("Y"))	
		{
			AccountOpeningExtract.Exception_Count =AccountOpeningExtract.Exception_Count+1;
		}
		Logger.onError(" CountCheck_Function END ====> :: "+ Exception_Flag+" Exception_Count ::"+AccountOpeningExtract.Exception_Count);
		
		return AccountOpeningExtract.Exception_Count;
	}
	
	 public static boolean SMS_EMAIL_Function (String Exception_Flag, int Exception_Count) throws SQLException 
		{
		 
		    boolean status=false;
			String strMobileNum  = null;
			String strEmailId = null;
			
			PreparedStatement pstmt_EMAIL = null;
			PreparedStatement pstmt_SMS = null;
			Connection sbicon = null;
			Logger.onError(" SMS_EMAIL_Function START====> :: "+ Exception_Flag);
			
			int Exception_Count_Value=Exception_Count;
			Logger.onError(" Flag count value  "+ Exception_Count);
			
			try{
				
				/*
				Logger.onError("Inside Try" );
				Class.forName(ConfigManager.getInstance().getAccountConfig("ORA_CLS"));
				String dbURL = ConfigManager.getInstance().getAccountConfig("ORA_URL");
				
				String dbUserName =ConfigManager.getInstance().getAccountConfig("ORA_UID");
				
				String password= ConfigManager.getInstance().getAccountConfig("ORA_PWD");
				
				String decPassword=PasswordUtils.decrypt(password);	
			
			    sbicon =DriverManager.getConnection(dbURL,dbUserName,decPassword);*/
				if (sbicon == null || sbicon.isClosed()) {
					sbicon = DBUtils.getConnection();
				}
			}
			/*catch(ClassNotFoundException e)
			{
				Logger.onError("print error ClassNotFoundException"+e);
			}	*/
			catch(Exception e)
			{
				Logger.onError("print error Exception :"+e);
			}	
			int contactDetailsCount=Integer.parseInt(ConfigManager.getInstance().getAccountConfig("CONTACT_PERSON"));
	//	    if ((Exception_Count_Value>0) && Exception_Flag.equalsIgnoreCase("Y") )
		    if ((Exception_Count_Value>0))
		     {
		       Logger.onError(" SMS_EMAIL_Function inside of IF condition ====> :: ");
		    	for(int i=1;i<=contactDetailsCount;i++){
				String phNo="PHONE_NUMBER_"+i;
				String email="EMAIL_ID_"+i;
				
				
			strMobileNum  =ConfigManager.getInstance().getAccountConfig(phNo) ;
			strEmailId = ConfigManager.getInstance().getAccountConfig(email);
					
			Logger.onError("strMobileNum  :: "+strMobileNum);
			Logger.onError("strEmailId  :: "+strEmailId);
			
			String query_EMAIL=SQLQuery.insertEmailDetailsInException();
			String query_SMS=SQLQuery.insertSmsDetailsInException();
			Logger.onError("Print query::"+query_EMAIL);
			try
			{
				pstmt_EMAIL=sbicon.prepareStatement(query_EMAIL);
				Logger.onError("Print query::"+query_EMAIL);
			  
			  
					pstmt_EMAIL.setString(1, MESSAGE_ID);
			    	pstmt_EMAIL.setString(2, "mighty.support@tcs.com");
			    	pstmt_EMAIL.setString(3, strEmailId);
			    	pstmt_EMAIL.setString(4, "");
			    	pstmt_EMAIL.setString(5, MODULE_NAME);
			    	pstmt_EMAIL.setString(6, PROCESS_NAME);
			    	pstmt_EMAIL.setString(7, "");
			    	pstmt_EMAIL.setString(8, "");
			    	pstmt_EMAIL.setString(9,CONTACT_TYPE_EMAIL);
			    	pstmt_EMAIL.setString(10, "N");
			    	    	
			    	pstmt_EMAIL.setString(11, "");
			    	pstmt_EMAIL.setString(12, "");
			    	pstmt_EMAIL.setString(13, "");
			    	pstmt_EMAIL.setString(14, MODULE_NAME);
			    	pstmt_EMAIL.setString(15, PROCESS_NAME);
			    	pstmt_EMAIL.setString(16, "");
			    	pstmt_EMAIL.setString(17, "");
			    	pstmt_EMAIL.setString(18, "");
			    	pstmt_EMAIL.setString(19, "");
			    	pstmt_EMAIL.setString(20, "");
			    	pstmt_EMAIL.setString(21, "");
			    	pstmt_EMAIL.setString(22, "");
			    	pstmt_EMAIL.setString(23, "");
			    	pstmt_EMAIL.setString(24, "");
			    	pstmt_EMAIL.setString(25, "");
			    	pstmt_EMAIL.setString(26, "");
			    	pstmt_EMAIL.setString(27, "");
			    	pstmt_EMAIL.setString(28, "");
			    	pstmt_EMAIL.setString(29, "");
			    	pstmt_EMAIL.setString(30, "");
			    	pstmt_EMAIL.setString(31, "");
			    	pstmt_EMAIL.setString(32, "");
			    	pstmt_EMAIL.setString(33, "");
			    	pstmt_EMAIL.setString(34, "");
			    	pstmt_EMAIL.setString(35, "");
			    	pstmt_EMAIL.setString(36, "");
			    	pstmt_EMAIL.setString(37, "");
			    	pstmt_EMAIL.setString(38, "");
			    	pstmt_EMAIL.setString(39, "");
			    	if(pstmt_EMAIL.executeUpdate()>0)
			    	{
			    		status=true;
			    		Logger.onError("Record entered successfully");
			    		sbicon.commit();
			    	}
			    	else
			    	{
			    		Logger.onError("Record is not entered successfully");
			    	}
			
				    pstmt_SMS=sbicon.prepareStatement(query_SMS);
					Logger.onError("Print query::"+pstmt_SMS);
				    
			   
					pstmt_SMS.setString(1, MESSAGE_ID);
			    	pstmt_SMS.setString(2, "mighty.support@tcs.com");
			    	pstmt_SMS.setString(3, strMobileNum);
			    	pstmt_SMS.setString(4, "");
			    	pstmt_SMS.setString(5, MODULE_NAME);
			    	pstmt_SMS.setString(6, PROCESS_NAME);
			    	pstmt_SMS.setString(7, "");
			    	pstmt_SMS.setString(8, "");
			    	pstmt_SMS.setString(9, CONTACT_TYPE_SMS);
			    	pstmt_SMS.setString(10, "N");
			    	    	
			    	pstmt_SMS.setString(11, "");
			    	pstmt_SMS.setString(12, "");
			    	pstmt_SMS.setString(13, "");
			    	pstmt_SMS.setString(14, MODULE_NAME);
			    	pstmt_SMS.setString(15, PROCESS_NAME);
			    	pstmt_SMS.setString(16, "");
			    	pstmt_SMS.setString(17, "");
			    	pstmt_SMS.setString(18, "");
			    	pstmt_SMS.setString(19, "");
			    	pstmt_SMS.setString(20, "");
			    	pstmt_SMS.setString(21, "");
			    	pstmt_SMS.setString(22, "");
			    	pstmt_SMS.setString(23, "");
			    	pstmt_SMS.setString(24, "");
			    	pstmt_SMS.setString(25, "");
			    	pstmt_SMS.setString(26, "");
			    	pstmt_SMS.setString(27, "");
			    	pstmt_SMS.setString(28, "");
			    	pstmt_SMS.setString(29, "");
			    	pstmt_SMS.setString(30, "");
			    	pstmt_SMS.setString(31, "");
			    	pstmt_SMS.setString(32, "");
			    	pstmt_SMS.setString(33, "");
			    	pstmt_SMS.setString(34, "");
			    	pstmt_SMS.setString(35, "");
			    	pstmt_SMS.setString(36, "");
			    	pstmt_SMS.setString(37, "");
			    	pstmt_SMS.setString(38, "");
			    	pstmt_SMS.setString(39, "");
			    	if(pstmt_SMS.executeUpdate()>0)
			    	{
			    		status=true;
			    		Logger.onError("Record entered successfully");
			    		sbicon.commit();
			    		
			    	}
			    	else
			    	{
			    		Logger.onError("Record is not entered successfully");
			    	}
			}
	
		catch(SQLException e){
			
			Logger.onError("Exception occured : "+e);
			
		}
		catch(Exception e)
		{
			Logger.onError("Exception occured : "+e);
			
		}
	
		
		Logger.onError("Result  "+status);
		
	}
		 }
		 else
		 {
			 Logger.onError(" There is no exception in log file ");
		 }
		 
		 
		 return status;
		 
		}
	 
}

===============SQLQuery==========
	
	package com.tcs.infrastructure.extractutil;

/**
 * SQL Query
 * 
 * @author 938900
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */

public class SQLQuery {

	public static String getActiveListOfBranchesQuery() {
		String strSQLQuery = "SELECT DISTINCT WBCR.WFL_BRANCH_CODE FROM WFL_BRANCH_CPC_RELATION WBCR WHERE WBCR.WFL_CPC_TYPE = 'LCPC' AND WBCR.WFL_ACTIVE_FOR_EXTRACT = 'Y'";
		return strSQLQuery;
	}

	public static String insertExtractMn() {
		String strSQLQuery = " INSERT INTO WFL_EXTRACT_MN  (WFL_EXMN_PK, "
				+ "WFL_EXMN_EXTRACT_NAME, " + "WFL_EXMN_EXTRACT_DATE, "
				+ "WFL_EXMN_REQUEST_TYPE_CODE, " + "WFL_EXMN_STATUS_CODE, "
				+ "WFL_EXMN_CREATED_BY, "
				+ "WFL_EXMN_CREATE_DATE, "
				+ "WFL_EXMN_UPDATED_BY, "
				+ "WFL_EXMN_UPDATE_DATE, "
				+ "WFL_EXMN_LCPC_CODE) VALUES (?,? "
				+ // to get the filename
				",(SELECT to_date(? ,'dd-mm-yyyy') FROM DUAL)"
				+ ",'Account Opening'" + ",'ACOP_EXTRACT_UPLOADED'"
				+ ",'ADMIN'" + ",(SELECT SYSDATE FROM DUAL)" + ",'ADMIN'"
				+ ",(SELECT SYSDATE FROM DUAL)" + ",(LPAD(? ,5,'0'))) ";
		return strSQLQuery;
	}

	public static String executeSequence() {
		String strSQLQuery = " SELECT HIBERNATE_SEQUENCE.nextVal FROM DUAL ";
		return strSQLQuery;
	}

	public static String insertAccountOpeningMn() {
		String strSQLQuery = " Insert into LCPC_ACCOUNT_OPENING_MN ("
				+ "LCPC_AOM_PK, " + // 1
				"LCPC_AOM_EXMN_FK, " + // 2
				"LCPC_AOM_ACC_NUM, " + // 3
				"LCPC_AOM_CIF_NUM1, " + // 4
				"LCPC_AOM_CIF_NUM2, " + // 5
				"LCPC_AOM_CIF_NUM3, " + // 6
				"LCPC_AOM_BRANCH_CD, " + // 7
				"LCPC_AOM_STATUS_CD, " + // 8
				"LCPC_AOM_CREATED_BY, " + // 9
				"LCPC_AOM_CREATE_DATE, " + // 10
				"LCPC_AOM_UPDATED_BY, " + // 11
				"LCPC_AOM_UPDATE_DATE, " + // 12
				"LCPC_AOM_ACCT_CATEGORY, " + // 13
				"LCPC_AOM_CPC_CODE, " + "LCPC_AOM_CDM_FK,"+"LCPC_AOM_OPENING_BRANCH_CD) VALUES( " + // 14
				"? , " + // 1
				"? , " + // 2
				"(LTRIM(?,'0')), " + // 3
				"(LTRIM(?,'0')), " + // 4
				"(LTRIM(?,'0')), " + // 5
				"(LTRIM(?,'0')), " + // 6
				/*
				 * "'"+Long.parseLong(records[2])+ "', " + // 3 "'"+ cifNum1 +
				 * "', " + // 4 "'"+ cifNum2 + "', " + // 5 "'"+ cifNum3 + "', "
				 * + // 6
				 */
				"(LPAD(?,5,'0')), " + // 7
				"'ACOP_APP_FORM_DISP_FRM_BR', " + // 8
				"'ADMIN', " + // 9
				"(SELECT SYSDATE FROM DUAL), " + // 10
				"'ADMIN', " + // 11
				"(SELECT SYSDATE FROM DUAL), " + // 12
				"?, " + // 13
				// "'NEW', " + // 13
				"(LPAD(?,5,'0')), " + "?,?) ";
		return strSQLQuery;
	}

	/*
	 * public static String getExtractDate() { String strSQLQuery =
	 * "select w.wfl_exmn_extract_date"+
	 * "from wfl_extract_mn w, lcpc_account_opening_mn lmn"+
	 * "where w.wfl_exmn_pk = lmn.lcpc_aom_exmn_fk"+
	 * "and lmn.lcpc_aom_acc_num = ? " ; return strSQLQuery; }
	 */
	public static String insertAccountDetailMn() {
		String strSQLQuery = " Insert into LCPC_ACCOUNT_OPENING_DTL ("
				+ "LCPC_AOD_PK, " + // 1
				"LCPC_AOD_ACCT_FK, " + // 2
				"LCPC_AOD_CUST_NAME, " + // 3
				"LCPC_AOD_ACCT_PROD_TYPE, " + // 4
				"LCPC_AOD_ACCT_OPENING_DATE, " + // 5
				"LCPC_AOD_ACCT_OPENING_USERID, " + // 6
				"LCPC_AOD_CREATED_BY, " + // 7
				"LCPC_AOD_CREATED_DATE, " + // 8
				"LCPC_AOD_UPDATED_BY, " + // 9
				"LCPC_AOD_UPDATED_DATE) VALUES(" + // 10
				"?, " + // 1
				"?, " + // 2
				"?, " + // 3
				"?, " + // 4
				// "(SELECT SYSDATE FROM DUAL), " + // 5 Modified by Manu for IR
				// 30834
				"(SELECT to_date(? ,'dd-mm-yyyy') FROM DUAL)," + // 5
				"'ADMIN', " + // 6
				"'ADMIN', " + // 7
				"(SELECT SYSDATE FROM DUAL), " + // 8
				"'ADMIN', " + // 9
				"(SELECT SYSDATE FROM DUAL)) "; // 10
		return strSQLQuery;
	}

	public static String bulkQuery() {
		String strSQLQuery = "Select MN.LCPC_AOM_PK from LCPC_ACCOUNT_OPENING_MN MN where MN.LCPC_AOM_ACCT_CATEGORY like '%BULK' and "
				+ " MN.LCPC_AOM_BRANCH_CD = ? and MN.LCPC_AOM_EXMN_FK = ? ";
		return strSQLQuery;
	}

	public static String commonQuery() {
		String strSQLQuery = " Select MN.LCPC_AOM_PK from LCPC_ACCOUNT_OPENING_MN MN,  LCPC_ACCOUNT_OPENING_DTL DTL "
				+ " where MN.LCPC_AOM_PK = DTL.LCPC_AOD_ACCT_FK and substr(DTL.LCPC_AOD_ACCT_PROD_TYPE,1,2) NOT in ('20', '25', '28') and "
				+ " MN.LCPC_AOM_ACCT_CATEGORY not like '%BULK' and MN.LCPC_AOM_BRANCH_CD = ? and MN.LCPC_AOM_EXMN_FK = ? ";
		return strSQLQuery;
	}

	public static String insertQuery() {

		String strSQLQuery = "Insert into WFL_COURIER_DISPATCH_MN_LCPC("
				+ "WFL_CDM_PK,"
				+ // 1
				"WFL_FORWARDING_NUM,"
				+ // 2
				"WFL_DISPATCH_FROM,"
				+ // 3
				"WFL_DISPATCH_TO,"
				+ // 4
				"WFL_STATUS,"
				+ // 5
				"WFL_CREATED_BY,"
				+ // 6
				"WFL_CREATED_DT,"
				+ // 7
				"WFL_UPDATED_BY,"
				+ // 8
				"WFL_UPDATED_DT) "
				+ // 9
				"values "
				+ "(?,"
				+ // 1
				"?,"
				+ // 2
				"?,"
				+ // 3
				"(Select distinct WFL_CPC_CODE from WFL_BRANCH_CPC_RELATION WHERE WFL_BRANCH_CODE = ? AND WFL_CPC_TYPE = 'LCPC'),"
				+ // 4
				"?," + // 5
				"?," + // 6
				"(SELECT SYSDATE FROM DUAL)," + // 7
				"?," + // 8
				"(SELECT SYSDATE FROM DUAL)" + // 9
				")";
		return strSQLQuery;
	}

	public static String updateQuery() {
		String strSQLQuery = " Update LCPC_ACCOUNT_OPENING_MN set LCPC_AOM_CDM_FK = ? WHERE LCPC_AOM_PK = ? ";
		return strSQLQuery;
	}

	public static String cpcBranchNameQuery() {
		String strSQLQuery = "Select WFL_CPC_CODE, NAME as BRANCH_NAME  from  WFL_BRANCH_CPC_RELATION, BUSINESS_UNIT "
				+ "WHERE BUSINESS_UNIT_ID = WFL_BRANCH_CODE  and WFL_BRANCH_CODE = ? AND WFL_CPC_TYPE = 'LCPC'";
		return strSQLQuery;
	}

	public static String getBranchNameQuery() {
		String strSQLQuery = "SELECT NAME AS WFL_BRANCH_NAME FROM BUSINESS_UNIT WHERE BUSINESS_UNIT_ID = ?";
		return strSQLQuery;
	}

	// Added on 19-July-2010 for Customer Detail MN
	// table(lcpc_customer_detail_mn)
	public static String insertCustomerDetailMN() {
		String strSQLQuery = "Insert into lcpc_customer_detail_mn ("
				+ "LCPC_AOCM_ACC_NUM, " + "	LCPC_AOCM_CIF_NUM1, "
				+ "	LCPC_AOCM_CIF_NUM2, " + "	LCPC_AOCM_CIF_NUM3, "
				+ "	LCPC_AOCD_CUST_NAME, " + "	LCPC_AOCD_ACCT_PROD_TYPE, "
				+ "	LCPC_AOCM_CREATED_BY, " + "	LCPC_AOCM_CREATE_DATE, "
				+ "	LCPC_AOCM_UPDATED_BY, " + "	LCPC_AOCM_UPDATE_DATE) "
				+ " VALUES (" + "(LTRIM(?,'0')), " + "(LTRIM(?,'0')), "
				+ "(LTRIM(?,'0')), " + "(LTRIM(?,'0')), " + "(?)," + "(?),"
				+ "'ADMIN', " + "(SELECT SYSDATE FROM DUAL)," + "'ADMIN', "
				+ "(SELECT SYSDATE FROM DUAL))";
		return strSQLQuery;
	}

	// Added by akta for email and sms functionality
	public static String insertEmailDetailsInException() {
		String strSQLQuery = "INSERT INTO BATCHJOBQUEUE(ID,PROGRAM_ID,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,PARAM6,PARAM7,PARAM8,PARAM9,PARAM10,RECSTATE,PARAM11,PARAM12,PARAM13,PARAM14,PARAM15,PARAM16,PARAM17,PARAM18,PARAM19,PARAM20,PARAM21,PARAM22,PARAM23,PARAM24,PARAM25,PARAM26,PARAM27,PARAM28,PARAM29,PARAM30,PARAM31,PARAM32,PARAM33,PARAM34,PARAM35,PARAM36,PARAM37,PARAM38,PARAM39,PARAM40) VALUES(HIBERNATE_SEQUENCE.NEXTVAL,7,?,?,?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?,Sysdate,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return strSQLQuery;
	}

	public static String insertSmsDetailsInException() {
		String strSQLQuery = "INSERT INTO BATCHJOBQUEUE(ID,PROGRAM_ID,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,PARAM6,PARAM7,PARAM8,PARAM9,PARAM10,RECSTATE,PARAM11,PARAM12,PARAM13,PARAM14,PARAM15,PARAM16,PARAM17,PARAM18,PARAM19,PARAM20,PARAM21,PARAM22,PARAM23,PARAM24,PARAM25,PARAM26,PARAM27,PARAM28,PARAM29,PARAM30,PARAM31,PARAM32,PARAM33,PARAM34,PARAM35,PARAM36,PARAM37,PARAM38,PARAM39,PARAM40) VALUES(HIBERNATE_SEQUENCE.NEXTVAL,7,?,?,?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?,Sysdate,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return strSQLQuery;
	}

	public static String getProdcutCodeQuery() {
		String strSQLQuery = "select ldpc.product_code as PRODUCT_CODE from  lcpc_discarded_product_code ldpc";
		return strSQLQuery;
	}

	// Start of IR 30917

	public static String insertAccForAmended() {
		String strSQLQuery = " Insert into AmendedAccountDoubleEntry (Account_Number,Entry_Time) Values (?,sysdate)";
		return strSQLQuery;
	}

	// End of IR 30917

	// Start of IR 18060136

	public static String getGNCProdcutCodeQuery() {
		String strSQLQuery = "select jj.product_code as PRODUCT_CODE  from gnc_product_codes jj ";
		return strSQLQuery;
	}

	public static String insertAccInGncBaseTable() {
		String strSQLQuery = "insert into gnc_base_table(gnc_id,account_number,customer_name,status,acc_opening_date,acc_acknowledge_date,branch_code,product_code,signoff_flag,signoff_date,ack_by,sign_off_by,ack_flag,lcpcCode,transactional_branch) "
				+ "values (GNC_SEQ_ID.NEXTVAL,?,?,?,to_date(?,'dd/mm/yyyy'),?,?,?,?,?,?,?,?,?,?)";
		return strSQLQuery;
	}

	public static String insertAccInGncDashRecords() {
		String strSQLQuery = "insert into gnc_aof_dash_records(branch_code,account_number,status_10_flag,status_10_date,status_20_flag,status_20_date,status_30_flag,status_30_date,transactional_branch) values(?,?,?,sysdate,?,?,?,?,?)";
		return strSQLQuery;
	}

	// End of IR 18060136

}
==========com.tcs.infrastructure.properties=====
	
	----------Extract.properties-------
	
	
#For Windows System

#CONN_PROP_FILE =.\\properties\\LCPC\\Conn.properties

#AO_LOG_PATH =\\Logs\\LCPC\\ActOpng\\
#AO_REP_PATH =\\Reports\\LCPC\\ActOpng\\



#For Unix System

CONN_PROP_FILE =./properties/LCPC/Conn.properties

AO_LOG_PATH =/Logs/LCPC/ActOpng/
AO_REP_PATH =/Reports/LCPC/ActOpng/



# Important
# Please check extractFileNameFromPath method in CSVExtract.java for file name

--DAT DATA

hh^21082017^1854^61038^21082017                                                                                                                                                                                                                
dd^06561^00000020290442753^10141101^00000088384805481^JANE THOMAS ^00000000000000000^00000000000000000^ ^010203 ^5549272^4287819
dd^06561^00000020290442764^10141101^00000088384805492^VIJU EDAMANA ^00000000000000000^00000000000000000^ ^010203 ^5549272^4287819
dd^06561^00000020446398403^10141101^00000089862611735^MOHAMMED CHOORI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398414^10141101^00000089862611746^TONY SEBASTIAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398425^10141101^00000089862611757^RAJESH CHEMMATTU ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398800^10141101^00000089862612149^DHANANJAY GOLDER ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398811^10141101^00000089862612150^SHAHID ANWAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398822^10141101^00000089862612161^IFTEKHAR AHAMAD ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446398833^10141101^00000089862612172^RAMSURAT CHAUHAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446402126^10141101^00000089862615504^RAHUMANKHAN ABDUL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446402217^10141101^00000089862615593^RENJITH MADHUSOODANAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446402262^10141101^00000089862615649^SHAIKH MAMLEKAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446402910^10141101^00000089862616303^BIKRAMJIT SINGH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446482715^10141101^00000089862972598^NAGA POTHAMSETTI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446482748^10141101^00000089862972622^MUHAMMAD MUHAMMADMANZIL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446482759^10141101^00000089862972633^VIJAY KUMAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446482793^10141101^00000089862972677^AKHIL SAYYAD ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446482806^10141101^00000089862972688^PRAGEETH PONNARASSERY ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446484280^10141101^00000089862974197^GIJIL KUNHIKANDY ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020446484315^10141101^00000089862974222^KHIROD KUND ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447033948^10141101^00000089865335953^RAJI VAZHAKOOTTATHIL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447037987^10141101^00000089865340055^SADATH CHEEROKARA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447037998^10141101^00000089865340066^FAISAL PEEDIYAKKAL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447038006^10141101^00000089865340077^ARUN PALLIYIL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447038017^10141101^00000089865340088^ABU MANALATH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447038039^10141101^00000089865340102^UMARUL METHAL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447038040^10141101^00000089865340113^ANEESH CHANDRAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447038051^10141101^00000089865340124^NASIR KHAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447039984^10141101^00000089865342075^MOHAMMAD SHAQUIB ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447039995^10141101^00000089865342086^MOHAMMAD JAUHRI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447040004^10141101^00000089865342097^BLESSY KIZHAKKEKARA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447040015^10141101^00000089865342100^KOMURAIAH AENUGULA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447040026^10141101^00000089865342111^MAHIN KATTAKKAL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447041020^10141101^00000089865343136^KALIM SEK ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447041326^10141101^00000089865343432^KISHAN SHANIGARAM ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447041609^10141101^00000089865343715^JISSO THOMAS ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447041881^10141101^00000089865343997^VARUGHESE DANIEL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447041949^10141101^00000089865344050^JOMON THOMAS ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447045365^10141101^00000089865347493^DINESHBHAI AHIR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051005^10141101^00000089865353224^ANUPAMA PERUMBILAVIL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051016^10141101^00000089865353235^MANOHARAN VANIYAM ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051027^10141101^00000089865353246^NOWFAL MOHAMMED NOUSHAD ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051038^10141101^00000089865353257^PRISTY KUTTIYEDATHU ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051083^10141101^00000089865353304^PARVATHY SURESH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051094^10141101^00000089865353315^DINESH KAMALAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051118^10141101^00000089865353337^ISMAIL MOHAMMED KUNHI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051130^10141101^00000089865353359^ARUN PARAPPALLIL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020447051163^10141101^00000089865353382^GOPAL SINGH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450536833^10141101^00000089890168922^SAJABUL MONDAL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450541811^10141101^00000089890174561^THANGARASU RAMAIYAH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546933^10141101^00000089890180575^MUTHURAMAN MALAISAMY ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546944^10141101^00000089890180586^NANDALAL . ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546955^10141101^00000089890180597^MAHESH GUYYA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546966^10141101^00000089890180609^DEEPU MAURYA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546977^10141101^00000089890180610^ANGAD PRASAD ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546988^10141101^00000089890180621^JYOTHI DONDAPATI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450546999^10141101^00000089890180643^RAHMAN NOORTHIN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450547007^10141101^00000089890180654^RAJU PULIKKAL ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556500^10141101^00000089890192127^SHIV SHANKAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556511^10141101^00000089890192138^SHAHID NAKASH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556522^10141101^00000089890192149^DILIP SHA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556533^10141101^00000089890192150^KRISHNA GANGA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556544^10141101^00000089890192161^RAJESH PENTALA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556555^10141101^00000089890192172^NARSAIAH KALLEDA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556566^10141101^00000089890192183^MOHAMMED MUJEEBUDDIN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556588^10141101^00000089890192218^KHADER RIZWAN ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556599^10141101^00000089890192229^AASHUTOSH KUMAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556714^10141101^00000089890192376^ALAGARSAMY MAYAALAGU ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556725^10141101^00000089890192387^RAMESH GUMMADI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556985^10141101^00000089890192671^RENGADURAI VELLAISAMY ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450556996^10141101^00000089890192682^RAKESH BIND ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557004^10141101^00000089890192693^SUGHAR SINGH ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557015^10141101^00000089890192706^JYOTHISH JAYA ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557037^10141101^00000089890192728^SHARAVAN . ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557048^10141101^00000089890192751^BABU MANDHADI ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557059^10141101^00000089890192762^AMARAJEET KUMAR ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000020450557060^10141101^00000089890192773^ANIL . ^00000000000000000^00000000000000000^ ^010203 ^6806589^4282787
dd^06561^00000037102941597^10131101^00000086415610348^SOORAJ MATTAPPILLY ^00000000000000000^00000000000000000^ ^010203 ^5549272^4287819
ff^79
========


#For Windows System

#CONN_PROP_FILE =.\\properties\\LCPC\\Conn.properties

#AO_LOG_PATH =\\Logs\\LCPC\\ActOpng\\
#AO_REP_PATH =\\Reports\\LCPC\\ActOpng\\



#For Unix System

CONN_PROP_FILE =./properties/LCPC/Conn.properties

AO_LOG_PATH =/Logs/LCPC/ActOpng/
AO_REP_PATH =/Reports/LCPC/ActOpng/



# Important
# Please check extractFileNameFromPath method in CSVExtract.java for file name

========
ORA_CLS=oracle.jdbc.driver.OracleDriver
ORA_URL=jdbc:oracle:thin:@10.189.8.236:1537:WFUAT
ORA_UID=wfbranch
ORA_PWD=Password
#WFL_SRV_URL=iiop://wfapp1.sbi:2810
#WFL_SRV_URL=iiop://10.0.22.202:2810
#database.filepath =/wps_pf/users/wpsadmin/extractSetup/bin/DBProperty/sbiadmin.properties
#database.filepath = D://ahamad//AccountOpeningEAR//props//sbiadmin.properties
database.filepath=D:\\ritu1\\AccountOpeningEAR\\props\\New_AC_61038_20170821.dat_FAILED_Wed_Aug_23_08_37_48_IST_2017

database.url=jdbc:oracle:thin:@10.248.0.18:1522:GCD
database.cls=oracle.jdbc.driver.OracleDriver
database.username=gcdckyc
database.pwd=8FonMlnjFv2JJSpQl9fUJg==


