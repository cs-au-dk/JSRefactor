
function G() {}

var x = 5;

try {
	G();
} finally {
	function F() {
		return x;
	}
}

var z = F();
