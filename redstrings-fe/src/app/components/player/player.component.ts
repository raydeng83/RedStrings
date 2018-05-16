import { Component, OnInit } from '@angular/core';

declare const videojs: any;


@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.css']
})
export class PlayerComponent implements OnInit {
	id: string;    
  public url = 'https://storage.googleapis.com/redstrings-test-bucket/test.mp4';
  private videoJSplayer: any;

  constructor() { }

  ngOnInit() {
  }

  ngAfterViewInit() {
    this.videoJSplayer = videojs(document.getElementById('video_player_id'), {}, () => {
    });
  }

}
