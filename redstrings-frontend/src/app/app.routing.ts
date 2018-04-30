import {ModuleWithProviders}  from '@angular/core';
import {Routes, RouterModule} from '@angular/router';

import { LoginComponent } from './components/login/login.component';
import { LandingComponent } from './components/landing/landing.component';
import { HomeComponent } from './components/home/home.component';

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
  	path: 'login',
    component: LoginComponent
  },
  {
    path: 'landing',
    component: LandingComponent
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);