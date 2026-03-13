import { useEffect, useState } from 'react';
import { Layout, Menu, Skeleton } from 'antd';
import type { MenuProps } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAppStore } from '@/store';
import { menuApi } from '@/api/modules';
import type { MenuDto } from '@/types';

const { Sider } = Layout;

// Map menu URL to icon (fallback to FolderOutlined for unknown)
const ICON_MAP: Record<string, string> = {
  '/users':        '👤',
  '/roles':        '🏷',
  '/authorities':  '🔐',
  '/equipment':    '🔧',
  '/documents':    '📄',
  '/logs':         '📋',
  '/stats/users':  '📊',
  '/online-users': '🟢',
};

function buildMenuItems(nodes: MenuDto[]): MenuProps['items'] {
  return nodes
    .slice()
    .sort((a, b) => (a.seq ?? 0) - (b.seq ?? 0))
    .map((node) => {
      if (node.children && node.children.length > 0) {
        return {
          key: node.id,
          label: node.name,
          children: buildMenuItems(node.children),
        };
      }
      return {
        key: node.url ?? node.id,
        label: node.name,
        icon: node.url ? <span>{ICON_MAP[node.url] ?? '📌'}</span> : undefined,
      };
    });
}

export default function Sidebar() {
  const navigate = useNavigate();
  const { sidebarCollapsed } = useAppStore();
  const openTab = useAppStore((s) => s.openTab);

  const [menuItems, setMenuItems] = useState<MenuProps['items']>([]);
  const [loading, setLoading] = useState(true);
  const [openKeys, setOpenKeys] = useState<string[]>([]);

  useEffect(() => {
    menuApi
      .tree()
      .then((tree) => {
        const items = buildMenuItems(tree);
        setMenuItems(items);
        // expand all top-level groups by default
        const topKeys = tree.map((n) => n.id);
        setOpenKeys(topKeys);
      })
      .catch(() => {
        // If API not ready, show static fallback menu
        setMenuItems(FALLBACK_MENU);
        setOpenKeys(['system', 'assets', 'stats']);
      })
      .finally(() => setLoading(false));
  }, []);

  function handleSelect({ key }: { key: string }) {
    // key is the URL path if it starts with '/', otherwise a node id
    if (key.startsWith('/')) {
      const label = findLabel(menuItems, key) ?? key;
      openTab({ key, label, path: key });
      navigate(key);
    }
  }

  return (
    <Sider
      collapsible
      collapsed={sidebarCollapsed}
      trigger={null}
      width={220}
      style={{ overflow: 'auto', height: '100%', background: '#001529' }}
    >
      {loading ? (
        <Skeleton active paragraph={{ rows: 8 }} style={{ padding: 16 }} />
      ) : (
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          openKeys={openKeys}
          onOpenChange={setOpenKeys}
          onSelect={handleSelect}
          style={{ border: 'none' }}
        />
      )}
    </Sider>
  );
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function findLabel(items: MenuProps['items'], key: string): string | null {
  for (const item of items ?? []) {
    if (!item) continue;
    if ('key' in item && item.key === key && 'label' in item) {
      return typeof item.label === 'string' ? item.label : null;
    }
    if ('children' in item && item.children) {
      const found = findLabel(item.children as MenuProps['items'], key);
      if (found) return found;
    }
  }
  return null;
}

const FALLBACK_MENU: MenuProps['items'] = [
  {
    key: 'system',
    label: 'System',
    children: [
      { key: '/users',       label: '👤 Users' },
      { key: '/roles',       label: '🏷 Roles' },
      { key: '/authorities', label: '🔐 Authorities' },
    ],
  },
  {
    key: 'assets',
    label: 'Assets',
    children: [
      { key: '/equipment', label: '🔧 Equipment' },
      { key: '/documents', label: '📄 Documents' },
    ],
  },
  {
    key: 'stats',
    label: 'Reports',
    children: [
      { key: '/logs',         label: '📋 Access Logs' },
      { key: '/stats/users',  label: '📊 User Stats' },
      { key: '/online-users', label: '🟢 Online Users' },
    ],
  },
];
