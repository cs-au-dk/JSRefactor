function F() {
	var x = 5;
	var A;
	function g() {
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
var y1 = g1.h(), // 7
	y2 = g2.h(); // 8

