var o = { x: 42 };
with(o) {
    var x = 23;
}
alert(o.x /* -> y */);
