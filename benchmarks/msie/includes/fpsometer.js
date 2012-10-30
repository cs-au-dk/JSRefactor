/* FPS Meter Class ---------------------------------------------------------------- */

function FpsMeter() {

    // Get browser information.
    var UA = navigator.userAgent.toLowerCase();
    var index;

    if (UA.indexOf('msie') > -1) {
        index = UA.indexOf('msie');
        this.browserName = "Internet Explorer";
        this.browserVersion = "" + parseFloat('' + UA.substring(index + 5));
    }
    else if (UA.indexOf('chrome') > -1) {
        index = UA.indexOf('chrome');
        this.browserName = "Chrome";
        this.browserVersion = "" + parseFloat('' + UA.substring(index + 7));
    }
    else if (UA.indexOf('firefox') > -1) {
        index = UA.indexOf('firefox');
        this.browserName = "Firefox";
        this.browserVersion = "" + parseFloat('' + UA.substring(index + 8));
    }
    else if (UA.indexOf('opera') > -1) {
        browserName = "Opera";
    }
    else if (UA.indexOf('safari') > -1) {
        index = UA.indexOf('safari');
        this.browserName = "Safari";
        this.browserVersion = "" + parseFloat('' + UA.substring(index + 7));
    }

    // Create Canvas for FPS control and add it the document.
    this.canvas = document.createElement("canvas");
    this.ctx = this.canvas.getContext("2d");
    this.canvas.id = "fpsCanvas";
    this.canvas.setAttribute('width', 130);
    this.canvas.setAttribute('height', 80);
    this.canvas.style.position = "absolute";
    this.canvas.style.top = "70px";
    this.canvas.style.right = "62px";
    this.canvas.style.width = "100px";
    this.canvas.style.zIndex = "15";
    document.body.insertBefore(this.canvas, document.body.firstChild);
}



/* Class Properties ----------------------------------------------------------------- */

FpsMeter.prototype.fps = 0;                     // how many frames per second are calculated
FpsMeter.prototype.displayedFps = 0;            // the displayed FPS. This is updated to match fps according to fpsDisplayUpdateFrequency
FpsMeter.prototype.lastFrameTime = new Date();  // time of the last frame
FpsMeter.prototype.timeDelta = .001;            // milliseconds since the last frame
FpsMeter.prototype.timeDeltaS = .1;             // seconds since the last frame
FpsMeter.prototype.currentSecond = 0;
FpsMeter.prototype.framesThisSecond = 0;
FpsMeter.prototype.timeDeltaSinceLastFrame = 0;
FpsMeter.prototype.timeFpsDisplayLastChanged = 0;
FpsMeter.prototype.fpsDisplayUpdateFrequency = 500;
FpsMeter.prototype.ctx;
FpsMeter.prototype.canvas;
FpsMeter.prototype.browserName;
FpsMeter.prototype.browserVersion;
FpsMeter.prototype.meterPercent = -1;
FpsMeter.prototype.meterPercentGoal = -1;
FpsMeter.prototype.browserVersion = "";
FpsMeter.prototype.browserName = "Other Browser";
FpsMeter.prototype.visible = "false";


/* Meter Visibility ---------------------------------------------------------------- */

FpsMeter.prototype.Hide = function () {
    this.canvas.style.display = "none";
    this.visible = false;
}

FpsMeter.prototype.Show = function () {
    this.canvas.style.display = "inline";
    this.visible = true;
}


/* Draw FPS Meter ------------------------------------------------------------------ */

