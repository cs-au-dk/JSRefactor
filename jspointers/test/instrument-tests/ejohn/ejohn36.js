function User(first, last){ 
  if ( !(this instanceof User) ) 
    return new User(first, last); 
   
  this.name = first + " " + last; 
} 
 
var name = "Resig"; 
var user = User("John", name); 
 
assert( user ); 
assert( name == "Resig" );
