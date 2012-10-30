try {
    throw 23;
} catch(y) {
    var y = 42;
    alert(y /* -> y */);
}