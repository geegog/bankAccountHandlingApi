import { inject, Injectable } from '@angular/core';
import { IPagedResult, ITransaction } from './account';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Transaction {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/transaction';

  getAccountTransactions(
    accountNumber: number,
    page: number,
    size: number,
  ): Observable<IPagedResult<ITransaction>> {
    const body = {
      page: page,
      size: size,
    };

    return this.http.post<IPagedResult<ITransaction>>(
      `${this.apiUrl}/account/${accountNumber}/history`,
      body,
    );
  }

  getTransactionDetails(id: string): Observable<ITransaction> {
    return this.http.get<ITransaction>(
      `${this.apiUrl}/${id}`
    );
  }
}
