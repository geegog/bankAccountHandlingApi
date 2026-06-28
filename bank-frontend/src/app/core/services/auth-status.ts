import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthStatus {
  errorMessage = signal<string | null>(null);

}
