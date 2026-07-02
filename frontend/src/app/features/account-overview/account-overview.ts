import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Account } from '../../core/models/account.model';
import { Transaction } from '../../core/models/transaction.model';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { BalanceChart } from './balance-chart';

const PAGE_SIZE = 10;
const CHART_LIMIT = 1000;

@Component({
  selector: 'app-account-overview',
  imports: [CurrencyPipe, DatePipe, RouterLink, BalanceChart],
  templateUrl: './account-overview.html',
})
export class AccountOverview implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly accountService = inject(AccountService);
  private readonly transactionService = inject(TransactionService);

  readonly account = signal<Account | null>(null);
  readonly transactions = signal<Transaction[]>([]);
  readonly chartData = signal<Transaction[]>([]);
  readonly loading = signal(false);
  readonly loadingMore = signal(false);
  readonly error = signal<string | null>(null);

  private accountId = 0;
  private nextPage = 0;
  private last = false;

  ngOnInit(): void {
    this.accountId = Number(this.route.snapshot.paramMap.get('id'));

    this.loading.set(true);
    this.error.set(null);
    this.accountService.get(this.accountId).subscribe({
      next: (account) => this.account.set(account),
      error: () => this.error.set('Failed to load account'),
    });
    this.transactionService.history(this.accountId, 0, CHART_LIMIT).subscribe({
      next: (page) => this.chartData.set(page.content),
      error: () => this.chartData.set([]),
    });
    this.loadNextPage();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const nearBottom =
      window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 200;
    if (nearBottom) {
      this.loadNextPage();
    }
  }

  private loadNextPage(): void {
    if (this.loadingMore() || this.last) {
      return;
    }
    this.loadingMore.set(true);
    this.transactionService.history(this.accountId, this.nextPage, PAGE_SIZE).subscribe({
      next: (page) => {
        this.transactions.update((current) => [...current, ...page.content]);
        this.last = page.last;
        this.nextPage++;
        this.loading.set(false);
        this.loadingMore.set(false);
        setTimeout(() => this.fillViewport());
      },
      error: () => {
        this.error.set('Failed to load transactions');
        this.loading.set(false);
        this.loadingMore.set(false);
      },
    });
  }

  private fillViewport(): void {
    const scrollable = document.documentElement.scrollHeight > window.innerHeight;
    if (!scrollable) {
      this.loadNextPage();
    }
  }
}
