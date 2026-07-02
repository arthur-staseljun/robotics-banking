import { Currency } from './currency.model';

export interface Account {
  id: number;
  userId: number;
  currency: Currency;
  balance: number;
}
