/**
 * Unit tests for the Zustand auth and app stores.
 *
 * No React context needed — tests the store state machines directly.
 */
import { beforeEach, describe, it, expect } from 'vitest';

// We need to isolate store state between tests
// Zustand persists to localStorage; we reset between tests
import { useAuthStore } from '@/store';

describe('useAuthStore', () => {
  beforeEach(() => {
    // Reset store state between tests
    useAuthStore.setState({
      token: null,
      user: null,
      isAuthenticated: false,
    });
    localStorage.clear();
  });

  it('starts with no authentication', () => {
    const state = useAuthStore.getState();
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('setAuth stores token and user, sets isAuthenticated=true', () => {
    const mockUser = {
      id: 'user-1',
      username: 'admin',
      roles: ['ADMIN'],
      authorities: ['/api/users'],
    };

    useAuthStore.getState().setAuth('mock-jwt-token', mockUser);

    const state = useAuthStore.getState();
    expect(state.token).toBe('mock-jwt-token');
    expect(state.user).toEqual(mockUser);
    expect(state.isAuthenticated).toBe(true);
  });

  it('clearAuth resets all auth state', () => {
    const mockUser = {
      id: 'user-1',
      username: 'admin',
      roles: ['ADMIN'],
      authorities: [],
    };

    useAuthStore.getState().setAuth('some-token', mockUser);
    expect(useAuthStore.getState().isAuthenticated).toBe(true);

    useAuthStore.getState().clearAuth();

    const state = useAuthStore.getState();
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('setAuth followed by clearAuth leaves no residual data', () => {
    useAuthStore.getState().setAuth('t1', { id: 'u1', username: 'alice', roles: [], authorities: [] });
    useAuthStore.getState().setAuth('t2', { id: 'u2', username: 'bob', roles: ['ADMIN'], authorities: [] });
    useAuthStore.getState().clearAuth();

    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().token).toBeNull();
  });
});
