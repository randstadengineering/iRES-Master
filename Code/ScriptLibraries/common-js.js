var selectedJob;
var timer = null;
var dialURL = "http://10.100.1.121/re_ws.nsf/cucmDialer?OpenAgent";
/*
$.ajax({
	  xhr: function()
	  {
	    var xhr = new window.XMLHttpRequest();
	    //Upload progress
	    xhr.upload.addEventListener("progress", function(evt){
	      if (evt.lengthComputable) {
	        var percentComplete = (evt.loaded / evt.total) * 100;
	        console.log(percentComplete);
	        $('#progressBar').attr('value', percentComplete);
	      }
	    }, false);
	    //Download progress
	    xhr.addEventListener("progress", function(evt){
	      if (evt.lengthComputable) {
	        var percentComplete = (evt.loaded / evt.total) * 100;
	        console.log(percentComplete);
	        $('#progressBar').attr('value', percentComplete);
	      }
	    }, false);
	    return xhr;
	  },
	  type: 'POST',
	  url: "/",
	  data: {},
	  success: function(data){
		  $('#progressBar').attr('value', 0);
	  }
	});
*/
function dialPhone(tn,pass,num) {
	var tel=num.substring(num.indexOf(":")+1,num.length).replace(/[^\d]/g, "");
	var url=dialURL + "&user=" + tn + "&password=" + pass + "&args=9" + tel + "&args=" ;

	console.log('dialPhone: ' + url);
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
	  var xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
	  var xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.open("GET",url,false);
	//xmlhttp.send();
}
function getSelected(opt) {
	var selected = new Array();
	var index = 0;
	for (var intLoop=0; intLoop < opt.length; intLoop++) {
	   if (opt[intLoop].selected) {
	      index = selected.length;
	      selected[index] = new Object;
	      selected[index].value = opt[intLoop].value;
	      selected[index].index = intLoop;
	   }
	}
	return selected;
}
function getUrlParameter(fetch) {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars[fetch];
}
function indexOf(array, value) {
	for (var i = 0, l = array.length; i < l; ++i) {
		if (array[i] === value) return i;    
	}    
	return -1;
}
function isArray(obj) {
	   if (obj.constructor.toString().indexOf("Array") == -1)
	      return false;
	   else
	      return true;
	}
function isSetURLParameter(name) {
    return (new RegExp('[?|&]' + name + '(?:[=|&|#|;|]|$)','i').exec(location.search) !== null)
}
//@LeftBack equivalent
function leftBack(sourceStr, keyStr){
arr = sourceStr.split(keyStr)
arr.pop();
return (keyStr==null | keyStr=='') ? '' : arr.join(keyStr)
}
function ltrim(str, chars) {
	chars = chars || "\\s";
	return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
}
function filterPipeline() {
	var table1 = $("#jQDTPipeline").DataTable();
	//check for selected job by inspecting ajax url for JONumber paramater
	if (table1.ajax.url()) {
		if (table1.ajax.url().indexOf('JONumber') > -1) {
			//get job number
			var jobnum = middle(table1.ajax.url(),'JONumber=','&');
			//set one time event to move the job row after data reload for selected job
			table1.one('draw', function(){
				moveJobGroupingRow(jobnum);
				showSelectJob(false);
			});
		}
	};
	table1.draw(false);
}
function loadPipeline(jobnum, initials) {
	showBusy(true);
	selectedJob = jobnum;
	createCookie('selectedJob',jobnum,0);
	var table1 = $("#jQDTPipeline").DataTable();
	//set one time event to move the job row after data reload for selected job
	table1.on('draw', function(){
		if (selectedJob) {
			 //check for data and enable all jobs button if none
			if ($('.dataTables_empty').html() == 'No data available in table') {
				$('.btnAlljobs').removeClass('inactive');
			} else {
				$('.btnAlljobs').addClass('inactive');
			}
			moveJobGroupingRow(jobnum);
			showSelectJob(false);
		};
	});
	//update table for selected job
	table1.ajax.url( 'RESTServiceProvider.xsp/JOdata?JONumber=' +  jobnum + "&recruiterInitials=" + initials).load();
	refreshLeftNav(jobnum);
	showViewResume(false);	
}
function logLastResult() {
	/*
	 //LOG LAST RESULT
var root="#{javascript:@LeftBack(context.getUrl(),'/');}";
var targURL=root + "/logCallLR?OpenAgent&job=#{javascript:viewScope.joNumber}" +
"&profileID=#{javascript:compositeData.applyDoc.getItemValueString('CandidateUniqueID2');}";
targURL+="&user=" + escape("#{javascript:sessionScope.user_name}");
xmlhttp.open("GET",targURL,false);
//xmlhttp.send();
console.log("LR URL: " + targURL);
	 */
}
function Mid(str, start, len){
	if (start < 0 || len < 0) return "";
	var iEnd, iLen = String(str).length;
	if (start + len > iLen)
	        iEnd = iLen;
	else
	        iEnd = start + len;
	
	return String(str).substring(start,iEnd);
}
//@Middle equivalent
function middle(sourceStr, keyStrLeft, keyStrRight){ 
return strLeft(strRight(sourceStr,keyStrLeft), keyStrRight);
}

