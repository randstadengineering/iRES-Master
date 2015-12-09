package com.randstadusa;

import java.io.*;
import java.util.*;

import javax.faces.context.*;

import lotus.domino.*;

import com.ibm.commons.util.io.json.*;

public class frameworkUtilities implements Serializable {

	private static final long serialVersionUID = 1L;
	// public static final String wkServer = "Search/ATS";
	public static final String wkServer = "DominoDev2/ATS";

	//public static final String wkServer = "WebApp1a/ATS";

	public frameworkUtilities() {
	}

	public String individualNavEntries(String initials) throws NotesException {
		// we may change to another return type
		java.util.Vector allObjects = new java.util.Vector();
		// //System.out.println("before Session");
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		// //System.out.println("after Session");
		StringBuilder sb = new StringBuilder(); // to test results, let's just
		// save it to a local directory
		ArrayList<Object> collections = new ArrayList<Object>();
		JsonJavaObject obj = new JsonJavaObject();
		String newline = System.getProperty("line.separator");
		String indivNav = null;
		String parentCat = null;

		// Database currDb = s.getCurrentDatabase();
		// Database db = s.getCurrentDatabase();
		Database db;
		try {
			db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			// Database db = Factory.getSession().getCurrentDatabase();
			/*
			 * if (db != null) {
			 * 
			 * Vector viewNames = new Vector(); allObjects.addElement(viewNames); sb.append("current database:" + db.getTitle() + newline); viewNames = db.getViews(); sb.append("did colValues" + newline); if (!viewNames.isEmpty()) { sb.append("I have colValues" + newline); for (int i = 0; i <
			 * viewNames.size(); i++) { if (viewNames.elementAt(i) != null) { if (viewNames.elementAt(i).toString().length() > 0) { // sb.append("view[" + i + "] " + colValues.elementAt(i) + newline); } } } sb.append("Bean got views" + newline); } else { sb.append("colValues is empty" + newline); }
			 * sb.append("after getting the views" + newline); else { sb.append("db is null" + newline); } }
			 */

			if (db != null) {
				View pipelineView = db.getView("(Candidates \\ New Recruiters Pipeline Scored)");
				allObjects.addElement(pipelineView);
				if (pipelineView != null) {

					// //System.out.println("I should have a view by now");
					ViewNavigator pipelineNav = pipelineView.createViewNavFromCategory(initials);
					allObjects.addElement(pipelineNav);
					if (pipelineNav != null) {
						//System.out.println("before we check the count");
						if (pipelineNav.getCount() > 0) {

							ViewEntry ve = pipelineNav.getFirst();
							allObjects.addElement(ve);
							// sb.append("indent level is " +
							// ve.getIndentLevel() + " ");
							// sb.append("count is : " + pipelineNav.getCount()
							// + newline); colValues = ve.getColumnValues();
							Vector colValues = new Vector();
							try {
								while (ve != null) {
									HashMap<String, Object> collection = new HashMap<String, Object>();
									ViewEntry tmpVE = pipelineNav.getNextCategory();
									colValues.clear();
									colValues = ve.getColumnValues();
									if (!colValues.isEmpty()) {
										for (int i = 0; i < colValues.size(); i++) {
											if (colValues.elementAt(i) != null) {
												if (colValues.elementAt(i).toString().length() > 0) {
													if (i == 1) {
														sb.append("Count: " + colValues.elementAt(i) + " ");
														collection.put("count", colValues.elementAt(i));
													}
													if (i == 2) {
														sb.append("CategoryA: " + colValues.elementAt(i) + newline);
														collection.put("categoryA", colValues.elementAt(i));
														parentCat = (String) colValues.elementAt(i);
													}
													if (i == 4) {
														sb.append("CategoryB: " + colValues.elementAt(i) + newline);
														collection.put("categoryB", colValues.elementAt(i));
														collection.put("parentCat", parentCat);
													}
												}
											}
										}
									}
									ve.recycle();
									ve = tmpVE;
									collections.add(collection);
								}
							} catch (Exception e) {
								sb.append("No next entry" + newline);
							}
						} else {
							sb.append("Nothing found in the navigator for " + initials + newline);
						}
					} else {
						sb.append("Could not get navigator" + newline);
					}
				} else {
					sb.append("Could not get the one Candidate view" + newline);
				}
			} else {
				sb.append("database is gone" + newline);
			}

			// //System.out.println("about to write the file");
			try {
				obj.put("data", collections);
				try {
					// //System.out.println("about to put hashmap into a json format");
					// data = JsonGenerator.toJson(JsonJavaFactory.instanceEx,
					// obj);
					indivNav = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
					// //System.out.println("after putting hashmap into a json format");
				} catch (JsonException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// //System.out.println(e.getMessage());
					// //System.out.println(e.getCause());
					e.printStackTrace();
				}
				return JsonGenerator.toJson(JsonJavaFactory.instance, collections);
			} catch (Exception e) {
				// //System.out.println(e.getMessage());
				// //System.out.println(e.getCause());
				e.printStackTrace();
			} finally {
				try {
					//System.out.println("about to recycle Domino");
					s.recycle(allObjects);
					s.recycle();
					System.runFinalization();
					System.gc();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return indivNav;
		} finally {
			try {
				//System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public String joNavEntries(String joNumber) throws NotesException {
		// we may change to another return type
		java.util.Vector allObjects = new java.util.Vector();
		// //System.out.println(joNumber + "*****************************");
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		// //System.out.println("after Session");
		StringBuilder sb = new StringBuilder(); // to test results, let's just
		// save it to a local directory
		ArrayList<Object> collections = new ArrayList<Object>();
		JsonJavaObject obj = new JsonJavaObject();
		String newline = System.getProperty("line.separator");
		String indivNav = null;
		String parentCat = null;
		Database db;
		try {
			db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			if (db != null) {
				View pipelineView = db.getView("JsonJOPipelineCategory");
				allObjects.addElement(pipelineView);
				if (pipelineView != null) {

					////System.out.println("I should have a view by now " + pipelineView.getName());
					ViewNavigator pipelineNav = pipelineView.createViewNavFromCategory(joNumber);
					allObjects.addElement(pipelineNav);
					if (pipelineNav != null) {
						////System.out.println("before we check the count " + pipelineNav.getCount());
						if (pipelineNav.getCount() > 0) {

							ViewEntry ve = pipelineNav.getFirst();
							allObjects.addElement(ve);
							// sb.append("indent level is " +
							// ve.getIndentLevel() + " ");
							// sb.append("count is : " + pipelineNav.getCount()
							// + newline); colValues = ve.getColumnValues();
							Vector colValues = new Vector();
							try {
								while (ve != null) {
									HashMap<String, Object> collection = new HashMap<String, Object>();
									ViewEntry tmpVE = pipelineNav.getNextCategory();
									// here
									alColumnValues(colValues, ve, sb, collection, newline, parentCat);
									if (collection.get("categoryA") != null) {
										parentCat = (String) collection.get("categoryA");
									}

									ve.recycle();
									ve = tmpVE;
									collections.add(collection);
								}
							} catch (Exception e) {
								sb.append("No next entry" + newline);
							}
						} else {
							sb.append("Nothing found in the navigator for " + joNumber + newline);
						}
					} else {
						sb.append("Could not get navigator" + newline);
					}
				} else {
					sb.append("Could not get the one Candidate view" + newline);
				}
			} else {
				sb.append("database is gone" + newline);
			}
			try {
				obj.put("data", collections);
				try {
					indivNav = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
				} catch (JsonException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// //System.out.println(e.getMessage());
					// //System.out.println(e.getCause());
					e.printStackTrace();
				}
				return JsonGenerator.toJson(JsonJavaFactory.instance, collections);
			} catch (Exception e) {
				// //System.out.println(e.getMessage());
				// //System.out.println(e.getCause());
				e.printStackTrace();
			} finally {
				try {
					//System.out.println("about to recycle Domino");
					s.recycle(allObjects);
					s.recycle();
					System.runFinalization();
					System.gc();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return indivNav;
		} finally {
			try {
				//System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public String changeSubStatus(String docUNID, String newSubStatus, String fieldName) {
		// //System.out.println("before Session subStatus execution");
		// //System.out.println("field: " + fieldName + " to: " + newSubStatus);
		java.util.Vector allObjects = new java.util.Vector();
		// //System.out.println("docUNID is: " + docUNID);
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		// //System.out.println("after Session");
		String rtnString = null;

		try {
			Database db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			// //System.out.println("Title is: " + db.getTitle());
			Document nDoc = db.getDocumentByUNID(docUNID);
			allObjects.addElement(nDoc);
			// //System.out.println("I should have the doc now");
			if (nDoc != null) {
				// //System.out.println("nDoc exists");
				nDoc.replaceItemValue(fieldName, newSubStatus);
				rtnString = "Did it";
				if (nDoc.save()) {
					 //System.out.println("Save successful");
				} else {
					 //System.out.println("Save failed");
				}
			} else {
				 //System.out.println("nDoc does not exist");
				rtnString = "nDoc does not exist";
			}

		} catch (Exception e) {
			// //System.out.println(e.getMessage());
			// //System.out.println(e.getCause());
			e.printStackTrace();
		} finally {
			try {
				// //System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rtnString;
	}

	public HashMap alColumnValues(Vector colValues, ViewEntry ve, StringBuilder sb, HashMap collection, String newline, String parentCat) {
		try {
			colValues.clear();
			colValues = ve.getColumnValues();
			if (!colValues.isEmpty()) {
				for (int i = 0; i < colValues.size(); i++) {
					if (colValues.elementAt(i) != null) {
						if (colValues.elementAt(i).toString().length() > 0) {
							if (i == 1) {
								sb.append("Count: " + colValues.elementAt(i) + " ");
								collection.put("count", colValues.elementAt(i));
								//System.out.println("count " + colValues.elementAt(i) + " ");
							}
							if (i == 2) {
								sb.append("CategoryA: " + colValues.elementAt(i) + newline);
								collection.put("categoryA", colValues.elementAt(i));
								//System.out.println("Category A " + colValues.elementAt(i) + " ");

							}
							if (i == 4) {
								sb.append("CategoryB: " + colValues.elementAt(i) + newline);
								collection.put("categoryB", colValues.elementAt(i));
								//System.out.println("Category B " + colValues.elementAt(i) + " ");
								collection.put("parentCat", parentCat);
							}
						}
					}
				}
			}
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return collection;
	}

	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}
}
