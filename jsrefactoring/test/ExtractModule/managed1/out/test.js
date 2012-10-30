var M = (function() {
    var x = 23;
    function setX(newX) {
        M.x = newX;
    }
    return {
        x: x,
        setX: setX
    };
})();
/* extract module M { */
/* } */
alert(M.x);
M.setX(42);
alert(M.x);
