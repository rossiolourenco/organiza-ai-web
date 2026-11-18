import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () =>
  !!sessionStorage.getItem('organiza-token') && !!localStorage.getItem('usuario')
    ? true : inject(Router).createUrlTree(['/login']);
