import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { AccountOverview } from './features/account-overview/account-overview';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'accounts/:id', component: AccountOverview },
  { path: '**', redirectTo: '' },
];
