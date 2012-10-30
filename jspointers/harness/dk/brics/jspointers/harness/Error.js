function Error(msg) {
	return { message: $string };
}

Error.prototype.constructor = Error;
Error.prototype.name = $string;
Error.prototype.message = $string;

Error.prototype.toString = function() {
	return $string;
}

// model the native errors as aliases for Error
var RangeError = Error;
var ReferenceError = Error;
var SyntaxError = Error;
var TypeError = Error;
var URIError = Error;
