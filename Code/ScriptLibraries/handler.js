// text handling JS

function removeDups(arr) {
    var seen = {};
    var newArray = [];
    var len = arr.length;
    var j = 0;
    for(var i = 0; i < len; i++) {
         var item = arr[i];
         if(seen[item] !== 1) {
               seen[item] = 1;
               newArray[j++] = item;
         }
    }
    return newArray;
}

function cleanArray(arr){
	var newArray = [];
	for(var i = 0; i < arr.length; i++){
	      if (arr[i]){
	        newArray.push(arr[i]);
	      }
	}
	return newArray;
}