function moveJobGroupingRow(selJob) {
	$('#contextMenuJobGrouping').empty();
	$('#jQDTPipeline > tbody > tr.nohover > td > table').each(function (index) {
		var thisJob = $(this).find('.joNumber').attr('data-jobnumber');
		if (thisJob == selJob) {
			$(this).appendTo($('#contextMenuJobGrouping'));
			$(this).find('td.leftArrow').css('display','none');
			$('.btnAlljobs.inactive').removeClass('inactive'); //set all jobs button active
			//re-attach onclick events for job number and job details arrow 
			return false;
		}
	});	
}

function partialRefreshOnIds(ids,post){

	if (ids.constructor != Array) ids = [ids];
	var ids2=new Array();
	var funct=(post)?'XSP.partialRefreshPost':'XSP.partialRefreshGet';
	
	for (var i in ids) {
		try {
			var idsarr=dojo.query('[id$="' + ids[i] + '"]');
			for (var ia=0;ia<idsarr.length;ia++) {
				ids2.push(funct + '("' + idsarr[ia].id + '"');
			}
		} catch (e) {}
	}
	ids=ids2;
	var exec="";
	for (var ct=1;ct<ids.length;ct++) {
		exec+=ids[ct-1]+', {onComplete: function() {';
	}
	exec+=ids[ids.length-1] + ');';
	
	if (ids.length>1) {
		for (var ct=1;ct<ids.length;ct++) {
			exec+="}})"
		}
	}
	eval(exec);
	try { showBusy(false); } catch (e) {};
	
}

//Author: Phillip Roberts - phillroberts@yahoo.com
//Released under GNU General Public License : http://www.gnu.org/copyleft/gpl.html

function refreshLeftNav(jobnum) {
	var navid = $("div[id$='leftNavRepeatPanel']")[0].id; //gets leftNav id
	XSP.partialRefreshGet( navid , {
		params: { 'joNumber': jobnum},
		onComplete: function() { 
			leftNavListener();
			}
		});	
}

