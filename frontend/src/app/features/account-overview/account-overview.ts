import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Account } from '../../core/models/account.model';
import { Transaction } from '../../core/models/transaction.model';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-account-overview',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './account-overview.html',
})
export class AccountOverview implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly accountService = inject(AccountService);
  private readonly transactionService = inject(TransactionService);

  readonly account = signal<Account | null>(null);
  readonly transactions = signal<Transaction[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const accountId = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.error.set(null);

    this.accountService.get(accountId).subscribe({
      next: (account) => this.account.set(account),
      error: () => this.error.set('Failed to load account'),
    });

    this.transactionService.history(accountId, 0, PAGE_SIZE).subscribe({
      next: (page) => {
        this.transactions.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load transactions');
        this.loading.set(false);
      },
    });
  }
}
