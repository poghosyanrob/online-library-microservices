import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { retry } from 'rxjs';
import { BookService } from '../../core/services/book/book.service';
import { LibrarianService, OrderDto } from '../../core/services/librarian/librarian.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { LangSwitcher } from '../../shared/lang-switcher/lang-switcher';

@Component({
  selector: 'app-librarian-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, LangSwitcher],
  templateUrl: './librarian-orders.html',
  styleUrls: ['./librarian-orders.css']
})
export class LibrarianOrders implements OnInit {
  books: any[] = [];
  booksMap: { [key: number]: any } = {};
  isBookModalOpen = false;
  isEditMode = false;
  isDeleteModalOpen = false;
  newBook = { title: '', author: '', price: 0, quantity: 0 };
  editingBookId: number | null = null;
  bookToDeleteId: number | null = null;
  bookFieldErrors: { [key: string]: string } = {};
  modalErrorMessage = '';
  selectedImageFile: File | null = null;
  imagePreview: string | null = null;

  orders: OrderDto[] = [];
  orderErrorMessage = '';

  activeSection: 'books' | 'orders' = 'books';

  loading = true;
  private pending = 0;

  constructor(
    private bookService: BookService,
    private librarianService: LibrarianService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.pending = 2;

    this.bookService.getBooks().pipe(retry(2)).subscribe({
      next: (data) => {
        this.books = data.sort((a: any, b: any) => b.id - a.id);
        this.booksMap = {};
        data.forEach((b: any) => this.booksMap[b.id] = b);
        this.done();
      },
      error: () => this.done()
    });

    this.librarianService.getAllOrders().pipe(retry(2)).subscribe({
      next: (data) => { this.orders = [...data].sort((a: any, b: any) => b.id - a.id); this.done(); },
      error: () => { this.orderErrorMessage = 'LIBRARIAN.ERR_ORDERS_LOAD'; this.done(); }
    });
  }

  private done(): void {
    if (--this.pending === 0) {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  loadBooks(): void {
    this.bookService.getBooks().pipe(retry(2)).subscribe({
      next: (data) => {
        this.books = data.sort((a: any, b: any) => b.id - a.id);
        this.booksMap = {};
        data.forEach((b: any) => this.booksMap[b.id] = b);
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  openModal(): void {
    this.isEditMode = false;
    this.newBook = { title: '', author: '', price: 0, quantity: 0 };
    this.bookFieldErrors = {};
    this.modalErrorMessage = '';
    this.selectedImageFile = null;
    this.imagePreview = null;
    this.isBookModalOpen = true;
  }

  openEditModal(book: any): void {
    this.isEditMode = true;
    this.editingBookId = book.id;
    this.newBook = { title: book.title, author: book.author, price: book.price, quantity: book.quantity };
    this.bookFieldErrors = {};
    this.modalErrorMessage = '';
    this.selectedImageFile = null;
    this.imagePreview = book.imageUrl ? ('http://localhost:8080' + book.imageUrl) : null;
    this.isBookModalOpen = true;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedImageFile = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(this.selectedImageFile);
    }
  }

  getImageUrl(book: any): string {
    if (book.imageUrl) {
      return 'http://localhost:8080' + book.imageUrl;
    }
    return '';
  }

  closeModal(): void {
    this.isBookModalOpen = false;
  }

  saveBook(): void {
    this.bookFieldErrors = {};
    this.modalErrorMessage = '';

    if (!this.newBook.title?.trim()) { this.bookFieldErrors['title'] = 'BOOKS.ERR_TITLE_REQUIRED'; return; }
    if (!this.newBook.author?.trim()) { this.bookFieldErrors['author'] = 'BOOKS.ERR_AUTHOR_REQUIRED'; return; }
    if (this.newBook.price < 0) { this.bookFieldErrors['price'] = 'BOOKS.ERR_PRICE_NEGATIVE'; return; }
    if (this.newBook.quantity < 0) { this.bookFieldErrors['quantity'] = 'BOOKS.ERR_QTY_NEGATIVE'; return; }

    const request = this.isEditMode
      ? this.bookService.updateBook(this.editingBookId!, this.newBook)
      : this.bookService.addBook(this.newBook);

    request.subscribe({
      next: (savedBook) => {
        const bookId = this.isEditMode ? this.editingBookId! : savedBook.id;
        if (this.selectedImageFile && bookId) {
          this.bookService.uploadBookImage(bookId, this.selectedImageFile).subscribe({
            next: () => { this.closeModal(); this.loadBooks(); },
            error: () => { this.closeModal(); this.loadBooks(); }
          });
        } else {
          this.closeModal();
          this.loadBooks();
        }
      },
      error: (err) => { this.modalErrorMessage = err.error?.message || 'LIBRARIAN.ERR_SAVE'; }
    });
  }

  openDeleteModal(id: number): void {
    this.bookToDeleteId = id;
    this.isDeleteModalOpen = true;
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.bookToDeleteId = null;
  }

  confirmDelete(): void {
    if (this.bookToDeleteId === null) return;
    this.bookService.deleteBook(this.bookToDeleteId).subscribe({
      next: () => { this.closeDeleteModal(); this.loadBooks(); },
      error: () => { this.closeDeleteModal(); }
    });
  }

  getBookTitle(bookId: number): string {
    return this.booksMap[bookId]?.title || this.translate.instant('BOOKS.DELETED');
  }

  approveOrder(order: OrderDto): void {
    this.orderErrorMessage = '';
    this.librarianService.approveOrder(order.id).subscribe({
      next: (updated) => { order.status = updated.status; this.cdr.detectChanges(); },
      error: () => { this.orderErrorMessage = 'LIBRARIAN.ERR_ORDERS_LOAD'; this.cdr.detectChanges(); }
    });
  }

  cancelOrder(order: OrderDto): void {
    this.orderErrorMessage = '';
    this.librarianService.cancelOrder(order.id).subscribe({
      next: (updated) => { order.status = updated.status; this.cdr.detectChanges(); },
      error: () => { this.orderErrorMessage = 'LIBRARIAN.ERR_ORDERS_LOAD'; this.cdr.detectChanges(); }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
