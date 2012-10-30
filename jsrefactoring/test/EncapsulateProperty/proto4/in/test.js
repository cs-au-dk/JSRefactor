function A(b, v) {
    if(b)
	this.x /* encapsulate */ = v;
}

var a = new A(true, "a");
A.prototype = a;

var b = new A(false);

alert(b.x);
