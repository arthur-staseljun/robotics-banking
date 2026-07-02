import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Account } from '../../core/models/account.model';
import { AccountService } from '../../core/services/account.service';

const CURRENT_USER_ID = 1;

@Component({
  selector: 'app-home',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './home.html',
})
export class Home implements OnInit {
  private readonly accountService = inject(AccountService);

  readonly accounts = signal<Account[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.accountService.listByUser(CURRENT_USER_ID).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load accounts');
        this.loading.set(false);
      },
    });
  }
}
