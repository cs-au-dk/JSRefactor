/* extract module M { */
function f(y) {
    return y+19;
}

function g() {
    var f = 23;
    return this.f(f);
}
/* } */

alert(f(23)+g());
