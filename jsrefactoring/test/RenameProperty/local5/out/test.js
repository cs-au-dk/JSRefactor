var o = { y: 42 };
with(o) {
    var x = 23;
}
alert(o.y /* -> y */);
