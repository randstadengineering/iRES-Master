// Your Client ID can be retrieved from your project in the Google
// Developer Console, https://console.developers.google.com
var CLIENT_ID = '822155928413-sk32249492ngu0b29m1b5av6jm88l1sf.apps.googleusercontent.com';
var SCOPES = ['https://www.googleapis.com/auth/drive'];

function appendPre(message) {
	/**
	 * Append a pre element to the body containing the given message
	 * as its text node.
	 *
	 * @param {string} message Text to be placed in pre element.
	 */
	var pre = document.getElementById('output');
	var textContent = document.createTextNode(message + '\n');
	pre.appendChild(textContent);
}

function checkAuth() {
	/**
	 * Check if current user has authorized this application.
	 */
	gapi.auth.authorize(
			{
				'client_id': CLIENT_ID,
				'scope': SCOPES.join(' '),
				'immediate': true
			}, handleAuthResult);
}

function copyFile(originFileId, copyTitle, callback) {
	/**
	 * Copy an existing file.
	 *
	 * @param {String} originFileId ID of the origin file to copy.
	 * @param {String} copyTitle Title of the copy.
	 */
  var body = {'title': copyTitle};
  var request = gapi.client.drive.files.copy({
    'fileId': originFileId,
    'resource': body,
    'convert' : true,
    'ocr' : true
  });
  request.execute(function(resp) {
	    console.log('Copy ID: ' + resp.id);
	    callback(resp.id);
	  });
}

function downloadFile(file, callback) {
	/**
	 * Download a file's content.
	 *
	 * @param {File} file Drive File instance.
	 * @param {Function} callback Function to call when the request is complete.
	 */
  if (file.exportLinks) {
	var downloadUrl = file.exportLinks['application/pdf'];
	console.log(downloadUrl);
    var accessToken = gapi.auth.getToken().access_token;
    var xhr = new XMLHttpRequest();
    xhr.open('GET', downloadUrl);
    xhr.setRequestHeader('Authorization', 'Bearer ' + accessToken);
    xhr.onload = function() {
      callback(xhr.responseText);
    };
    xhr.onerror = function() {
      callback(null);
    };
    xhr.send();
  } else {
    callback(null);
  }
}

function getFile(fileId, callback) {
	/**
	 * Get file object
	 *
	 * @param {String} fileId ID of the file to print metadata for.
	 */
  var request = gapi.client.drive.files.get({
    'fileId': fileId
  });
  request.execute(function(resp) {
    callback(resp);
  });
}
function handleAuthResult(authResult) {
	/**
	 * Handle response from authorization server.
	 *
	 * @param {Object} authResult Authorization result.
	 */
	var authorizeDiv = document.getElementById('authorize-div');
	if (authResult && !authResult.error) {
		// Hide auth UI, then load client library.
		authorizeDiv.style.display = 'none';
		console.log(authResult);
		loadDriveApi();
	} else {
		// Show auth UI, allowing the user to initiate authorization by
		// clicking authorize button.
		console.log(authResult);
		authorizeDiv.style.display = 'inline';
	}
}

function handleAuthClick(event) {
	/**
	 * Initiate auth flow in response to user clicking authorize button.
	 *
	 * @param {Event} event Button click event.
	 */
	gapi.auth.authorize(
			{client_id: CLIENT_ID, scope: SCOPES, immediate: false},
			handleAuthResult);
	return false;
}

function insertFileBase64(fileData, title, callback) {
	/**
	 * Insert new file.
	 *
	 * @param {File} fileData File object to read data from.
	 * @param {Function} callback Function to call when the request is complete.
	 */
  var boundary = '-------314159265358979323846';
  var delimiter = "\r\n--" + boundary + "\r\n";
  var close_delim = "\r\n--" + boundary + "--";

  //var reader = new FileReader();
  //reader.readAsBinaryString(fileData);
  // reader.onload = function(e) {
    var contentType = 'application/octet-stream';
    contentType = 'application/msword';
    var metadata = {
    		'title': title,
    		'mimeType': contentType
    };

    var base64Data = btoa(fileData);
    //var base64Data = fileData;
    var multipartRequestBody =
        delimiter +
        'Content-Type: application/json\r\n\r\n' +
        JSON.stringify(metadata) +
        delimiter +
        'Content-Type: ' + contentType + '\r\n' +
        'Content-Transfer-Encoding: base64\r\n' +
        '\r\n' +
        base64Data +
        close_delim;

    var request = gapi.client.request({
        'path': '/upload/drive/v2/files',
        'method': 'POST',
        'params': {'uploadType': 'multipart'},
        'convert': false,
        'headers': {
          'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
        },
        'body': multipartRequestBody});
    if (!callback) {
      callback = function(file) {
        console.log(file)
      };
    }
    request.execute(callback);
  //}
}

