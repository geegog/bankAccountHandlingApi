import { ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Transaction } from '../../../../core/services/transaction';
import { jsPDF } from 'jspdf';
import { ITransaction } from '../../../../core/models/transaction';
import { ETransactionType } from '../../enums/transaction-type';

@Component({
  selector: 'app-transaction-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  providers: [DatePipe],
  templateUrl: './transaction-overview.html',
  styleUrl: './transaction-overview.scss',
})
export class TransactionOverview implements OnInit {
  private route = inject(ActivatedRoute);
  private transactionService = inject(Transaction);
  private cdr = inject(ChangeDetectorRef);
  private datePipe = inject(DatePipe);
  private platformId = inject(PLATFORM_ID);

  transactionId!: string;
  transaction?: ITransaction;
  isLoading = true;

  ngOnInit() {
    this.transactionId = this.route.snapshot.paramMap.get('id') || '';

    this.transactionService.getTransactionDetails(this.transactionId).subscribe({
      next: (data) => {
        this.transaction = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading transaction profile details:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  exportToPdf() {
    if (!this.transaction || !isPlatformBrowser(this.platformId)) return;

    const doc = new jsPDF();

    const formattedDate = this.datePipe.transform(this.transaction.created, 'medium') || '';

    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(22);
    doc.setTextColor(44, 62, 80);
    doc.text('GLOBAL BANKING PORTAL', 20, 25);

    doc.setFont('Helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(127, 140, 141);
    doc.text('Official Transaction Summary Receipt Statement', 20, 32);

    doc.setDrawColor(220, 221, 225);
    doc.line(20, 38, 190, 38);

    doc.setFontSize(12);
    doc.setTextColor(44, 62, 80);

    let currentY = 50;
    const addRow = (label: string, value: string) => {
      doc.setFont('Helvetica', 'bold');
      doc.text(label, 20, currentY);
      doc.setFont('Helvetica', 'normal');
      doc.text(value, 75, currentY);
      currentY += 12;
    };

    addRow('Transaction ID:', this.transaction.id);
    addRow('Execution Timestamp:', formattedDate);
    addRow('Transaction Type:', this.transaction.transactionType.toString());
    addRow('Source Account Number:', this.transaction.accountNumber);
    addRow('Target Account Number:', this.transaction.targetAccountNumber || 'N/A');
    addRow('Reference Memo:', this.transaction.reference || 'No memo attachment reference found.');

    if (this.transaction.transactionType === ETransactionType.EXCHANGE) {
      addRow('Exchange Conversion Rate:', this.transaction.exchangeRate || '1.0');
    }

    doc.line(20, currentY, 190, currentY);
    currentY += 15;

    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(16);
    doc.text('Transaction Net Total Value:', 20, currentY);

    const sign = this.transaction.transactionType === ETransactionType.CREDIT ? '+' : '-';
    doc.text(`${sign}${this.transaction.value?.amount} ${this.transaction.value?.currency}`, 110, currentY);

    doc.save(`Receipt-Tx-${this.transaction.id.substring(0, 8)}.pdf`);
  }

  protected readonly ETransactionType = ETransactionType;
}
