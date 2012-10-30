function A() {
    this.valueOf /* encapsulate */ = function() {
	return 23;
    };
}

var a = new A();
alert(a+19);
