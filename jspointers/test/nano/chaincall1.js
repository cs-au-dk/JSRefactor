function F1(x, y) {
	x.back = y;
	return y(x);
}
function F2(x, y) {
	x.back = y;
	if (x.next != null)
		return F1(x.next ,y);
	else
		return F1(x.prev, y);
}
function F3(x, y) {
	x.back = y;
	if (x.next != null)
		return F2(x.next ,y);
	else
		return F2(x.prev, y);
}
function F4(x, y) {
	x.back = y;
	if (x.next != null)
		return F3(x.next ,y);
	else
		return F3(x.prev, y);
}
function F5(x, y) {
	x.back = y;
	if (x.next != null)
		return F4(x.next ,y);
	else
		return F4(x.prev, y);
}
function F6(x, y) {
	x.back = y;
	if (x.next != null)
		return F5(x.next ,y);
	else
		return F5(x.prev, y);
}
function F7(x, y) {
	x.back = y;
	if (x.next != null)
		return F6(x.next ,y);
	else
		return F6(x.prev, y);
}
function F8(x, y) {
	x.back = y;
	if (x.next != null)
		return F7(x.next ,y);
	else
		return F7(x.prev, y);
}
function F9(x, y) {
	x.back = y;
	if (x.next != null)
		return F8(x.next ,y);
	else
		return F8(x.prev, y);
}
function F10(x, y) {
	x.back = y;
	if (x.next != null)
		return F9(x.next ,y);
	else
		return F9(x.prev, y);
}
function F11(x, y) {
	x.back = y;
	if (x.next != null)
		return F10(x.next ,y);
	else
		return F10(x.prev, y);
}
function F12(x, y) {
	x.back = y;
	if (x.next != null)
		return F11(x.next ,y);
	else
		return F11(x.prev, y);
}
function F13(x, y) {
	x.back = y;
	if (x.next != null)
		return F12(x.next ,y);
	else
		return F12(x.prev, y);
}
function F14(x, y) {
	x.back = y;
	if (x.next != null)
		return F13(x.next ,y);
	else
		return F13(x.prev, y);
}
function F15(x, y) {
	x.back = y;
	if (x.next != null)
		return F14(x.next ,y);
	else
		return F14(x.prev, y);
}
function F16(x, y) {
	x.back = y;
	if (x.next != null)
		return F15(x.next ,y);
	else
		return F15(x.prev, y);
}
function F17(x, y) {
	x.back = y;
	if (x.next != null)
		return F16(x.next ,y);
	else
		return F16(x.prev, y);
}
function F18(x, y) {
	x.back = y;
	if (x.next != null)
		return F17(x.next ,y);
	else
		return F17(x.prev, y);
}
function F19(x, y) {
	x.back = y;
	if (x.next != null)
		return F18(x.next ,y);
	else
		return F18(x.prev, y);
}
function F20(x, y) {
	x.back = y;
	if (x.next != null)
		return F19(x.next ,y);
	else
		return F19(x.prev, y);
}

function Foo() {
	return 50;
}
function Foo2() {
	return "";
}
function Bar(x) {
	return x.y;
}
var obj9 = {next:{next:{next:{next:{next:{next:{next:{next:{next:{y:Foo}}}}}}}}}, y:Foo2};
var o = obj9;
while (o.next != null) {
	o.next.prev = o;
	o = o.next;
}
var z = F20(obj9, Bar);
var z2 = F19(obj9, Bar);

z();
z2();