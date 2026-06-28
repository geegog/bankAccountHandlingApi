import { IBase } from '../../shared/models/base';
import { IMoney } from '../../shared/models/money';
import { IUser } from './user';

export interface IBankAccount extends IBase {
  accountNumber: string;
  accountName: string;
  balance: IMoney;
  user: IUser;
}
