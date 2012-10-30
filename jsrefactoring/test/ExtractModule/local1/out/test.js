var M = (function() {
    var x = 23;
    function setX(newX) {
        x = newX;
    }
    function getX() {
        return x;
    }
    return {
        setX: setX,
        getX: getX
    };
})();
/* extract module M { */
/* } */
alert(M.getX());
M.setX(42);
alert(M.getX());
