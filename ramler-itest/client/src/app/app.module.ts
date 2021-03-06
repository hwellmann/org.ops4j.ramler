import { BrowserModule } from '@angular/platform-browser';
import { NgModule, InjectionToken } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';


import { AppComponent } from './app.component';
import { UserService } from './gen/user.service';

export const CRUD_BASE_URL = new InjectionToken<string>('crud.url');

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [
    UserService,
    { provide: CRUD_BASE_URL, useValue: 'http://localhost:8081' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
