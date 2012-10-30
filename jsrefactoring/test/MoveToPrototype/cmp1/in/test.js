function A() {
    this.x = 23;
    this.getX = /* move to prototype */ function() {
	return this.x;
    };
}

var a1 = new A();
var a2 = new A();
alert(a1.getX == a2.getX);
