var o1 = {};
var o2 = {};
var o3 = {};
var o4 = {};
var o5 = {};
var o6 = {};
var o7 = {};
var o8 = {};
var o9 = {};
var o10 = {};
var o11 = {};
var o12 = {};
var o13 = {};
var o14 = {};
var o15 = {};
var o16 = {};
var o17 = {};
var o18 = {};
var o19 = {};
var o20 = {};
var o21 = {};
var o22 = {};
var o23 = {};
var o24 = {};
var o25 = {};
var o26 = {};
var o27 = {};
var o28 = {};
var o29 = {};
var o30 = {};
var o31 = {};
var o32 = {};
var o33 = {};
var o34 = {};
var o35 = {};
var o36 = {};
var o37 = {};
var o38 = {};
var o39 = {};
var o40 = {};

function Choose(x,y) {
	if (Math.random() > 0.5)
		return x;
	else
		return y;
}

var a01_02 = Choose(o1,o2);
var a01_03 = Choose(o1,o3);
var a02_03 = Choose(o2,o3);
var a02_04 = Choose(o2,o4);
var a03_04 = Choose(o3,o4);
var a04_05 = Choose(o4,o5);
var a01_05 = Choose(o1,o5);
var a05_06 = Choose(o1,o6);
var a06_07 = Choose(o6,o7);
var a03_07 = Choose(o3,o7);
var a05_07 = Choose(o5,o7);
var a07_08 = Choose(o7,o8);

var $ = Choose;

function Work(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,x) {
	b.bar = {foo:""};
	a.foo = b.bar;
	c.foo.foo = e;
	g.bar.foo = b.foo;
	f.bar = d;
	a.bar = c.foo;
	e.foo = f.bar;
	f.foo = g.foo.bar;
	g.foo = h.foo.bar;
	h.foo = i.foo.bar;
	i.foo = j.foo;
}

Work(	$(o1,o2), $(o2,o3), $(o3,o4), $(o4,o5), $(o5,o6), $(o6,o7), $(o7,o8), $(o8,o9),
		$(o9,o10), $(o10,o11), $(o11,o12), $(o12,o13), $(o13,o14));

var z = o1.bar.foo;
