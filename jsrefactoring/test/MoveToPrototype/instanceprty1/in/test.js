function F() {
	this.func = /* move to prototype */ function() {
		return 1;
	}
}

function test() {
	var a = new F();
	var b = new F();
	a.func.x = 5;
	b.func.x = 6;
	return a.func.x;
}

var z = test();

alert(z);
