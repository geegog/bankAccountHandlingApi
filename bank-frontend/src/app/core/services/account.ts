import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IBankAccount } from '../models/account';

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
