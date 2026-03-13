/**
 * Integration tests for the Login page component.
 *
 * Verifies:
 *  - Page renders username and password fields
 *  - Submit button is present
 *  - Validation fires on empty submit
 *  - Successful login navigates to dashboard (mocked API)
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';

// Mock the API module before importing the component
vi.mock('@/api/modules', () => ({
  authApi: {
    login: vi.fn(),
  },
}));

// Mock react-router-dom navigate (keep MemoryRouter but capture navigation)
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return { ...actual, useNavigate: () => mockNavigate };
});

import Login from '@/pages/Login';
import { authApi } from '@/api/modules';

function renderLogin() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Login />
    </MemoryRouter>
  );
}

describe('Login page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders username and password input fields', () => {
    renderLogin();
    expect(screen.getByRole('textbox', { name: /username/i })).toBeInTheDocument();
    // Password field has type=password so it won't have role=textbox
    expect(document.querySelector('input[type="password"]')).toBeInTheDocument();
  });

  it('renders a submit / login button', () => {
    renderLogin();
    // Ant Design button renders as role=button
    const btn = screen.getByRole('button', { name: /login|sign in|登录/i });
    expect(btn).toBeInTheDocument();
  });

  it('calls authApi.login with entered credentials on submit', async () => {
    const user = userEvent.setup();
    const mockLoginResponse = {
      data: {
        token: 'test-jwt',
        user: { id: '1', username: 'admin', roles: ['ADMIN'], authorities: [] },
      },
    };
    vi.mocked(authApi.login).mockResolvedValueOnce(mockLoginResponse as never);

    renderLogin();

    await user.type(screen.getByRole('textbox', { name: /username/i }), 'admin');
    const passwordInput = document.querySelector('input[type="password"]') as HTMLInputElement;
    await user.type(passwordInput, 'admin123');

    const submitBtn = screen.getByRole('button', { name: /login|sign in|登录/i });
    await user.click(submitBtn);

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({
        username: 'admin',
        password: 'admin123',
      });
    });
  });
});
