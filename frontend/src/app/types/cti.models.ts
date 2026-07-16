export type CallStatus = 'RINGING' | 'IN_CALL' | 'ON_HOLD' | 'TRANSFERRED';
export type AgentStatus = 'AVAILABLE' | 'RINGING' | 'BUSY' | 'ON_HOLD' | 'UNKNOWN';
export type ExtensionStatus = 'IDLE' | 'RINGING' | 'IN_USE' | 'ON_HOLD' | 'UNKNOWN';

export interface ConnectionSnapshot {
  connected: boolean;
  message: string;
  updatedAt: string;
}

export interface CallSnapshot {
  callId: string;
  extension: string;
  agentId: string;
  phoneNumber: string;
  status: CallStatus;
  lastEventType: string;
  updatedAt: string;
}

export interface AgentSnapshot {
  agentId: string;
  status: AgentStatus;
  currentCallId: string | null;
  updatedAt: string;
}

export interface ExtensionSnapshot {
  extension: string;
  status: ExtensionStatus;
  currentCallId: string | null;
  updatedAt: string;
}

export interface DashboardSnapshot {
  connection: ConnectionSnapshot;
  activeCalls: CallSnapshot[];
  agents: AgentSnapshot[];
  extensions: ExtensionSnapshot[];
  generatedAt: string;
}
