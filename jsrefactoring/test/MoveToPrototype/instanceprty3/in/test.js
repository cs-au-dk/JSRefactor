function F() {
	this.func = /* move to prototype */ function() {
		return 1;
	}
}

function test() {
	var a = new F();
	var b = new F();
	a.func.toString.x = 5;
	b.func.toString.x = 6;
	return a.func.toString.x;
}

var z = test();

alert(z);
