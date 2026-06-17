import { Routes } from '@angular/router';
import { adminGuard } from './core/services/guards/admin.guard';
import { librarianGuard } from './core/services/guards/librarian.guard';
import { userGuard } from './core/services/guards/user.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./features/home/home').then(m => m.Home)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then(m => m.Register)
  },
  {
    path: 'search',
    canActivate: [userGuard],
    loadComponent: () => import('./features/search/book-search/book-search').then(m => m.BookSearch)
  },
  {
    path: 'orders',
    canActivate: [userGuard],
    loadComponent: () => import('./features/orders/order-list/order-list').then(m => m.OrderList)
  },
  {
    path: 'librarian',
    canActivate: [librarianGuard],
    loadComponent: () => import('./features/librarian/librarian-orders').then(m => m.LibrarianOrders)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/admin-dashboard').then(m => m.AdminDashboard)
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];
