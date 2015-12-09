// core JS
/*
$(document).ready(function(){
	$(".longpress").longPress();
}); */

function createCookie(name, value, days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
		var expires = "; expires=" + date.toGMTString();
	} else
		var expires = "";
	document.cookie = name + "=" + value + expires + "; path=";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for ( var i = 0; i < ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0) == ' ')
			c = c.substring(1, c.length);
		if (c.indexOf(nameEQ) == 0)
			return c.substring(nameEQ.length, c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name, "", -1);
}
var executeOnServer = function () {
    // must supply event handler id or we're outta here....
    if (!arguments[0])
        return false;
    // the ID of the event handler we want to execute
    var functionName = arguments[0];

    // OPTIONAL - The Client Side ID that you want to partial refresh after executing the event handler
    var refreshId = (arguments[1]) ? arguments[1] : "@none";
    var form = (arguments[1]) ? XSP.findForm(arguments[1]) : dojo.query('form')[0];

    // OPTIONAL - Options object contianing onStart, onComplete and onError functions for the call to the 
    // handler and subsequent partial refresh
    var options = (arguments[2]) ? arguments[2] : {};

    // Set the ID in $$xspsubmitid of the event handler to execute
    dojo.query('[name="$$xspsubmitid"]')[0].value = form.id + ':' + functionName;
    XSP._partialRefresh("post", form, refreshId, options);
}