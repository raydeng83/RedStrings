import { Injectable } from '@angular/core';
import {AppConst} from '../app-const';
import {Http, Headers} from '@angular/http';
import {Router} from "@angular/router";
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map'

@Injectable()
export class LoginService {
  private serverPath:string = AppConst.serverPath;

  constructor (private http: Http, private router:Router) {}

  sendCredential(username: string, password: string) {
    let url = this.serverPath+"/login";
    const body = new URLSearchParams();
    body.set("username", username);
    body.set("password", password);
    let headers = new Headers(
      {
        'Content-Type': 'application/x-www-form-urlencoded',

      });


    return this.http.post(url, body.toString(), {withCredentials: true, headers: headers});
  }

  checkSession() {
    let url = this.serverPath+"/checkSession";
    let headers = new Headers(
      {
        'Accept': 'application/json',
      });
    
    return this.http.get(url, {withCredentials: true,headers : headers}).map(
      res => {
        if (res) {
          return true;
        }
        return false;
      }
    );
  }

  logout() {
    let url = this.serverPath+"/user/logout";
    
    return this.http.post(url,'', {withCredentials: true});
  }

  logoutClient1() {
    let url = "http://localhost:8383/logout";
    
    return this.http.post(url,'', {withCredentials: true});
  }

  ssoClient1(issued_token){
    let url = "http://localhost:8383/onSSO";
    let headers = new Headers(
      {
        'Authorization': 'Bearer '+issued_token
     });

    return this.http.get(url, {withCredentials:true, headers : headers});
  }

  oauthSsoClient1(access_token) {
    let url = "http://localhost:8383/oauthSso";
    let headers = new Headers(
      {
        'AuthorizationToken': 'Bearer '+access_token
     });

    return this.http.get(url, {headers:headers});
  }

  sendOtp(otpId, passcode) {
    let url = this.serverPath+"/otp";

    let otpInfo = {
      "otpId": otpId,
      "passcode" : passcode
    };
    
    let tokenHeader = new Headers ({
      'Content-Type': 'application/json',
    });
    return this.http.post(url, otpInfo, {withCredentials:true,headers : tokenHeader});
  }
}
