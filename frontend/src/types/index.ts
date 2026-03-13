// ─── Pagination ───────────────────────────────────────────────────────────────

export interface PageRequest {
  page: number;   // 0-based (Spring Data default)
  size: number;
  sort?: string;  // e.g. "cname,asc"
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;  // current page (0-based)
  size: number;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: UserInfo;
}

export interface UserInfo {
  id: string;
  username: string;
  roles: string[];
  authorities: string[];
}

// ─── User ─────────────────────────────────────────────────────────────────────

export interface UserDto {
  id: string;
  username: string;
  createdAt?: string;
  modifiedAt?: string;
  roles: RoleDto[];
}

export interface UserCreateRequest {
  username: string;
  password: string;
}

export interface UserUpdateRequest {
  username?: string;
  password?: string;
}

export interface UserRoleEditRequest {
  userIds: string[];
  roleIds: string[];
}

// ─── Role ─────────────────────────────────────────────────────────────────────

export interface RoleDto {
  id: string;
  name: string;
  description?: string;
}

export interface RoleCreateRequest {
  name: string;
  description?: string;
}

export interface RoleUpdateRequest {
  name?: string;
  description?: string;
}

// ─── Authority ────────────────────────────────────────────────────────────────

export interface AuthDto {
  id: string;
  parentId?: string;
  name: string;
  description?: string;
  seq?: number;
  url?: string;
  children?: AuthDto[];
}

export interface AuthCreateRequest {
  parentId?: string;
  name: string;
  description?: string;
  seq?: number;
  url?: string;
}

// ─── Menu ─────────────────────────────────────────────────────────────────────

export interface MenuDto {
  id: string;
  parentId?: string;
  name: string;
  url?: string;
  iconCls?: string;
  seq?: number;
  children?: MenuDto[];
}

// ─── Equipment ────────────────────────────────────────────────────────────────

export interface EquipDto {
  id: string;
  model: string;
  name?: string;
  producer?: string;
  quantity?: number;
  description?: string;
}

export interface EquipCreateRequest {
  model: string;
  name?: string;
  producer?: string;
  quantity?: number;
  description?: string;
}

// ─── Document ─────────────────────────────────────────────────────────────────

export interface DocDto {
  id: string;
  model: string;
  name?: string;
  producer?: string;
  quantity?: number;
  manualFilename?: string;
}

export interface DocCreateRequest {
  model: string;
  name?: string;
  producer?: string;
  quantity?: number;
}

// ─── Log ──────────────────────────────────────────────────────────────────────

export interface LogDto {
  id: string;
  username: string;
  ip: string;
  datetime?: string;
  message?: string;
}

// ─── Online User ──────────────────────────────────────────────────────────────

export interface OnlineUserDto {
  id: string;
  username: string;
  ip: string;
  loginTime: string;
}

// ─── Statistics ───────────────────────────────────────────────────────────────

export interface UserRoleStat {
  roleName: string;
  userCount: number;
}

// ─── Simple (non-paginated) list result ───────────────────────────────────────

/** Matches the backend {total, rows} format used for non-paginated list endpoints */
export interface SimpleListResult<T> {
  total: number;
  rows: T[];
}

// ─── API error ────────────────────────────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
