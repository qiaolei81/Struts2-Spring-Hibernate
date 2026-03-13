import { Layout, Tabs, Dropdown, type MenuProps } from 'antd';
import { Outlet, useNavigate } from 'react-router-dom';
import { useAppStore } from '@/store';
import Sidebar from './Sidebar';
import AppHeader from './Header';

const { Content } = Layout;

export default function MainLayout() {
  const navigate = useNavigate();
  const { openTabs, activeTabKey, closeTab, closeOtherTabs, closeAllTabs, setActiveTab } =
    useAppStore();

  function handleTabChange(key: string) {
    setActiveTab(key);
    navigate(key);
  }

  function handleTabEdit(key: unknown, action: 'add' | 'remove') {
    if (action === 'remove' && typeof key === 'string') {
      const tab = openTabs.find((t) => t.key === key);
      closeTab(key);
      if (activeTabKey === key && tab) {
        const remaining = openTabs.filter((t) => t.key !== key);
        const next = remaining[remaining.length - 1];
        if (next) navigate(next.path);
      }
    }
  }

  function buildContextMenu(tabKey: string): MenuProps {
    return {
      items: [
        { key: 'refresh',     label: 'Refresh' },
        { key: 'close',       label: 'Close',       disabled: tabKey === '/dashboard' },
        { key: 'closeOther',  label: 'Close Others' },
        { key: 'closeAll',    label: 'Close All' },
      ],
      onClick: ({ key }) => {
        if (key === 'close')      closeTab(tabKey);
        if (key === 'closeOther') closeOtherTabs(tabKey);
        if (key === 'closeAll')   closeAllTabs();
        if (key === 'refresh')    window.location.reload();
        const target = useAppStore.getState().activeTabKey;
        navigate(target);
      },
    };
  }

  const renderTabLabel = (tab: (typeof openTabs)[number]) => (
    <Dropdown menu={buildContextMenu(tab.key)} trigger={['contextMenu']}>
      <span>{tab.label}</span>
    </Dropdown>
  );

  const richTabItems = openTabs.map((tab) => ({
    key: tab.key,
    label: renderTabLabel(tab),
    closable: tab.key !== '/dashboard',
  }));

  return (
    <Layout style={{ height: '100vh', overflow: 'hidden' }}>
      <AppHeader />
      <Layout>
        <Sidebar />
        <Layout style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <Tabs
            type="editable-card"
            hideAdd
            activeKey={activeTabKey}
            items={richTabItems}
            onChange={handleTabChange}
            onEdit={handleTabEdit}
            style={{ padding: '4px 8px 0', background: '#fff', flexShrink: 0 }}
            size="small"
          />
          <Content
            style={{
              flex: 1,
              overflow: 'auto',
              padding: 16,
              background: '#f0f2f5',
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}
