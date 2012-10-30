			var game = function() {
			
				// private methods/properties
				var private = {};
				var level1 = [ // set the fighters
						{id:"F1",speed:2,direction:"left"},
						{id:"F2",speed:2,direction:"left"},
						{id:"F3",speed:3,direction:"left"},
						{id:"F4",speed:2,direction:"right"},
						{id:"F5",speed:2,direction:"left"},
						{id:"F6",speed:2,direction:"left"},
						{id:"F7",speed:3,direction:"right"},
						{id:"F8",speed:2,direction:"left"},
						{id:"F9",speed:4,direction:"right"},
						{id:"F1",speed:3,direction:"left"},
						{id:"F2",speed:3,direction:"left"},
						{id:"F3",speed:2,direction:"right"},
						{id:"F4",speed:3,direction:"left"},
						{id:"F5",speed:4,direction:"right"},
						{id:"F6",speed:3,direction:"left"},
						{id:"F7",speed:5,direction:"left"},
						{id:"F8",speed:3,direction:"right"},
						{id:"F9",speed:3,direction:"left"},
						{id:"F1",speed:3,direction:"left"},
						{id:"F2",speed:4,direction:"right"},
						{id:"F3",speed:5,direction:"left"},
						{id:"F4",speed:3,direction:"right"},
						{id:"F5",speed:4,direction:"left"},
						{id:"F6",speed:5,direction:"right"},
						{id:"F7",speed:2,direction:"left"},
						{id:"F8",speed:4,direction:"left"},
						{id:"F9",speed:4,direction:"right"},
						{id:"F1",speed:3,direction:"left"},
						{id:"F2",speed:4,direction:"right"},
						{id:"F3",speed:5,direction:"left"},
						{id:"F4",speed:3,direction:"right"},
						{id:"F5",speed:4,direction:"left"},
						{id:"F6",speed:6,direction:"right"},
						{id:"F7",speed:6,direction:"left"},
						{id:"F8",speed:4,direction:"left"},
						{id:"F9",speed:5,direction:"right"},
						{id:"F1",speed:6,direction:"right"},
						{id:"F2",speed:4,direction:"left"},
						{id:"F3",speed:5,direction:"left"},
						{id:"F4",speed:7,direction:"right"},
						{id:"F5",speed:6,direction:"left"},
						{id:"F6",speed:6,direction:"left"},
						{id:"F7",speed:6,direction:"right"},
						{id:"F8",speed:4,direction:"left"},
						{id:"F9",speed:7,direction:"left"},
						{id:"F1",speed:5,direction:"left"},
						{id:"F2",speed:4,direction:"right"},
						{id:"F3",speed:5,direction:"left"},
						{id:"F4",speed:7,direction:"right"},
						{id:"F5",speed:4,direction:"left"},
						{id:"F6",speed:5,direction:"right"},
						{id:"F7",speed:6,direction:"left"},
						{id:"F8",speed:4,direction:"left"},
						{id:"F9",speed:7,direction:"right"},
						{id:"F1",speed:6,direction:"left"},
						{id:"F2",speed:4,direction:"right"},
						{id:"F3",speed:6,direction:"left"},
						{id:"F4",speed:7,direction:"right"},
						{id:"F5",speed:6,direction:"left"},
						{id:"F6",speed:8,direction:"right"},
						{id:"F7",speed:2,direction:"left"},
						{id:"F8",speed:4,direction:"left"},
						{id:"F9",speed:6,direction:"right"},
						{id:"F1",speed:6,direction:"right"},
						{id:"F2",speed:5,direction:"left"},
						{id:"F3",speed:5,direction:"left"},
						{id:"F4",speed:7,direction:"right"},
						{id:"F5",speed:6,direction:"left"},
						{id:"F6",speed:5,direction:"left"},
						{id:"F7",speed:6,direction:"right"},
						{id:"F8",speed:6,direction:"left"},
						{id:"F9",speed:7,direction:"left"},							
						{id:"F10",speed:8,direction:"left"}
						
				];
				var activeMissile = []; // for testing
				var misID=0;
				var lastMisId = "";
				var fighterID=0;
				var numbOfFighters=1;				
				var fighterDestroyed=0;
				var fighterMissed=0;
				var shots=0;
				var totalFighters=0;
				var liveShots=0;
				var playerPos2 = "";
				var fighterMisID=1;	
				var fighterShot=0;				
				var fighty = "";
				var shieldLevel = 5;
				var shieldLevelAmt = 0.35;
				var visibleShips=0;
				var shipsMade=0;
				private.PLAYGROUND_WIDTH = 800;
				private.PLAYGROUND_HEIGHT = 400;
				private.status = -1;

				var get = function(key) {
					return private[key];
				}
				
				var set = function(key, val) {
					private[key] = val;
				}
 
				// public methods/properties
				return {
					init: function() {
						var classObj = this;
						

						
						$("#pause").hide();
						$(document).playground("#playground", {height: get('PLAYGROUND_HEIGHT'), width: get('PLAYGROUND_WIDTH'), refreshRate: 30});
						$('#playground').css('width', get('PLAYGROUND_WIDTH'));
						$('#playground').css('height', get('PLAYGROUND_HEIGHT'));
						$('#playground').css('border', '1px solid #787878');
						$('#playground').css('position', 'relative');
					    //$().playground().addSound('background','');   maybe background sound for future
						$().playground().addSprite('playerBottom', { animation:new Animation( { imageURL:"spaceship.png" } ), width:60, height:28 });
						$().playground().addSprite('btmLayer', { animation:new Animation( { imageURL:"./blank.gif" } ), width:800, height:10 });
						$().playground().addSprite('shield', { animation:new Animation( { imageURL:"./blank.gif" } ), width:100, height:50 });
						
						this.initPlayer();
						classObj.generateFighter(level1);
						
						$().playground().registerCallback(function() {
							
							var status = get('status');
							if (status > 0) {
								// game play
								if(fighterDestroyed+fighterMissed>=70) { alert("Congrats, you have defeated the. The game will restart!"); location.reload(true); }
								
								for(var i=misID-6;i<=misID;i++) {

									for(var b=0;b<=shipsMade-1;b++) {

										if($('#Missile'+i).is(':visible')) {
											var missy = "Missile"+i;
											// to make sure were counting the right array in case for diff levels
											if(level1.length != 0) {
												var shipy = level1[b].id;
											} 											
											
											$("#"+missy).collision('#'+shipy).each(function(){
												var misPos = $("#"+missy).position();
												classObj.missileExplode(i,misPos.top,misPos.left);
												visibleShips--;
												$('#visibleFighters').html(visibleShips);
												$("#"+missy).remove();
												// to remove the right item from the array
												if(level1.length != 0) {
													$('#'+level1[b].id).remove();
													level1.splice(b,1);
												} 
												liveShots--;
												$('#liveShots').html(liveShots);
												fighterDestroyed++;
												$('#fkill').html(fighterDestroyed)
												
												if(fighterDestroyed==fighterID-fighterMissed){
												
												// to make sure were sending the right array
												if(level1.length != 0) {
													if(visibleShips<=0) {
																classObj.generateFighter(level1);
															}
															
													} else {
														alert("Done!!!");
														 status=0;
													}
												}
										});
									}
								}}

											
								classObj.fireMissileGO();
								// to make sure were using the right array 
								if(level1.length != 0) {
										classObj.moveFighterR(level1);
										classObj.fighterFireMissile(level1);		 
								} 											
								
							}
							
							
							return false;
						}, 30);
						
					},
					initPlayer: function() {

						var playerRight = $('#playerBottom');
						playerRight.addClass('player');
						playerRight.css('top', get('PLAYGROUND_HEIGHT')-30);
						playerRight.css('left', get('PLAYGROUND_WIDTH')/2);

						var shield = $('#shield');
						shield.addClass('shield');
						shield.css('top', get('PLAYGROUND_HEIGHT')-45);
						shield.css('left', (get('PLAYGROUND_WIDTH')/2)-20);
						shield.css('background-color','yellow');
						$("#shield").fadeTo("slow",shieldLevelAmt)
						$('#shieldDisplay').html(shieldLevel);

						var btmLayer = $('#btmLayer');
						btmLayer.addClass('btmLayer');
						btmLayer.css('top', 400);
						btmLayer.css('left', 0);
										

					},
					movePlayer: function(player, dir){
						if (get('status') == 1) {
							var pos = $(player).position();
							var newPos = pos.left+dir;
							if (newPos > 0 && newPos+$(player).width() < get('PLAYGROUND_WIDTH')) {
								$(player).css('left', newPos);
								$('#shield').css('left', newPos-20);
							}
						}
					},
					fireMissile: function(player) {
					var status = get('status');
						if (status > 0) {
							var thisObj = this;
							if(liveShots < 5) {
								liveShots++; // count how many shots are in the air
								shots++; // count total amount of shots
								$('#liveShots').html(liveShots);
								$('#shots').html(shots);
								// create the Missile:
								misID++;
								var pos = $(player).position();
								var Missileid = "Missile"+misID;	
								$().playground().addSprite(Missileid, { animation:new Animation( { imageURL:"./blank.gif" } ), width:2, height:5 });
								$("#"+Missileid).show();
								var Missile = $('#'+Missileid);
								Missile.addClass('Missile');
								Missile.css('top', pos.top-5);
								Missile.css('left', pos.left+27);
								Missile.css('background-color', 'yellow');
								thisObj.fireMissileGO(Missile,Missileid);
							}
						}
						
					},
					fireMissileGO: function() {
						
						for(var i=0;i<=misID;i++) {
							if($('#Missile'+i).is(':visible')) {		
								var missy = "Missile"+i;
								var misPosition = $("#"+missy).position();
								var Missile = $('#Missile'+i);
								Missile.css('top', misPosition.top-5);
								if(misPosition.top<0) { liveShots--; $('#liveShots').html(liveShots); $('#Missile'+i).remove(); }
							}
						}	
												
					},
					generateFighter: function(level) {
						var howMany = (Math.round(Math.random()*4)+1);
					    shots++; 
						$('#amtFighters').html(howMany);
						shipsMade=howMany;
					    if(howMany>level.length) { howMany=level.length; }
							for(var i=0;i<howMany;i++){
									totalFighters ++;
									visibleShips ++;
									$('#visibleFighters').html(visibleShips);
								if(level[i].direction=='left') {	
									$('#totalFighters').html(totalFighters);		
									var randTop = (Math.round(Math.random()*250)+20);
									var randLeft = (Math.round(Math.random()*250)+1);	
									fighterID++;	
									var jetid = level[i].id;						
									$().playground().addSprite(level[i].id, { animation:new Animation( { imageURL:"jet2.png" } ), width:65, height:15 });
									//visibleShips++;
									
									var jet = $('#'+level[i].id);
									jet.addClass('jet');
									jet.css('top', randTop);
									jet.css('left', -randLeft);
									$("#"+level[i].id).show();
								} else if(level[i].direction=='right') {
									//totalFighters ++; 
									$('#totalFighters').html(totalFighters);		
									var randTop = (Math.round(Math.random()*250)+20);
									//var randLeft = (Math.round(Math.random()*250)+1);	
									fighterID++;	
									var jetid = level[i].id;						
									$().playground().addSprite(level[i].id, { animation:new Animation( { imageURL:"jet_r3.png" } ), width:55, height:13 });
									//visibleShips++;
									var jet = $('#'+level[i].id);
									jet.addClass('jet');
									jet.css('top', randTop);
									jet.css('left', 850);
									$("#"+level[i].id).show();	
								}
							
					     }
					


					},
					moveFighterR: function(level) {
						var classObj = this;
												
							for(var i=0;i<=shipsMade;i++) {
								if($('#'+level[i].id).is(':visible')) {	 // if the plane has a left setting
									if(level[i].direction=='left') {
										var figPosition = $("#"+level[i].id).position();
										var playerPos = $("#playerBottom").position();
										var fighter = $('#'+level[i].id);
										if(figPosition.left > 10 && fighterShot < 1) {	
											fighterShot=5;
										}
										fighter.css('left', figPosition.left+level[i].speed);
										if(figPosition.left>800) {
											fighterMissed++;
											$('#fpass').html(fighterMissed);									 
											$('#'+level[i].id).remove();
											visibleShips--;
											if(fighterDestroyed+fighterMissed==totalFighters) {  
												// if plane passes we kill it..
												classObj.fighterPassed(i);
											}
																														
										}
									} else if(level[i].direction=='right') { // if the plane has a right setting
										var figPosition = $("#"+level[i].id).position();
										var playerPos = $("#playerBottom").position();
										var fighter = $('#'+level[i].id);
										if(figPosition.left > 10 && fighterShot < 1) {	
											fighterShot=5;
										}
										fighter.css('left', figPosition.left-level[i].speed);
										if(figPosition.left<-50) {
											fighterMissed++;
											$('#fpass').html(fighterMissed);									 
											$('#'+level[i].id).remove(); 
											visibleShips--;
											if(fighterDestroyed+fighterMissed==totalFighters) {  
												// if plane passes we kill it..
													classObj.fighterPassed(i);

											}
																														
										}										
									}
								} 
								
							}	
					},
					fighterPassed: function(i){
						var classObj = this;
						
						$('#visibleFighters').html(visibleShips);
						if(level1.length != 0) {
							$('#'+level1[i].id).remove();
							level1.splice(i,1);
							if(level1.length<=0){ visibleShips=0; }
								if(visibleShips-1 <= 0){
									if(level1.length > 0) { visibleShips=0; classObj.generateFighter(level1); }
								 }
							} 							
					},
					fighterFireMissile: function(level) {
						var classObj = this;
						var eMissileid = "fMissile"+fighterMisID;	
						for(var i=0;i<=shipsMade;i++) {
						
						var figPosition = $("#"+level[i].id).position();	
								if(playerPos2=="") { playerPos2 = $("#playerBottom").position(); }
								if(fighterShot==5) {						
								   	fighterShot=1;
									$().playground().addSprite(eMissileid, { animation:new Animation( { imageURL:"./blank.gif" } ), width:5, height:5 });
									var eMissile = $('#'+eMissileid);
									eMissile.addClass('Missile');
									eMissile.css('top', figPosition.top+10);
									eMissile.css('left', figPosition.left+40);
									eMissile.css('background-color', 'red');
									$("#"+eMissileid).show();

								}						
								if(fighterShot==1) {
									var feMissile = $('#'+eMissileid);		   
									var misPos = $('#'+eMissileid).position();
									var stepsTop = playerPos2.top - misPos.top;
									var stepsLeft = playerPos2.left+27 - misPos.left;		  
									feMissile.css('top', misPos.top+1);			  
									if(stepsLeft<0) {
										if(misPos.left>playerPos2.left){ feMissile.css('left', misPos.left-1);} 
									} else {
									   	if(misPos.left<playerPos2.left){ feMissile.css('left', misPos.left+1); }
									}
									if(misPos.top>420) { playerPos2=""; $('#'+eMissileid).hide(); fighterShot--; fighterMisID++; }
									if(shieldLevel<=0) {
												$("#"+eMissileid).collision("#playerBottom").each(function(){   
													classObj.missileExplode(i,misPos.top,misPos.left);
													playerPos2=""; 
													$('#'+eMissileid).remove(); 
													fighterShot--;
													fighterMisID++; 
													alert("Opps, you have been shot. The game will restart!");
													location.reload(true);
												});

											} else {
										$("#"+eMissileid).collision("#shield").each(function(){ 
													shieldLevel--; 
													classObj.missileExplode(i,misPos.top,misPos.left);
													$('#shieldDisplay').html(shieldLevel); 
													shieldLevelAmt = shieldLevelAmt - .05;
													shieldLevelAmt = Math.round(shieldLevelAmt*100)/100;;
													switch (shieldLevel) {
														case 5:
															$("#shield").css("background-color","#dbe98f");
															break;
														case 4:
															$("#shield").css("background-color","#e9d78f");
															break;
														case 3:
															$("#shield").css("background-color","#e9c28f");
															break;
														case 2:
															$("#shield").css("background-color","#e9af8f");
															break;
														case 1:
															$("#shield").css("background-color","#e98f8f");
															break;	
														default:
															$("#shield").css("background-color","transparent");
															break;	
														}
													$("#shield").fadeTo("slow",shieldLevelAmt);  
													playerPos2=""; 
													$('#'+eMissileid).remove(); 
													fighterShot--;
													fighterMisID++; });	
									}
								 } 

						}

					},
					missileExplode: function(i,top,left) {
							$().playground().addSprite('explode'+i, { animation:new Animation( { imageURL:"explode.png" } ), width:25, height:25 });
							var explode = $('#explode'+i);
							explode.addClass('explode'+i);
							explode.css('top', top-4);
							explode.css('left', left-5);
							$('#explode'+i).fadeOut('fast',function(){$('#explode'+i).remove();});
					},
					keyDownHandler: function(evt) {
						var thisObj = this;
						switch(evt.keyCode) {
							case 13:
								if (get('status') == -1) {
									this.start();
								} else {
									this.pause();
								}
								break;
							case 32:
								thisObj.fireMissile('#playerBottom');
								break;
							case 37:
								if (! this.moveRightInt) {
									this.moveRightInt = window.setInterval( function() { thisObj.movePlayer('#playerBottom', -6); }, 20);
								}
								break;
							case 39:
								if (! this.moveRightInt) { 
									this.moveRightInt = window.setInterval( function() { thisObj.movePlayer('#playerBottom', 6); }, 20);
								}
								break;
						}
					},
					keyUpHandler: function(evt) {
						//alert(evt.keyCode);
						switch(evt.keyCode) {
							case 37:
							case 39:
								window.clearInterval(this.moveRightInt);
								this.moveRightInt = null;
								break;
							case 81:
							case 63: 
								window.clearInterval(this.moveLeftInt);
								this.moveLeftInt = null;
						}						
						
					},
					start: function() {
						if (get('status') == -1) {
							set('status', 1);
							$().playground().startGame(function(){
								$("#welcome").remove();
							});
						}						
					},
					pause: function() {
						var status = get('status');
						if (status == 1) {
							status = 0;
							$("#pause").show();
						} else if (status == 0) {
							status = 1;
							$("#pause").hide();
						}
						set('status', status);
					}
				}
				
			}();