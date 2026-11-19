import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () =>
  !!(localStorage.getItem('organiza-token') || sessionStorage.getItem('organiza-token'))
  && !!localStorage.getItem('usuario')
    ? true : inject(Router).createUrlTree(['/login']);
