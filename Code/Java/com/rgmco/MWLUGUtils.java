package com.rgmco;

import java.io.*;

import javax.faces.context.*;

import lotus.domino.*;

public class MWLUGUtils implements Serializable {

	/**
	 * Unique serial ID used with serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Database object representing the current database
	 */
	private static Database db;

	/**
	 * Document object that can be returned
	 */
	private static Document doc;

	/**
	 * Public no-argument constructor
	 */
	public MWLUGUtils() {

	} // end MWLUGUtils method

	/**
	 * Return an instantiated db object
	 * 
	 * @return the db
	 */
	public static Database getDb() {

		try {
			// Create a new Session object
			Session s = (Session) getVariableValue("session");

			// Get the current database
			db = s.getCurrentDatabase();

		} catch (NotesException e) {
			e.printStackTrace();
		}

		// Return the database object
		return db;
	}

	/**
	 * @return the doc
	 */
	public static Document getDoc(String UNID) {

		doc = null;

		try {
			doc = getDb().getDocumentByUNID(UNID);
		} catch (NotesException e) {
			e.printStackTrace();
		}

		return doc;
	} // end method getDoc

	// UTILITY METHODS =============================

	/**
	 * 
	 * Returns a global variable from the FacesContext.
	 * <p>
	 * Used to grab a global variable such as a session or database object.
	 * 
	 * @param varName
	 * @return
	 */
	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}

	/**
	 * 
	 * Utility method - Reads object from session scope
	 * 
	 * @param key
	 * @return
	 */
	public static Object getSessionMapValue(String key) {
		return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
	} // end method getSessionMapValue

	/**
	 * Utility method - Sets object in session scope
	 * 
	 * @param key
	 * @param value
	 */
	public static void setSessionMapValue(String key, Object value) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);
	} // end method setSessionmapValue

} // end class
