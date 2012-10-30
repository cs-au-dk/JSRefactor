function A() {}
function B() {}

var a=A, b=a, c={f:b}, d=c.f

d();