function insertFile(fileData, callback) {
	/**
	 * Insert new file.
	 *
	 * @param {File} fileData File object to read data from.
	 * @param {Function} callback Function to call when the request is complete.
	 */
  var boundary = '-------314159265358979323846';
  var delimiter = "\r\n--" + boundary + "\r\n";
  var close_delim = "\r\n--" + boundary + "--";

  var reader = new FileReader();
  reader.readAsBinaryString(fileData);
  reader.onload = function(e) {
	console.log(reader.result);
    var contentType = fileData.type || 'application/octet-stream';
    var metadata = {
    		'title': fileData.fileName,
    		'mimeType': contentType
    		//'title': title
    		//'mimeType': 'application/msword'
    };

    var base64Data = btoa(reader.result);
    var multipartRequestBody =
        delimiter +
        'Content-Type: application/json\r\n\r\n' +
        JSON.stringify(metadata) +
        delimiter +
        'Content-Type: ' + contentType + '\r\n' +
        'Content-Transfer-Encoding: base64\r\n' +
        '\r\n' +
        base64Data +
        close_delim;

    var request = gapi.client.request({
        'path': '/upload/drive/v2/files',
        'method': 'POST',
        'params': {'uploadType': 'multipart'},
        'convert': true,
        'headers': {
          'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
        },
        'body': multipartRequestBody});
    if (!callback) {
      callback = function(file) {
        console.log(file)
      };
    }
    request.execute(callback);
  }
}

function loadDriveApi() {
	/**
	 * Load Drive API client library.
	 */
	gapi.client.load('drive', 'v2', listFiles);
}

function listFiles() {
	/**
	 * Print files.
	 */
	var request = gapi.client.drive.files.list({
		'maxResults': 10
	});

	request.execute(function(resp) {
		appendPre('Files:');
		var files = resp.items;
		if (files && files.length > 0) {
			for (var i = 0; i < files.length; i++) {
				var file = files[i];
				appendPre(file.title + ' (' + file.id + ')');
			}
		} else {
			appendPre('No files found.');
		}
	});
}

function loadResume(fileData, title, callback) {
	insertFileBase64(fileData, title, function(resp) {
		var fileID = resp.id;
		copyFile(fileID, '', function(copyID){ 
			if (copyID) {
				src='https://docs.google.com/document/d/' + copyID + '/edit?chrome=false&embedded=true&rm=demo';
				document.getElementById('resumeFrame').src=src;
				console.log('resume upload and conversion done');
				callback(copyID);
			};
		});
		trashFile(fileID);
	});
}

function renameFile(fileId, newTitle) {
	/**
	 * Rename a file.
	 *
	 * @param {String} fileId <span style="font-size: 13px; ">ID of the file to rename.</span><br> * @param {String} newTitle New title for the file.
	 */
  var body = {'title': newTitle};
  var request = gapi.client.drive.files.patch({
    'fileId': fileId,
    'resource': body
  });
  request.execute(function(resp) {
    console.log('New Title: ' + resp.title);
  });
}

function printFile(fileId) {
	/**
	 * Print a file's metadata.
	 *
	 * @param {String} fileId ID of the file to print metadata for.
	 */
  var request = gapi.client.drive.files.get({
    'fileId': fileId
  });
  request.execute(function(resp) {
    console.log('Title: ' + resp.title);
    console.log('Description: ' + resp.description);
    console.log('MIME type: ' + resp.mimeType);
    console.log(resp);
  });
}
function trashFile(fileId) {
	/**
	 * Move a file to the trash.
	 *
	 * @param {String} fileId ID of the file to trash.
	 */
  var request = gapi.client.drive.files.trash({
    'fileId': fileId
  });
  request.execute(function(resp) { });
}

function updateMimeType(fileId, mimeType) {
	var mime = {'mimeType': mimeType};
	var request = gapi.client.drive.files.patch({
		'fileId': fileId,
		'resource': mime
	});
	 request.execute(function(resp) {
		 console.log('New Mime: ' + resp.mimeType);
	 });
}
