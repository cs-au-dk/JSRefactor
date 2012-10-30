Array.prototype.sum /* -> sum */ = function() {
    var res = 0;
    for(var p in this)
	res += this[p];
    return res;
};

var a = [1, 2, 3];
alert(0 in a);