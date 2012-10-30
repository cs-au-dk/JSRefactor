
function Stream(str, index) {
	this.str = str;
	this.index = index || 0;
}
Stream.make = function(str) {
	return new Stream(str,0);
};
Stream.prototype.take = function(len) {
	return this.str.substring(this.index, this.index+len);
}
Stream.prototype.drop = function(len) {
	return new Stream(this.str, this.index+len);
}
Stream.prototype.rest = function() {
	return this.str.substring(this.index);
}
Stream.prototype.isEmpty = function() {
	return this.index >= this.str.length;
};
Stream.prototype.toString = function() {
	return this.str.substring(this.index);
};

function Success(value,remainder) {
	this.value = value;
	this.remainder = remainder;
}
function Failure(msg) {
	this.msg = msg;
}

function Parser(apply) {
	this.apply = apply;
}
Parser.token = function(str) {
	return new Parser(function(stream) {
		var trunc = stream.take(str.length);
		if (trunc === str) {
			return new Success([str], stream.drop(str.length));
		} else {
			return new Failure("Expected '" + str + "' but got '" + trunc + "'");
		}
	});
};
Parser.regexp = function (regexp) {
	return new Parser(function (stream) {
		var match = regexp.exec(stream.rest());
		if (match !== null && match.index === 0) {
			var s = match[0];
			return new Success([s], stream.drop(s.length));
		} else {
			return new Failure("Expected something to match '" + regexp + "' but got '" + stream + "'");
		}
	});
};
Parser.recursive = function() {
	var child;
	var parser = new Parser(function(stream) {
		return child.apply(stream);
	});
	parser.init = function (ch) {
		child = ch;
		return this;
	};
	return parser;
};
Parser.prototype.andThen = function(s2) { // sequence
	var left = this;
	var right = s2;
	return new Parser(function (stream) {
		var lm = left.apply(stream);
		if (lm.constructor === Failure) {
			return lm;
		}
		var rm = right.apply(lm.remainder);
		if (rm.constructor === Failure)
			return rm;
		return new Success(lm.value.concat(rm.value), rm.remainder);
	});
};
Parser.prototype.or = function(s2) { // disjunction
	var left = this;
	var right = s2;
	return new Parser(function (stream) {
		var lm = left.apply(stream);
		if (lm.constructor === Success)
			return lm;
		return right.apply(stream);
	});
};
Parser.prototype.repeat = function (min,sep) {
	if (typeof min === "undefined")
		min = 1;
	var left = this;
	return new Parser(function (stream) {
		var count = 0;
		var result = [];
		var rem = stream;
		while (true) {
			if (count > 0 && sep) {
				var sm = sep.apply(rem);
				if (sm.constructor === Failure) {
					if (count < min)
						return sm;
					else
						break;
				}
				rem = sm.remainder;
				result = result.concat(sm.value);
			}
			var lm = left.apply(rem);
			if (lm.constructor === Failure) {
				if (count < min)
					return lm;
				else
					break;
			}
			result = result.concat(lm.value);
			rem = lm.remainder;
			count++;
		}
		return new Success([result], rem); // return singleton array with one array in it
	});
};
Parser.prototype.action = function (f) {
	var left = this;
	return new Parser(function (stream) {
		var lm = left.apply(stream);
		if (lm.constructor === Failure)
			return lm;
		var result = f.apply(null, lm.value);
		return new Success([result], lm.remainder);
	});
};
Parser.prototype.ign = function(num) { // ignore some elements
	if (typeof num === "undefined") {
		num = 1;
	}
	var left = this;
	return new Parser(function (stream) {
		var lm = left.apply(stream);
		if (lm.constructor === Failure)
			return lm;
		return new Success(lm.value.slice(0,-num), lm.remainder);
	});
};


// Unification Exp
var Exp = (function() {
	function Var(v) {
		this.name = v;
	}
	Var.prototype.toString = function() {
		return this.name;
	}
	function Term(t, children) {
		this.head = t;
		this.children = children;
	}
	Term.prototype.toString = function() {
		return this.head + "[" + this.children.join(",") + "]";
	}
	function fold(caseVar, caseTerm) {
		return function f(exp) {
			switch (exp.constructor) {
			case Var:
				return caseVar(exp.name);
			case Term:
				return caseTerm(exp.head, exp.children.map(f));
			default:
				throw "Unknown exp: " + exp;
			}
		}
	}
	return {Var:Var, Term:Term, fold:fold};
})();

var keyw = Parser.token;
var regex = Parser.regexp;
function igno (str) {
	return Parser.token(str).ign();
}

var termOrVar = Parser.recursive();
var term = regex(/[A-Z][a-z]*/).andThen(igno("[")).andThen(termOrVar.repeat(0,igno(","))).andThen(igno("]")).action(
		function (head,children) {
			return new Exp.Term(head,children);
		});
var evar = regex(/[a-z][a-z]*/).action(
		function (name) {
			return new Exp.Var(name);
		});
termOrVar.init(term.or(evar));

function parseExp(str) {
	var result = termOrVar.apply(Stream.make(str));
	if (result.constructor === Success) {
		if (result.remainder.isEmpty()) {
			return result.value;
		} else {
			throw new Error("Expected end of string, but got: " + result.remainder);
		}
	} else {
		throw new Error(result.msg);
	}
}

var e1 = parseExp("G[x,F[]]");
var e2 = parseExp("G[G[x],x]");

print(e1);
print(e2);

