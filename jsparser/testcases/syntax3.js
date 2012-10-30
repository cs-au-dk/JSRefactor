// can the automatic semi-colon insertion distinguish between object literals and blocks?
// (no, but the grammar accepts a fake semicolon in object literals)
function bar() {}

x = {foo:bar()};
{foo:bar()}
