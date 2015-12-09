

function initializeUserEnvironment() {
	//setup user environment
	if (!sessionScope.user) {
		var user=@Name("[CN]", @UserName());
		var v=session.getDatabase(session.getCurrentDatabase().getServer(),"ats\\data10.nsf").getView("AllEmployeesByName");
		var doc:NotesDocument=v.getDocumentByKey(user);
		var roles:java.util.Vector=database.queryAccessRoles(@UserName());
		sessionScope.joLeadsNotified=false;
		sessionScope.user=user;
		sessionScope.user_name=doc.getItemValueString("EmployeeName");
		sessionScope.user_signature=doc.getItemValueString("AltEmployeeName")
		sessionScope.user_called_name=doc.getItemValueString("EmployeeFullName");
		sessionScope.user_initials=doc.getItemValueString("initials");
		sessionScope.user_email=doc.getItemValueString("EmployeeEmail");
		sessionScope.user_phone=doc.getItemValueString("EmployeePhone");
		sessionScope.user_mobile=doc.getItemValueString("EmployeeMobilePhone");
		sessionScope.user_department=doc.getItemValueString("EmployeeTitle");
		sessionScope.user_roles=roles;
		sessionScope.user_singledesk=doc.getItemValueString("sdesk");
		sessionScope.user_cbPost=(doc.getItemValueString("option_cbpost")=="Allow CB Post") ? true : false;
		if (doc.getItemValueString("cucm")=="CUCM") {
			sessionScope.dialurl="http://10.100.1.121/re_ws.nsf/cucmDialer?OpenAgent";
		} else {
			sessionScope.dialurl="https://hostedconnect.m5net.com/bobl/bobl?name=org.m5.apps.v1.cti.ClickToDial.dial";
		}
		if (sessionScope.user_signature == "") sessionScope.user_signature = sessionScope.user_name;
		//figure out title
		var title=doc.getItemValueString("EmployeeTitleText");
		if (!title) {
			switch (sessionScope.user_department) {
				case "Salesmen":
					title="Account Manager";
				break;
				case "Recruiter":
					title="";
				break;
			}
		}
		sessionScope.user_title=title;
		//figure out address based on branchid
		sessionScope.user_branch=doc.getItemValueString("branchid");
		var sig_branch=(doc.getItemValueString("branchid_1")!="")?doc.getItemValueString("branchid_1"):doc.getItemValueString("branchid");
		switch (@Left(sig_branch,4)){
			case "5301": //ATLANTA	
				sessionScope.user_address1="6200 The Corners Pkwy, 6th Floor";
				sessionScope.user_address2="Atlanta, GA 30092";
				sessionScope.user_fax="770-446-7518"
			break;
			case "5310": //ATLANTA	
				sessionScope.user_address1="6200 The Corners Pkwy, 6th Floor";
				sessionScope.user_address2="Atlanta, GA 30092";
				sessionScope.user_fax="770-446-7518"
			break;
			case "5311": //ATLANTA	
				sessionScope.user_address1="6200 The Corners Pkwy, 6th Floor";
				sessionScope.user_address2="Atlanta, GA 30092";
				sessionScope.user_fax="770-446-7518"
			break;
			case "5303": //HOUSTON
				sessionScope.user_address1="10111 Richmond Avenue, Suite 100";
				sessionScope.user_address2="Houston, TX 77042";
				sessionScope.user_fax="713-278-8051"
			break;
			case "5304": //LA
				sessionScope.user_address1="909 N. Sepulveda Blvd Suite 300";
				sessionScope.user_address2="El Segundo, CA 90245";
				sessionScope.user_fax="916-720-0523"
			break;
			case "5305": //BOSTON
				sessionScope.user_address1="10 Presidential Way Suite 101";
				sessionScope.user_address2="Woburn, MA 01801";
				sessionScope.user_fax="781-938-1410"
			break;
			case "5312": //Legacy Mergis
				switch (sessionScope.user_branch) {
					case "5312-1":  //Toledo
						sessionScope.user_address1="1745 Indian Wood Cir Ste 150";
						sessionScope.user_address2="Maumee, OH 43537";
						sessionScope.user_fax="";
					break;
					case "5312-2":  //Troy
						sessionScope.user_address1="100 West Big Beaver Rd Ste 500";
						sessionScope.user_address2="Troy, MI 48084";
						sessionScope.user_fax$="";
					break;
					case "5312-3":  //Cinci
						sessionScope.user_address1="5151 Pfeiffer Rd Ste 120";
						sessionScope.user_address2="Cincinnati, OH 45242";
						sessionScope.user_fax="";
					break;
					case "5312-4":  //Greenville
						sessionScope.user_address1="110 West North Street Ste 200";
						sessionScope.user_address2="Greenville, SC 29601";		
						sessionScope.user_fax="";	
					break;
					case "5312-5": //ATLANTA	
						sessionScope.user_address1="6200 The Corners Pkwy, 6th Floor";
						sessionScope.user_address2="Atlanta, GA 30092";
						sessionScope.user_fax="770-446-7518"
					break;
				}
			}
		if (doc.getItemValueString("linkedin")!=""){
			sessionScope.user_linkedin="http://www.linkedin.com/" + doc.getItemValueString("linkedin");
		}
		sessionScope.user_m5pass=doc.getItemValueString("m5password");
		sessionScope.user_recruitingCenter=doc.getItemValueString("recruitingCenter");
		var frm=new Array("(",")","-"," ");
		sessionScope.user_tn=@ReplaceSubstring(sessionScope.user_phone,frm,"");
		if (roles.contains("[admin]")||roles.contains("[sysadmin]")) {
			sessionScope.admin=roles.contains("[admin]");
			sessionScope.sysadmin=roles.contains("[sysadmin]");	
			sessionScope.admin_initials=doc.getItemValueString("initials");
		}

		print("New User Session: " + user + " @ " + @Now());
		
		recycleObject(v);
		recycleObject(doc);
	}
}