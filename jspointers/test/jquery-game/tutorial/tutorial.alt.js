// Global constants:
var PLAYGROUND_WIDTH	= 700;
var PLAYGROUND_HEIGHT	= 250;
var REFRESH_RATE		= 15;

var GRACE		= 2000;
var MISSILE_SPEED = 10; //px per frame

/*Constants for the gameplay*/
var smallStarSpeed    	= 1 //pixels per frame
var mediumStarSpeed		= 3 //pixels per frame
var bigStarSpeed		= 4 //pixels per frame

// Gloabl animation holder
var playerAnimation = new Array();
var missile = new Array();
var enemies = new Array(3); // There are three kind of enemies in the game

// Game state
var bossMode = false;
var bossName = null;
var playerHit = false;
var timeOfRespawn = 0;
var gameOver = false;

// Some hellper functions : 

// Function to restart the game:
function restartgame(){
	window.location.reload();
};

function explodePlayer(playerNode){
	playerNode.children().hide();
	playerNode.addSprite("explosion",{animation: playerAnimation["explode"], width: 100, height: 26})
	playerHit = true;
}


// Game objects:
function Player(node){

	this.node = node;
	//this.animations = animations;

	this.grace = false;
	this.replay = 3; 
	this.shield = 3; 
	this.respawnTime = -1;
	
	// This function damage the ship and return true if this cause the ship to die 
	this.damage = function(){
		if(!this.grace){
			this.shield--;
			if (this.shield == 0){
				return true;
			}
			return false;
		}
		return false;
	};
	
	// this try to respawn the ship after a death and return true if the game is over
	this.respawn = function(){
		this.replay--;
		if(this.replay==0){
			return true;
		}
		
		this.grace 	= true;
		this.shield	= 3;
		
		this.respawnTime = (new Date()).getTime();
		$(this.node).fadeTo(0, 0.5); 
		return false;
	};
	
	this.update = function(){
		if((this.respawnTime > 0) && (((new Date()).getTime()-this.respawnTime) > 3000)){
			this.grace = false;
			$(this.node).fadeTo(500, 1); 
			this.respawnTime = -1;
		}
	}
	
	return true;
}

function Enemy(node){
	this.shield	= 2;
	this.speedx	= -5;
	this.speedy	= 0;
	this.node = $(node);
	
	// deals with damage endured by an enemy
	this.damage = function(){
		this.shield--;
		if(this.shield == 0){
			return true;
		}
		return false;
	};
	
	// updates the position of the enemy
	this.update = function(playerNode){
		this.updateX(playerNode);
		this.updateY(playerNode);
	};	
	this.updateX = function(playerNode){
		var newpos = parseInt(this.node.css("left"))+this.speedx;
		this.node.css("left",""+newpos+"px");
	};
	this.updateY= function(playerNode){
		var newpos = parseInt(this.node.css("top"))+this.speedy;
		this.node.css("top",""+newpos+"px");
	};
}

function Minion(node){
	this.node = $(node);
}
Minion.prototype = new Enemy();
Minion.prototype.updateY = function(playerNode){
	var pos = parseInt(this.node.css("top"));
	if(pos > (PLAYGROUND_HEIGHT - 100)){
		this.node.css("top",""+(pos - 2)+"px");
	}
}

function Brainy(node){
	this.node = $(node);
	this.shield	= 5;
	this.speedy = 1;
	this.alignmentOffset = 5;
}
Brainy.prototype = new Enemy();
Brainy.prototype.updateY = function(playerNode){
	if((this.node[0].gameQuery.posy+this.alignmentOffset) > $(playerNode)[0].gameQuery.posy){
		var newpos = parseInt(this.node.css("top"))-this.speedy;
		this.node.css("top",""+newpos+"px");
	} else if((this.node[0].gameQuery.posy+this.alignmentOffset) < $(playerNode)[0].gameQuery.posy){
		var newpos = parseInt(this.node.css("top"))+this.speedy;
		this.node.css("top",""+newpos+"px");
	}
}

function Bossy(node){
	this.node = $(node);
	this.shield	= 20;
	this.speedx = -1;
	this.alignmentOffset = 35;
}
Bossy.prototype = new Brainy();
Bossy.prototype.updateX = function(){
	var pos = parseInt(this.node.css("left"));
	if(pos > (PLAYGROUND_WIDTH - 200)){
		this.node.css("left",""+(pos+this.speedx)+"px");
	}
}



