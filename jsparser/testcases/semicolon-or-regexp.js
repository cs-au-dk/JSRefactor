RegExp.prototype.foo = 5;

var a = 1;
var b = 2;
var c = {foo:3};
var x = a/b/c.foo;

// /b/c can be seen as a regular expression literal, but it
// should be treated as "a divided by b divided by c.foo".

var y = /b/c.foo;

// /b/c is a regular expression literal

var z = a
/b/c.foo;

// The above expression is a division. A semicolon must NOT be inserted after "a".

var w = /b/c.foo
/b/c.foo;

// The above is: REGEXP.foo divided by b divided by c.foo

"foo".foo = 6;

/b/c.foo = 6;

// The above is: REGEXP.foo assigned to 6

for (var i=0; i<4; i++) {
	if (i>2)
		break		// note: no semicolon after break
	/b/c.foo = 6;
	// here, a semicolon MUST be inserted in front of the regexp literal
	
	if (i>3)
		{}
	/b/c.foo;// = 6; // regexp
	
	if (i>4)
		z = {}
		/b/c.foo; // NOT a regexp
	
}
