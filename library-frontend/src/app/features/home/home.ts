import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { BookService } from '../../core/services/book/book.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { LangSwitcher } from '../../shared/lang-switcher/lang-switcher';
import { Subject, retry } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, LangSwitcher],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  private bookService = inject(BookService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  books: any[] = [];
  searchQuery = '';
  loading = true;
  errorMessage = '';

  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.loadBooks('');
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => this.loadBooks(query));
  }

  loadBooks(query: string): void {
    this.loading = true;
    this.bookService.searchBooks(query).pipe(retry(2)).subscribe({
      next: (data) => {
        this.books = data.sort((a: any, b: any) => b.id - a.id);
        this.errorMessage = '';
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'SEARCH.LOAD_ERROR';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
  }

  getImageUrl(book: any): string {
    if (book.imageUrl) {
      return 'http://localhost:8080' + book.imageUrl;
    }
    return '';
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  onOrder(book: any): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/search']);
  }

  onLogin(): void {
    this.router.navigate(['/login']);
  }

  goToDashboard(): void {
    const role = this.authService.getRole();
    if (role === 'ADMIN') {
      this.router.navigate(['/admin']);
    } else if (role === 'LIBRARIAN') {
      this.router.navigate(['/librarian']);
    } else {
      this.router.navigate(['/search']);
    }
  }
}