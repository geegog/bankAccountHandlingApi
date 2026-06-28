import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IBase {
  id: string;
  created: string;
  updated: string;
}

export interface IMoney {
  amount: number;
  currency: string;
}

export interface IUser extends IBase {
  firstName: string;
  lastName: string;
  email: string;
}

export interface IBankAccount extends IBase {
  accountNumber: string;
  accountName: string;
  balance: IMoney;
  user: IUser
}

export interface ITransaction extends IBase {
  accountNumber: string;
  targetAccountNumber: string;
  userId: string;
  value: IMoney;
  targetValue: IMoney;
  balance: IMoney;
  targetBalance: IMoney;
  transactionType: 'CREDIT' | 'DEBIT' | 'EXCHANGE';
  reference: string;
  exchangeRate: string;
}

export interface IPagedResult<T> {
  content: Array<T>;
  totalPages: number;
  totalElements: number;
  numberOfElements: number;
  number: number;
  size: number;
  last: boolean;
  first: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class Account {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/account';

  getAccounts(): Observable<IBankAccount[]> {
    return this.http.get<IBankAccount[]>(`${this.apiUrl}/all`);
  }

  getAccountDetails(accountNumber: number): Observable<IBankAccount> {
    return this.http.get<IBankAccount>(`${this.apiUrl}/${accountNumber}`);
  }
}
