function f() {
	return this.X /* -> X */;
}

var X = 5;
var y = f();