/* extract module M { */
function f() {
    return h();
}
/* } */
function g() {
    return f();
}
function h() {
    return 23;
}
g();