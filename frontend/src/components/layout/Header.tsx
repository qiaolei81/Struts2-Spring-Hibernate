import { Avatar, Dropdown, Layout, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore, useAppStore } from '@/store';
import { authApi } from '@/api/modules';

const { Header } = Layout;

export default function AppHeader() {
  const navigate = useNavigate();
  const { user, clearAuth } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar } = useAppStore();

  async function handleLogout() {
    try { await authApi.logout(); } catch { /* ignore */ }
    clearAuth();
    navigate('/login', { replace: true });
  }

  const userMenu: MenuProps = {
    items: [
      { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', danger: true },
    ],
    onClick: ({ key }) => {
      if (key === 'logout') handleLogout();
    },
  };

  return (
    <Header
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 16px',
        background: '#001529',
        color: '#fff',
        height: 48,
        lineHeight: '48px',
        flexShrink: 0,
      }}
    >
      {/* Left: toggle + title */}
      <Space size={12}>
        <span
          onClick={toggleSidebar}
          style={{ fontSize: 18, cursor: 'pointer', color: '#fff' }}
        >
          {sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        </span>
        <Typography.Text style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>
          Lab Management System
        </Typography.Text>
      </Space>

      {/* Right: user avatar + dropdown */}
      <Dropdown menu={userMenu} trigger={['click']}>
        <Space style={{ cursor: 'pointer', color: '#fff' }}>
          <Avatar size="small" icon={<UserOutlined />} />
          <span>{user?.username ?? 'User'}</span>
        </Space>
      </Dropdown>
    </Header>
  );
}
