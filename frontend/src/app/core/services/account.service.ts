import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  listByUser(userId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/users/${userId}/accounts`);
  }

  get(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/accounts/${id}`);
  }
}
