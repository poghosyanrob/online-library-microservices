import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { authInterceptor } from './core/services/interceptors/auth.interceptor';
import { langInterceptor } from './core/services/interceptors/lang.interceptor';
import { LangService } from './core/services/lang/lang.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([langInterceptor, authInterceptor])),
    provideTranslateService({ lang: 'hy', fallbackLang: 'hy' }),
    provideTranslateHttpLoader({ prefix: '/assets/i18n/', suffix: '.json' }),
    {
      provide: APP_INITIALIZER,
      useFactory: (langService: LangService) => () => langService.init(),
      deps: [LangService],
      multi: true
    }
  ]
};
