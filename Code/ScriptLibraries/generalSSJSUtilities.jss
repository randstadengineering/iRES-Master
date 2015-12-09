//var jobs10 = new Array("WebApp1a/ats", "ats/jobs10.nsf");
//var prof10 = new Array("WebApp1a/ats", "ats/prof10.nsf");
var jobs10 = new Array("DominoDev2/ats", "ATS\\jobs10.nsf");
var prof10 = new Array("DominoDev2/ats", "ats/prof10.nsf");

var recycleObject = function(object) {  
	if (null != object) {
	    try { 
	    	if (object.getClass().getName() != "com.ibm.domino.xsp.module.nsf.NSFComponentModule$XPagesDatabase") {
	    		//dBar.info("RECYCLED: " + object.getClass().getName());
	    		object.recycle(); 
				object = null;
	    	}
  		} catch (e) {};
	}
}

//this may duplicate some of the stuff in the bean, but it's for a different code base (SSJS, natch)
function SSJSnewOrAppendItemValue(fieldname:String, inputValue:Object, nDoc:Document) {
		//object is valid for the value of an item
		//this may be a cool thing to move into a 
		//call as: newOrAppendItemValue("statusHistory", "Text or other to add", nDoc);
		//System.out.println("in the newOrAppendItemValue " + fieldname);
	//print('Starting function ' + fieldname);
		try {
			if (nDoc.hasItem(fieldname)){
				//print('append value');
				var oV:java.util.Vector = nDoc.getItemValue(fieldname);
				oV.addElement(inputValue);
				nDoc.replaceItemValue(fieldname, oV);
			} else {
			//	print('new item');
				nDoc.replaceItemValue(fieldname, inputValue);
			}
		} catch (e) {	
			//print('error is ' + e.toString());
			e.toString();
		}
	}

function SSJSnowDateTimeStampString(){
	var dt:NotesDateTime = session.createDateTime("Today");
	dt.setNow();
	return dt.getDateOnly() + " " + dt.getTimeOnly();
}

function SSJSgetItemValueSet(iDoc:NotesDocument, iItemName:String, iVector:java.util.Vector) {
	//this is designed to see if there is any value in the field, and if so, to get all of it.
	//if there is only one value, still put it in a vector
	//if null, put null in as the value
	//java.util.Vector.size() is the # of elements in the vector
	//call as: iVector = SSJSgetItemValueSet(nDoc, approvedField, iVector);
	//this overloaded method is for when we want to do this from an XPage, and we can't pass a Notes object (like a Doc) into a bean, 
	iVector = null; // always set to null
	//print('going to look for ' + iItemName);
		try {
			if (iDoc.hasItem(iItemName)) {				
				var iItem:NotesItem = iDoc.getFirstItem(iItemName);
				//iVector = iItem.getValues();	
				var passObj = getValueAsVector(iItem.getValues());
				iVector = passObj;
				//print('returing from obj');
			} else {
			//	print('returing a null');
				iVector = null;
			}
		} catch (e) {
			//print('error is ' + e.toString());
			e.toString();
		}	
	
	//print('returing iVector ');
	return iVector;
}


function getValueAsVector(obj){
    switch(typeof obj){
        case "java.util.Vector":
            //it's already a Vector, just return it
            return obj;
            break;
        case "java.util.ArrayList":
            //it's an ArrayList, return it as a Vector
            var x:java.util.Vector = new java.util.Vector();
            for(i=0;i<obj.size();i++){
                x.add(obj[i]);
            }
            return x;
            break;
        case "Array":
            //it's an Array prototype, return it as a Vector
            var x:java.util.Vector = new java.util.Vector();
            for(i=0;i<obj.length;i++){
                x.add(obj[i]);
            }
            return x;
            break;
        default:
            //it's most likely a String, return it as a Vector
            var x:java.util.Vector = new java.util.Vector();
            x.add(obj);
            return x;
            break;
    }
}

function addFacesMessage(message, component){
// Here is how it is called: addFacesMessage('stuff','inputText6');
	try { 
	if(typeof component === 'string' ){
	component = getComponent(component);
	}

	var clientId = null;
	if (component) {
	clientId = component.getClientId(facesContext);
	}

	facesContext.addMessage(clientId, 
	new javax.faces.application.FacesMessage(message));
	} catch(e){ }
	}

function getCookie(cookieName){
	var c = facesContext.getExternalContext().getRequestCookieMap().get(cookieName)
	return (c!=null)?c.getValue():""
}

function setCookie(cookiename, cookieval, expires){
	response = facesContext.getExternalContext().getResponse(); 
	userCookie = new javax.servlet.http.Cookie(cookiename, cookieval);
	if(expires) userCookie.setMaxAge(expires*24*60*60*1000); 
	response.addCookie(userCookie); 
}
function eraseCookie(cookieName) {
	setCookie(cookieName,'',-1);
}

