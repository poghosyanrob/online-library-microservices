import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth/auth.service';
import { LangSwitcher } from '../../../shared/lang-switcher/lang-switcher';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslatePipe, LangSwitcher],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private authService = inject(AuthService);
  private router = inject(Router);
  private translate = inject(TranslateService);

  registerData = {
    username: '',
    email: '',
    password: ''
  };

  errorMessage: string = '';
  successMessage: string = '';
  fieldErrors: { [key: string]: string } = {};

  onRegister(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.fieldErrors = {};

    this.authService.register(this.registerData).subscribe({
      next: () => {
        this.successMessage = 'AUTH.REGISTER.SUCCESS';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        if (err.status === 400 && err.error?.errors) {
          err.error.errors.forEach((e: { field: string; message: string }) => {
            this.fieldErrors[e.field] = e.message;
          });
        } else {
          this.errorMessage = 'AUTH.REGISTER.ERROR';
        }
      }
    });
  }
}
