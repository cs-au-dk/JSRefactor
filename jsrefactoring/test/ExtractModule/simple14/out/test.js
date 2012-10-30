var M = (function() {
    function f() {
        var x = 23;

        return x;
    }
    return {
        f: f
    };
})();
/* extract module M { */
/* } */
M.f();
