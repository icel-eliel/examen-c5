import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { DashboardSnapshot } from '../types/cti.models';
import { apiBaseUrl } from '../config/runtime-config';

@Injectable({ providedIn: 'root' })
export class CtiStreamService {
  private readonly baseUrl = apiBaseUrl();

  connect(): Observable<DashboardSnapshot> {
    return new Observable((subscriber) => {
      const source = new EventSource(`${this.baseUrl}/stream/cti`);

      source.addEventListener('cti-snapshot', (event) => {
        try {
          subscriber.next(JSON.parse((event as MessageEvent).data) as DashboardSnapshot);
        } catch {
          subscriber.error(new Error('Invalid SSE payload'));
        }
      });

      source.onerror = () => {
        subscriber.error(new Error('SSE connection lost'));
        source.close();
      };

      return () => source.close();
    });
  }
}