FpsMeter.prototype.Draw = function (score) {

    // Calculate the current FPS.
    var now = new Date();
    this.timeDeltaSinceLastFrame = .001;
    if (this.lastFrameTime != 0) this.timeDeltaSinceLastFrame = now - this.lastFrameTime;
    this.lastFrameTime = now;

    if (now.getSeconds() == this.currentSecond) {
        this.framesThisSecond++;
    }
    else {
        this.currentSecond = now.getSeconds();
        this.fps = this.framesThisSecond;
        this.framesThisSecond = 1;

        var timingDelayReached = ((now.getTime() - this.timeFpsDisplayLastChanged) > this.fpsDisplayUpdateFrequency);
        var fpsNotChangedYet = (this.timeFpsDisplayLastChanged == 0);

        if (timingDelayReached || fpsNotChangedYet) {
            this.timeFpsDisplayLastChanged = now.getTime();
            this.displayedFps = (this.fps > 65 ? 65 : this.fps);
            this.canvas.setAttribute("title", "Demo is running at " + this.fps + " FPS");

        }
    }

    // Draw the FPS Meter.
    this.ctx.clearRect(0, 0, 130, 70);

    // Draw the border of the meter.
    this.ctx.save();
    this.ctx.fillStyle = "rgb(73, 166, 222)";
    this.ctx.beginPath();
    this.ctx.arc(65, 65, 54, Math.PI, Math.PI * 2, false);
    this.ctx.fill();
    this.ctx.restore();

    // Draw the background of the meter.
    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.fillStyle = "silver";
    var backgroundgrad = this.ctx.createLinearGradient(0, 0, 100, 100);
    backgroundgrad.addColorStop(0, "rgb(241,251,253)");
    backgroundgrad.addColorStop(0.95, "rgb(208,238,246)");
    backgroundgrad.addColorStop(1, "rgb(241,251,253)");
    this.ctx.fillStyle = backgroundgrad;
    this.ctx.arc(65, 65, 50, Math.PI, Math.PI * 2, false);
    this.ctx.fill();
    this.ctx.restore();

    // Draw the gradient representing the meter value.
    if (this.displayedFps > 1) {

        // A maximum of 60fps are drawn so cap the gauge at 60fps.
        this.meterPercentGoal = (this.displayedFps > 60 ? 60 : this.displayedFps) / 60;       //a maximum of 60fps are drawn so cap the gauge at 60fps.

        if (this.meterPercent == -1) {
            this.meterPercent = .01;
        }

        if (this.meterPercent < this.meterPercentGoal) {
            var delta = Math.abs(this.meterPercent - this.meterPercentGoal);
            this.meterPercent *= 1 + delta / 3;

            if (this.meterPercent > this.meterPercentGoal) {
                this.meterPercent = this.meterPercentGoal;
            }
        } else if (this.meterPercent > this.meterPercentGoal) {
            this.meterPercent *= .99;

            if (this.meterPercent < this.meterPercentGoal) {
                this.meterPercent = this.meterPercentGoal;
            }
        }

        this.ctx.save();
        var lingrad = this.ctx.createLinearGradient(0, 65, 100, -45);
        lingrad.addColorStop(0, "rgb(206,5,13)");
        lingrad.addColorStop(Math.ceil((.75 - this.meterPercent) * 100) / 100 < 0 ? 0 : Math.ceil((.75 - this.meterPercent) * 100) / 100, "rgb(265,241,22)");
        lingrad.addColorStop(1, "rgb(1,197,10)");
        this.ctx.fillStyle = lingrad;
        this.ctx.beginPath();
        this.ctx.arc(65, 65, 50, Math.PI, Math.PI + Math.PI * (this.meterPercent), false);
        this.ctx.arc(65, 65, 6, Math.PI + Math.PI, Math.PI, true);
        this.ctx.fill();
        this.ctx.restore();
    }


    // Draw the labels
    this.ctx.textAlign = "center";
    this.ctx.font = "7pt Verdana";
    if (this.fps < 15) {
        this.ctx.fillStyle = "rgb(218, 226, 0)";
        this.ctx.fillText("0", 25, 58);
        this.ctx.fillStyle = "rgb(73, 166, 222)";
        this.ctx.fillText("15", 36, 40);
        this.ctx.fillText("30", 65, 27);
        this.ctx.fillText("45", 91, 40);
        this.ctx.fillText("60", 102, 58);
    }
    else if (this.fps >= 15 && this.fps < 30) {
        this.ctx.fillStyle = "rgb(218, 226, 0)";
        this.ctx.fillText("0", 25, 58);
        this.ctx.fillStyle = "rgb(238, 168, 0)";
        this.ctx.fillText("15", 36, 40);
        this.ctx.fillStyle = "rgb(73, 166, 222)";
        this.ctx.fillText("30", 65, 27);
        this.ctx.fillText("45", 91, 40);
        this.ctx.fillText("60", 102, 58);
    }
    else if (this.fps >= 30 && this.fps < 45) {
        this.ctx.fillStyle = "rgb(226, 170, 0)";
        this.ctx.fillText("0", 25, 58);
        this.ctx.fillStyle = "rgb(85, 158, 9)";
        this.ctx.fillText("15", 36, 40);
        this.ctx.fillText("30", 65, 27);
        this.ctx.fillStyle = "rgb(73, 166, 222)";
        this.ctx.fillText("45", 91, 40);
        this.ctx.fillText("60", 102, 58);
    }
    else {
        this.ctx.fillStyle = "rgb(85, 158, 9)";
        this.ctx.fillText("0", 25, 58);
        this.ctx.fillText("15", 36, 40);
        this.ctx.fillText("30", 65, 27);
        this.ctx.fillText("45", 91, 40);
        this.ctx.fillText("60", 102, 58);
    }

    // Draw divider line
    this.ctx.save();
    this.ctx.strokeStyle = "rgb(73, 166, 222)";
    this.ctx.lineWidth = 3;
    this.ctx.lineCap = "round";
    this.ctx.beginPath();
    this.ctx.moveTo(12, 63);
    this.ctx.lineTo(118, 63);
    this.ctx.stroke();
    this.ctx.restore();

    // Draw needle border
    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.lineWidth = 10;
    this.ctx.lineCap = "round";
    this.ctx.moveTo(63, 63);
    this.ctx.lineTo(65 + 58 * -Math.cos(Math.PI * this.meterPercent), 65 + 58 * -Math.sin(Math.PI * this.meterPercent));
    this.ctx.strokeStyle = "rgb(73, 166, 222)";
    this.ctx.stroke();
    this.ctx.restore();

    // Draw needle body
    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.lineWidth = 4;
    this.ctx.lineCap = "round";
    this.ctx.moveTo(63, 63);
    this.ctx.lineTo(65 + 58 * -Math.cos(Math.PI * this.meterPercent), 65 + 58 * -Math.sin(Math.PI * this.meterPercent));
    this.ctx.strokeStyle = "rgb(255,241,22)";
    this.ctx.stroke();
    this.ctx.restore();

    // Draw needle endcap
    this.ctx.beginPath();
    this.ctx.fillStyle = "rgb(73, 166, 222)";
    this.ctx.arc(63, 63, 8, Math.PI + Math.PI, Math.PI, true);
    this.ctx.arc(63, 63, 8, Math.PI + Math.PI, Math.PI, false);
    this.ctx.fill();
}