// --------------------------------------------------------------------------------------------------------------------
// --                                      the main declaration:                                                     --
// --------------------------------------------------------------------------------------------------------------------
$(function(){
	// Aniomations declaration: 
	
	// The background:
	var background1 = new $.gameQuery.Animation({imageURL: "background1.png"});
	var background2 = new $.gameQuery.Animation({imageURL: "background2.png"}); 
	var background3 = new $.gameQuery.Animation({imageURL: "background3.png"});
	var background4 = new $.gameQuery.Animation({imageURL: "background4.png"});
	var background5 = new $.gameQuery.Animation({imageURL: "background5.png"});
	var background6 = new $.gameQuery.Animation({imageURL: "background6.png"});
 
	
	// Player space shipannimations:
	playerAnimation["idle"]		= new $.gameQuery.Animation({imageURL: "player_spaceship.png"});
	playerAnimation["explode"]	= new $.gameQuery.Animation({imageURL: "player_explode.png", numberOfFrame: 4, delta: 26, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	playerAnimation["up"]		= new $.gameQuery.Animation({imageURL: "boosterup.png", numberOfFrame: 6, delta: 14, rate: 60, type: $.gameQuery.ANIMATION_HORIZONTAL});
	playerAnimation["down"]		= new $.gameQuery.Animation({imageURL: "boosterdown.png", numberOfFrame: 6, delta: 14, rate: 60, type: $.gameQuery.ANIMATION_HORIZONTAL});
	playerAnimation["boost"]	= new $.gameQuery.Animation({imageURL: "booster1.png" , numberOfFrame: 6, delta: 14, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	playerAnimation["booster"]	= new $.gameQuery.Animation({imageURL: "booster2.png", numberOfFrame: 6, delta: 14, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	
	//  List of enemies animations :
	// 1st kind of enemy:
	enemies[0] = new Array(); // enemies have two animations
	enemies[0]["idle"]	= new $.gameQuery.Animation({imageURL: "minion_idle.png", numberOfFrame: 5, delta: 52, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	enemies[0]["explode"]	= new $.gameQuery.Animation({imageURL: "minion_explode.png", numberOfFrame: 11, delta: 52, rate: 30, type: $.gameQuery.ANIMATION_VERTICAL | $.gameQuery.ANIMATION_CALLBACK});
	
	// 2nd kind of enemy:
	enemies[1] = new Array();
	enemies[1]["idle"]	= new $.gameQuery.Animation({imageURL: "brainy_idle.png", numberOfFrame: 8, delta: 42, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	enemies[1]["explode"]	= new $.gameQuery.Animation({imageURL: "brainy_explode.png", numberOfFrame: 8, delta: 42, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL | $.gameQuery.ANIMATION_CALLBACK});
	
	// 3rd kind of enemy:
	enemies[2] = new Array();
	enemies[2]["idle"]	= new $.gameQuery.Animation({imageURL: "bossy_idle.png", numberOfFrame: 5, delta: 100, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL});
	enemies[2]["explode"]	= new $.gameQuery.Animation({imageURL: "bossy_explode.png", numberOfFrame: 9, delta: 100, rate: 60, type: $.gameQuery.ANIMATION_VERTICAL | $.gameQuery.ANIMATION_CALLBACK});
	
	// Weapon missile:
	missile["player"] = new $.gameQuery.Animation({imageURL: "player_missile.png", numberOfFrame: 6, delta: 10, rate: 90, type: $.gameQuery.ANIMATION_VERTICAL});
	missile["enemies"] = new $.gameQuery.Animation({imageURL: "enemy_missile.png", numberOfFrame: 6, delta: 15, rate: 90, type: $.gameQuery.ANIMATION_VERTICAL});
	missile["playerexplode"] = new $.gameQuery.Animation({imageURL: "player_missile_explode.png" , numberOfFrame: 8, delta: 23, rate: 90, type: $.gameQuery.ANIMATION_VERTICAL | $.gameQuery.ANIMATION_CALLBACK});
	missile["enemiesexplode"] = new $.gameQuery.Animation({imageURL: "enemy_missile_explode.png" , numberOfFrame: 6, delta: 15, rate: 90, type: $.gameQuery.ANIMATION_VERTICAL | $.gameQuery.ANIMATION_CALLBACK});
	
	// Initialize the game:
	$("#playground").playground({height: PLAYGROUND_HEIGHT, width: PLAYGROUND_WIDTH, keyTracker: true});
				
	// Initialize the background
	$.playground().addGroup("background", {width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT})
						.addSprite("background1", {animation: background1, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT})
						.addSprite("background2", {animation: background2, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT, posx: PLAYGROUND_WIDTH})
						.addSprite("background3", {animation: background3, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT})
						.addSprite("background4", {animation: background4, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT, posx: PLAYGROUND_WIDTH})
						.addSprite("background5", {animation: background5, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT})
						.addSprite("background6", {animation: background6, width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT, posx: PLAYGROUND_WIDTH})
					.end()
					.addGroup("actors", {width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT})
						.addGroup("player", {posx: PLAYGROUND_WIDTH/2, posy: PLAYGROUND_HEIGHT/2, width: 100, height: 26})
							.addSprite("playerBoostUp", {posx:37, posy: 15, width: 14, height: 18})
							.addSprite("playerBody",{animation: playerAnimation["idle"], posx: 0, posy: 0, width: 100, height: 26})
							.addSprite("playerBooster", {animation:playerAnimation["boost"], posx:-32, posy: 5, width: 36, height: 14})
							.addSprite("playerBoostDown", {posx:37, posy: -7, width: 14, height: 18})
						.end()
					.end()
					.addGroup("playerMissileLayer",{width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT}).end()
					.addGroup("enemiesMissileLayer",{width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT}).end()
					.addGroup("overlay",{width: PLAYGROUND_WIDTH, height: PLAYGROUND_HEIGHT});
	
	$("#player")[0].player = new Player($("#player"));
	
	//this is the HUD for the player life and shield
	$("#overlay").append("<div id='shieldHUD'style='color: white; width: 100px; position: absolute; font-family: verdana, sans-serif;'></div><div id='lifeHUD'style='color: white; width: 100px; position: absolute; right: 0px; font-family: verdana, sans-serif;'></div>")
	
	// this sets the id of the loading bar:
	$().setLoadBar("loadingBar", 400);
	
	//initialize the start button
	$("#startbutton").click(function(){
		$.playground().startGame(function(){
			$("#welcomeScreen").fadeTo(1000,0,function(){$(this).remove();});
		});
	})
	
	// this is the function that control most of the game logic 
	$.playground().registerCallback(function(){
		if(!gameOver){
			$("#shieldHUD").html("shield: "+$("#player")[0].player.shield);
			$("#lifeHUD").html("life: "+$("#player")[0].player.replay);
			//Update the movement of the ship:
			if(!playerHit){
				$("#player")[0].player.update();
				if(jQuery.gameQuery.keyTracker[65]){ //this is left! (a)
					var nextpos = parseInt($("#player").css("left"))-5;
					if(nextpos > 0){
						$("#player").css("left", ""+nextpos+"px");
					}
				}
				if(jQuery.gameQuery.keyTracker[68]){ //this is right! (d)
					var nextpos = parseInt($("#player").css("left"))+5;
					if(nextpos < PLAYGROUND_WIDTH - 100){
						$("#player").css("left", ""+nextpos+"px");
					}
				}
				if(jQuery.gameQuery.keyTracker[87]){ //this is up! (w)
					var nextpos = parseInt($("#player").css("top"))-3;
					if(nextpos > 0){
						$("#player").css("top", ""+nextpos+"px");
					}
				}
				if(jQuery.gameQuery.keyTracker[83]){ //this is down! (s)
					var nextpos = parseInt($("#player").css("top"))+3;
					if(nextpos < PLAYGROUND_HEIGHT - 30){
						$("#player").css("top", ""+nextpos+"px");
					}
				}
			} else {
				var posy = parseInt($("#player").css("top"))+5;
				var posx = parseInt($("#player").css("left"))-5;
				if(posy > PLAYGROUND_HEIGHT){
					//Does the player did get out of the screen?
					if($("#player")[0].player.respawn()){
						gameOver = true;
						$("#playground").append('<div style="position: absolute; top: 50px; width: 700px; color: white; font-family: verdana, sans-serif;"><center><h1>Game Over</h1><br><a style="cursor: pointer;" id="restartbutton">Click here to restart the game!</a></center></div>');
						$("#restartbutton").click(restartgame);
						$("#actors,#playerMissileLayer,#enemiesMissileLayer").fadeTo(1000,0);
						$("#background").fadeTo(5000,0);
					} else {
						$("#explosion").remove();
						$("#player").children().show();
						$("#player").css("top", PLAYGROUND_HEIGHT / 2);
						$("#player").css("left", PLAYGROUND_WIDTH / 2);
						playerHit = false;
					}
				} else {
					$("#player").css("top", ""+ posy +"px");
					$("#player").css("left", ""+ posx +"px");
				}
			}
			
			//Update the movement of the enemies
			$(".enemy").each(function(){
					this.enemy.update($("#player"));
					var posx = parseInt($(this).css("left"));
					if((posx + 100) < 0){
						$(this).remove();
						return;
					}
					//Test for collisions
					var collided = $(this).collision("#playerBody,.group");
					if(collided.length > 0){
						if(this.enemy instanceof Bossy){
								$(this).setAnimation(enemies[2]["explode"], function(node){$(node).remove();});
								$(this).css("width", 150);
						} else if(this.enemy instanceof Brainy) {
							$(this).setAnimation(enemies[1]["explode"], function(node){$(node).remove();});
							$(this).css("width", 150);
						} else {
							$(this).setAnimation(enemies[0]["explode"], function(node){$(node).remove();});
							$(this).css("width", 200);
						}
						$(this).removeClass("enemy");
						//The player has been hit!
						if($("#player")[0].player.damage()){
							explodePlayer($("#player"));
						}
					}
					//Make the enemy fire
					if(this.enemy instanceof Brainy){
						if(Math.random() < 0.05){
							var enemyposx = parseInt($(this).css("left"));
							var enemyposy = parseInt($(this).css("top"));
							var name = "enemiesMissile_"+Math.ceil(Math.random()*1000);
							$("#enemiesMissileLayer").addSprite(name,{animation: missile["enemies"], posx: enemyposx, posy: enemyposy + 20, width: 30,height: 15});
							$("#"+name).addClass("enemiesMissiles");
						}
					}
				});
			
			//Update the movement of the missiles
			$(".playerMissiles").each(function(){
					var posx = parseInt($(this).css("left"));
					if(posx > PLAYGROUND_WIDTH){
						$(this).remove();
						return;
					}
					$(this).css("left", ""+(posx+MISSILE_SPEED)+"px");
					//Test for collisions
					var collided = $(this).collision(".group,.enemy");
					if(collided.length > 0){
						//An enemy has been hit!
						collided.each(function(){
								if($(this)[0].enemy.damage()){
									if(this.enemy instanceof Bossy){
											$(this).setAnimation(enemies[2]["explode"], function(node){$(node).remove();});
											$(this).css("width", 150);
									} else if(this.enemy instanceof Brainy) {
										$(this).setAnimation(enemies[1]["explode"], function(node){$(node).remove();});
										$(this).css("width", 150);
									} else {
										$(this).setAnimation(enemies[0]["explode"], function(node){$(node).remove();});
										$(this).css("width", 200);
									}
									$(this).removeClass("enemy");
								}
							})
						$(this).setAnimation(missile["playerexplode"], function(node){$(node).remove();});
						$(this).css("width", 38);
						$(this).css("height", 23);
						$(this).css("top", parseInt($(this).css("top"))-7);
						$(this).removeClass("playerMissiles");
					}
				});
			$(".enemiesMissiles").each(function(){
					var posx = parseInt($(this).css("left"));
					if(posx < 0){
						$(this).remove();
						return;
					}
					$(this).css("left", ""+(posx-MISSILE_SPEED)+"px");
					//Test for collisions
					var collided = $(this).collision(".group,#playerBody");
					if(collided.length > 0){
						//The player has been hit!
						collided.each(function(){
								if($("#player")[0].player.damage()){
									explodePlayer($("#player"));
								}
							})
						//$(this).remove();
						$(this).setAnimation(missile["enemiesexplode"], function(node){$(node).remove();});
						$(this).removeClass("enemiesMissiles");
					}
				});
		}
	}, REFRESH_RATE);
	
	//This function manage the creation of the enemies
	$.playground().registerCallback(function(){
		if(!bossMode && !gameOver){
			if(Math.random() < 0.4){
				var name = "enemy1_"+Math.ceil(Math.random()*1000);
				$("#actors").addSprite(name, {animation: enemies[0]["idle"], posx: PLAYGROUND_WIDTH, posy: Math.random()*PLAYGROUND_HEIGHT,width: 150, height: 52});
				$("#"+name).addClass("enemy");
				$("#"+name)[0].enemy = new Minion($("#"+name));
			} else if (Math.random() < 0.5){
				var name = "enemy1_"+Math.ceil(Math.random()*1000);
				$("#actors").addSprite(name, {animation: enemies[1]["idle"], posx: PLAYGROUND_WIDTH, posy: Math.random()*PLAYGROUND_HEIGHT,width: 100, height: 42});
				$("#"+name).addClass("enemy");
				$("#"+name)[0].enemy = new Brainy($("#"+name));
			} else if(Math.random() > 0.8){
				bossMode = true;
				bossName = "enemy1_"+Math.ceil(Math.random()*1000);
				$("#actors").addSprite(bossName, {animation: enemies[2]["idle"], posx: PLAYGROUND_WIDTH, posy: Math.random()*PLAYGROUND_HEIGHT,width: 100, height: 100});
				$("#"+bossName).addClass("enemy");
				$("#"+bossName)[0].enemy = new Bossy($("#"+bossName));
			}
		} else {
			if($("#"+bossName).length == 0){
				bossMode = false;
			}
		}
		
	}, 1000); //once per seconds is enough for this 
	
	
	//This is for the background animation
	$.playground().registerCallback(function(){
		//Offset all the pane:
		var newPos = (parseInt($("#background1").css("left")) - smallStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background1").css("left", newPos);
		
		newPos = (parseInt($("#background2").css("left")) - smallStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background2").css("left", newPos);
		
		newPos = (parseInt($("#background3").css("left")) - mediumStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background3").css("left", newPos);
		
		newPos = (parseInt($("#background4").css("left")) - mediumStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background4").css("left", newPos);
		
		newPos = (parseInt($("#background5").css("left")) - bigStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background5").css("left", newPos);
		
		newPos = (parseInt($("#background6").css("left")) - bigStarSpeed - PLAYGROUND_WIDTH) % (-2 * PLAYGROUND_WIDTH) + PLAYGROUND_WIDTH;
		$("#background6").css("left", newPos);
		
		
	}, REFRESH_RATE);
	
	//this is where the keybinding occurs
	$(document).keydown(function(e){
		if(!gameOver && !playerHit){
			switch(e.keyCode){
				case 75: //this is shoot (k)
					//shoot missile here
					var playerposx = parseInt($("#player").css("left"));
					var playerposy = parseInt($("#player").css("top"));
					var name = "playerMissle_"+Math.ceil(Math.random()*1000);
					$("#playerMissileLayer").addSprite(name,{animation: missile["player"], posx: playerposx + 90, posy: playerposy + 14, width: 36,height: 10});
					$("#"+name).addClass("playerMissiles")
					break;
				case 65: //this is left! (a)
					$("#playerBooster").setAnimation();
					break;
				case 87: //this is up! (w)
					$("#playerBoostUp").setAnimation(playerAnimation["up"]);
					break;
				case 68: //this is right (d)
					$("#playerBooster").setAnimation(playerAnimation["booster"]);
					break;
				case 83: //this is down! (s)
					$("#playerBoostDown").setAnimation(playerAnimation["down"]);
					break;
			}
		}
	});
	//this is where the keybinding occurs
	$(document).keyup(function(e){
		if(!gameOver && !playerHit){
			switch(e.keyCode){
				case 65: //this is left! (a)
					$("#playerBooster").setAnimation(playerAnimation["boost"]);
					break;
				case 87: //this is up! (w)
					$("#playerBoostUp").setAnimation();
					break;
				case 68: //this is right (d)
					$("#playerBooster").setAnimation(playerAnimation["boost"]);
					break;
				case 83: //this is down! (s)
					$("#playerBoostDown").setAnimation();
					break;
			}
		}
	});
});

