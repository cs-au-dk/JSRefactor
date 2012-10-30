var o = { M: {x: 42} };
var M = (function() {
    with(o) {
        var x = 23;
    }
    return {
        x: x
    };
})();
/* extract module M { */
/* } */
alert(M.x);