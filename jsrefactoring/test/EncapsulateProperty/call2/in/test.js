/*
 * This example shows that it is not enough to have a one-to-one 
 * correspondence between receivers and stack frames of A.
 * When m is invoked the second time, its receiver is a2, yet the
 * receiver of the invocation corresponding to the captured
 * stack frame of the closure is a1.
 */
function A(v) {
    this.x /* encapsulate */ = v;
    this.m = function() {
	return this.x;
    };
}

var a1 = new A(23), a2 = new A(42);
alert(a1.m());
alert(a1.m.call(a2));
