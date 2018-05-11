import { Component, OnInit } from '@angular/core';
import {AppConst} from '../../app-const';
import {Router} from "@angular/router";
import {LoginService} from "../../services/login.service";
import {UserService} from "../../services/user.service";
import {OauthService} from '../../services/oauth.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private busy:boolean =false;

  private serverPath = AppConst.serverPath;
  private loginError:boolean = false;
  private loggedIn = false;
  private credential = {'username':'', 'password':''};

  private emailSent:boolean = false;
  private usernameExists:boolean = false;
  private emailExists:boolean = false;
  private username:string;
  private email:string;

  private emailNotExists: boolean = false;
  private forgetPasswordEmailSent: boolean = false;
  private recoverEmail:string;

  constructor (
    private loginService: LoginService, 
    private userService: UserService, 
    private router: Router,
    private oauthService: OauthService
    ){
  }

  onLogin() {
    this.busy=true;

    if(this.credential.username.trim()=="" || this.credential.password.trim()=="") {
      this.busy=false;
      this.loggedIn=false;
      this.loginError=true;
    } else {
      this.loginService.sendCredential(this.credential.username, this.credential.password).subscribe(
        res=>{
          let message = res.text();
          let otpId = message.substring(6);
          if(message.startsWith("otp")) {
            this.router.navigate(['/otp', otpId]);
          } else {
            this.loggedIn=true;
            this.router.navigate(['/landing']);
          }
        },
        error=>{
          this.busy=false;
          this.loggedIn=false;
          this.loginError=true;
        }
      );
    }
  }

  onCheckSession() {
    this.loginService.checkSession().subscribe(
      res => {
        this.loggedIn=res;
        if(this.loggedIn) {
          this.router.navigate(['/landing']);
        }
        this.busy=false;
      },
      error => {
        this.busy=false;
      }
    );
  }

  onGoogleLogin() {
    // let url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=705445061234-hg27gdnem5jnfqlob55a0eb3as885lfh.apps.googleusercontent.com&response_type=code&scope=openid%20email%20profile&redirect_uri=http://localhost:4200/code&state=security_token%3DLmpiEjTkLqqgr_Q9Qs87KJ4g%26";

    this.oauthService.initialPost().subscribe(
      res => {
        let response = res.json();
        console.log(response.googleSsoUrl);
        let googleSsoUrl = response.googleSsoUrl;
        localStorage.setItem('authId', response.authId);
        window.location.href=googleSsoUrl;
      },
      error => {
        console.log(error);
      }
    );
  }


  // onForgetPassword() {
  //   this.forgetPasswordEmailSent = false;
  //   this.emailNotExists = false;
    
  //   this.userService.retrievePassword(this.recoverEmail).subscribe(
  //     res => {
  //       console.log(res);
  //       this.emailSent = true;
  //     },
  //     error => {
  //       console.log(error.text());
  //       let errorMessage=error.text();
  //       if (errorMessage==="usernameExists") this.usernameExists=true;
  //       if (errorMessage==="emailExists") this.emailExists=true;
  //     }
  //   );
  // }

  ngOnInit() {
    this.busy=true;
    this.onCheckSession();
  }


}
