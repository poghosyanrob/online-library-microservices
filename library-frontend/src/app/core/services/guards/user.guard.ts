import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const userGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const role = authService.getRole();
  if (role === 'USER') return true;
  if (role === 'LIBRARIAN') { router.navigate(['/librarian']); return false; }
  if (role === 'ADMIN') { router.navigate(['/admin']); return false; }

  router.navigate(['/login']);
  return false;
};
