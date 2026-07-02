import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/page.model';
import { Transaction } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  history(accountId: number, page: number, size: number): Observable<Page<Transaction>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Transaction>>(`${this.baseUrl}/accounts/${accountId}/transactions`, { params });
  }
}
