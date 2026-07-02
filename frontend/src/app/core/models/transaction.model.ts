import { Currency } from './currency.model';

export type TransactionType = 'DEPOSIT' | 'DEBIT' | 'EXCHANGE_OUT' | 'EXCHANGE_IN';

export interface Transaction {
  id: number;
  accountId: number;
  type: TransactionType;
  amount: number;
  currency: Currency;
  balanceAfter: number;
  description: string | null;
  createdAt: string;
  counterpartyTransactionId: number | null;
}
