function foo(obj) {
	var prty = 'foo';
	return obj[prty];
}

var o = {};
o.foo /* -> bar */ = 6;

foo(o);
