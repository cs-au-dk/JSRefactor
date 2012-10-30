/*
 * Variant on noCover.
 */
function f(v) {
    this.x /* encapsulate */ = v;
    this.m = this.m || [];
    this.m.push(function(){return this.x;});
}

var o = new f(23);
f.call(o, 42);
alert(o.m[0].call(o));

