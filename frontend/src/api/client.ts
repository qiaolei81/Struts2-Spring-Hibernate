import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/store';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL ?? '/api';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request interceptor: attach JWT ──────────────────────────────────────────

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ─── Response interceptor: handle errors ─────────────────────────────────────

apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    const status: number | undefined = error.response?.status;
    const msg: string =
      error.response?.data?.message ?? error.message ?? 'Unknown error';

    if (status === 401) {
      useAuthStore.getState().clearAuth();
      // Redirect to login without importing react-router (avoids circular deps)
      window.location.href = '/login';
      return Promise.reject(error);
    }

    if (status === 403) {
      message.error('No permission to perform this action');
    } else if (status === 404) {
      message.error('Resource not found');
    } else if (status && status >= 500) {
      message.error(`Server error: ${msg}`);
    } else if (!status) {
      message.error('Network error — please check your connection');
    }

    return Promise.reject(error);
  }
);

// ─── Typed helper wrappers ────────────────────────────────────────────────────

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await apiClient.get<T>(url, config);
  return res.data;
}

export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const res = await apiClient.post<T>(url, data, config);
  return res.data;
}

export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const res = await apiClient.put<T>(url, data, config);
  return res.data;
}

export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await apiClient.delete<T>(url, config);
  return res.data;
}

export async function upload<T>(url: string, file: File, extraFields?: Record<string, string>): Promise<T> {
  const form = new FormData();
  form.append('file', file);
  if (extraFields) {
    Object.entries(extraFields).forEach(([k, v]) => form.append(k, v));
  }
  const res = await apiClient.post<T>(url, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}

export async function downloadBlob(url: string, filename: string): Promise<void> {
  const res = await apiClient.get(url, { responseType: 'blob' });
  const href = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement('a');
  link.href = href;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(href);
}
