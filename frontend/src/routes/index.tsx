import React, { lazy, Suspense } from 'react';
import { Navigate, Outlet, type RouteObject } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuthStore } from '@/store';
import MainLayout from '@/components/layout/MainLayout';

// ─── Lazy page imports ────────────────────────────────────────────────────────

const Login     = lazy(() => import('@/pages/Login'));
const Dashboard = lazy(() => import('@/pages/Dashboard'));

// Module pages — stubbed here; implemented in t10–t12
const UserManagement      = lazy(() => import('@/pages/UserManagement'));
const RoleManagement      = lazy(() => import('@/pages/RoleManagement'));
const AuthorityManagement = lazy(() => import('@/pages/AuthorityManagement'));
const EquipManagement     = lazy(() => import('@/pages/EquipManagement'));
const DocManagement       = lazy(() => import('@/pages/DocManagement'));
const LogViewer           = lazy(() => import('@/pages/LogViewer'));
const UserStats           = lazy(() => import('@/pages/UserStats'));
const OnlineUsers         = lazy(() => import('@/pages/OnlineUsers'));

// ─── Guards ───────────────────────────────────────────────────────────────────

function RequireAuth() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

function RedirectIfAuthed() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : <Outlet />;
}

// ─── Loading fallback ─────────────────────────────────────────────────────────

function PageSpin() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
      <Spin size="large" />
    </div>
  );
}

function S({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<PageSpin />}>{children}</Suspense>;
}

// ─── Route tree ───────────────────────────────────────────────────────────────

export const routes: RouteObject[] = [
  // Public routes
  {
    element: <RedirectIfAuthed />,
    children: [
      { path: '/login', element: <S><Login /></S> },
    ],
  },

  // Protected routes wrapped in main layout
  {
    element: <RequireAuth />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { index: true,                       element: <Navigate to="/dashboard" replace /> },
          { path: '/dashboard',                element: <S><Dashboard /></S> },
          { path: '/users',                    element: <S><UserManagement /></S> },
          { path: '/roles',                    element: <S><RoleManagement /></S> },
          { path: '/authorities',              element: <S><AuthorityManagement /></S> },
          { path: '/equipment',                element: <S><EquipManagement /></S> },
          { path: '/documents',                element: <S><DocManagement /></S> },
          { path: '/logs',                     element: <S><LogViewer /></S> },
          { path: '/stats/users',              element: <S><UserStats /></S> },
          { path: '/online-users',             element: <S><OnlineUsers /></S> },
        ],
      },
    ],
  },

  // Fallback
  { path: '*', element: <Navigate to="/dashboard" replace /> },
];
