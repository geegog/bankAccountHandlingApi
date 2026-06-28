import { ChangeDetectorRef, Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Transaction } from '../../../../core/services/transaction';
import { ITransaction } from '../../../../core/services/account';
import { jsPDF } from 'jspdf';

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
    // 1. Capture the unique transaction ID string from the active URL path
    this.transactionId = this.route.snapshot.paramMap.get('id') || '';

    // 2. Load the transaction profile metrics
    this.transactionService.getTransactionDetails(this.transactionId).subscribe({
      next: (data) => {
        this.transaction = data;
        this.isLoading = false;
        this.cdr.detectChanges(); // Sync view layout safely
      },
      error: (err) => {
        console.error('Error loading transaction profile details:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // 3. Method to build and trigger the *.pdf download report
  exportToPdf() {
    if (!this.transaction || !isPlatformBrowser(this.platformId)) return;

    // Instantiate a standard default blank page doc model
    const doc = new jsPDF();

    const formattedDate = this.datePipe.transform(this.transaction.created, 'medium') || '';

    // Add Branding / Header Title
    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(22);
    doc.setTextColor(44, 62, 80); // Deep Blue corporate theme accent
    doc.text('GLOBAL BANKING PORTAL', 20, 25);

    doc.setFont('Helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(127, 140, 141);
    doc.text('Official Transaction Summary Receipt Statement', 20, 32);

    // Draw an elegant divider line separating header from transaction metrics
    doc.setDrawColor(220, 221, 225);
    doc.line(20, 38, 190, 38);

    // Document Data Matrix Rows
    doc.setFontSize(12);
    doc.setTextColor(44, 62, 80);

    let currentY = 50;
    const addRow = (label: string, value: string) => {
      doc.setFont('Helvetica', 'bold');
      doc.text(label, 20, currentY);
      doc.setFont('Helvetica', 'normal');
      doc.text(value, 75, currentY);
      currentY += 12; // Advance cursor position down vertically
    };

    addRow('Transaction ID:', this.transaction.id);
    addRow('Execution Timestamp:', formattedDate);
    addRow('Transaction Type:', this.transaction.transactionType);
    addRow('Source Account Number:', this.transaction.accountNumber);
    addRow('Target Account Number:', this.transaction.targetAccountNumber || 'N/A');
    addRow('Reference Memo:', this.transaction.reference || 'No memo attachment reference found.');

    if (this.transaction.transactionType === 'EXCHANGE') {
      addRow('Exchange Conversion Rate:', this.transaction.exchangeRate || '1.0');
    }

    // Divider for Totals section
    doc.line(20, currentY, 190, currentY);
    currentY += 15;

    // Bold Summary Statement Amount Highlights
    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(16);
    doc.text('Transaction Net Total Value:', 20, currentY);

    // Show positive sign if CREDIT, minus sign if DEBIT
    const sign = this.transaction.transactionType === 'CREDIT' ? '+' : '-';
    doc.text(`${sign}${this.transaction.value?.amount} ${this.transaction.value?.currency}`, 110, currentY);

    // 4. File-saver trigger prompt delivered straight to user browser downloads directory
    doc.save(`Receipt-Tx-${this.transaction.id.substring(0, 8)}.pdf`);
  }
}
