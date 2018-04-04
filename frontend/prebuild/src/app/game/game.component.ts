import * as $ from 'jquery';

import { Component, OnInit } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { GameService } from './game.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  static readonly BOX_SIZE = 5;
  static readonly PLAYER_WIDTH = 15;
  static readonly PLAYER_HEIGHT = 15;

  roundId: string;
  serverHost: string;
  serverPort: string;

  output: HTMLElement;
  idDisplay: HTMLElement;
  bluePlayer: HTMLImageElement;
  orangePlayer: HTMLImageElement;
  redPlayer: HTMLImageElement;
  yellowPlayer: HTMLImageElement;

  canvas: any;
  context: CanvasRenderingContext2D;

  constructor(private meta: Meta, private gameService: GameService) {
    gameService.messages.subscribe((msg) => {
      const json = msg as any;
      if (json.requeue) {
        this.roundId = json.requeue;
        sessionStorage.setItem('roundId', this.roundId);
        location.reload();
      }
      if (json.obstacles) {
        for (let obstacle of json.obstacles) {
          this.drawObstacle(obstacle);
        }
      }
      if (json.movingObstacles) {
        for (let obstacle of json.movingObstacles) {
          this.drawMovingObstacle(obstacle);
        }
      }
      if (json.players) {
        for (let player of json.players) {
          if (player.alive) {
            this.drawPlayer(player);
          }
        }
      }
      if (json.countdown) {
        this.startingCountdown(json.countdown);
      }
      if (json.keepAlive) {
        this.gameService.send({ keepAlive: true });
      }
    }, (err) => {
      console.log(`Error occurred: ${err}`);
    });
  }

  ngOnInit() {
    this.roundId = sessionStorage.getItem('roundId');

    if (sessionStorage.getItem('isSpectator') === 'true') {
      console.log('is a spectator... showing game id');
      // Set the Round ID and make visible
      $('#game-code').html(this.roundId);
      const gameId = $('#game-code-display');
      gameId.removeClass('d-none');
      gameId.addClass('d-inline-block');
      this.gameService.send({'spectatorjoined': true});
    } else {
      this.gameService.send({'playerjoined': sessionStorage.getItem('userId'), 'hasGameBoard' : 'true'});
    }


    this.meta.addTag({name: 'viewport', content: 'width=1600'}, true);

    this.output = document.getElementById('output');
    this.idDisplay = document.getElementById('gameIdDisplay');
    
    this.bluePlayer = new Image(GameComponent.PLAYER_WIDTH, GameComponent.PLAYER_HEIGHT);
    this.bluePlayer.src = '/assets/images/player_blue.png';
    this.orangePlayer = new Image(GameComponent.PLAYER_WIDTH, GameComponent.PLAYER_HEIGHT);
    this.orangePlayer.src = '/assets/images/player_orange.png';
    this.redPlayer = new Image(GameComponent.PLAYER_WIDTH, GameComponent.PLAYER_HEIGHT);
    this.redPlayer.src = '/assets/images/player_red.png';
    this.yellowPlayer = new Image(GameComponent.PLAYER_WIDTH, GameComponent.PLAYER_HEIGHT);
    this.yellowPlayer.src = '/assets/images/player_yellow.png';

    this.canvas = document.getElementById('gameCanvas');
    this.context = this.canvas.getContext('2d');

    window.onkeydown = (e: KeyboardEvent): any => {
      const key = e.keyCode ? e.keyCode : e.which;

      if (key === 38) {
        this.moveUp();
      } else if (key === 40) {
        this.moveDown();
      } else if (key === 37) {
        this.moveLeft();
      } else if (key === 39) {
        this.moveRight();
      }
    };
  }

  // Game actions
  startGame() {
    this.gameService.send({ message: 'GAME_START' });
  }

  requeue() {
    this.gameService.send({ message: 'GAME_REQUEUE' });
  }
  
  moveUp() {
    this.gameService.send({ direction: 'UP' });
  }

  moveDown() {
    this.gameService.send({ direction: 'DOWN' });
  }

  moveLeft() {
    this.gameService.send({ direction: 'LEFT' });
  }

  moveRight() {
    this.gameService.send({ direction: 'RIGHT' });
  }

  // Update display
  drawPlayer(player) {
    this.context.fillStyle = this.playerHtmlColor(player);
    // Clear the previous player location
    this.context.clearRect(GameComponent.BOX_SIZE * player.oldX, GameComponent.BOX_SIZE * player.oldY,
                          GameComponent.PLAYER_WIDTH, GameComponent.PLAYER_HEIGHT);
    
    // Draw the player trail
    this.context.fillRect(GameComponent.BOX_SIZE * player.trailPosX, GameComponent.BOX_SIZE * player.trailPosY,
                          GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
    this.context.fillRect(GameComponent.BOX_SIZE * player.trailPosX2, GameComponent.BOX_SIZE * player.trailPosY2,
                          GameComponent.BOX_SIZE, GameComponent.BOX_SIZE);
    
    // Draw the player
    //this.context.save();
    //this.context.rotate(90*Math.PI/180);
    this.context.drawImage(this.playerImage(player), GameComponent.BOX_SIZE * player.x, GameComponent.BOX_SIZE * player.y);
    //this.context.restore();
  }

  drawObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
  }

  drawMovingObstacle(obstacle) {
    this.context.fillStyle = '#808080'; // obstacles always grey
    if (obstacle.hasMoved) {
      this.context.clearRect(GameComponent.BOX_SIZE * obstacle.oldX, GameComponent.BOX_SIZE * obstacle.oldY,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
    }
    this.context.fillRect(GameComponent.BOX_SIZE * obstacle.x, GameComponent.BOX_SIZE * obstacle.y,
                          GameComponent.BOX_SIZE * obstacle.width, GameComponent.BOX_SIZE * obstacle.height);
  }
  
  playerImage(player) {
	  if (player.color === 'blue')
		  return this.bluePlayer;
	  if (player.color === 'orange')
		  return this.orangePlayer;
	  if (player.color === 'red')
		  return this.redPlayer;
	  if (player.color === 'yellow')
		  return this.yellowPlayer;
  }
  
  playerHtmlColor(player) {
	  if (player.color === 'orange')
		  return '#DF740C';
	  if (player.color === 'blue')
		  return '#6FC3DF';
	  if (player.color === 'red')
		  return '#FF0000';
	  if (player.color === 'yellow')
		  return '#FFE64D';
  }

  writeToScreen(message: string) {
    const pre = document.createElement('p');
    pre.style.wordWrap = 'break-word';
    pre.innerHTML = message;
    this.output.appendChild(pre);
  }

  getStatus(status) {
    if (status === 'Connected') {
      return '<span class=\'badge badge-pill badge-primary\'>Connected</span>';
    }
    if (status === 'Alive' || status === 'Winner') {
      return `<span class='badge badge-pill badge-success'>${status}</span>`;
    }
    if (status === 'Dead') {
      return '<span class=\'badge badge-pill badge-danger\'>Dead</span>';
    }
    if (status === 'Disconnected') {
      return '<span class=\'badge badge-pill badge-secondary\'>Disconnected</span>';
    }
  }
  
  startingCountdown(seconds) {
    const loader = $('#loader-overlay');
    loader.removeClass('d-none');
    setTimeout(function() {
      loader.addClass('d-none');
    }, (1000 * seconds));
  }

}
