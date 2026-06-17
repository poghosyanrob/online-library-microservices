import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminDashboardStats {
  totalBookTitles: number;
  totalBookQuantity: number;
  totalOrders: number;
  totalRevenue: number;
}

export interface Librarian {
  id: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
}

export interface AppUser {
  id: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getDashboardStatistics(): Observable<AdminDashboardStats> {
    return this.http.get<AdminDashboardStats>(`${this.apiUrl}/dashboard-statistics`);
  }

  getLibrarians(): Observable<Librarian[]> {
    return this.http.get<Librarian[]>(`${this.apiUrl}/librarians`);
  }

  getAllUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.apiUrl}/users`);
  }

  createLibrarian(librarianData: any): Observable<Librarian> {
    return this.http.post<Librarian>(`${this.apiUrl}/librarians`, librarianData);
  }

  toggleLibrarianStatus(id: number, active: boolean): Observable<Librarian> {
    return this.http.put<Librarian>(`${this.apiUrl}/librarians/${id}/status`, { active });
  }

  toggleUserStatus(id: number, active: boolean): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/users/${id}/status`, { active });
  }
}
