/*
 * This example shows why it is important to have a constructor covering. 
 * Here, function f is invoked twice on the same object, hence there is
 * no one-to-one correspondence between receivers and stack frames
 * of the function: the single property o.x would be split into two 
 * distinct local variables by the refactoring.
 */
function f(v) {
    this.x /* encapsulate */ = v;
    this.m = this.m || [];
    this.m.push(function(){return this.x;});
}

var o = {};
f.call(o, 23);
f.call(o, 42);
alert(o.m[0].call(o));
alert(o.m[1].call(o));
