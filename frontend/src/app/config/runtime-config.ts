export interface RuntimeEnvironment {
  apiBaseUrl?: string;
}

declare global {
  interface Window {
    __env?: RuntimeEnvironment;
  }
}

export function apiBaseUrl(): string {
  const configuredUrl = window.__env?.apiBaseUrl?.trim();

  if (!configuredUrl) {
    return '/api';
  }

  return configuredUrl.endsWith('/')
    ? configuredUrl.slice(0, -1)
    : configuredUrl;
}
