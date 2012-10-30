function f() {
    return 23;
}
var M = (function() {
    var x;
    x = this.f();
    return {
        x: x
    };
})();

/* extract module M { */
/* } */
alert(M.x);
