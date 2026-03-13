import { useRef } from 'react';
import { Button, Tag, Tooltip, Typography } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import DataTable from '@/components/shared/DataTable';
import { logApi } from '@/api/modules';
import type { LogDto } from '@/types';

const columns: ColumnsType<LogDto> = [
  {
    title: 'Time',
    dataIndex: 'datetime',
    key: 'datetime',
    width: 180,
    sorter: true,
    render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—',
  },
  {
    title: 'User',
    dataIndex: 'username',
    key: 'username',
    width: 140,
    sorter: true,
    render: (v: string) => <Tag color="blue">{v}</Tag>,
  },
  {
    title: 'IP Address',
    dataIndex: 'ip',
    key: 'ip',
    width: 140,
    render: (v: string) => <code style={{ fontSize: 12 }}>{v || '—'}</code>,
  },
  {
    title: 'Message',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
    render: (v: string) =>
      v ? (
        <Tooltip title={v} placement="topLeft">
          <span>{v}</span>
        </Tooltip>
      ) : (
        <Typography.Text type="secondary">—</Typography.Text>
      ),
  },
];

export default function LogViewer() {
  const tableRef = useRef<{ reload: () => void }>(null);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          Access Logs
        </Typography.Title>
        <Button icon={<ReloadOutlined />} onClick={() => tableRef.current?.reload()}>
          Refresh
        </Button>
      </div>

      <DataTable<LogDto>
        columns={columns}
        fetchData={logApi.list}
        rowKey="id"
        searchPlaceholder="Search by username…"
        tableRef={tableRef}
        pageSize={20}
      />
    </div>
  );
}
