import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full',
  },

  {
    path: 'home',
    loadComponent: () =>
      import('./modules/account/components/home/home').then((m) => m.Home),
    canActivate: [authGuard],
  },

  {
    path: 'account/:id',
    loadComponent: () =>
      import('./modules/account/components/account-overview/account-overview').then(
        (m) => m.AccountOverview,
      ),
    canActivate: [authGuard],
  },

  {
    path: 'transaction/:id',
    loadComponent: () =>
      import('./modules/account/components/transaction-overview/transaction-overview').then(
        (m) => m.TransactionOverview,
      ),
    canActivate: [authGuard],
  },

  {
    path: '**',
    redirectTo: '/home',
  },
];
