
var Object = $Object;

Object.getPrototypeOf = $GetPrototypeOf;
Object.create = $Create;

Object.getOwnPropertyDescriptor = function Object_getOwnPropertyDescriptor(O,P) {
	// TODO: Make intern to avoid prototype chain
	return { writable:$bool, value:O[P] };
}

Object.getOwnPropertyNames = function Object_getOwnPropertyNames(O) {
	var a = [];
	a[$int] = $string;
	return a;
}

Object.defineProperty = function Object_defineProperty(O,P,Attributes) {
	O[P] = Attributes.value;
	return O;
}

Object.defineProperties = function Object_defineProperties(O,Properties) {
	for (var p in Properties) {
		O[p] = Properties[p].value;
	}
	return O;
}

Object.seal = function(O) {
	return O;
}

Object.freeze = function(O) {
	return O;
}

Object.preventExtensions = function(O) {
	return O;
}

Object.isSealed = function(O) {
	return $bool;
}

Object.isFrozen = function(O) {
	return $bool;
}

Object.isExtensible = function(O) {
	return $bool;
}

Object.keys = function Object_keys(O) {
	var a = [];
	var i = 0;
	for(var p in O)
		a[i++] = p;
	return a;
}

Object.prototype.toString = function() {
	return $string;
}

Object.prototype.toLocaleString = function() {
	return this.toString();
}

Object.prototype.valueOf = function() {
	// make this internal instead?
	if ($bool)
		return $number;
	else if ($bool)
		return $string;
	else if ($bool)
		return $bool;
	else if ($bool)
		return new Number();
	else if ($bool)
		return new String();
	else if ($bool)
		return new Boolean();
	else if ($bool)
		return this;
}

Object.prototype.hasOwnProperty = function Object_prototype_hasOwnProperty(V) {
	"" + V;
	return $bool;
}

Object.prototype.isPrototypeOf = function Object_prototype_isPrototypeOf(V) {
	return $bool;
}

Object.prototype.propertyIsEnumerable = function Object_prototype_propertyIsEnumerable(V) {
	"" + V;
	return $bool;
}

