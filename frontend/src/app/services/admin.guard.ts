import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(SessionService);
  if (!session.token()) return router.createUrlTree(['/login'], { queryParams: { admin: '1' } });
  if (!session.isAdmin()) return router.createUrlTree(['/login'], { queryParams: { admin: '1' } });
  return true;
};
