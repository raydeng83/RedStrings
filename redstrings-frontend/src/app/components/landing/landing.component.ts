import { Component, OnInit } from '@angular/core';
import {AppConst} from '../../app-const';
import {Router} from "@angular/router";
import {LoginService} from "../../services/login.service";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {

  private serverPath = AppConst.serverPath;
  private loggedIn = false;
  private issued_token;

  constructor (private loginService: LoginService, private userService: UserService, private router: Router){}


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
  }

}
