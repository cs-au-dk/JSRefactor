Object.prototype.$invoke_method = function(methodName, callId, args) {
	var func = this[methodName];
	if (typeof func !== "function") {
		throw "Invoking a non-function at callsite " + callId;
	}
	if (func.$_callers && func.$_callers.indexOf(callId) === -1) {
		throw "Unexpected caller " + callId + " of " + func.$_info;
	}
	return func.apply(this, args);
};
function $invoke_function(func, callId, args) {
	if (typeof func !== "function") {
		throw "Invoking a non-function at callsite " + callId;
	}
	if (func.$_callers && func.$_callers.indexOf(callId) === -1) {
		throw "Unexpected caller " + callId + " of " + func.$_info;
	}
	return func.apply(this, args);
};
Function.prototype.$init = function(id, protoId, callers, info) {
	this.$_obj_id = id;
	this.$_callers = callers;
	this.$_info = info;
	this.prototype.$_obj_id = protoId;
	return this;
};
Object.prototype.$init = function(id) {
	this.$_obj_id = id;
	return this;
}

function $isObject(obj) {
	return !(typeof obj === "number" || typeof obj === "boolean" || typeof obj === "string")
		&& obj !== undefined && obj !== null;
}

function $construct(constructor, id, args) {
	var obj;
	if (!constructor.$_obj_id) { // native
		switch (args.length) {
		case 0: obj = new constructor(); break;
		case 1: obj = new constructor(args[0]); break;
		case 2: obj = new constructor(args[0], args[1]); break;
		case 3: obj = new constructor(args[0], args[1], args[2]); break;
		case 4: obj = new constructor(args[0], args[1], args[2], args[3]); break;
		case 5: obj = new constructor(args[0], args[1], args[2], args[3], args[4]); break;
		default: throw "Native constructor called with >5 arguments";
		}
		obj.$_obj_id = id;
		return obj;
	} else {
		// have to set id before invoking constructor body
		obj = Object.create(constructor.prototype);
		obj.$_obj_id = id;
		var result = constructor.apply(obj, args);
		if ($isObject(result))
			return result;
		else
			return obj;
	}
}

function $check_object_graph(objects,expectedIds,info,exp,maxDepth) {
	var visited = new Array($graph.length);
	var chain = [];
	function chain2string() {
		var s = chain[0];
		for (var i=1; i<chain.length; i++) {
			s += "." + chain[i];
		}
		return s;
	}
	function visit(obj,expectedIds,depth) {
		if (!$isObject(obj))
			return;
		var names;
		try {
			names = Object.getOwnPropertyNames(obj);
		} catch (annoyingError) {
			return;
		}
		if (names.indexOf("$_obj_id") === -1)
			return;
		if (!expectedIds) {
			throw "Invalid object graph at " + info + ": " + chain2string() + " unexpectedly exists";
		}
		if (expectedIds.indexOf(-1) !== -1)
			return; // non-standard object explicitly excluded
		var id = obj.$_obj_id;
		if (visited[id])
			return;
		visited[id] = true;
		if (expectedIds.indexOf(id) === -1) {
			throw "Invalid object graph at " + info + ": " + chain2string() + " == " + id + " but expected " + expectedIds;
		}
		var abstractObj = $graph[id];
		if (depth < maxDepth) {
			for (var i=0; i<names.length; i++) {
				var name = names[i];
				var ch = name.charAt(0);
				if (ch === "$" || (ch >= "0" && ch <= "9"))
					continue;
				var obj2 = obj[name];
				chain.push(name);
				visit(obj2, abstractObj[name], depth+1);
				chain.pop();
			}
			var proto = Object.getPrototypeOf(obj);
			if (proto) {
				chain.push("[Prototype]");
				visit(proto, abstractObj.$_proto, depth+1);
				chain.pop();
			}
		}
	}
	for (var j=0; j<objects.length; j++) {
		chain.push(exp[j]);
		visit(objects[j],expectedIds[j],exp[j],0);
		chain.pop();
	}
}

// ----- some commonly used functions in the test cases ------

function assert(b,msg) {
	if (!b) {
		throw (msg || "Assertion failed!");
	}
}
function dumpValue(x) {
	print(x);
}
function dumpObject(x) {
	print(x);
}
function assertEquals(x,y,msg) {
	if (x !== y) {
		throw (msg || "assertEquals("+x+","+y+")");
	}
}
function assertArrayEquals(x,y,msg) {
	if (x.length !== y.length) {
		throw (msg || "assertEquals("+x+","+y+")");
	}
	for (var i=0; i<x.length; i++) {
		assertEquals(x[i], y[i], msg);
	}
}
function assertThrows(code) {
	throw "assertThrows is not supported";
}

