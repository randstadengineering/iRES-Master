//GLOBAL FUNCTIONS


//clear resume and note state cookies on reload
eraseCookie('groupingPaneState');
eraseCookie('notesPaneState');
eraseCookie('resumeViewer');
var catA;
var catB;
var filterCatA;
var filterCatB;

jQuery(document).ready(function($){
	//showBusy(true);
	
	
	//hide view resume and notes panes (this cleans up any accidental open on refresh or load)
	$('.resumeSlideout').removeClass('is-visible');
	$('.notesPane').removeClass('is-visible');
	
	//### Listeners for MastHead Menu
	$('.mastHeadMenu').each(function(index, value){
		var id = $(this).attr('id');
		$('#' + id).on('click', function() { changeNav(id) });
	});


	
	window.selectPipelineRow = function(unid) {
		var lastRow = readCookie('lastDoc');
		$('#' + lastRow).removeClass('selected');
		$('#' + unid).addClass('selected');
	}
	
	window.leftNavListener = function() {
		//### LEFT NAV
		//listener for leftNav
		$('.CatA, .CatB').on('click', function() {
			var id = this.id;
			var cn = this.className;
			//selects catB
			if (cn.indexOf('CatB')>-1) {
				//switch to pipeline
				changeNav('pipeline');
				//clear other selection
				$('.CatB').removeClass('selected');
				var rtn =  $(this).attr('data-status');
				filterCatB = rtn;
				filterPipeline(); 
				if (catB) $('#' + catB).removeClass('selected');
				catB = id;
				$(this).addClass('selected');
				//hide all catA except this one
				$('.CatB:not(div[data-cat=' + $(this).attr('data-cat') + '])').css('display','none');
				$('#jQDTPipeline').DataTable().ajax.reload();
				showViewResume(false);
				
			} else if (cn == 'CatA') {
				var tmp = $(this);
				$('.CatB').css('display','none');
				$('div[data-cat=' + $(this).attr('data-pcat') + ']').css('display','block');
			}	
		});
	}
	leftNavListener();
	
	//###Select Job Button
	$('.btnSelectjob').on('click', function() {
		showSelectJob('toggle');
	});
	//###Utility Menu Button
	$('.btnUtilityMenu').on('click', function() {
		showUtilityMenu('toggle');
	});
	
	//default selected
	if (!catA) {
		$('.CatA a:first-child, .CatB a:first-child').each(function(index, value) {
			$('.CatB').css('display','none');
			$('div[data-cat=Applies').css('display','block');
			if ($(this).parent().attr('data-status') == 'Sent for review') {
				$(this).parent().addClass('selected');
				catB = this.id;
			}
		})
	}
	
});
function adjustCounters(fromStatus, toStatus) {
	var fDiv = $('div[data-status="' + fromStatus + '"] > a > span');
	var tDiv = $('div[data-status="' + toStatus + '"] > a > span');
	fDiv.html(parseInt(fDiv.html(),10)-1);
	tDiv.html(parseInt(tDiv.html(),10)+1);
	//console.log(fDiv.html() - tDiv.html());
}
function checkLeftNavForUpdate(catB) { 
	$('.CatA a:first-child, .CatB a:first-child').each(function(index, value) {
		if (this.innerHTML == catB) {
			return false;
		}
	});
	refreshLeftNav(selectedJob);
}
function closeFormDialog() {
	XSP.closeDialog($('.dijitDialog').attr('id'));
}
function goUtility() {
	$('#utility').removeClass('round-button');
	$('#utility').addClass('')
}

