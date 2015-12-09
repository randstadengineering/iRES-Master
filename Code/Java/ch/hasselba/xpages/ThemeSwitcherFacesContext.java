package ch.hasselba.xpages;

import javax.faces.context.*;

import com.ibm.xsp.application.*;
import com.ibm.xsp.context.*;
import com.ibm.xsp.stylekit.*;

/**
 * ThemeSwitcherFacesContext allows to switch the theme during runtime
 * 
 * @author Sven Hasselbach
 * @version 0.1
 */
public class ThemeSwitcherFacesContext extends FacesContextExImpl {

	private StyleKit styleKit;
	private String styleKitId;
	private FacesContext delegated;

	/**
	 * constructor
	 * 
	 * @param fc
	 *            delegated javax.faces.context.FacesContext
	 */
	public ThemeSwitcherFacesContext(FacesContext fc) {
		super(fc);
		this.delegated = fc;
	}

	/**
	 * returns current StyleKit
	 * 
	 * @return com.ibm.xsp.stylekit.StyleKit the StyleKit used for rendering the view
	 */
	public StyleKit getStyleKit() {
		if (this.styleKit == null) {
			this.styleKit = super.getStyleKit();
		}
		return this.styleKit;
	}

	/**
	 * sets the StyleKit
	 * 
	 * @param stlyeKit
	 *            the com.ibm.xsp.stylekit.StyleKit to use
	 */
	public void setStyleKit(final StyleKit styleKit) {
		this.styleKit = styleKit;
	}

	/**
	 * returns the current StyleKitId (aka Theme name)
	 * 
	 * @return String the id of the StyleKit
	 */
	@Override
	public String getStyleKitId() {
		if (this.styleKitId == null) {
			this.styleKitId = super.getStyleKitId();
		}
		return styleKitId;
	}

	/**
	 * sets the StyleKitId
	 * 
	 * @param styleKitId
	 *            the id of the StyleKit
	 */
	public void setStyleKitId(final String styleKitId) {
		this.styleKitId = styleKitId;
	}

	/**
	 * initializes the StyleKit for the current view
	 */
	public void loadStyleKit() {
		this.styleKit = ((ApplicationExImpl) getApplication()).getStyleKit(this.styleKitId);
	}
}