import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { BookService } from '../../../core/services/book/book.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { OrderService } from '../../../core/services/order/order.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LangSwitcher } from '../../../shared/lang-switcher/lang-switcher';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, LangSwitcher],
  templateUrl: './book-list.html',
  styleUrls: ['./book-list.css']
})
export class BookList implements OnInit {
  private bookService = inject(BookService);
  private authService = inject(AuthService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private translate = inject(TranslateService);

  books: any[] = [];
  errorMessage: string = '';
  modalErrorMessage: string = '';
  bookFieldErrors: { [key: string]: string } = {};

  isModalOpen: boolean = false;
  isEditMode: boolean = false;
  currentBookId: number | null = null;

  isDeleteModalOpen: boolean = false;
  bookIdToDelete: number | null = null;

  isOrderModalOpen: boolean = false;
  selectedBook: any = null;
  orderQuantity: number = 1;
  orderModalErrorMessage: string = '';

  newBook = { title: '', author: '', price: 0, quantity: 0 };

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.bookService.getBooks().subscribe({
      next: (data: any[]) => {
        if (data && data.length > 0) {
          this.books = data.sort((a: any, b: any) => b.id - a.id);
        } else {
          this.books = [];
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'BOOKS.ERR_LOAD';
        this.cdr.detectChanges();
      }
    });
  }

  openModal(): void {
    this.isModalOpen = true;
    this.isEditMode = false;
    this.modalErrorMessage = '';
    this.bookFieldErrors = {};
    this.newBook = { title: '', author: '', price: 0, quantity: 0 };
    this.cdr.detectChanges();
  }

  openEditModal(book: any): void {
    this.isModalOpen = true;
    this.isEditMode = true;
    this.modalErrorMessage = '';
    this.bookFieldErrors = {};
    this.currentBookId = book.id;
    this.newBook = { ...book };
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.currentBookId = null;
    this.modalErrorMessage = '';
    this.bookFieldErrors = {};
    this.cdr.detectChanges();
  }

  saveBook(): void {
    this.modalErrorMessage = '';
    this.bookFieldErrors = {};

    if (this.isEditMode && this.currentBookId !== null) {
      this.bookService.updateBook(this.currentBookId, this.newBook).subscribe({
        next: () => {
          this.closeModal();
          this.loadBooks();
        },
        error: (err) => {
          if (err.status === 400 && err.error?.errors) {
            err.error.errors.forEach((e: { field: string; message: string }) => {
              this.bookFieldErrors[e.field] = e.message;
            });
          } else {
            this.modalErrorMessage = 'BOOKS.ERR_SAVE';
          }
          this.cdr.detectChanges();
        }
      });
    } else {
      this.bookService.addBook(this.newBook).subscribe({
        next: () => {
          this.closeModal();
          this.loadBooks();
        },
        error: (err) => {
          if (err.status === 400 && err.error?.errors) {
            err.error.errors.forEach((e: { field: string; message: string }) => {
              this.bookFieldErrors[e.field] = e.message;
            });
          } else {
            this.modalErrorMessage = 'BOOKS.ERR_SAVE';
          }
          this.cdr.detectChanges();
        }
      });
    }
  }

  openDeleteModal(id: number): void {
    this.bookIdToDelete = id;
    this.isDeleteModalOpen = true;
    this.cdr.detectChanges();
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.bookIdToDelete = null;
    this.cdr.detectChanges();
  }

  confirmDelete(): void {
    if (this.bookIdToDelete !== null) {
      this.bookService.deleteBook(this.bookIdToDelete).subscribe({
        next: () => {
          this.closeDeleteModal();
          this.loadBooks();
        },
        error: () => {
          this.errorMessage = 'BOOKS.ERR_SAVE';
          this.closeDeleteModal();
        }
      });
    }
  }

  openOrderModal(book: any): void {
    this.selectedBook = book;
    this.orderQuantity = 1;
    this.orderModalErrorMessage = '';
    this.isOrderModalOpen = true;
    this.cdr.detectChanges();
  }

  closeOrderModal(): void {
    this.isOrderModalOpen = false;
    this.selectedBook = null;
    this.orderModalErrorMessage = '';
    this.cdr.detectChanges();
  }

  confirmOrder(): void {
    this.orderModalErrorMessage = '';

    if (this.orderQuantity < 1) {
      this.orderModalErrorMessage = 'ORDERS.ERR_MIN_QTY';
      return;
    }

    if (this.orderQuantity > this.selectedBook.quantity) {
      this.orderModalErrorMessage = this.translate.instant('ORDERS.ERR_MAX_QTY', { qty: this.selectedBook.quantity });
      return;
    }

    const orderRequest = {
      bookId: this.selectedBook.id,
      quantity: this.orderQuantity
    };

    this.orderService.createOrder(orderRequest).subscribe({
      next: () => {
        this.closeOrderModal();
        this.loadBooks();
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        if (err.status === 400 && err.error?.errors) {
          const messages = err.error.errors.map((e: { message: string }) => e.message).join(', ');
          this.orderModalErrorMessage = messages;
        } else {
          this.orderModalErrorMessage = err.error || 'ORDERS.ERR_PLACE';
        }
        this.cdr.detectChanges();
      }
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isLibrarian(): boolean {
    return this.authService.isLibrarian();
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
