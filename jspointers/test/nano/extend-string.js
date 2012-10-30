String.prototype.compareTo = function(other) {
	if (this < other)
		return -1;
	if (other < this)
		return 1;
	return 0;
}
var globalVar;
String.prototype.foo = function() {
	globalVar = this;
}


var z = "foo".compareTo("bar");

var w = "foo".toString();

var str = "foo";
str.toString();

"baz".foo();

//var w = new String("foo").compareTo("bar");
