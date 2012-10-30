var y = 23;
/* extract module M { */
x = 42;
/* } */
var n = Math.random() > 0.5 ? "x" : "y";
alert(this[n]);
