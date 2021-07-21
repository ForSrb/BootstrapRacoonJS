import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LoginRequest } from '@bootstrapbugz/shared';
import { Store } from '@ngrx/store';
import { login } from '../+state/auth.actions';
import { AuthState } from '../+state/auth.reducer';

@Component({
  selector: 'bootstrapbugz-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  loginForm = new FormGroup({
    usernameOrEmail: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  constructor(private store: Store<AuthState>) {}

  onLogin() {
    const request = this.loginForm.value as LoginRequest;
    this.store.dispatch(login({ request }));
  }
}
