// test all kinds of syntactic constructs
// DO NOT assume automatic semicolon insertion! This is for testing the grammar.

var a = true;
var b;

b = a;

if (a) {
	var c;
}
if (b) {
	var d;
} else {
	var e;
}

if (a)
	a = b;
if (b)
	a = b;
else
	b = a;

for (;;) break;
for (;;) { break; }
for (a=5; ; false) {}
for (var f=5; ; false) {}
for (var g=5,h=6; ; false) {}

for (;;)
	if (a)
		break;
	else
		break;

if (a)
	for (;;)
		break;
else
	a = b;

if (a)
	for (;;)
		if (b)
			break;
		else
			break;

while (true) break;
while (a) {break;}
while (a = b) {break;}
while (false) {}
if (a)
	while (b)
		if (c)
			break;
		else
			break;

var y = {}; // empty object literal
var y2 = {foo:5};
var y3 = {foo:5, bar:6 };
var y3 = {foo:5, bar:6, }; // trailing comma
for (var x in y) {
	a = b;
}
for (var x in {foo:"bar"}) {
}
for (y2.foo in y) {
	a = y2.foo;
}

function Foo() {}

var fooresult = Foo();