function replaceSubstring(exp, str, wit) { return exp.split(str).join(wit); }  
//@RightBack equivalent
function rightBack(sourceStr, keyStr){
arr = sourceStr.split(keyStr);
return (sourceStr.indexOf(keyStr) == -1 | keyStr=='') ? '' : arr.pop()
}
function Round(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}
function rtrim(str, chars) {
	chars = chars || "\\s";
	return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
}
function sendEmailFromTemplate(row) {
	var tempId = $(row).attr('template');
	var doc = $(row).attr('doc');
	var db = $(row).attr('db');
	console.log([tempId, doc, db]);
	var id="#{javascript:"
	var url="/ires/mailTemp.nsf/gmailSendTemplate.xsp?template=" + tempId + 
	"&doc=" + doc + "&db=" + db;
	$('#work').attr('src',url);
}
function setCookie(name, value, expires, days) {
            var cookies = document.cookie;            // cookies for site
            var ix1;                                  // index
            var n1 = name;                            // main cookie name
            var n2 = "";                              // crumb name
            var out;                                  // returned value
            var p2 = value;                           // new value
            var p3 = expires;                         // param work area
            var p4 = days;
            var msd = 1000 * 60 * 60 * 24;            // millisecs per day
            if (!p3) {                                // no expiration
              if (!p4) { p4 = 1; }                    // no days: use 1
              p3 = new Date();
              p3.setTime(p3.getTime() + msd * days);
            }
            ix1 = n1.indexOf(";");                    // cookie/crumb break
            if (ix1 != -1) {
              n2 = n1.substring(ix1 + 1);             // crumb name
              n1 = n1.substring(0, ix1);              // main cookie name
            }
            out = getCookieCrumb(cookies, n1);
            if (n2) {
              if (out) { out = getCookieCrumb(out, n2); }
              else { rest = ""; }
              if (rest) { p2 = rest + ";" + n2 + "=" + p2; }
              else      { p2 =              n2 + "=" + p2; }
            }
            document.cookie=n1+"="+escape(p2)+"; expires="+p3.toGMTString();
            return true;
          }
                
function setSelect(obj,val){
	for(index = 0; index < obj.length; index++) {
		try {
			if(obj[index].value == val){
		    	obj.selectedIndex = index;
		    }
		} catch (e) {}
	}
}
function setSearchListener(obj) {
	$('#search').unbind('keyup');
	$('#search').keyup( function() {
    	obj.dataTable().fnFilter(this.value);
    });
}
function showBusy(state) {
	if (!state) { $('#progressBar').attr('value', 100); return; };
	if ( timer && timer.timerState == 'running') return;
	
		timer=new Timer(200, function () {
	    var bar = $('#progressBar');
		var val = parseInt( bar.attr('value'), 10);
		val = (isNaN(val)) ? 0 : val;
		if (val >= 100) {
			bar.attr('value', 0);
			timer.cancel();
		} else {
			bar.attr('value', val + 5);
		}
		console.log(bar.attr('value'));
    });
	timer.start();
}

String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}
//@Left equivalent
function strLeft(sourceStr, keyStr){
return (sourceStr.indexOf(keyStr) == -1 | keyStr=='') ? '' : sourceStr.split(keyStr)[0];
}

//@Right equivalent
function strRight(sourceStr, keyStr){
idx = sourceStr.indexOf(keyStr);
return (idx == -1 | keyStr=='') ? '' : sourceStr.substr(idx+ keyStr.length);
}





function unique(arrayName)
{
    var newArray=new Array();
    label:for(var i=0; i<arrayName.length;i++ )
    {  
        for(var j=0; j<newArray.length;j++ )
        {
            if(newArray[j]==arrayName[i]) 
                continue label;
        }
        newArray[newArray.length] = arrayName[i];
    }
    return newArray;
  }

function zoomResume(v) {
	$('.resumeViewer font').each(function(index) {
		if ($(this).attr('size')) {
			$(this).attr('size',parseInt($(this).attr('size'),10) + v);
		};
	});	
}

/** class Timer **/
var Timer = function(delayMs, callbackFunc) {
    this.delayMs = delayMs;
    this.callbackFunc = callbackFunc;
    this.timerState = 'new';
}
Timer.prototype.start = function() {
    if( this.tmr ) return;

    var self = this;
    this.timerState = 'running';
    this.tmr = setTimeout(function() { self._handleTmr(); }, this.delayMs);
}
Timer.prototype.cancel = function() {
    if( ! this.tmr ) return;

    clearTimeout(this.tmr);
    this.tmr = null;
    this.timerState = 'canceled';
}
Timer.prototype._handleTmr = function() {
    this.tmr = null;
    this.timerState = 'completed';
    this.callbackFunc();
}