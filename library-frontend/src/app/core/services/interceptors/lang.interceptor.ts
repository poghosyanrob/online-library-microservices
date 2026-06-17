import { HttpInterceptorFn } from '@angular/common/http';

export const langInterceptor: HttpInterceptorFn = (req, next) => {
  const lang = localStorage.getItem('lang') || 'hy';
  const cloned = req.clone({
    setHeaders: { 'Accept-Language': lang }
  });
  return next(cloned);
};
