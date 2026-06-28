import { IBase } from '../../shared/models/base';

export interface IUser extends IBase {
  firstName: string;
  lastName: string;
  email: string;
}
