try {
    throw 23;
} catch(x) {
    var x = 42;
    alert(x /* -> y */);
}