function changeNav(selected) {
	//loop through context menu icons and find currently selected
	$('.mastHeadMenu').each(function(index, value){
		if (!$(this).hasClass('round-button')) { 
			//close currently open
			$(this).removeClass();
			$(this).addClass('round-button mastHeadMenu');
			$(this).html($(this).attr('id').substring(0,1).toUpperCase());
			//hide context menu
			$('.' + $(this).attr('id') + 'Menu').removeClass('is-visible');
			//hide working pane
			$('.' + $(this).attr('id') + 'View').removeClass('is-visible');
			//hide view resume
			showViewResume(false);
		};
	});
	//open selected menu button
	var cl = $('#' + selected);
	cl.removeClass();
	cl.addClass(selected + ' mastHeadMenu');
	cl.html(selected.toUpperCase());
	//open context menu
	$('.' + selected + 'Menu').addClass('is-visible');
	//open working pane
	$('.' + selected + 'View').addClass('is-visible');
	//
}
function loadJobOrder(jonum) {
	//showBusy(true);
	url="editJobOrder.xsp?cid=#{javascript:compositeData.sourceID}&joid=#{javascript:dsJo.getDocument().getUniversalID()}";
	window.open(url,"joborder_#{javascript:dsJo.getNoteID()}","toolbar=0,status=0,scrollbars,height=600,width=850");
}
function loadUtility(url) {
	$('#utilityViewFrame').attr('src',url);
	showUtilityMenu(false);
}
function loadCandidate(unid) {
	console.log("Candidate UNID: " + unid);
	changeNav('candidate');
	var url = "CandidateProfile.xsp?documentId=" + unid + "&candidateCC=contactInfo&action=readDocument";
	$('#candidateViewFrame').attr('src',url);
}
function minimizeFormDialog() {
	var id = $('.dijitDialog').attr('id');
	//XSP.closeDialog(id);
	showDijitDialog(id,false);
	parkNode(id,'dialog',"Submit Dialog: " + id);
};
function moveJobGroupingRow(selJob) {
	$('#contextMenuJobGrouping').empty();
	$('#jQDTPipeline > tbody > tr.nohover > td > table').each(function (index) {
		var thisJob = $(this).find('.joNumber').attr('data-jobnumber');
		if (thisJob == selJob) {
			$(this).appendTo($('#contextMenuJobGrouping'));
			$(this).find('td.leftArrow').css('display','none');
			//set listener on job details arrow
			$(this).find('td.arrow').on('click', function() {
				toggleJobDetailOverlay(thisJob);
				$('#jQDTPipeline > tbody > tr.nohover').attr('style','display');
			});
			//hide first row which is the job details overlay
			$('#jQDTPipeline > tbody > tr.nohover').attr('style','display:none');
			$('.btnAlljobs.inactive').removeClass('inactive'); //set all jobs button active
			//re-attach onclick events for job number and job details arrow 
			return false;
		}
	});	
}
function navSetStatus(catA, catB) {
	var table1 = $('#jQDTPipeline').DataTable();
	var row = $('#' + getCookie('lastDoc'));
	var rowData = table1.row(row).data();
	//get nextrow
	var idx = table1.column(5).data().indexOf(rowData.unid);
	var nextRow = table1.row(idx + 1);
	//hide current row
	//
	//rowData.categoryA = catA;
	row.css('display','none');
	selectPipelineRow(nextRow.data().unid);
	document.cookie = "nextDoc=" + nextRow.data().unid;
	adjustCounters(rowData.categoryB, catB);
}
function navSetRating(rating) {
	var table1 = $('#jQDTPipeline').DataTable();
	var row = $('#' + getCookie('lastDoc'));
	var data = table1.row(row).data();
	data.rating = rating;
	table1.row(row).data(data).draw();
	//highlight button
	if (rating=="1") {
		rating = "one";
	} else if (rating=="2") {
		rating = "two";
	} else if (rating=="3") {
		rating = "three";
	} else if (rating=="4") {
		rating = "four";
	}
	$('.resumeNav a.select').removeClass('select');
	$('.resumeNav a.' + rating + 'Icon').addClass('select');
}
function popResumeSlideout(toggle) {
	if (toggle == 'keepstate') {
		var state = (readCookie('resumeViewer') == 'max') ? 'max' : 'dock';
	} else if (toggle == 'max' || toggle == 'dock') {
		var state = toggle;
	} else {
		var state = ($('#pop').hasClass('max')) ? 'dock' : 'max';
	}
	if (state === 'max') {
		$('.resumeSlideout').appendTo($('.outerFrame'));
		$('.resumeSlideout').addClass('max');
		$('.resumeViewer').addClass('max');
		createCookie('resumeViewer','max',0);
		$('#pop').addClass('max');
	} else {
		$('.resumeSlideout').appendTo($('.workingPane'));
		$('.resumeSlideout').removeClass('max');
		$('.resumeViewer').removeClass('max');
		$('#pop').removeClass('max');
		eraseCookie('resumeViewer');
	}
}
function showSelectJob(state) {
	if (state === 'toggle') {
		state = ($('.selectJobOverlay').hasClass('is-visible')) ? false : true;
	}
	if ( state ) {
		$('.selectJobOverlay').addClass('is-visible');
		setTimeout(function() {
			$('.joSelectTable').addClass('is-visible');
		}, 300);
		
		$('.btnSelectjob').addClass('open');
		setSearchListener($('#jQDTSelectJO'));
		$('#jQDTSelectJO_filter').css('display','none');
		
	} else {
		$(".selectJobOverlay").removeClass('is-visible');
		$('.joSelectTable').removeClass('is-visible');
		$('.btnSelectjob').removeClass('open');
		setSearchListener($('#jQDTPipeline'));
	}
}
function showUtilityMenu(state) {
	if (state === 'toggle') {
		state = ($('.utilityMenuOverlay').hasClass('is-visible')) ? false : true;
	}
	if (state) {
		$('.utilityMenuOverlay').addClass('is-visible');
		setTimeout(function() {
			$('.utilityMenuTable').addClass('is-visible');
		}, 300);
		$('.btnUtilityMenu').addClass('open');
	} else {
		$('.utilityMenuOverlay').removeClass('is-visible');
		$('.utilityMenuTable').removeClass('is-visible');
		$('.btnUtilityMenu').removeClass('open');
	}
}
function setDialogTitleBar(id) {
	var tNode = $("span:contains('" + id + "')").parent();
	tNode.css("display", "none");
	var cNode = tNode.next();
	cNode.removeAttr('style');
	$('.tundra .dijitDialog').removeClass();
	//cNode.removeClass();
	//cNode.addClass('formDialog');
	//cNode.css('overflow','hidden');
}

