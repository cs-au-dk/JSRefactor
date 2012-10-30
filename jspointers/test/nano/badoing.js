function find(array,key) {
	for (var i=0; i<array.length; i++) {
		if (array[i].key.equals(key))
			return array[i].value;
	}
	return null;
}

function KeyA(x) {this.x = x;}
KeyA.prototype.equals = function(y) {
	return this.x == ...;
}
function KeyB(x) {this.x = x;}
KeyB.prototype.equals = function(y) {
	return this.x == ...;
}

find([...], new KeyA());



function f(x) {
	x.bar = z;
}


function RBNode(left,right,data,red) {
	this.left = left;
	this.right = right;
	this.data = data;
	this.red = red;
}
function successor(node, cmp) {
	var result = null;
	while (node != null) {
		var c = cmp(node.data);
		if (c == 0)
			return node.data;
		else if (c < 0) {
			result = node.data;
			node = node.left;
		} else {
			node = node.right;
		}
	}
	return result;
}

function Stream(data, index) {
	this.data = data;
	this.index = index || 0;
}
Stream.prototype.take = function(len) {
	return this.data.substring(this.index, this.index+len);
}