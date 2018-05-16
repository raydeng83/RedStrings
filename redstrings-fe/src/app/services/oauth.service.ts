import { Injectable } from '@angular/core';
import {AppConst} from '../app-const';
import {Http, Headers} from '@angular/http';
import {Router} from "@angular/router";
import {Observable} from 'rxjs';
import { map, take } from 'rxjs/operators';

@Injectable()
export class OauthService {

  private serverPath:string = AppConst.serverPath;

  constructor (private http: Http, private router:Router) {}

  getAuthCode(){
  	let url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=705445061234-hg27gdnem5jnfqlob55a0eb3as885lfh.apps.googleusercontent.com&response_type=code&scope=openid%20email&redirect_uri=http://localhost:4200/code&state=security_token%3DLmpiEjTkLqqgr_Q9Qs87KJ4g%26";

  	return this.http.get(url, {withCredentials: true});
  }

  getAccessToken(code) {
  	let url = "https://www.googleapis.com/oauth2/v4/token";
    const body = new URLSearchParams();
    body.set("code", code);
    body.set("client_id", "705445061234-hg27gdnem5jnfqlob55a0eb3as885lfh.apps.googleusercontent.com");
    body.set("client_secret", "LmpiEjTkLqqgr_Q9Qs87KJ4g");
    body.set("redirect_uri", "http://localhost:4200/code");
    body.set("grant_type", "authorization_code");
    let headers = new Headers(
      {
        'Content-Type': 'application/x-www-form-urlencoded',
      });


    return this.http.post(url, body.toString(), {withCredentials: true, headers: headers});
  }

  oauthUser(profile){
    let url = this.serverPath+"/user/oauthUser";
    
    let tokenHeader = new Headers ({
      'Content-Type': 'application/json',
    });
    return this.http.post(url, profile, {withCredentials: true,headers : tokenHeader});
  }

  initialPost() {
    let url = this.serverPath+"/googleLogin";
    let tokenHeader = new Headers ({
      'Content-Type': 'application/json',
    });
    return this.http.get(url, {withCredentials: true,headers : tokenHeader});
  }

  getSession(authId, code, session_state, state){
    let url = this.serverPath+"/googleLogin";

    let info = {
      "authId": authId,
      "authIndexType":"service",
      "authIndexValue":"GoogleSocialAuthenticationService",
      "authuser":"0",
      "code":code,
      "prompt":"none",
      "realm":"/phoenix-dev",
      "session_state":session_state,
      "state":state
    }

    let tokenHeader = new Headers ({
      'Content-Type': 'application/json',
    });
    return this.http.post(url, info, {withCredentials: true,headers : tokenHeader});
  }

  getOauthAccessToken(code) {
    let url = this.serverPath+"/oauth";
    let tokenHeader = new Headers ({
      'Content-Type': 'application/json',
    });
    

    return this.http.post(url, code, {withCredentials: true,headers : tokenHeader});
  }

}
