import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { UserInfo } from '@/types';

// ─── Auth Store ───────────────────────────────────────────────────────────────

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
  setAuth: (token: string, user: UserInfo) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isAuthenticated: false,

      setAuth: (token, user) =>
        set({ token, user, isAuthenticated: true }),

      clearAuth: () =>
        set({ token: null, user: null, isAuthenticated: false }),
    }),
    {
      name: 'lab-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ token: state.token, user: state.user }),
      onRehydrateStorage: () => (state) => {
        if (state?.token) {
          state.isAuthenticated = true;
        }
      },
    }
  )
);

// ─── App / UI Store ───────────────────────────────────────────────────────────

interface AppState {
  sidebarCollapsed: boolean;
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;

  // Active tab tracking (tab key → label + url)
  openTabs: TabItem[];
  activeTabKey: string;
  openTab: (tab: TabItem) => void;
  closeTab: (key: string) => void;
  closeOtherTabs: (key: string) => void;
  closeAllTabs: () => void;
  setActiveTab: (key: string) => void;
}

export interface TabItem {
  key: string;   // unique tab identifier (usually URL path)
  label: string;
  path: string;
}

const HOME_TAB: TabItem = { key: '/dashboard', label: '首页', path: '/dashboard' };

export const useAppStore = create<AppState>()((set, get) => ({
  sidebarCollapsed: false,
  toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),

  openTabs: [HOME_TAB],
  activeTabKey: HOME_TAB.key,

  openTab: (tab) => {
    const { openTabs } = get();
    const existing = openTabs.find((t) => t.key === tab.key);
    if (existing) {
      set({ activeTabKey: tab.key });
    } else {
      set({ openTabs: [...openTabs, tab], activeTabKey: tab.key });
    }
  },

  closeTab: (key) => {
    const { openTabs, activeTabKey } = get();
    if (key === HOME_TAB.key) return; // cannot close home
    const remaining = openTabs.filter((t) => t.key !== key);
    const newActive =
      activeTabKey === key
        ? (remaining[remaining.length - 1]?.key ?? HOME_TAB.key)
        : activeTabKey;
    set({ openTabs: remaining, activeTabKey: newActive });
  },

  closeOtherTabs: (key) => {
    const { openTabs } = get();
    const kept = openTabs.filter((t) => t.key === HOME_TAB.key || t.key === key);
    set({ openTabs: kept, activeTabKey: key });
  },

  closeAllTabs: () =>
    set({ openTabs: [HOME_TAB], activeTabKey: HOME_TAB.key }),

  setActiveTab: (key) => set({ activeTabKey: key }),
}));
