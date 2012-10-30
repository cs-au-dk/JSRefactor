
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

var Unification = (function() {
	function Subst(list) {
		this.list = list;
	}
	Subst.empty = new Subst([]);
	Subst.singleton = function(k,v) {
		return new Subst([{key:k, value:v}]);
	};
	Subst.prototype.lookup = function(key) {
		for (var i=0; i<this.list.length; i++) {
			if (this.list[i].key === key)
				return this.list[i].value;
		}
		return null;
	};
	Subst.prototype.toString = function() {
		return this.list.map(function (obj) {
			return obj.key + "->" + obj.value;
		}).join(",");
	};
	Subst.prototype.map = function(f) {
		return new Subst(this.list.map(function (kv) {
			return {key:kv.key, value:f(kv.value)};
		}));
	};
	Subst.prototype.concat = function(s2) {
		return new Subst(this.list.concat(s2.list));
	};
	Subst.prototype.compose = function(s2) {
		return this.map(applySubst(s2)).concat(s2);
	};
	function Err(msg) {
		this.msg = msg;
	}
	Err.prototype.toString = function() {
		return "Error: " + this.msg;
	}
	function unify(t1, t2) {
		if (t1.constructor === Exp.Var) {
			return Subst.singleton(t1.name, t2);
		} else if (t2.constructor === Exp.Var) {
			return Subst.singleton(t2.name, t1);
		} else if (t1.head !== t2.head) {
			return new Err("Head mismatch: " + t1.head + ", " + t2.head);
		} else {
			return unifyList(t1.children, t2.children);
		}
	}
	function unifyList(ts1, ts2) {
		if (ts1.length !== ts2.length) {
			return new Err("Arity mismatch");
		} else {
			var len = ts1.length;
			var subst = Subst.empty;
			for (var i=0; i<len; i++) {
				var t1 = ts1[i];
				var t2 = ts2[i];
				var s2 = unify(applySubst(subst)(t1), applySubst(subst)(t2));
				if (s2.constructor === Err)
					return s2;
				subst = subst.compose(s2);
			}
			return subst;
		}
	}
	function applySubst(subst) {
		return Exp.fold(
			function (v) { // caseVar
				var w = subst.lookup(v);
				if (w !== null)
					return w;
				else
					return new Exp.Var(v);
			},
			function(t, children) { // caseTerm
				return new Exp.Term(t, children);
			}
		);
	}
	return {unify:unify, unifyList:unifyList, applySubst:applySubst, Subst:Subst, Err:Err};
})();

var exp1 = new Exp.Term("h", [new Exp.Var("x"), new Exp.Term("h",[new Exp.Var("x")])]);
var exp2 = new Exp.Term("h", [new Exp.Term("g",[]), new Exp.Var("y")]);

print("Exp1 = " + exp1);
print("Exp2 = " + exp2);

var uni = Unification.unify(exp1,exp2);

print("uni = " + uni);