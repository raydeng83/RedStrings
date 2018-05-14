import { Component, AfterViewInit, OnInit, OnDestroy } from '@angular/core';
import {AppConst} from '../../app-const';
import {Router} from "@angular/router";
import {LoginService} from "../../services/login.service";
import {UserService} from "../../services/user.service";

declare const videojs: any;


@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements AfterViewInit, OnInit {
  private serverPath = AppConst.serverPath;
  private loggedIn = false;
  private issued_token;  

  id: string;    
  public url = 'https://storage.googleapis.com/redstrings-test-bucket/test.mp4';
  private videoJSplayer: any;

  constructor (private loginService: LoginService, private userService: UserService, private router: Router){

  }


  onCheckSession() {
    this.loginService.checkSession().subscribe(
      res => {
        this.loggedIn=res;
        if(this.loggedIn) {
          this.router.navigate(['/landing']);
        }
      },
      error => {
      }
      );
  }

  onLogout() {
    this.loginService.logout().subscribe(
      res => {
        this.loggedIn=false;
        this.router.navigate(['/login']);
      },
      error => {
        this.loggedIn=true;
      }
      );
  }
  

  ngOnInit() {
    this.onCheckSession();

  }

  ngAfterViewInit() {
    this.videoJSplayer = videojs(document.getElementById('video_player_id'), {}, () => {
    });
  }
}
