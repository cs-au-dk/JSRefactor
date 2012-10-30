function Company(addr, taxId) {
  this.addr /* -> address */ = addr;
  this.taxId = taxId;
}

function Person(addr, taxId, name) {
  this.addr = addr;
  this.taxId = taxId;
  this.name = name;
}

function Student(addr, taxId, name, id) {
  Person.call(this, addr, taxId, name);
  this.id = id;
}
Student.prototype = new Person;

function $(id) {
  return document.getElementById(id);
}

function create() {
  var addr = $('addr').value;
  var taxId = $('taxId').value;
  var name = $('name').value;
  if(name === "")
    return new Company(addr, taxId);
  else
    return new Person(addr, taxId, name);
}

var x = create();
var addr = x.addr;