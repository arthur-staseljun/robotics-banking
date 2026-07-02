import { Component, computed, input } from '@angular/core';
import { Transaction } from '../../core/models/transaction.model';

const WIDTH = 600;
const HEIGHT = 200;
const PADDING = 20;

@Component({
  selector: 'app-balance-chart',
  templateUrl: './balance-chart.html',
})
export class BalanceChart {
  readonly transactions = input.required<Transaction[]>();

  readonly width = WIDTH;
  readonly height = HEIGHT;

  readonly polyline = computed(() => {
    const points = [...this.transactions()]
      .map((t) => ({ time: new Date(t.createdAt).getTime(), balance: t.balanceAfter }))
      .sort((a, b) => a.time - b.time);

    if (points.length < 2) {
      return '';
    }

    const times = points.map((p) => p.time);
    const balances = points.map((p) => p.balance);
    const minTime = Math.min(...times);
    const minBalance = Math.min(...balances);
    const spanTime = Math.max(...times) - minTime || 1;
    const spanBalance = Math.max(...balances) - minBalance || 1;
    const innerWidth = WIDTH - PADDING * 2;
    const innerHeight = HEIGHT - PADDING * 2;

    return points
      .map((p) => {
        const x = PADDING + ((p.time - minTime) / spanTime) * innerWidth;
        const y = PADDING + (1 - (p.balance - minBalance) / spanBalance) * innerHeight;
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  });
}
