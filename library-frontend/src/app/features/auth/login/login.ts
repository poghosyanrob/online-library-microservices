import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth/auth.service';
import { LangSwitcher } from '../../../shared/lang-switcher/lang-switcher';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslatePipe, LangSwitcher],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  loginData = {
    email: '',
    password: ''
  };

  errorMessage: string = '';
  errorShake = false;
  fieldErrors: { [key: string]: string } = {};
  touched: { email: boolean; password: boolean } = { email: false, password: false };

  onBlur(field: 'email' | 'password'): void {
    this.touched[field] = true;
    this.validateField(field);
  }

  onInput(field: 'email' | 'password'): void {
    this.errorMessage = '';
    this.errorShake = false;
    if (this.touched[field]) {
      this.validateField(field);
    }
  }

  private validateField(field: 'email' | 'password'): void {
    if (field === 'email') {
      const email = this.loginData.email.trim();
      if (!email) {
        this.fieldErrors['email'] = 'AUTH.LOGIN.EMAIL_REQUIRED';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        this.fieldErrors['email'] = 'AUTH.LOGIN.EMAIL_INVALID';
      } else {
        delete this.fieldErrors['email'];
      }
    } else if (field === 'password') {
      if (!this.loginData.password) {
        this.fieldErrors['password'] = 'AUTH.LOGIN.PASSWORD_REQUIRED';
      } else {
        delete this.fieldErrors['password'];
      }
    }
  }

  onLogin(): void {
    this.errorMessage = '';
    this.errorShake = false;
    this.fieldErrors = {};
    this.touched = { email: true, password: true };
    this.validateField('email');
    this.validateField('password');

    if (this.fieldErrors['email'] || this.fieldErrors['password']) {
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: () => {
        const role = this.authService.getRole();
        if (role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (role === 'LIBRARIAN') {
          this.router.navigate(['/librarian']);
        } else {
          this.router.navigate(['/search']);
        }
      },
      error: (err) => {
        if (err.status === 400 && err.error?.errors) {
          err.error.errors.forEach((e: { field: string; message: string }) => {
            this.fieldErrors[e.field] = e.message;
          });
        } else if (err.status === 403) {
          this.errorMessage = 'AUTH.LOGIN.BLOCKED';
        } else {
          this.errorMessage = 'AUTH.LOGIN.ERROR';
        }
        this.triggerShake();
      }
    });
  }

  private triggerShake(): void {
    this.errorShake = true;
    setTimeout(() => this.errorShake = false, 500);
  }
}