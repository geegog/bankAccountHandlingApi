import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ITransaction } from '../models/transaction';
import { IPagedResult } from '../../shared/models/paged-result';
import { ITransactionSearch } from '../../modules/account/models/transaction-search';
import { ETransactionType } from '../../modules/account/enums/transaction-type';

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
    const body: ITransactionSearch = {
      page: page,
      size: size,
      transactionTypes: [ETransactionType.DEBIT, ETransactionType.CREDIT],
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
