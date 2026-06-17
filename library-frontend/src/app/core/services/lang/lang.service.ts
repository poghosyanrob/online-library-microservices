import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';

export type SupportedLang = 'hy' | 'en' | 'ru';

@Injectable({ providedIn: 'root' })
export class LangService {
  private readonly STORAGE_KEY = 'lang';
  readonly supported: SupportedLang[] = ['hy', 'en', 'ru'];

  constructor(private translate: TranslateService) {}

  init(): Promise<void> {
    const saved = (localStorage.getItem(this.STORAGE_KEY) as SupportedLang) || 'hy';
    return firstValueFrom(this.translate.use(saved)).then(() => {});
  }

  setLang(lang: SupportedLang): void {
    localStorage.setItem(this.STORAGE_KEY, lang);
    this.translate.use(lang);
  }

  getCurrentLang(): SupportedLang {
    return (localStorage.getItem(this.STORAGE_KEY) as SupportedLang) || 'hy';
  }
}
