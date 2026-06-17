import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { retry } from 'rxjs';
import { AdminDashboardStats, AdminService, AppUser, Librarian } from '../../core/services/admin/admin.service';
import { BookService } from '../../core/services/book/book.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { LangSwitcher } from '../../shared/lang-switcher/lang-switcher';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe, TranslatePipe, LangSwitcher],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboard implements OnInit {
  stats: AdminDashboardStats | null = null;
  librarians: Librarian[] = [];
  users: AppUser[] = [];
  loading = true;
  errorMessage: string = '';

  isLibrarianModalOpen = false;
  librarianModalError = '';
  newLibrarian = { username: '', email: '', password: '' };

  books: any[] = [];
  showBooks = false;

  private pending = 0;

  constructor(
    private adminService: AdminService,
    private bookService: BookService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.pending = 3;

    this.adminService.getDashboardStatistics().pipe(retry(2)).subscribe({
      next: (data) => { this.stats = data; this.done(); },
      error: () => { this.errorMessage = 'ADMIN.ERR_STATS'; this.done(); }
    });

    this.adminService.getLibrarians().pipe(retry(2)).subscribe({
      next: (data) => { this.librarians = data; this.done(); },
      error: () => { this.done(); }
    });

    this.adminService.getAllUsers().pipe(retry(2)).subscribe({
      next: (data) => { this.users = data; this.done(); },
      error: () => { this.done(); }
    });
  }

  private done(): void {
    if (--this.pending === 0) {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  openLibrarianModal(): void {
    this.newLibrarian = { username: '', email: '', password: '' };
    this.librarianModalError = '';
    this.isLibrarianModalOpen = true;
  }

  closeLibrarianModal(): void {
    this.isLibrarianModalOpen = false;
  }

  onAddLibrarian(): void {
    this.librarianModalError = '';
    this.adminService.createLibrarian(this.newLibrarian).subscribe({
      next: () => {
        this.closeLibrarianModal();
        this.loadDashboardData();
      },
      error: () => { this.librarianModalError = 'ADMIN.ERR_ADD_LIBRARIAN'; }
    });
  }

  onToggleLibrarian(librarian: Librarian): void {
    if (!librarian?.id) return;
    this.errorMessage = '';
    const nextStatus = !librarian.active;
    this.adminService.toggleLibrarianStatus(librarian.id, nextStatus).subscribe({
      next: () => { librarian.active = nextStatus; this.cdr.detectChanges(); },
      error: () => { this.errorMessage = 'ADMIN.ERR_TOGGLE'; this.cdr.detectChanges(); }
    });
  }

  onToggleUser(user: AppUser): void {
    if (!user?.id) return;
    this.errorMessage = '';
    const nextStatus = !user.active;
    this.adminService.toggleUserStatus(user.id, nextStatus).subscribe({
      next: () => { user.active = nextStatus; this.cdr.detectChanges(); },
      error: () => { this.errorMessage = 'ADMIN.ERR_TOGGLE'; this.cdr.detectChanges(); }
    });
  }

  toggleBooks(): void {
    this.showBooks = !this.showBooks;
    if (this.showBooks && this.books.length === 0) {
      this.bookService.getBooks().pipe(retry(2)).subscribe({
        next: (data) => { this.books = data.sort((a: any, b: any) => b.id - a.id); this.cdr.detectChanges(); },
        error: () => { this.cdr.detectChanges(); }
      });
    }
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
