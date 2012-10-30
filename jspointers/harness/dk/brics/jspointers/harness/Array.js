// this file simulates the native Array function

// Special variables:
// - $int, an unknown integer
// - $string, an unknown string
// - $bool, an unknown boolean

function Array() {
	var result = {};
	if ($bool)
		result = this;
	// imprecision: if the only argument is an integer, it should not be stored in the array
	result[$int] = arguments[$int];
	result.length = $int;
	return result;
}

Array.prototype.constructor = Array;

Array.prototype.push = function Array_prototype_push(x) {
	this[$int] = arguments[$int];
	return $int;
}

Array.prototype.pop = function Array_prototype_pop() {
	return this[$int];
}

Array.prototype.toLocaleString = function Array_prototype_toLocaleString() {
	return $string;
}

Array.prototype.concat = function Array_prototype_concat() {
	var result = new Array();
	result[$int] = this[$int];
	result[$int] = arguments[$int]; // non-arrays are stored directly
	result[$int] = arguments[$int][$int]; // arrays are flattened once
	return result;
}

Array.prototype.join = function Array_prototype_join() {
	return $string;
}

Array.prototype.reverse = function Array_prototype_reverse() {
	return Object(this);
}

Array.prototype.shift = function Array_prototype_shift() {
	return this[0];
}

Array.prototype.slice = function Array_prototype_slice(start,end) {
	var array = new Array();
	array[$int] = this[$int];
	return array;
}

Array.prototype.sort = function Array_prototype_sort(comparefn) {
	comparefn(this[$int], this[$int]);
	return this;
}

Array.prototype.splice = function Array_prototype_splice(start, deleteCount) {
	var array = new Array();
	array[$int] = this[$int];
	this[$int] = arguments[$int]; // imprecision: start and deleteCount should not be stored in the array
	return array;
}

Array.prototype.unshift = function Array_prototype_unshift(x) {
	this[$int] = arguments[$int];
	return $int;
}

Array.prototype.indexOf = function Array_prototype_indexOf(searchElement) {
	return $int;
}

Array.prototype.lastIndexOf = function Array_prototype_lastIndexOf(searchElement) {
	return $int;
}

Array.prototype.every = function Array_prototype_every(callbackfn, thisArg) {
	callbackfn.call(thisArg, this[$int], $int, this);
	return $bool;
}

Array.prototype.some = function Array_prototype_some(callbackfn, thisArg) {
	callbackfn.call(thisArg, this[$int], $int, this);
	return $bool;
}

Array.prototype.forEach = function Array_prototype_forEach(callbackfn, thisArg) {
	callbackfn.call(thisArg, this[$int], $int, this);
	return;
}

Array.prototype.map = function Array_prototype_map(callbackfn, thisArg) {
	var array = new Array();
	array[$int] = callbackfn.call(thisArg, this[$int], $int, this);
	return array;
}

Array.prototype.filter = function Array_prototype_filter(callbackfn, thisArg) {
	var array = new Array();
	callbackfn.call(thisArg, this[$int], $int, this);
	array[$int] = this[$int];
	return array;
}

Array.prototype.reduce = function Array_prototype_reduce(callbackfn, initialValue) {
	var x = initialValue;
	if ($bool) {
		x = this[$int];
	}
	while ($bool) {
		x = callbackfn.call(undefined, x, this[$int], $int);
	}
	return x;
}

Array.prototype.reduceRight = function Array_prototype_reduceRight(callbackfn, initialValue) {
	var x = initialValue;
	if ($bool) {
		x = this[$int];
	}
	while ($bool) {
		// for some reason ECMA wants 'null' as the this arg here, and 'undefined' in the reduce function
		x = callbackfn.call(null, x, this[$int], $int);
	}
	return x;
}

