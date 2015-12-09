package ch.hasselba.xpages;

import javax.faces.*;
import javax.faces.context.*;
import javax.faces.lifecycle.*;

import com.ibm.xsp.*;
import com.ibm.xsp.context.*;

/**
 * ThemeSwitcherFacesContextFactory
 * 
 * @author Sven Hasselbach
 * @version 0.1
 */
public class ThemeSwitcherFacesContextFactory extends FacesContextFactory {

	private FacesContextFactory delegate;

	@SuppressWarnings("unchecked")
	public ThemeSwitcherFacesContextFactory() {
		try {
			Class clazz = Class.forName("com.sun.faces.context.FacesContextFactoryImpl");
			this.delegate = (FacesContextFactory) clazz.newInstance();
		} catch (Exception e) {
			throw new FacesExceptionEx(e);
		}
	}

	public ThemeSwitcherFacesContextFactory(FacesContextFactory fcFactory) {
		this.delegate = fcFactory;
		if ((this.delegate instanceof FacesContextFactoryImpl)) {
			this.delegate = ((FacesContextFactoryImpl) this.delegate).getDelegate();
		}
	}

	public FacesContext getFacesContext(Object param1, Object param2, Object param3, Lifecycle paramLC) throws FacesException {
		FacesContext fc = this.delegate.getFacesContext(param1, param2, param3, paramLC);
		return new ThemeSwitcherFacesContext(fc);
	}
}