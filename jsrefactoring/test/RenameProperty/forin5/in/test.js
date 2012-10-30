// fails because o[p] occurs inside an inner function
// refactoring can not handle this at the moment
function arrayContainsSomePropertyFromObject(o, array) {
	var b = false;
	for (var p in o) {
		b |= array.some(function(item) {
			return o[p] === item;
		});
	}
	return b;
}

var arr = [1,2,3];
var obj = {foo:6, bar:3};
obj.zang /* -> x */ = 27;

var b = arrayContainsSomePropertyFromObject(obj, arr);
