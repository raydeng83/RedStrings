import { Component, OnInit } from '@angular/core';
import {AppConst} from '../../app-const';
import {Router} from "@angular/router";
import {LoginService} from "../../services/login.service";
import {UserService} from "../../services/user.service";


@Component({
  selector: 'app-forgetpassword',
  templateUrl: './forgetpassword.component.html',
  styleUrls: ['./forgetpassword.component.css']
})
export class ForgetpasswordComponent implements OnInit {
  private serverPath = AppConst.serverPath;
  private username="";
  private emailSent:boolean = false;

  constructor (private loginService: LoginService, private userService: UserService, private router: Router){
  }

  ngOnInit() {
  }

  onForgetPassword(){
  	this.userService.forgetPassword(this.username).subscribe(
  		res => {
  			this.emailSent=true;
  		},
  		error => {
  			console.log(error);
  			this.emailSent=false;
  		}
  	);
  }

}
