import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, map, Observable } from 'rxjs';

import {
  AgentSnapshot,
  CallSnapshot,
  ConnectionSnapshot,
  DashboardSnapshot,
  ExtensionSnapshot,
} from '../types/cti.models';

@Injectable({ providedIn: 'root' })
export class CtiApiService {
  private readonly baseUrl = '/api';

  constructor(private readonly http: HttpClient) {}

  getSnapshot(): Observable<DashboardSnapshot> {
    return forkJoin({
      connection: this.http.get<ConnectionSnapshot>(`${this.baseUrl}/cti/connection`),
      activeCalls: this.http.get<CallSnapshot[]>(`${this.baseUrl}/calls/active`),
      agents: this.http.get<AgentSnapshot[]>(`${this.baseUrl}/agents`),
      extensions: this.http.get<ExtensionSnapshot[]>(`${this.baseUrl}/extensions`),
    }).pipe(
      map((snapshot) => ({
        ...snapshot,
        generatedAt: new Date().toISOString(),
      })),
    );
  }
}
