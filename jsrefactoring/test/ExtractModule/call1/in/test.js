/* extract module M { */
function f() {
    return this.h();
}
/* } */
function g() {
    return f();
}
function h() {
    return 23;
}
g();