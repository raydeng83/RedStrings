import { Component, OnInit } from '@angular/core';
import {AppConst} from '../../app-const';
import {Params, ActivatedRoute,Router} from "@angular/router";
import {LoginService} from "../../services/login.service";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-otp',
  templateUrl: './otp.component.html',
  styleUrls: ['./otp.component.css']
})
export class OtpComponent implements OnInit {

  private serverPath = AppConst.serverPath;
  private passcode: string;
  private otpId: string;
  private passcodeError: boolean = false;

  constructor (private loginService: LoginService, private userService: UserService, private router: Router, private route: ActivatedRoute){
  }

  onSendOtp() {
    this.loginService.sendOtp(this.otpId, this.passcode).subscribe(
      res => {
        this.router.navigate(['/home']);
      },
      error => {
      	this.passcodeError = true;
      	console.log(error);
      }
    );
  }

  ngOnInit() {
  	this.route.params.forEach((params: Params) => {
      this.otpId = params['otpId'];
    });
  }

}
