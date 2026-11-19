import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE } from './api.config';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('organiza-token');
  const api = request.url.startsWith(`${API_BASE}/`) || request.url.startsWith('/api/');
  const authenticated = token && api
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(authenticated).pipe(catchError((error: HttpErrorResponse) => {
    if (api && error.status === 401 && !request.url.endsWith('/login')) {
      sessionStorage.removeItem('organiza-token');
      localStorage.removeItem('usuario');
      void router.navigate(['/login']);
    }
    return throwError(() => error);
  }));
};