function parkNode(nodeID, parentID, label) {
	//TRY HIDING THE DIALOG AND THEN SETTING THE OVERLAY TO DISPLAY NONE
	
	//create new parking lot div and add nodeID and parentID, if not dialog move node to parking div and hide
	var p = $('<div class="parkingDiv" />').appendTo($('.parkingLot'));
	p.attr('dialogid',nodeID);
	p.html('<span>' + label + '<span>');
	p.click(function() {
		showDijitDialog(nodeID, true);
		$(this).remove();
	});
	
	if (!parentID === 'dialog') {
		//move node to parking div and hide
	};
	//add close button with listener
	$(p).append('<div class="parkedDivClose" />');
	$(p).find('.parkedDivClose').click(function() {
		XSP.closeDialog(nodeID);
		$(this).parent().remove();
	});	
}
function showViewResume(state) {
	//set resumeSlideout visibility
	var gp = $("div[id$='resumeSlideout']"); //gets view resume id);
	//no state provided toggles
	if (state == null) {
		state = (gp.hasClass('is-visible')) ? false : true;
	}
	if ( state ) {
		document.cookie = 'groupingPaneState=is-visible';
		gp.addClass('is-visible');
	} else {
		document.cookie = 'groupingPaneState=';
		gp.removeClass('is-visible');
		$('.notesPane').removeClass('is-visible');
	}
}
function showDijitDialog(dialogID, state) {
	//no state provided toggles
	if (state == null) {
		state = ($('#' + dialogID).css('display') == 'none') ? true : false;
	}
	var val = (state) ? '' : 'none';
	var node = $("div[widgetid='" + dialogID + "']");
	node.css('display',val);
	$('.dijitDialogUnderlayWrapper').css('display',val);
}
function toggleJobDetailOverlay(jobnum) {
	var divID = '#jobDetailOverlay_' + jobnum;
	var frameID = '#jobDetailOverlayFrame_' + jobnum;
	if ($(divID).hasClass('is-visible')) {
		$(divID).removeClass('is-visible');
		//change button image to off
		$("td.arrow[data-jobnumber='" + jobnum + "'] > img").attr('src','images/downbuttonOver.png');
	} else {
		$(divID).addClass('is-visible');
		var src = 'xpDocJobDetails.xsp?jonum=' + jobnum;
		if ($(frameID).attr('src') != src) $(frameID).attr('src',src);
		//change button image to selected
		$("td.arrow[data-jobnumber='" + jobnum + "'] > img").attr('src','images/downbuttonOn.png');
	}
}
function toggleBurgerMenu(menuId) {
	var menu = $('#' + menuId);
	if (menu.hasClass('open-menu')) {
		menu.removeClass('open-menu');
	} else {
		menu.addClass('open-menu');
	}
}
function checkForOpenSub() {
	var t = $(".parkingLot").find("div:contains('submittal')");
	if (t.length > 0) {
		alert('You must finish or cancel the open submittal before creating a new one.');
		t.remove();
		showDijitDialog(t.attr('dialogid'),true);
		return false;
	} else { return true};
}
