function A(b, x) {
    this.v /* encapsulate */ = x;
    this.m = function() { return this.v; };
    if(b)
	this.w = new A(false, x+19).m();
}

var a = new A(true, 23);
alert(a.w);