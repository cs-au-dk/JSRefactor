
function Boolean(x) {
	if ($bool)
		return $bool;
}

Boolean.prototype.constructor = Boolean;

Boolean.prototype.toString = function Boolean_prototype_toString() {
	return $string;
}

Boolean.prototype.valueOf = function Boolean_prototype_valueOf() {
	return $string;
}

