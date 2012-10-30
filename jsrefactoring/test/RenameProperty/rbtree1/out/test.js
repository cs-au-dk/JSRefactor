/*
 * Benchmark program that uses persistent red-black trees.
 * 
 * Asger Feldthaus <asf@cs.au.dk>
 */


function RBNode(left,right,data,red) {
	this.left = left;
	this.right = right;
	this.data = data;
	this.red = red;
}
RBNode.prototype.toString = function() {
	return (this.red? "R":"B") + "[" + data + "]";
}

/**
 * Returns the data of a node that compares equal to the given comparator.
 * @param node an RBNode
 * @param comparator unary comparator
 */
function find(node, comparable) {
	while (node != null) {
		var c = comparable(node.data);
		if (c == 0)
			return node.data;
		else if (c < 0)
			node = node.left;
		else
			node = node.right;
	}
	return null;
}

function successor(node, comparable) {
	var result = null;
	while (node != null) {
		var c = comparable(node.data);
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

function predecessor(node, comparable) {
	var result = null;
	while (node != null) {
		var c = comparable(node.data);
		if (c == 0)
			return node.data;
		else if (c < 0) {
			node = node.left;
		} else {
			result = node.data;
			node = node.right;
		}
	}
	return result;
}

/**
 * Assumes data has a compareTo method. Returns new tree.
 */
function insert(node, data) {
	var comparator = function(other) {
		return data.compareTo(other);
	};
	return insert_sub(node,data,comparator,false).node;
}

function insert_sub(node, data, comparator, isLeftChild) {
	if (node == null) {
		return {
			node: new RBNode(null, null, data, true),
			existing: null,
			higher: false
		};
	}
	function makeBlack(n) {
		return new RBNode(n.left, n.right, n.data, false);
	}
	// invariant 1: if tree got taller, then returned node is black, and its child leaning towards parent is also black (or null)
	var c = comparator(node.data);
	if (c == 0) {
		return {
			node: new RBNode(node.left, node.right, data, node.red),
			existing: node.data,
			higher: false
		};
	}
	if (c < 0) {
		var answer = insert_sub(node.left, data, comparator, true);
		var newleft = answer.node;
		if (answer.higher /* -> higher */) {
			answer.higher = false;
			var sibling = node.right;
			if (sibling != null && sibling.red) {
				answer.node = new RBNode(newleft, makeBlack(sibling), node.data, true);
			} else {
				answer.node = new RBNode(newleft.left, new RBNode(newleft.right, node.right, node.data, true), newleft.data, false);
			}
		} else {
			if (node.red && newleft.red) {
				answer.higher = true;
				if (!isLeftChild) {
					answer.node = new RBNode(newleft.left, new RBNode(newleft.right, node.right, node.data, true), newleft.data, false);
				} else {
					answer.node = new RBNode(newleft, node.right, node.data, false);
				}
			} else {
				answer.node = new RBNode(newleft, node.right, node.data, node.red); 
			}
		}
		return answer;
	} else {
		var answer = insert_sub(node.right, data, comparator, false);
		var newright = answer.node;
		if (answer.higher) {
			answer.higher = false;
			var sibling = node.left;
			if (sibling != null && sibling.red) {
				answer.node = new RBNode(makeBlack(sibling), newright, node.data, true);
			} else {
				answer.node = new RBNode(new RBNode(node.left, newright.left, node.data, true), newright.right, newright.data, false);
			}
		} else {
			if (node.red && newright.red) {
				answer.higher = true;
				if (isLeftChild) {
					answer.node = new RBNode(new RBNode(node.left, newright.left, node.data, true), newright.right, newright.data, false);
				} else {
					answer.node = new RBNode(node.left, newright, node.data, false);
				}
			} else {
				answer.node = new RBNode(node.left, newright, node.data, node.red);
			}
		}
		return answer;
	}
}

function KeyValue(key,value) {
	this.key = key;
	this.value = value;
}
KeyValue.prototype.compareTo = function(other) {
	return this.key.compareTo(other.key);
}
KeyValue.makeComparator = function(key) {
	return function (kv) {
		return key.compareTo(kv.key);
	}
}

function Dictionary() {
	this.tree = null;
}
Dictionary.prototype.put = function (key,value) {
	this.tree = insert(this.tree, new KeyValue(key,value));
}
Dictionary.prototype.get = function (key) {
	return find(this.tree, KeyValue.makeComparator(key)).value;
}

String.prototype.compareTo = function(other) {
	if (this < other)
		return -1;
	else if (other < this)
		return 1;
	else
		return 0;
}

var dict1 = new Dictionary();
dict1.put("The", 45);
dict1.put("quick", 20);
dict1.put("brown", 16);
dict1.put("fox", 55);
dict1.put("zoomed", 10);
dict1.put("around", 16);
dict1.put("eagerly", 60);

var dict2 = new Dictionary();
dict2.put("foo", true);
dict2.put("bar", true);
dict2.put("baz", true);
dict2.put("bong", true);
dict2.put("diz", true);

var readDict1 = dict1.get("quick");
var readDict2 = dict2.get("baz");
