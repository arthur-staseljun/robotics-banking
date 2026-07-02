import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Transaction } from '../../core/models/transaction.model';
import { TransactionService } from '../../core/services/transaction.service';

@Component({
  selector: 'app-transaction-overview',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './transaction-overview.html',
})
export class TransactionOverview implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly transactionService = inject(TransactionService);

  readonly transaction = signal<Transaction | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.transactionService.get(id).subscribe({
      next: (transaction) => this.transaction.set(transaction),
      error: () => this.error.set('Failed to load transaction'),
    });
  }

  exportPdf(): void {
    window.print();
  }
}
