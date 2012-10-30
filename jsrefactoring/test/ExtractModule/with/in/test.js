var o = { M: {x: 42} };
/* extract module M { */
with(o) {
    var x = 23;
}
/* } */
alert(x);