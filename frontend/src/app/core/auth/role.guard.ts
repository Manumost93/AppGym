import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Role } from './auth.models';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.currentUser();
    if (!user) {
      router.navigate(['/login']);
      return false;
    }

    if (!allowedRoles.includes(user.role)) {
      router.navigate(['/dashboard']);
      return false;
    }

    return true;
  };
};
