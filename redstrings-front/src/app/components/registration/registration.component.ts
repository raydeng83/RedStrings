import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {User} from "../../models/user";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {
  private user: User = new User();
  private userCreated: boolean = false;

  constructor(private userService: UserService, private router: Router) { }

  onCreateAccount() {
    this.userService.newUser(this.user).subscribe(
      res => {
        this.userCreated=true;
      },
      error => {
        console.log(error.text());
      }
    );
  }

  ngOnInit() {
  }

}
