/* extract module M { */
var x = 23;
function setX(newX) {
    x = newX;
}
function getX() {
    return x;
}
/* } */
alert(getX());
setX(42);
alert(getX());
