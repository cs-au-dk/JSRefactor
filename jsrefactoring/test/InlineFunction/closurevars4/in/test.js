function F() {
	var A;
	function g() {
		var x = 5;
		A = function() {
			return x++;
		}
		function h() {
			return A() /* inline */;
		}
		return {h:h};
	}
	return {g:g};
}

var obj = F();

var g1 = obj.g();
var x1 = g1.h(), // 5
    x2 = g1.h(); // 6
    
var g2 = obj.g();
var y1 = g1.h(), // 5 (because g1's instance of A changed)
	y2 = g2.h(); // 6

