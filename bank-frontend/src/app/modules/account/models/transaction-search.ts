import { ETransactionType } from '../enums/transaction-type';

export interface ITransactionSearch {
  size: number;
  page: number;
  transactionTypes: Array<ETransactionType>;
}
