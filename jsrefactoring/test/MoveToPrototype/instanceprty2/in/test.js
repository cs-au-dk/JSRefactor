function F() {
	this.func = /* move to prototype */ function() {
		return 1;
	}
}

function test() {
	var a = new F();
	var b = new F();
	a.func.prototype.x = 5;
	b.func.prototype.x = 6;
	return a.func.prototype.x;
}

var z = test();

alert(z);
