var M = (function() {
    function f() {
        return this.h();
    }
    return {
        f: f
    };
})();
/* extract module M { */
/* } */
function g() {
    return M.f.call(null);
}
function h() {
    return 23;
}
g();