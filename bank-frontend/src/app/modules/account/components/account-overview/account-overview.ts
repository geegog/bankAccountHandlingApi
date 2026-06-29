import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  HostListener,
  inject,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import {
  Account,
} from '../../../../core/services/account';
import { Transaction } from '../../../../core/services/transaction';

import { Chart, registerables } from 'chart.js';
import { IBankAccount } from '../../../../core/models/account';
import { ITransaction } from '../../../../core/models/transaction';
import { ETransactionType } from '../../enums/transaction-type';
Chart.register(...registerables);

@Component({
  selector: 'app-account-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  providers: [DatePipe],
  templateUrl: './account-overview.html',
  styleUrl: './account-overview.scss',
})
export class AccountOverview implements OnInit {
  private route = inject(ActivatedRoute);
  private accountService = inject(Account);
  private transactionService = inject(Transaction);
  private cdr = inject(ChangeDetectorRef);
  private datePipe = inject(DatePipe);

  private platformId = inject(PLATFORM_ID);

  accountId!: number;
  account?: IBankAccount;
  transactions: ITransaction[] = [];
  isLoading = true;

  currentPage = 0;
  pageSize = 10;
  loadingTransactions = false;
  hasMoreTransactions = true;

  chartInstance: any;

  ngOnInit() {
    this.accountId = Number(this.route.snapshot.paramMap.get('id'));

    this.accountService.getAccountDetails(this.accountId).subscribe({
      next: (data) => {
        this.account = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.cdr.detectChanges();
        console.error('Error fetching account details:', err);
      },
    });

    this.loadNextPage();
  }

  loadNextPage() {
    if (this.loadingTransactions || !this.hasMoreTransactions) return;

    this.loadingTransactions = true;
    this.transactionService
      .getAccountTransactions(this.accountId, this.currentPage, this.pageSize)
      .subscribe({
        next: (pageResult) => {
          if (pageResult.last || pageResult.content.length < this.pageSize) {
            this.hasMoreTransactions = false;
          }

          this.transactions = [...this.transactions, ...pageResult.content];

          this.currentPage++;
          this.loadingTransactions = false;

          this.cdr.detectChanges();

          this.updateBalanceChart();
        },
        error: (err) => {
          console.error('Error loading transactions:', err);
          this.loadingTransactions = false;
          this.cdr.detectChanges();
        },
      });
  }

  updateBalanceChart() {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    setTimeout(() => {
      const chronologicalData = [...this.transactions].reverse();
      const chartLabels = chronologicalData.map(
        (tx) => this.datePipe.transform(tx.created, 'shortDate') || '',
      );
      const balancePoints = chronologicalData.map((tx) => tx.balance?.amount || 0);

      if (this.chartInstance) {
        this.chartInstance.destroy();
      }

      const ctx = document.getElementById('balanceChart') as HTMLCanvasElement;

      if (!ctx) {
        console.warn('Chart Canvas element not found in DOM yet. Retrying on next scroll...');
        return;
      }

      this.chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: chartLabels,
          datasets: [
            {
              label: `Balance History (${this.account?.balance?.currency})`,
              data: balancePoints,
              borderColor: '#EE7200',
              backgroundColor: 'rgba(238, 114, 0, 0.05)',
              borderWidth: 2,
              fill: true,
              tension: 0.2,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { beginAtZero: false },
          },
        },
      });
    }, 50);
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const pos =
      (document.documentElement.scrollTop || document.body.scrollTop) +
      document.documentElement.clientHeight;
    const max = document.documentElement.scrollHeight;

    if (pos >= max * 0.9) {
      this.loadNextPage();
    }
  }

  protected readonly ETransactionType = ETransactionType;
}
