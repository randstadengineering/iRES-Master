package com.randstadusa;

import java.io.*;
import java.util.*;

import javax.faces.context.*;

import lotus.domino.*;

import com.ibm.commons.util.io.json.*;

public class individualPipeline implements Serializable {

	private static final long serialVersionUID = 1L;
	private String data;
	// public static final String wkServer = "Search/ATS";
	 public static final String wkServer = "DominoDev2/ATS";

	//public static final String wkServer = "WebApp1a/ATS";

	public individualPipeline() {
		// init();
	}

	public String getIndividualPipeline(String recruiterInitials) {
		String indivPipe = null;

		// System.out.println(recruiterInitials +
		// " Pulling indivPipe ***********************");
		java.util.Vector allObjects = new java.util.Vector();
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		try {
			ArrayList<Object> collections = new ArrayList<Object>();
			LinkedHashMap joCollection = new LinkedHashMap();
			JsonJavaObject obj = new JsonJavaObject();
			Database db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			View v = db.getView("JsonPipeline");
			allObjects.addElement(v);
			View joView = db.getView("JsonAllJOs");
			allObjects.addElement(joView);
			Document joDoc = null;
			allObjects.addElement(joDoc);
			String joNumber = "";
			String holdingJONumber = "";
			Item item = null;
			allObjects.addElement(item);
			ViewEntryCollection names = v.getAllEntriesByKey(recruiterInitials, true);
			allObjects.addElement(names);
			//System.out.println("count is: " + names.getCount());
			if (names.getCount() > 0) {
				ViewEntry ve = names.getFirstEntry();
				allObjects.addElement(ve);
				DateTime nDateTime = null;
				allObjects.addElement(nDateTime);

				// the below will actually come from a config doc
				// the order will be set in that config doc and each will have a
				// field name
				Database thisDb = s.getCurrentDatabase();
				allObjects.addElement(thisDb);
				View configView = thisDb.getView("Admin\\User Profile");
				allObjects.addElement(configView);
				Document configDoc = configView.getDocumentByKey(recruiterInitials, true);
				allObjects.addElement(configDoc);
				if (configDoc != null) {
					item = configDoc.getFirstItem("pipelineGroupSelFieldNames");
					Enumeration values = item.getValues().elements();
					while (values.hasMoreElements()) {
						// this field holds field names, so we just need to put
						// thi in the collection
						String nodeStr = values.nextElement() + "";
						joCollection.put(nodeStr, "Not found");
						//System.out.println("A selection " + nodeStr);
					}
				}
				try {
					while (ve != null) {
						// we will need to clear these out with each doc
						Document doc = ve.getDocument();
						allObjects.addElement(doc);
						ViewEntry nextVE = names.getNextEntry(ve);
						HashMap<String, Object> collection = new HashMap<String, Object>();
						collection.put("unid", doc.getUniversalID());
						collection.put("Recruiter", doc.getItemValueString("Recruiter"));
						if (doc.getItemValueString("Form").equalsIgnoreCase("Apply")
								|| doc.getItemValueString("Form").equalsIgnoreCase("4. Apply")
								|| doc.getItemValueString("Form").equalsIgnoreCase("New 4. Apply")) {
							collection.put("JONumber", doc.getItemValueString("JONumber"));
							joNumber = doc.getItemValueString("JONumber1");
						} else if (doc.getItemValueString("Form").equalsIgnoreCase("Subsheet")
								|| doc.getItemValueString("Form").equalsIgnoreCase("6. Submittal Sheet")) {
							collection.put("JONumber", doc.getItemValueString("JONumber"));
							joNumber = doc.getItemValueString("JONumber");
						} else {
							collection.put("JONumber", doc.getItemValueString(""));
							joNumber = "";
						}
						// we only need to get this once for each
						// System.out.println("JONumber " + joNumber);

						if (holdingJONumber.equalsIgnoreCase(joNumber)) {
							// we already have this for this job number
							// System.out.println(holdingJONumber + " same as "
							// + joNumber);
						} else {
							joDoc = joView.getDocumentByKey(joNumber, true);
							// System.out.println("joNumber is " + joNumber);
							if (configDoc != null) {
								item = configDoc.getFirstItem("pipelineGroupSelFieldNames");
								Enumeration values = item.getValues().elements();
								while (values.hasMoreElements()) {
									// this field holds field names, so we just
									// need to put thi in the collection
									joCollection.put(values.nextElement() + "", "Not found");
								}
							}
							if (joDoc != null) {
								// .out.println("Doc for " + joNumber);
								joDetails(joCollection, joNumber.toString().trim(), joDoc);
							} else {
								// System.out.println("no Doc for " + joNumber);
							}
							holdingJONumber = joNumber;
						}

						Set set = joCollection.entrySet();
						HashMap<String, Object> subcollection = new HashMap<String, Object>();
						subcollection.put("dataJobNum", joNumber);
						StringBuilder jSB = new StringBuilder();
						jSB.append("<td border=0 class='leftArrow' data-jobNumber=" + joNumber + ">");
						jSB.append("<img src='images/UpbuttonOver.png'></img>");
						jSB.append("</td>");
						jSB.append("<td class=joNumber border=0  data-jobNumber=" + joNumber + "><a>");
						jSB.append(joNumber);
						jSB.append("</a></td><td border=0>");
						// add job grouping values
						jSB.append("<div class='jobGroupRowParent'>");
						Iterator iterator = set.iterator();
						while (iterator.hasNext()) {
							Map.Entry me = (Map.Entry) iterator.next();
							// subcollection.put(me.getKey().toString(),
							// me.getValue());
							jSB.append("<div class='jobGroupRowChild'>" + me.getValue() + "</div>");
							jSB.append(" ");
						}
						jSB.append("</div></td><td data-jobNumber=" + joNumber + " class=arrow>"); // passes
						// into
						// the
						// arrow
						subcollection.put("consolidate", jSB.toString());
						collection.put("config", subcollection);
						collection.put("CandidatesName", doc.getItemValueString("CandidatesName"));
						collection.put("CompanyName", getZip(doc.getItemValue("CompanyName")));
						collection.put("VisaStatus", doc.getItemValueString("VisaStatus"));
						nDateTime = doc.getCreated();
						collection.put("docCreated", nDateTime.getDateOnly());
						String rating = doc.getItemValueString("rating");
						if (rating != null && !rating.isEmpty()) {
							if (rating.equals("A")) rating = "1";
							if (rating.equals("B")) rating = "2";
							if (rating.equals("C")) rating = "3";
							if (rating.equals("D")) rating = "4";
						} 
						collection.put("rating", rating);
						
						if (doc.getItemValueString("form").equalsIgnoreCase("Subsheet")) {
							collection.put("categoryA", "Subs");
						} else {
							if (doc.getItemValueString("SubmittalStatus").equalsIgnoreCase("Potential")) {
								collection.put("categoryA", "Potential");
							} else {
								collection.put("categoryA", "Applies");
							}
						}
						Vector colValues = new Vector();
						colValues.clear();
						colValues = ve.getColumnValues();
						if (!colValues.isEmpty()) {
							if (colValues.elementAt(3) != null) {
								collection.put("categoryB", colValues.elementAt(3));
							}
						}
						collections.add(collection);
						// System.out.println("before loop");
						ve.recycle();
						ve = nextVE;

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// System.out.println("about to put hashmap into object");
			obj.put("data", collections);
			try {
				// System.out.println("about to put hashmap into a json format");
				// data = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
				indivPipe = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
				// System.out.println("after putting hashmap into a json format");
			} catch (JsonException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// System.out.println(e.getMessage());
				// System.out.println(e.getCause());
				e.printStackTrace();
			}

		} catch (Exception e) {
			// System.out.println(e.getMessage());
			// System.out.println(e.getCause());
			e.printStackTrace();
		} finally {
			try {
				// System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// System.out.println("about to do the new data return");
		return indivPipe;

	}

	public String getJOPipeline(String JONumberStr, String recruiterInitials) {
		String JOPipeline = null;
		// Need to create a method to read in the entries passed from getting
		// the collection
		// System.out.println(JONumberStr +
		// " Pulling JOPipe ***********************");
		java.util.Vector allObjects = new java.util.Vector();
		// System.out.println("before Session");
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		// System.out.println("after Session");
		LinkedHashMap joCollection = new LinkedHashMap();

		try {
			ArrayList<Object> collections = new ArrayList<Object>();
			// System.out.println("after array list  v 1");
			JsonJavaObject obj = new JsonJavaObject();
			// System.out.println("after declaring json object");
			Database db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			View v = db.getView("JsonJOPipeline");
			allObjects.addElement(v);
			// System.out.println("view is: " + v.getName());
			ViewEntryCollection names = v.getAllEntriesByKey(JONumberStr, true);
			allObjects.addElement(names);
			// System.out.println("Collection size: " + names.getCount());
			if (names.getCount() > 0) {

				View joView = db.getView("JsonAllJOs");
				allObjects.addElement(joView);
				Document joDoc = null;
				allObjects.addElement(joDoc);
				String joNumber = "";
				String holdingJONumber = "";

				ViewEntry ve = names.getFirstEntry();
				allObjects.addElement(ve);
				// Document tmpDoc;
				DateTime nDateTime = null;
				allObjects.addElement(nDateTime);
				Item item = null;
				allObjects.addElement(item);

				// the below will actually come from a config doc
				// the order will be set in that config doc and each will have a
				// field name
				Database thisDb = s.getCurrentDatabase();
				allObjects.addElement(thisDb);
				View configView = thisDb.getView("Admin\\User Profile");
				allObjects.addElement(configView);
				Document configDoc = configView.getDocumentByKey(recruiterInitials, true);
				allObjects.addElement(configDoc);
				if (configDoc != null) {
					item = configDoc.getFirstItem("pipelineGroupSelFieldNames");
					Enumeration values = item.getValues().elements();
					while (values.hasMoreElements()) {
						// this field holds field names, so we just need to put
						// thi in the collection
						String nodeStr = values.nextElement() + "";
						joCollection.put(nodeStr, "Not found");
						System.out.println("A selection " + nodeStr);
					}
				}

				while (ve != null) {
					Document doc = ve.getDocument();
					allObjects.addElement(doc);
					ViewEntry nextVE = names.getNextEntry(ve);
					HashMap<String, Object> collection = new HashMap<String, Object>();

					collection.put("unid", doc.getUniversalID());
					collection.put("Recruiter", doc.getItemValueString("Recruiter"));
					if (doc.getItemValueString("Form").equalsIgnoreCase("Apply")
							|| doc.getItemValueString("Form").equalsIgnoreCase("4. Apply")
							|| doc.getItemValueString("Form").equalsIgnoreCase("New 4. Apply")) {
						collection.put("JONumber", doc.getItemValueString("JONumber"));
						joNumber = doc.getItemValueString("JONumber1");
					} else if (doc.getItemValueString("Form").equalsIgnoreCase("Subsheet")
							|| doc.getItemValueString("Form").equalsIgnoreCase("6. Submittal Sheet")) {
						collection.put("JONumber", doc.getItemValueString("JONumber"));
						joNumber = doc.getItemValueString("JONumber");
					} else {
						collection.put("JONumber", doc.getItemValueString(""));
						joNumber = "";
					}

					if (holdingJONumber.equalsIgnoreCase(joNumber)) {
						// we already have this for this job number
						// System.out.println(holdingJONumber + " same as " +
						// joNumber);
					} else {
						joDoc = joView.getDocumentByKey(joNumber, true);
						// System.out.println("joNumber is " + joNumber);
						if (configDoc != null) {
							item = configDoc.getFirstItem("pipelineGroupSelFieldNames");
							Enumeration values = item.getValues().elements();
							while (values.hasMoreElements()) {
								// this field holds field names, so we just
								// need to put thi in the collection
								joCollection.put(values.nextElement() + "", "Not found");
							}
						}
						if (joDoc != null) {
							//System.out.println("Doc for " + joNumber);
							joDetails(joCollection, joNumber.toString().trim(), joDoc);
						} else {
							//System.out.println("no Doc for " + joNumber);
						}
						holdingJONumber = joNumber;
					}

					Set set = joCollection.entrySet();
					HashMap<String, Object> subcollection = new HashMap<String, Object>();
					subcollection.put("dataJobNum", joNumber);
					StringBuilder jSB = new StringBuilder();
					jSB.append("<td border=0  data-jobNumber=" + joNumber + "><div class='leftArrow'></div></td>");
					jSB.append("<td class=joNumber border=0  data-jobNumber=" + joNumber + "><a>");
					jSB.append(joNumber);
					jSB.append("</a></td><td border=0>");
					// add job grouping values
					jSB.append("<div class='jobGroupRowParent'>");
					Iterator iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry me = (Map.Entry) iterator.next();
						// subcollection.put(me.getKey().toString(),
						// me.getValue());
						jSB.append("<div class='jobGroupRowChild'>" + me.getValue() + "</div>");
						jSB.append(" ");
					}
					jSB.append("</div></td><td data-jobNumber=" + joNumber + " class=arrow>"); // passes
					// into
					// the
					// arrow

					subcollection.put("consolidate", jSB.toString());
					collection.put("config", subcollection);

					collection.put("CandidatesName", doc.getItemValueString("CandidatesName"));
					collection.put("CompanyName", getZip(doc.getItemValue("CompanyName")));
					collection.put("VisaStatus", doc.getItemValueString("VisaStatus"));
					String rating = doc.getItemValueString("rating");
					if (rating != null && !rating.isEmpty()) {
						if (rating.equals("A")) rating = "1";
						if (rating.equals("B")) rating = "2";
						if (rating.equals("C")) rating = "3";
						if (rating.equals("D")) rating = "4";
					} 
					collection.put("rating", rating);
					nDateTime = doc.getCreated();
					collection.put("docCreated", nDateTime.getDateOnly());
					// collection.put("country",
					// doc.getItemValueString("country"));

					if (doc.getItemValueString("form").equalsIgnoreCase("Subsheet")) {
						collection.put("categoryA", "Subs");
					} else {
						if (doc.getItemValueString("SubmittalStatus").equalsIgnoreCase("Potential")) {
							collection.put("categoryA", "Potential");
						} else {
							collection.put("categoryA", "Applies");
						}
					}

					Vector colValues = new Vector();
					colValues.clear();
					colValues = ve.getColumnValues();
					if (!colValues.isEmpty()) {
						if (colValues.elementAt(3) != null) {
							collection.put("categoryB", colValues.elementAt(3));
							// System.out.println(colValues.elementAt(2));
						}
					}
					collections.add(collection);
					ve.recycle();
					ve = nextVE;

				}
			}
			// System.out.println("about to put hashmap into object");
			obj.put("data", collections);
			try {
				// System.out.println("about to put hashmap into a json format");
				JOPipeline = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
				// System.out.println("after putting hashmap into a json format");
			} catch (JsonException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// System.out.println(e.getMessage());
				// System.out.println(e.getCause());
				e.printStackTrace();
			}

		} catch (Exception e) {
			// System.out.println(e.getMessage());
			// System.out.println(e.getCause());
			e.printStackTrace();
		} finally {
			try {
				// System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// System.out.println("about to do the new data return");
		return JOPipeline;

	}

	public String getJOPipelineRecuiter(String recruiterInitials) {
		String JOPipelineR = null;
		//System.out.println(recruiterInitials + " Pulling JOPipeRecruiter ***********************");
		java.util.Vector allObjects = new java.util.Vector();
		// System.out.println("before Session");
		Session s = (Session) getVariableValue("session");
		allObjects.addElement(s);
		// System.out.println("after Session");
		LinkedHashMap joCollection = new LinkedHashMap();
		java.util.Vector iVector = new java.util.Vector();

		try {
			ArrayList<Object> collections = new ArrayList<Object>();
			// System.out.println("after array list  v 1");
			JsonJavaObject obj = new JsonJavaObject();
			// System.out.println("after declaring json object");
			Database db = s.getDatabase(wkServer, "ats\\jobs10.nsf");
			allObjects.addElement(db);
			View v = db.getView("JsonAllJOsByRecruiter");
			allObjects.addElement(v);
			// System.out.println("view is: " + v.getName());
			ViewEntryCollection names = v.getAllEntriesByKey(recruiterInitials, true);
			allObjects.addElement(names);
			// System.out.println("Collection size: " + names.getCount());
			if (names.getCount() > 0) {
				ViewEntry ve = names.getFirstEntry();
				allObjects.addElement(ve);
				DateTime nDateTime = null;
				allObjects.addElement(nDateTime);
				int subs = 0;
				int feeds = 0;
				int results = 0;
				int passInt = 0;

				while (ve != null) {
					Document doc = ve.getDocument();
					allObjects.addElement(doc);
					ViewEntry nextVE = names.getNextEntry(ve);
					HashMap<String, Object> collection = new HashMap<String, Object>();

					collection.put("unid", doc.getUniversalID());
					iVector = generalUtils.getItemValueSet(doc, "NumSubmits", iVector);
					if (iVector != null) {
						if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
							passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
							collection.put("NumSubmits", passInt + "");
							subs = passInt;
						} else {
							collection.put("NumSubmits", "0");
							subs = 0;
						}
					} else {
						collection.put("NumSubmits", "0");
						subs = 0;
					}
					// System.out.println("Numsubmits is: " + subs);
					iVector = generalUtils.getItemValueSet(doc, "NumInterviews", iVector);
					if (iVector != null) {
						if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
							passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
							collection.put("NumInterviews", passInt + "");
						} else {
							collection.put("NumInterviews", "0");
						}

					} else {
						collection.put("NumInterviews", "0");
					}
					iVector = generalUtils.getItemValueSet(doc, "NumFeedback", iVector);
					if (iVector != null) {
						if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
							passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
							collection.put("NumFeedback", passInt + "");
							feeds = passInt;
						} else {
							collection.put("NumFeedback", "0");
							feeds = 0;
						}
					} else {
						collection.put("NumFeedback", "0");
						feeds = 0;
					}

					results = subs - feeds;
					collection.put("results", results + "");
					iVector = generalUtils.getItemValueSet(doc, "NumApplies", iVector);
					if (iVector != null) {
						if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
							passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
							collection.put("NumApplies", passInt + "");
						} else {
							collection.put("NumApplies", "0");
						}
					} else {
						collection.put("NumApplies", "0");
					}
					iVector = generalUtils.getItemValueSet(doc, "JONumber", iVector);
					if (iVector != null) {
						collection.put("JONumber", iVector.elementAt(0).toString());
					} else {
						collection.put("JONumber", "Not found");
					}
					iVector = generalUtils.getItemValueSet(doc, "ProperJT", iVector);
					if (iVector != null) {
						collection.put("ProperJT", iVector.elementAt(0).toString());
					} else {
						collection.put("ProperJT", "Not found");
					}
					iVector = generalUtils.getItemValueSet(doc, "CompanyName", iVector);
					if (iVector != null) {
						collection.put("CompanyName", iVector.elementAt(0).toString());
					} else {
						collection.put("CompanyName", "Not found");
					}

					StringBuilder locSB = new StringBuilder();
					iVector = generalUtils.getItemValueSet(doc, "city1", iVector);
					if (iVector != null) {
						locSB.append(iVector.elementAt(0).toString());
						locSB.append(", ");
					} else {
						iVector = generalUtils.getItemValueSet(doc, "companycity", iVector);
						if (iVector != null) {
							locSB.append(iVector.elementAt(0).toString());
							locSB.append(", ");
						}
					}

					iVector = generalUtils.getItemValueSet(doc, "state1", iVector);
					if (iVector != null) {
						locSB.append(iVector.elementAt(0).toString());
					} else {
						iVector = generalUtils.getItemValueSet(doc, "companystate", iVector);
						if (iVector != null) {
							locSB.append(iVector.elementAt(0).toString());
						} else {
							locSB.append("Not found");
						}
					}
					collection.put("location", locSB.toString());

					iVector = generalUtils.getItemValueSet(doc, "JOStatus", iVector);
					if (iVector != null) {
						collection.put("JOStatus", iVector.elementAt(0).toString());
						if (iVector.elementAt(0).toString().equalsIgnoreCase("HOT")) {
							collection.put("hotornot", "0");
						} else {
							collection.put("hotornot", "1");
						}

					} else {
						collection.put("JOStatus", "Not found");
						collection.put("hotornot", "1");
					}

					collections.add(collection);
					ve.recycle();
					ve = nextVE;
				}
			}
			// System.out.println("about to put hashmap into object");
			obj.put("data", collections);
			try {
				// System.out.println("about to put hashmap into a json format");
				JOPipelineR = JsonGenerator.toJson(JsonJavaFactory.instanceEx, obj);
				// System.out.println("after putting hashmap into a json format");
			} catch (JsonException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// System.out.println(e.getMessage());
				// System.out.println(e.getCause());
				e.printStackTrace();
			}

		} catch (Exception e) {
			// System.out.println(e.getMessage());
			// System.out.println(e.getCause());
			e.printStackTrace();
		} finally {
			try {
				// System.out.println("about to recycle Domino");
				s.recycle(allObjects);
				s.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// System.out.println("about to do the new data return");
		return JOPipelineR;

	}

	@SuppressWarnings("unchecked")
	private String getZip(final Vector v) {
		if (v.elementAt(0).getClass() == String.class) {
			return v.elementAt(0).toString();
		} else {
			return v.elementAt(0).toString().replace(".0", "");
		}

	}

	public LinkedHashMap joDetails(LinkedHashMap joCollection, String joNumber, Document joDoc) {
		// View is JsonAllJOs
		// joDoc is declared above to put in allObects for recycling
		try {
			if (joDoc != null) {
				Set set = joCollection.entrySet();
				java.util.Vector iVector = new java.util.Vector();
				Iterator iterator = set.iterator();
				int passInt = 0;

				while (iterator.hasNext()) {
					Map.Entry me = (Map.Entry) iterator.next();
					StringBuilder locSB = new StringBuilder();
					iVector = generalUtils.getItemValueSet(joDoc, me.getKey().toString(), iVector);
					if (iVector != null) {
						if (me.getKey().toString().equalsIgnoreCase("NumApplies")) {
							// in some cases we need to make sure we have an
							// integer
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
								// yes we're making an integer then back to a
								// string.
								// This if so it looks like we want for the
								// user.
							} else {
								joCollection.put(me.getKey(), "0");
							}
						} else if (me.getKey().toString().equalsIgnoreCase("NumCltFeedback")) {
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
							} else {
								joCollection.put(me.getKey(), "0");
							}

						} else if (me.getKey().toString().equalsIgnoreCase("NumInterviews")) {
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
							} else {
								joCollection.put(me.getKey(), "0");
							}
						} else if (me.getKey().toString().equalsIgnoreCase("NumSubmits")) {
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
							} else {
								joCollection.put(me.getKey(), "0");
							}
						} else if (me.getKey().toString().equalsIgnoreCase("NumWebApplies")) {
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
							} else {
								joCollection.put(me.getKey(), "0");
							}
						} else if (me.getKey().toString().equalsIgnoreCase("NumEmailsSent")) {
							if (generalUtils.isNumeric(iVector.elementAt(0).toString())) {
								passInt = Double.valueOf(iVector.elementAt(0) + "").intValue();
								joCollection.put(me.getKey(), passInt + "");
							} else {
								joCollection.put(me.getKey(), "0");
							}
						} else {
							// not something we need to keep a number
							joCollection.put(me.getKey(), iVector.elementAt(0));
						}
					} else {
						joCollection.put(me.getKey(), ""); // this puts a value
						// in. It will be
						// overridden only
						// in the special
						// cases below
						// we have 2 special cases in city and state
						if (me.getKey().toString().equalsIgnoreCase("city1")) {
							// if city, we will also need state
							iVector = generalUtils.getItemValueSet(joDoc, "city1", iVector);
							if (iVector != null) {
								locSB.append(iVector.elementAt(0) + ", ");
								// now get the state
								iVector = generalUtils.getItemValueSet(joDoc, "state1", iVector);
								if (iVector != null) {
									locSB.append(iVector.elementAt(0));
								}
							} else {
								iVector = generalUtils.getItemValueSet(joDoc, "companycity", iVector);
								if (iVector != null) {
									// joCollection.put(me.getKey(),
									// iVector.elementAt(0));
									locSB.append(iVector.elementAt(0) + ", ");
									iVector = generalUtils.getItemValueSet(joDoc, "companystate", iVector);
									if (iVector != null) {
										locSB.append(iVector.elementAt(0));
									}
								}
							}
							joCollection.put(me.getKey(), locSB.toString());
						}
					}
					//System.out.println("value: " + joCollection.get(me.getKey()));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return joCollection;
	}

	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}
}
