/**
 * All REST API calls, organised by domain module.
 *
 * Base URL: /api  (proxied to http://localhost:8080 in dev)
 *
 * Contract assumptions (pending t4 / t7-t9 backend delivery):
 *  - Auth  : POST /api/auth/login | logout | register
 *  - Users : GET/POST/PUT/DELETE /api/users
 *  - Roles : GET/POST/PUT/DELETE /api/roles
 *  - Auths : GET/POST/PUT/DELETE /api/authorities (tree)
 *  - Menus : GET /api/menus/tree
 *  - Equip : GET/POST/PUT/DELETE /api/equipment + GET /api/equipment/export
 *  - Docs  : GET/POST/PUT/DELETE /api/documents + POST /api/documents/{id}/manual
 *  - Logs  : GET /api/logs (read-only)
 *  - Online: GET /api/online-users
 *  - Stats : GET /api/users/stats/by-role
 */

import { get, post, put, del, upload, downloadBlob } from './client';
import type {
  LoginRequest, LoginResponse,
  PageRequest, PageResult, SimpleListResult,
  UserDto, UserCreateRequest, UserUpdateRequest, UserRoleEditRequest,
  RoleDto, RoleCreateRequest, RoleUpdateRequest,
  AuthDto, AuthCreateRequest,
  MenuDto,
  EquipDto, EquipCreateRequest,
  DocDto, DocCreateRequest,
  LogDto,
  OnlineUserDto,
  UserRoleStat,
} from '@/types';

// ─── Auth ─────────────────────────────────────────────────────────────────────

export const authApi = {
  login: (req: LoginRequest) => post<LoginResponse>('/auth/login', req),
  logout: () => post<void>('/auth/logout'),
  register: (req: LoginRequest) => post<void>('/auth/register', req),
};

// ─── Users ────────────────────────────────────────────────────────────────────

export const userApi = {
  list: (params: PageRequest & { name?: string }) =>
    get<PageResult<UserDto>>('/users', { params }),

  listAll: () => get<UserDto[]>('/users/all'),

  create: (req: UserCreateRequest) => post<UserDto>('/users', req),

  update: (id: string, req: UserUpdateRequest) => put<UserDto>(`/users/${id}`, req),

  delete: (ids: string[]) =>
    del<void>('/users', { params: { ids: ids.join(',') } }),

  editRoles: (req: UserRoleEditRequest) => put<void>('/users/roles', req),

  statsByRole: () => get<UserRoleStat[]>('/users/stats/by-role'),
};

// ─── Roles ────────────────────────────────────────────────────────────────────

export const roleApi = {
  list: (params?: PageRequest & { name?: string }) =>
    get<PageResult<RoleDto>>('/roles', { params }),

  listAll: () => get<RoleDto[]>('/roles/all'),

  create: (req: RoleCreateRequest) => post<RoleDto>('/roles', req),

  update: (id: string, req: RoleUpdateRequest) => put<RoleDto>(`/roles/${id}`, req),

  delete: (ids: string[]) =>
    del<void>('/roles', { params: { ids: ids.join(',') } }),
};

// ─── Authorities ──────────────────────────────────────────────────────────────

export const authoritiesApi = {
  tree: () => get<AuthDto[]>('/authorities/tree'),

  flat: () => get<AuthDto[]>('/authorities'),

  create: (req: AuthCreateRequest) => post<AuthDto>('/authorities', req),

  update: (id: string, req: Partial<AuthCreateRequest>) =>
    put<AuthDto>(`/authorities/${id}`, req),

  delete: (id: string) => del<void>(`/authorities/${id}`),

  setRoleAuthorities: (roleId: string, authorityIds: string[]) =>
    put<void>(`/roles/${roleId}/authorities`, { authorityIds }),
};

// ─── Menus ────────────────────────────────────────────────────────────────────

export const menuApi = {
  tree: () => get<MenuDto[]>('/menus/tree'),
};

// ─── Equipment ────────────────────────────────────────────────────────────────

export const equipApi = {
  list: (params: PageRequest & { name?: string }) =>
    get<PageResult<EquipDto>>('/equipment', { params }),

  create: (req: EquipCreateRequest) => post<EquipDto>('/equipment', req),

  update: (id: string, req: Partial<EquipCreateRequest>) =>
    put<EquipDto>(`/equipment/${id}`, req),

  delete: (ids: string[]) =>
    del<void>('/equipment', { params: { ids: ids.join(',') } }),

  exportExcel: () => downloadBlob('/equipment/export', 'equipment.xls'),
};

// ─── Documents ────────────────────────────────────────────────────────────────

export const docApi = {
  list: (params: PageRequest & { name?: string }) =>
    get<PageResult<DocDto>>('/documents', { params }),

  create: (req: DocCreateRequest) => post<DocDto>('/documents', req),

  update: (id: string, req: Partial<DocCreateRequest>) =>
    put<DocDto>(`/documents/${id}`, req),

  delete: (ids: string[]) =>
    del<void>('/documents', { params: { ids: ids.join(',') } }),

  uploadManual: (id: string, file: File) =>
    upload<DocDto>(`/documents/${id}/manual`, file),

  downloadManual: (filename: string) =>
    downloadBlob(`/documents/manual/${filename}`, filename),
};

// ─── Access Logs ──────────────────────────────────────────────────────────────

export const logApi = {
  list: (params: PageRequest & { name?: string }) =>
    get<PageResult<LogDto>>('/logs', { params }),
};

// ─── Online Users ─────────────────────────────────────────────────────────────

export const onlineApi = {
  /** GET /api/online — returns {total, rows} of currently active users */
  list: () => get<SimpleListResult<OnlineUserDto>>('/online'),
};
