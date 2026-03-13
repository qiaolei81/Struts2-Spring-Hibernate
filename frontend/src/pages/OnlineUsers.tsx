import { useState, useEffect, useCallback } from 'react';
import { Table, Badge, Button, Space, Typography, Tag, Tooltip, Alert } from 'antd';
import { ReloadOutlined, UserOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { onlineApi } from '@/api/modules';
import type { OnlineUserDto } from '@/types';

const REFRESH_INTERVAL_MS = 30_000;

const columns: ColumnsType<OnlineUserDto> = [
  {
    title: '#',
    key: 'index',
    width: 60,
    align: 'center',
    render: (_v, _r, idx) => (
      <Typography.Text type="secondary">{idx + 1}</Typography.Text>
    ),
  },
  {
    title: 'Username',
    dataIndex: 'username',
    key: 'username',
    width: 180,
    render: (name: string) => (
      <Tag color="green" icon={<UserOutlined />} style={{ fontSize: 13 }}>
        {name}
      </Tag>
    ),
  },
  {
    title: 'IP Address',
    dataIndex: 'ip',
    key: 'ip',
    width: 160,
    render: (ip: string) => (
      <code style={{ fontFamily: 'monospace', fontSize: 13, color: '#595959' }}>
        {ip || '—'}
      </code>
    ),
  },
  {
    title: 'Login Time',
    dataIndex: 'loginTime',
    key: 'loginTime',
    render: (t: string) =>
      t ? (
        <Tooltip title={dayjs(t).format('YYYY-MM-DD HH:mm:ss')}>
          <Space size={4}>
            <ClockCircleOutlined style={{ color: '#8c8c8c' }} />
            <span>{dayjs(t).format('MM-DD HH:mm:ss')}</span>
          </Space>
        </Tooltip>
      ) : (
        <Typography.Text type="secondary">—</Typography.Text>
      ),
  },
];

export default function OnlineUsers() {
  const [data, setData]       = useState<OnlineUserDto[]>([]);
  const [total, setTotal]     = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);
  const [lastRefreshed, setLastRefreshed] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await onlineApi.list();
      setData(result.rows ?? []);
      setTotal(result.total ?? result.rows?.length ?? 0);
      setLastRefreshed(dayjs().format('HH:mm:ss'));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load online users';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [load]);

  return (
    <div>
      <Space
        style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}
        wrap
      >
        <Space align="center" size={8}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            Online Users
          </Typography.Title>
          <Badge
            count={total}
            showZero
            style={{ backgroundColor: '#52c41a' }}
            overflowCount={999}
          />
        </Space>

        <Space>
          {lastRefreshed && (
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              Last refreshed: {lastRefreshed} · auto-refreshes every 30s
            </Typography.Text>
          )}
          <Button
            icon={<ReloadOutlined />}
            onClick={load}
            loading={loading}
          >
            Refresh
          </Button>
        </Space>
      </Space>

      {error && (
        <Alert
          type="error"
          message={error}
          style={{ marginBottom: 12 }}
          closable
          onClose={() => setError(null)}
        />
      )}

      <Table<OnlineUserDto>
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        pagination={false}
        size="middle"
        scroll={{ x: 'max-content' }}
        locale={{ emptyText: 'No users currently online' }}
        footer={() => (
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            Showing users active within the last 30 minutes.
          </Typography.Text>
        )}
      />
    </div>
  );
}
