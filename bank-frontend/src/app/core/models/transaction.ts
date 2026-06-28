import { IMoney } from '../../shared/models/money';
import { ETransactionType } from '../../modules/account/enums/transaction-type';
import { IBase } from '../../shared/models/base';

export interface ITransaction extends IBase {
  accountNumber: string;
  targetAccountNumber: string;
  userId: string;
  value: IMoney;
  targetValue: IMoney;
  balance: IMoney;
  targetBalance: IMoney;
  transactionType: ETransactionType;
  reference: string;
  exchangeRate: string;
}
