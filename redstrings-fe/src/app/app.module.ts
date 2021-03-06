import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { routing }  from './app.routing';

import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';

import { LoginService } from './services/login.service';
import { UserService } from './services/user.service';
import { OauthService } from './services/oauth.service';
import { LoginGuardService } from './services/login-guard.service';

import { ForgetpasswordComponent } from './components/forgetpassword/forgetpassword.component';
import { OtpComponent } from './components/otp/otp.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { LandingComponent } from './components/landing/landing.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { PlayerComponent } from './components/player/player.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    ForgetpasswordComponent,
    OtpComponent,
    RegistrationComponent,
    LandingComponent,
    NavbarComponent,
    PlayerComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpModule,
    routing,
  ],
  providers: [
    UserService,
    LoginService,
    OauthService,
    LoginGuardService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
