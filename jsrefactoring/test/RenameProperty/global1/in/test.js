function f() {
	return this.x /* -> X */;
}

var x = 5;
var y = f();