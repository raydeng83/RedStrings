import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { LoginService } from './login.service';
import {Router} from "@angular/router";


@Injectable()
export class LoginGuardService implements CanActivate{
  private loggedIn: boolean = false;
  constructor(private loginService: LoginService, private router: Router) { }

  canActivate() {
  	let res = this.loginService.checkSession()
  	res.subscribe (
  		res => {
  			console.log(res)
  		},
  		error => {
  			this.router.navigate(['/login']);
  		}
  	);
  	return res;
  }
}
