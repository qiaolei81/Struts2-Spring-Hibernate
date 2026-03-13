import { useRef, useState } from 'react';
import {
  Button, Form, Input, message, Modal, Popconfirm, Space, Spin, Tag, Transfer,
} from 'antd';
import {
  DeleteOutlined, EditOutlined, PlusOutlined, TeamOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import DataTable from '@/components/shared/DataTable';
import ModalForm from '@/components/shared/ModalForm';
import { userApi, roleApi } from '@/api/modules';
import type { UserDto, RoleDto } from '@/types';

export default function UserManagement() {
  const tableRef = useRef<{ reload: () => void }>(null);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  // Create / Edit modal
  const [formOpen, setFormOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<UserDto | null>(null);

  // Assign roles modal
  const [roleOpen, setRoleOpen]       = useState(false);
  const [roleTarget, setRoleTarget]   = useState<UserDto | null>(null);
  const [allRoles, setAllRoles]       = useState<RoleDto[]>([]);
  const [targetKeys, setTargetKeys]   = useState<string[]>([]);
  const [roleLoading, setRoleLoading] = useState(false);

  // ── Columns ──────────────────────────────────────────────────────────────

  const columns: ColumnsType<UserDto> = [
    { title: 'Username', dataIndex: 'username', sorter: true, width: 200 },
    {
      title: 'Roles',
      dataIndex: 'roles',
      render: (roles: RoleDto[]) =>
        roles?.map((r) => <Tag key={r.id} color="blue">{r.name}</Tag>) ?? '—',
    },
    { title: 'Created', dataIndex: 'createdAt', sorter: true, width: 180 },
    { title: 'Modified', dataIndex: 'modifiedAt', sorter: true, width: 180 },
    {
      title: 'Actions',
      key: 'actions',
      width: 220,
      fixed: 'right',
      render: (_: unknown, record: UserDto) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            Edit
          </Button>
          <Button size="small" icon={<TeamOutlined />} onClick={() => openRoles(record)}>
            Roles
          </Button>
          <Popconfirm title="Delete this user?" onConfirm={() => handleDelete([record.id])}>
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

  function openEdit(record: UserDto) {
    setEditRecord(record);
    setFormOpen(true);
  }

  async function openRoles(record: UserDto) {
    setRoleTarget(record);
    setRoleOpen(true);
    setRoleLoading(true);
    try {
      const roles = await roleApi.listAll();
      setAllRoles(roles);
      setTargetKeys(record.roles?.map((r) => r.id) ?? []);
    } finally {
      setRoleLoading(false);
    }
  }

  async function handleSave(values: Record<string, unknown>) {
    if (editRecord) {
      await userApi.update(editRecord.id, {
        username: values.username as string,
        ...(values.password ? { password: values.password as string } : {}),
      });
      message.success('User updated');
    } else {
      await userApi.create({
        username: values.username as string,
        password: values.password as string,
      });
      message.success('User created');
    }
    setFormOpen(false);
    tableRef.current?.reload();
  }

  async function handleDelete(ids: string[]) {
    await userApi.delete(ids);
    message.success(`Deleted ${ids.length} user(s)`);
    setSelectedKeys([]);
    tableRef.current?.reload();
  }

  async function handleRoleSave() {
    if (!roleTarget) return;
    await userApi.editRoles({ userIds: [roleTarget.id], roleIds: targetKeys });
    message.success('Roles updated');
    setRoleOpen(false);
    tableRef.current?.reload();
  }

  // ── Toolbar ───────────────────────────────────────────────────────────────

  const toolbar = (
    <Space>
      <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
        Add User
      </Button>
      <Popconfirm
        title={`Delete ${selectedKeys.length} selected user(s)?`}
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
      <DataTable<UserDto>
        columns={columns}
        fetchData={userApi.list}
        rowKey="id"
        toolbar={toolbar}
        searchPlaceholder="Search by username"
        onSelectionChange={(keys) => setSelectedKeys(keys)}
        tableRef={tableRef}
      />

      {/* ── Add / Edit User ── */}
      <ModalForm
        open={formOpen}
        title={editRecord ? 'Edit User' : 'Add User'}
        onOk={handleSave}
        onCancel={() => setFormOpen(false)}
        initialValues={editRecord ? { username: editRecord.username } : null}
      >
        <Form.Item
          name="username"
          label="Username"
          rules={[{ required: true, message: 'Username is required' }]}
        >
          <Input placeholder="Enter username" autoFocus />
        </Form.Item>
        <Form.Item
          name="password"
          label={editRecord ? 'New Password (leave blank to keep)' : 'Password'}
          rules={editRecord ? [] : [{ required: true, message: 'Password is required' }]}
        >
          <Input.Password
            placeholder={editRecord ? 'Leave blank to keep current password' : 'Enter password'}
          />
        </Form.Item>
      </ModalForm>

      {/* ── Assign Roles ── */}
      <Modal
        open={roleOpen}
        title={`Assign Roles — ${roleTarget?.username}`}
        onOk={handleRoleSave}
        onCancel={() => setRoleOpen(false)}
        okText="Save"
        width={580}
        destroyOnClose
      >
        <Spin spinning={roleLoading}>
          <Transfer
            dataSource={allRoles.map((r) => ({
              key: r.id,
              title: r.name,
              description: r.description ?? '',
            }))}
            titles={['Available Roles', 'Assigned Roles']}
            targetKeys={targetKeys}
            onChange={(keys) => setTargetKeys(keys as string[])}
            render={(item) => item.title}
            showSearch
            listStyle={{ width: 230, height: 320 }}
          />
        </Spin>
      </Modal>
    </div>
  );
}
