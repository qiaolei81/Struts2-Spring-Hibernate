/**
 * Structural tests for the API modules.
 *
 * Purpose: verify that every domain module exports the expected function names,
 * matching the contract assumed by the frontend pages (t10/t11/t12).
 * These tests do NOT make real HTTP requests — they validate the shape of the
 * API layer, ensuring the frontend won't blow up at import time.
 */
import { describe, it, expect } from 'vitest';

import { authApi } from '@/api/modules';
import { userApi } from '@/api/modules';
import { roleApi } from '@/api/modules';
import { authoritiesApi } from '@/api/modules';
import { equipApi } from '@/api/modules';
import { docApi } from '@/api/modules';
import { logApi } from '@/api/modules';
import { onlineApi } from '@/api/modules';

describe('authApi — shape', () => {
  it('exports login, logout, register', () => {
    expect(typeof authApi.login).toBe('function');
    expect(typeof authApi.logout).toBe('function');
    expect(typeof authApi.register).toBe('function');
  });
});

describe('userApi — shape', () => {
  it('exports list, listAll, create, update, delete, editRoles, statsByRole', () => {
    expect(typeof userApi.list).toBe('function');
    expect(typeof userApi.listAll).toBe('function');
    expect(typeof userApi.create).toBe('function');
    expect(typeof userApi.update).toBe('function');
    expect(typeof userApi.delete).toBe('function');
    expect(typeof userApi.editRoles).toBe('function');
    expect(typeof userApi.statsByRole).toBe('function');
  });
});

describe('roleApi — shape', () => {
  it('exports list, listAll, create, update, delete', () => {
    expect(typeof roleApi.list).toBe('function');
    expect(typeof roleApi.listAll).toBe('function');
    expect(typeof roleApi.create).toBe('function');
    expect(typeof roleApi.update).toBe('function');
    expect(typeof roleApi.delete).toBe('function');
  });
});

describe('authoritiesApi — shape', () => {
  it('exports tree, flat, create, update, delete, setRoleAuthorities', () => {
    expect(typeof authoritiesApi.tree).toBe('function');
    expect(typeof authoritiesApi.flat).toBe('function');
    expect(typeof authoritiesApi.create).toBe('function');
    expect(typeof authoritiesApi.update).toBe('function');
    expect(typeof authoritiesApi.delete).toBe('function');
    expect(typeof authoritiesApi.setRoleAuthorities).toBe('function');
  });
});

describe('equipApi — shape', () => {
  it('exports list, create, update, delete, exportExcel', () => {
    expect(typeof equipApi.list).toBe('function');
    expect(typeof equipApi.create).toBe('function');
    expect(typeof equipApi.update).toBe('function');
    expect(typeof equipApi.delete).toBe('function');
    expect(typeof equipApi.exportExcel).toBe('function');
  });
});

describe('docApi — shape', () => {
  it('exports list, create, update, delete, uploadManual, downloadManual', () => {
    expect(typeof docApi.list).toBe('function');
    expect(typeof docApi.create).toBe('function');
    expect(typeof docApi.update).toBe('function');
    expect(typeof docApi.delete).toBe('function');
    expect(typeof docApi.uploadManual).toBe('function');
    expect(typeof docApi.downloadManual).toBe('function');
  });
});

describe('logApi — shape', () => {
  it('exports list', () => {
    expect(typeof logApi.list).toBe('function');
  });
});

describe('onlineApi — shape', () => {
  it('exports list', () => {
    expect(typeof onlineApi.list).toBe('function');
  });
});
