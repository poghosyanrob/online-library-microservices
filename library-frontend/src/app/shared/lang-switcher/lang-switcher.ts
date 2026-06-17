import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LangService, SupportedLang } from '../../core/services/lang/lang.service';

@Component({
  selector: 'app-lang-switcher',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="lang-switcher">
      <button *ngFor="let lang of langs"
              class="lang-btn"
              [class.active]="current === lang"
              (click)="switch(lang)">
        {{ labels[lang] }}
      </button>
    </div>
  `,
  styles: [`
    .lang-switcher { display: flex; gap: 4px; align-items: center; }
    .lang-btn {
      padding: 4px 10px;
      border-radius: 12px;
      border: 1px solid rgba(99,102,241,0.35);
      background: transparent;
      color: var(--text-muted, #94a3b8);
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.18s;
      letter-spacing: 0.5px;
    }
    .lang-btn:hover { background: rgba(99,102,241,0.12); color: #a5b4fc; }
    .lang-btn.active { background: rgba(99,102,241,0.22); color: #a5b4fc; border-color: rgba(99,102,241,0.6); }
  `]
})
export class LangSwitcher {
  private langService = inject(LangService);
  langs: SupportedLang[] = ['hy', 'en', 'ru'];
  labels: Record<SupportedLang, string> = { hy: 'ՀԱՅ', en: 'ENG', ru: 'РУС' };
  get current(): SupportedLang { return this.langService.getCurrentLang(); }
  switch(lang: SupportedLang): void { this.langService.setLang(lang); }
}
