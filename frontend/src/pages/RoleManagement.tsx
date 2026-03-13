import { useRef, useState } from 'react';
import {
  Button, Form, Input, message, Modal, Popconfirm, Space, Spin, Tree,
} from 'antd';
import {
  DeleteOutlined, EditOutlined, KeyOutlined, PlusOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'rc-tree/lib/interface';
import DataTable from '@/components/shared/DataTable';
import ModalForm from '@/components/shared/ModalForm';
import { roleApi, authoritiesApi } from '@/api/modules';
import type { RoleDto, AuthDto } from '@/types';

function toTreeNodes(nodes: AuthDto[]): DataNode[] {
  return nodes.map((n) => ({
    key: n.id,
    title: `${n.name}${n.url ? ` (${n.url})` : ''}`,
    children: n.children?.length ? toTreeNodes(n.children) : undefined,
  }));
}

export default function RoleManagement() {
  const tableRef = useRef<{ reload: () => void }>(null);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  // Create / Edit modal
  const [formOpen, setFormOpen]     = useState(false);
  const [editRecord, setEditRecord] = useState<RoleDto | null>(null);

  // Assign authorities modal
  const [authOpen, setAuthOpen]       = useState(false);
  const [authTarget, setAuthTarget]   = useState<RoleDto | null>(null);
  const [authTree, setAuthTree]       = useState<AuthDto[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);
  const [authLoading, setAuthLoading] = useState(false);

  // ── Columns ──────────────────────────────────────────────────────────────

  const columns: ColumnsType<RoleDto> = [
    { title: 'Name', dataIndex: 'name', sorter: true, width: 200 },
    { title: 'Description', dataIndex: 'description' },
    {
      title: 'Actions',
      key: 'actions',
      width: 240,
      fixed: 'right',
      render: (_: unknown, record: RoleDto) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            Edit
          </Button>
          <Button size="small" icon={<KeyOutlined />} onClick={() => openAuthorities(record)}>
            Authorities
          </Button>
          <Popconfirm title="Delete this role?" onConfirm={() => handleDelete([record.id])}>
            <Button size="small" danger icon={<DeleteOutlined />}>Delete</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ── Handlers ─────────────────────────────────────────────────────────────

  function openAdd() {
    setEditRecord(null);
    setFormOpen(true);
  }

  function openEdit(record: RoleDto) {
    setEditRecord(record);
    setFormOpen(true);
  }

  async function openAuthorities(record: RoleDto) {
    setAuthTarget(record);
    setAuthOpen(true);
    setAuthLoading(true);
    setCheckedKeys([]);
    try {
      const tree = await authoritiesApi.tree();
      setAuthTree(tree);
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleSave(values: Record<string, unknown>) {
    if (editRecord) {
      await roleApi.update(editRecord.id, {
        name: values.name as string,
        description: values.description as string | undefined,
      });
      message.success('Role updated');
    } else {
      await roleApi.create({
        name: values.name as string,
        description: values.description as string | undefined,
      });
      message.success('Role created');
    }
    setFormOpen(false);
    tableRef.current?.reload();
  }

  async function handleDelete(ids: string[]) {
    await roleApi.delete(ids);
    message.success(`Deleted ${ids.length} role(s)`);
    setSelectedKeys([]);
    tableRef.current?.reload();
  }

  async function handleAuthSave() {
    if (!authTarget) return;
    await authoritiesApi.setRoleAuthorities(authTarget.id, checkedKeys);
    message.success('Authorities updated');
    setAuthOpen(false);
  }

  // ── Toolbar ───────────────────────────────────────────────────────────────

  const toolbar = (
    <Space>
      <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
        Add Role
      </Button>
      <Popconfirm
        title={`Delete ${selectedKeys.length} selected role(s)?`}
        disabled={selectedKeys.length === 0}
        onConfirm={() => handleDelete(selectedKeys)}
      >
        <Button danger icon={<DeleteOutlined />} disabled={selectedKeys.length === 0}>
          Delete Selected
        </Button>
      </Popconfirm>
    </Space>
  );

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div style={{ padding: 16 }}>
      <DataTable<RoleDto>
        columns={columns}
        fetchData={roleApi.list}
        rowKey="id"
        toolbar={toolbar}
        searchPlaceholder="Search by role name"
        onSelectionChange={(keys) => setSelectedKeys(keys)}
        tableRef={tableRef}
      />

      {/* ── Add / Edit Role ── */}
      <ModalForm
        open={formOpen}
        title={editRecord ? 'Edit Role' : 'Add Role'}
        onOk={handleSave}
        onCancel={() => setFormOpen(false)}
        initialValues={editRecord ? { name: editRecord.name, description: editRecord.description } : null}
      >
        <Form.Item
          name="name"
          label="Name"
          rules={[{ required: true, message: 'Role name is required' }]}
        >
          <Input placeholder="Role name (e.g. ADMIN)" autoFocus />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} placeholder="Optional description" />
        </Form.Item>
      </ModalForm>

      {/* ── Assign Authorities ── */}
      <Modal
        open={authOpen}
        title={`Assign Authorities — ${authTarget?.name}`}
        onOk={handleAuthSave}
        onCancel={() => setAuthOpen(false)}
        okText="Save"
        width={480}
        destroyOnClose
      >
        <Spin spinning={authLoading}>
          {authTree.length === 0 && !authLoading ? (
            <p style={{ color: '#999' }}>No authorities configured yet.</p>
          ) : (
            <Tree
              checkable
              checkedKeys={checkedKeys}
              onCheck={(checked) =>
                setCheckedKeys(
                  Array.isArray(checked) ? (checked as string[]) : (checked.checked as string[])
                )
              }
              treeData={toTreeNodes(authTree)}
              defaultExpandAll
              style={{ maxHeight: 400, overflowY: 'auto' }}
            />
          )}
        </Spin>
      </Modal>
    </div>
  );
}
