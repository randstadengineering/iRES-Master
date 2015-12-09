package com.randstatusa;

import java.io.*;
import java.util.*;
import javax.faces.context.*;
import org.openntf.xsp.debugtoolbar.components.*;
import lotus.domino.*;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class GetFilesFromNotesDocs implements Serializable {
	private static final long serialVersionUID = 1L;

	public GetFilesFromNotesDocs() {

	}

	public String GetFilesFromNotesDocs(String strNoteID) {

		java.util.Vector allObjects = new java.util.Vector();
		Session session = (Session) getVariableValue("session");
		String attachmentName = "";
		String binaryName = "c:\\dxl\\" + strNoteID + "-" + System.nanoTime() + ".txt";
		String res = "";
		StringBuilder sb = new StringBuilder();
		
		
		try {
			Database db = session.getDatabase("", "JFS/cand_file_archive.ntf");
			allObjects.addElement(db);
			lotus.domino.Document doc = db.getDocumentByID(strNoteID);
			// lotus.domino.Document doc = db.getDocumentByUNID(strNoteID); //uncomment if we call via UNID
			allObjects.addElement(doc);
			final String attachmentField = "Attachments"; // whatever we want to call the field

			if (doc.hasItem(attachmentField)) {
				Item item = doc.getFirstItem(attachmentField);
				item.getType();
				RichTextItem nrt = null;
				switch (item.getType()) {
				case Item.ATTACHMENT:
					nrt = (RichTextItem) doc.getFirstItem(attachmentField);
					break;
				case Item.EMBEDDEDOBJECT:
					nrt = (RichTextItem) doc.getFirstItem(attachmentField);
					break;
				case Item.RICHTEXT:
					nrt = (RichTextItem) doc.getFirstItem(attachmentField);
					break;
				}
				if (nrt != null) {
					// dBar.info("message from a bean to the toolbar", "just before eos vector");
					java.util.Vector eos = nrt.getEmbeddedObjects();
					if (eos.isEmpty()) {
						// no files do nothing
						// System.out.println("eos is empty");
					} else {
						Enumeration e = eos.elements();
						// dBar.info("message from a bean to the toolbar", "just set enumerations");
						while (e.hasMoreElements()) {
							// dBar.info("message from a bean to the toolbar", "have more Elements");
							EmbeddedObject eoA = (EmbeddedObject) e.nextElement();
							allObjects.addElement(eoA);
							// dBar.info("message from a bean to the toolbar", "Type: " + eoA.getType() + " " + eoA.getName());
							switch (eoA.getType()) {
							case EmbeddedObject.EMBED_ATTACHMENT:
								// dBar.info("message from a bean to the toolbar", "an attachment..");
								EmbeddedObject att = doc.getAttachment(eoA.getName());
								// System.out.println("In RT name " + eoA.getName());
								attachmentName = eoA.getName();
								allObjects.addElement(att);
							} // closing switch, we only care about attachments. 

						}
					}

				} else {
					//
					// System.out.println("RT Field is null");
					attachmentName = checkDocumentAttachment(session, attachmentName, doc);
				}
			} else {
				//
				// System.out.println("no attachment field");
				attachmentName = checkDocumentAttachment(session, attachmentName, doc);

			}

			if (attachmentName.length() > 2) {
				
				// we have an attachment, now turn it into a binary
				EmbeddedObject att = doc.getAttachment(attachmentName);
				allObjects.addElement(att);
				InputStream isA = att.getInputStream();
				byte[] b = IOUtils.toByteArray(isA);
				res = new String( b , Charsets.ISO_8859_1 );
				
				/*
				//DataInputStream input = new DataInputStream(isA);
				try {
					while (true) {
						sb.append(input.read);
						// System.out.println(Integer.toBinaryString(input.readByte()));
					}
				} catch (EOFException eof) {

				} catch (IOException e) {
					e.printStackTrace();
					// System.out.println("in catch");
				}
				res = sb.toString();
				
				//char[] tmp = IOUtils.toCharArray(isA);
				//res = tmp.toString();
				
				//byte[] bytes = IOUtils.toByteArray(isA);
				//res = bytes.toString();
				//res = getBase64url(bytes);
				/*
				DataInputStream input = new DataInputStream(isA);
				try {
					while (true) {
						sb.append(Integer.toBinaryString(input.readByte()));
						// System.out.println(Integer.toBinaryString(input.readByte()));
					}
				} catch (EOFException eof) {

				} catch (IOException e) {
					e.printStackTrace();
					// System.out.println("in catch");
				}
				res = sb.toString();
				*/
				//writeTextFile(sb.toString(), binaryName);
				// sb.toString();
				// System.out.println("result: " + sb.length() + " " + sb.toString());
			}
			
		} catch (Exception e) { // catch at end of main try
			e.printStackTrace();
		} finally {
			try {
				session.recycle(allObjects);
				session.recycle();
				System.runFinalization();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;

	}

	private String checkDocumentAttachment(Session session, String attachmentName, lotus.domino.Document doc) throws NotesException {
		Iterator attIterator = null;
		Double count = (Double) session.evaluate("@Attachments", doc).elementAt(0);
		int counter = 0;
		if (count.intValue() > 0) {
			attIterator = session.evaluate("@AttachmentNames", doc).iterator();
			while (attIterator.hasNext() && counter < 1) {
				attachmentName = attIterator.next().toString();
				if (counter == 0) {
					// System.out.println("first file is " + attachmentName);

					counter++;
				}
			}

		}
		return attachmentName;
	}
  private String getBase64url(byte[] fileData) {
	  Base64 encoder = new Base64(true);
	  String encodedString = encoder.encodeBase64URLSafeString(fileData);
	  return encodedString;
  }
	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}

	private static void writeTextFile(String stringValue, String fileName) {
		try {
			FileWriter outStream = new FileWriter(fileName, true);
			outStream.write(stringValue);
			outStream.write("\n");
			outStream.close();
		} catch (IOException e) {
			System.out.println("IOERROR: " + e.getMessage() + "\n");
			e.printStackTrace();
		}
	} // writeTextFi
}
