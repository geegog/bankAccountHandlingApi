import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {

  const clonedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBzd2VkYmFuay5sb2NhbCIsImlhdCI6MTc4MjY0Mzc0MiwiZXhwIjoxNzgyNzMwMTQyfQ.R4FvqL4Vt_Bg25_goRKFN5seUo0OmHWchZ-xlN7y0NU`,
    },
  });
  return next(clonedRequest);

};
