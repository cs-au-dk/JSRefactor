
function RegExp(pattern, flags) {
	if ($bool)
		return pattern;
	var result = this;
	if ($bool)
		result = {};
	result.source = $string;
	result.global = $bool;
	result.ignoreCase = $bool;
	result.multiline = $bool;
	result.lastIndex = $int;
	return result;
}

RegExp.prototype.constructor = RegExp;

RegExp.prototype.exec = function(string) {
	if ($bool)
		return null;
	var array = new Array();
	array[$int] = $string;
	return array;
}

RegExp.prototype.test = function(string) {
	return $bool;
}

RegExp.prototype.toString = function() {
	return $string;
}

