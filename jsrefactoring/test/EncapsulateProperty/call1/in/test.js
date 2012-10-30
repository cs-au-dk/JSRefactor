function A() {
    this.x /* encapsulate */ = 23;
    this.m = function() {
	return this.x;
    };
}

var a = new A();
alert(a.m());
alert(a.m.call({ x: 42 }));
