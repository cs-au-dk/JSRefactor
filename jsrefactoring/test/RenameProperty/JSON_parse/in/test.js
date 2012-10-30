var x = "{foo:5}";

var obj = JSON.parse(x);

var y = obj.foo /* -> bar */;
