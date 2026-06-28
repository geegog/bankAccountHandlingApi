import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Account, IBankAccount } from '../../../../core/services/account';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private accountService = inject(Account);
  private cdr = inject(ChangeDetectorRef);

  accounts: IBankAccount[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.isLoading = true;

    this.accountService.getAccounts().subscribe({
      next: (data) => {
        this.accounts = data;
        this.isLoading = false;

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading account arrays from Spring Boot backend:', err);
        this.isLoading = false;

        this.cdr.detectChanges();
      },
    });
  }
}
