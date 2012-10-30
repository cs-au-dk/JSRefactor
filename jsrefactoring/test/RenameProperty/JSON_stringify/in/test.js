var obj = {foo: 5};
var obj2 = {child: obj};

obj.bar /* -> baz */ = 6;

// output changes because renamed property is reachable from obj2
alert(JSON.stringify(obj2));