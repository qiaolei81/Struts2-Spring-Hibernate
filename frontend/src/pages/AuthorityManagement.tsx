import { useCallback, useEffect, useState } from 'react';
import {
  Button, Form, Input, InputNumber, message, Popconfirm, Space, Spin, Table,
} from 'antd';
import {
  DeleteOutlined, EditOutlined, PlusCircleOutlined, PlusOutlined, ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import ModalForm from '@/components/shared/ModalForm';
import { authoritiesApi } from '@/api/modules';
import type { AuthDto } from '@/types';

export default function AuthorityManagement() {
  const [tree, setTree]       = useState<AuthDto[]>([]);
  const [loading, setLoading] = useState(false);

  // Modal state
  const [formOpen, setFormOpen]     = useState(false);
  const [editRecord, setEditRecord] = useState<AuthDto | null>(null);
  const [parentId, setParentId]     = useState<string | undefined>(undefined);

  // ── Data loading ─────────────────────────────────────────────────────────

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await authoritiesApi.tree();
      setTree(data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Handlers ─────────────────────────────────────────────────────────────

  function openAddRoot() {
    setEditRecord(null);
    setParentId(undefined);
    setFormOpen(true);
  }

  function openAddChild(record: AuthDto) {
    setEditRecord(null);
    setParentId(record.id);
    setFormOpen(true);
  }

  function openEdit(record: AuthDto) {
    setEditRecord(record);
    setParentId(record.parentId);
    setFormOpen(true);
  }

  async function handleDelete(id: string) {
    await authoritiesApi.delete(id);
    message.success('Authority deleted');
    load();
  }

  async function handleSave(values: Record<string, unknown>) {
    const req = {
      parentId: parentId,
      name: values.name as string,
      description: values.description as string | undefined,
      seq: values.seq as number | undefined,
      url: values.url as string | undefined,
    };
    if (editRecord) {
      await authoritiesApi.update(editRecord.id, req);
      message.success('Authority updated');
    } else {
      await authoritiesApi.create(req);
      message.success('Authority created');
    }
    setFormOpen(false);
    load();
  }

  // ── Columns ──────────────────────────────────────────────────────────────

  const columns: ColumnsType<AuthDto> = [
    { title: 'Name', dataIndex: 'name', width: 220 },
    { title: 'URL Pattern', dataIndex: 'url', width: 260 },
    { title: 'Description', dataIndex: 'description' },
    { title: 'Order', dataIndex: 'seq', width: 80 },
    {
      title: 'Actions',
      key: 'actions',
      width: 240,
      fixed: 'right',
      render: (_: unknown, record: AuthDto) => (
        <Space>
          <Button
            size="small"
            icon={<PlusCircleOutlined />}
            onClick={() => openAddChild(record)}
          >
            Add Child
          </Button>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            Edit
          </Button>
          <Popconfirm
            title="Delete this authority? Children will also be removed."
            onConfirm={() => handleDelete(record.id)}
          >
            <Button size="small" danger icon={<DeleteOutlined />}>Delete</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div style={{ padding: 16 }}>
      <Space style={{ marginBottom: 12 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={openAddRoot}>
          Add Root Authority
        </Button>
        <Button icon={<ReloadOutlined />} onClick={load}>
          Refresh
        </Button>
      </Space>

      <Spin spinning={loading}>
        <Table<AuthDto>
          columns={columns}
          dataSource={tree}
          rowKey="id"
          childrenColumnName="children"
          pagination={false}
          defaultExpandAllRows
          size="middle"
          scroll={{ x: 'max-content' }}
        />
      </Spin>

      {/* ── Add / Edit Authority ── */}
      <ModalForm
        open={formOpen}
        title={
          editRecord
            ? 'Edit Authority'
            : parentId
            ? 'Add Child Authority'
            : 'Add Root Authority'
        }
        onOk={handleSave}
        onCancel={() => setFormOpen(false)}
        initialValues={
          editRecord
            ? {
                name: editRecord.name,
                description: editRecord.description,
                seq: editRecord.seq,
                url: editRecord.url,
              }
            : null
        }
      >
        <Form.Item
          name="name"
          label="Name"
          rules={[{ required: true, message: 'Authority name is required' }]}
        >
          <Input placeholder="e.g. User Management" autoFocus />
        </Form.Item>
        <Form.Item name="url" label="URL Pattern">
          <Input placeholder="e.g. /users/**" />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input placeholder="Optional description" />
        </Form.Item>
        <Form.Item name="seq" label="Display Order">
          <InputNumber min={0} placeholder="0" style={{ width: '100%' }} />
        </Form.Item>
      </ModalForm>
    </div>
  );
}
