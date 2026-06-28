import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStatus } from '../services/auth-status';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {

  const router = inject(Router);
  const authStatusService = inject(AuthStatus);

  const token =
    'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBzd2VkYmFuay5sb2NhbCIsImlhdCI6MTc4MjY3NjMwMSwiZXhwIjoxNzgyNzYyNzAxfQ.fyxY7byLNs1kHOj37pgSTAfqnxsEUkM3brbNaedLCjE';

  const clonedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        console.error('Global Interceptor: Backend returned 401 Unauthorized!');

        authStatusService.errorMessage.set(
          'Session Expired or Invalid JWT Token! Paste your fresh backend console token into code.',
        );

        router.navigate(['/home']);
      }

      // Pass the error onward so components can log it too
      return throwError(() => error);
    }),
  );

};
