import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { AccountOverview } from './features/account-overview/account-overview';
import { TransactionOverview } from './features/transaction-overview/transaction-overview';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'accounts/:id', component: AccountOverview },
  { path: 'transactions/:id', component: TransactionOverview },
  { path: '**', redirectTo: '' },
];
