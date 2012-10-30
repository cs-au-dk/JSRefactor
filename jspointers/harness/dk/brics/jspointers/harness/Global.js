
var undefined;
var NaN = $number;
var Infinity = $number;

var eval = $Eval;

function parseInt(string, radix) {
	"x" + string; // force coercion
	1 + radix;
	return $number;
}

function parseFloat(string) {
	"x" + string;
	return $number;
}

function isNaN(number) {
	1 + number;
	return $bool;
}

function isFinite(number) {
	1 + number;
	return $bool;
}

function decodeURI(encodedURI) {
	"x" + encodedURI;
	return $string;
}

function decodeURIComponent(encodedURIComponent) {
	"x" + encodedURIComponent;
	return $string;
}

function encodeURI(uri) {
	"x" + uri;
	return $string;
}

function encodeURIComponent(uriComponent) {
	"x" + uriComponent;
	return $string;
}
