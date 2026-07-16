import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { Subscription, catchError, finalize, of } from 'rxjs';

import { CtiApiService } from './services/cti-api.service';
import { CtiStreamService } from './services/cti-stream.service';
import { AgentSnapshot, CallSnapshot, DashboardSnapshot, ExtensionSnapshot } from './types/cti.models';

@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly connected = signal(false);
  protected readonly connectionMessage = signal('Sin conexion');
  protected readonly lastUpdate = signal<string | null>(null);
  protected readonly activeCalls = signal<CallSnapshot[]>([]);
  protected readonly agents = signal<AgentSnapshot[]>([]);
  protected readonly extensions = signal<ExtensionSnapshot[]>([]);
  protected readonly callCount = computed(() => this.activeCalls().length);
  protected readonly busyAgents = computed(() =>
    this.agents().filter((agent) => agent.status !== 'AVAILABLE').length
  );

  private streamSubscription?: Subscription;

  constructor(
    private readonly apiService: CtiApiService,
    private readonly streamService: CtiStreamService,
  ) {}

  ngOnInit(): void {
    this.loadInitialState();
    this.connectStream();
  }

  ngOnDestroy(): void {
    this.streamSubscription?.unsubscribe();
  }

  protected reload(): void {
    this.loadInitialState();
  }

  protected trackCall(_: number, call: CallSnapshot): string {
    return call.callId;
  }

  protected trackAgent(_: number, agent: AgentSnapshot): string {
    return agent.agentId;
  }

  protected trackExtension(_: number, extension: ExtensionSnapshot): string {
    return extension.extension;
  }

  protected statusClass(status: string): string {
    return `status status-${status.toLowerCase().replaceAll('_', '-')}`;
  }

  private loadInitialState(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.apiService.getSnapshot()
      .pipe(
        catchError(() => {
          this.errorMessage.set('No se pudo conectar con el backend.');
          this.connected.set(false);
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((snapshot) => {
        if (snapshot) {
          this.applySnapshot(snapshot);
        }
      });
  }

  private connectStream(): void {
    this.streamSubscription = this.streamService.connect()
      .subscribe({
        next: (snapshot) => {
          this.errorMessage.set(null);
          this.applySnapshot(snapshot);
        },
        error: () => {
          this.connected.set(false);
          this.errorMessage.set('Se perdio la actualizacion en vivo. Revisa que el backend siga activo.');
        },
      });
  }

  private applySnapshot(snapshot: DashboardSnapshot): void {
    this.connected.set(snapshot.connection.connected);
    this.connectionMessage.set(snapshot.connection.message);
    this.lastUpdate.set(snapshot.generatedAt);
    this.activeCalls.set(snapshot.activeCalls);
    this.agents.set(snapshot.agents);
    this.extensions.set(snapshot.extensions);
  }
}
