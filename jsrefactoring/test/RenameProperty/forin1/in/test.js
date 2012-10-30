var obj = {foo:4, bar:5};
obj.baz /* -> x */ = 6;

var str = '';
for (var prty in obj) {
	str += prty;
}
alert(str);  // rename should fail, otherwise str could be "foobarx", which it cannot be in the original program
