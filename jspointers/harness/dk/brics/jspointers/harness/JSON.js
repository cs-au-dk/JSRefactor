
var JSON = {};

JSON.parse = function JSON_parse(str) {
	var x = {};
	x[$string] = x;
	x[$string] = $string;
	x[$string] = $number;
	x[$string] = $bool;
	x[$string] = null;
	x[$string] = undefined;
	return x;
}

JSON.stringify = function JSON_stringify(value, replacer, space) {
	var x = value;
	while ($bool) {
		x = x[$string]; // get all reachable objects
	}
	var val = x.toJSON();
	if ($bool) {
		val = $string;
	}
	var holder = {};
	holder[$string] = val;
	replacer.call(holder, $string, val);
	return $string;
}
