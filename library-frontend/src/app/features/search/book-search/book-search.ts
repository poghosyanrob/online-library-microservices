import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { BookService } from '../../../core/services/book/book.service';
import { OrderService } from '../../../core/services/order/order.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { LangSwitcher } from '../../../shared/lang-switcher/lang-switcher';
import { Subject, retry } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-book-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, LangSwitcher],
  templateUrl: './book-search.html',
  styleUrls: ['./book-search.css']
})
export class BookSearch implements OnInit {
  private bookService = inject(BookService);
  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private translate = inject(TranslateService);

  books: any[] = [];
  searchQuery: string = '';
  errorMessage: string = '';
  loading = true;

  isOrderModalOpen = false;
  selectedBook: any = null;
  orderQuantity: number = 1;
  orderModalErrorMessage: string = '';

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

  openOrderModal(book: any): void {
    this.selectedBook = book;
    this.orderQuantity = 1;
    this.orderModalErrorMessage = '';
    this.isOrderModalOpen = true;
  }

  closeOrderModal(): void {
    this.isOrderModalOpen = false;
    this.selectedBook = null;
    this.orderModalErrorMessage = '';
  }

  confirmOrder(): void {
    if (!this.orderQuantity || this.orderQuantity < 1) {
      this.orderModalErrorMessage = 'ORDERS.ERR_MIN_QTY';
      return;
    }
    if (this.orderQuantity > this.selectedBook.quantity) {
      this.orderModalErrorMessage = this.translate.instant('ORDERS.ERR_MAX_QTY', { qty: this.selectedBook.quantity });
      return;
    }
    this.orderService.createOrder({ bookId: this.selectedBook.id, quantity: this.orderQuantity }).subscribe({
      next: () => {
        this.closeOrderModal();
        this.loadBooks(this.searchQuery);
      },
      error: (err) => {
        this.orderModalErrorMessage = err.error || 'ORDERS.ERR_PLACE';
      }
    });
  }

  getImageUrl(book: any): string {
    if (book.imageUrl) {
      return 'http://localhost:8080' + book.imageUrl;
    }
    return '';
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
