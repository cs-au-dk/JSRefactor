// from delta-blue

BinaryConstraint.prototype.chooseMethod = function (mark) {
  if (this.v1.mark == mark) {
    this.direction = (this.v1.mark != mark && Strength.stronger(this.strength, this.v2.walkStrength))
      ? Direction.FORWARD
      : Direction.NONE;
  }
  if (this.v2.mark == mark) {
    this.direction = (this.v1.mark != mark && Strength.stronger(this.strength, this.v1.walkStrength))
      ? Direction.BACKWARD
      : Direction.NONE;
  }
  if (Strength.weaker(this.v1.walkStrength, this.v2.walkStrength)) {
    this.direction = Strength.stronger(this.strength, this.v1.walkStrength)
      ? Direction.BACKWARD
      : Direction.NONE;
  } else {
    this.direction = Strength.stronger(this.strength, this.v2.walkStrength)
      ? Direction.FORWARD
      : Direction.BACKWARD
  }
}