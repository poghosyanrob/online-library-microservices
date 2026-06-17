import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { OrderService } from '../../../core/services/order/order.service';
import { BookService } from '../../../core/services/book/book.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LangSwitcher } from '../../../shared/lang-switcher/lang-switcher';
import { retry } from 'rxjs';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe, LangSwitcher],
  templateUrl: './order-list.html',
  styleUrls: ['./order-list.css']
})
export class OrderList implements OnInit {
  private orderService = inject(OrderService);
  private bookService = inject(BookService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private translate = inject(TranslateService);

  orders: any[] = [];
  booksMap: { [key: number]: any } = {};
  errorMessage: string = '';
  loading = true;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.bookService.getBooks().pipe(retry(2)).subscribe({
      next: (books: any[]) => {
        books.forEach(book => this.booksMap[book.id] = book);
        this.orderService.getMyOrders().pipe(retry(2)).subscribe({
          next: (data: any[]) => {
            this.orders = (data || []).sort((a: any, b: any) => b.id - a.id);
            this.loading = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.errorMessage = 'ORDERS.ERR_LOAD';
            this.loading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.errorMessage = 'ORDERS.ERR_BOOKS_LOAD';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getBookInfo(bookId: number, field: 'title' | 'author'): string {
    const book = this.booksMap[bookId];
    return book ? book[field] : this.translate.instant('BOOKS.DELETED');
  }

  getStatusClass(status: string): string {
    if (!status) return 'status-pending';
    switch (status.toUpperCase()) {
      case 'COMPLETED': return 'status-completed';
      case 'CANCELED': return 'status-canceled';
      default: return 'status-pending';
    }
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
