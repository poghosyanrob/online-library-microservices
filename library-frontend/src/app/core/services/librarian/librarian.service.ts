import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderDto {
  id: number;
  bookId: number;
  quantity: number;
  status: 'PENDING' | 'COMPLETED' | 'CANCELED';
  userEmail: string;
}

@Injectable({
  providedIn: 'root'
})
export class LibrarianService {
  private apiUrl = 'http://localhost:8080/api/orders';

  constructor(private http: HttpClient) {}

  getAllOrders(): Observable<OrderDto[]> {
    return this.http.get<OrderDto[]>(this.apiUrl);
  }

  approveOrder(id: number): Observable<OrderDto> {
    return this.http.put<OrderDto>(`${this.apiUrl}/${id}/status?status=COMPLETED`, {});
  }

  cancelOrder(id: number): Observable<OrderDto> {
    return this.http.put<OrderDto>(`${this.apiUrl}/${id}/status?status=CANCELED`, {});
  }
}
