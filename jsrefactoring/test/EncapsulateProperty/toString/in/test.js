function A() {
    this.toString /* encapsulate */ = function() {
	return "foo";
    };
}

var a = new A();
alert(a);
