var M = (function() {
    function f() {
        return h();
    }
    return {
        f: f
    };
})();
/* extract module M { */
/* } */
function g() {
    return M.f();
}
function h() {
    return 23;
}
g();