import { useRef, useState } from 'react';
import {
  Button, Form, Input, InputNumber, Popconfirm, Space, Tag, Tooltip, Typography, message,
} from 'antd';
import {
  DeleteOutlined, DownloadOutlined, EditOutlined, PlusOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import DataTable from '@/components/shared/DataTable';
import ModalForm from '@/components/shared/ModalForm';
import { equipApi } from '@/api/modules';
import type { EquipDto, EquipCreateRequest } from '@/types';

export default function EquipManagement() {
  const tableRef = useRef<{ reload: () => void }>(null);

  const [addOpen, setAddOpen]     = useState(false);
  const [editOpen, setEditOpen]   = useState(false);
  const [editRecord, setEditRecord] = useState<EquipDto | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [exporting, setExporting] = useState(false);

  // ─── Column definitions ───────────────────────────────────────────────────

  const columns: ColumnsType<EquipDto> = [
    {
      title: 'Model',
      dataIndex: 'model',
      key: 'model',
      sorter: true,
      width: 150,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      width: 180,
    },
    {
      title: 'Producer',
      dataIndex: 'producer',
      key: 'producer',
      width: 160,
    },
    {
      title: 'Quantity',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
      align: 'right',
      render: (val?: number) =>
        val !== undefined ? (
          <Tag color={val > 0 ? 'green' : 'red'}>{val}</Tag>
        ) : (
          <span style={{ color: '#999' }}>—</span>
        ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Actions',
      key: 'actions',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Space>
          <Tooltip title="Edit">
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => {
                setEditRecord(record);
                setEditOpen(true);
              }}
            />
          </Tooltip>
          <Tooltip title="Delete">
            <Popconfirm
              title="Delete this equipment?"
              onConfirm={() => handleDelete([record.id])}
              okText="Yes"
              cancelText="No"
            >
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  // ─── Handlers ────────────────────────────────────────────────────────────

  async function handleAdd(values: Record<string, unknown>) {
    await equipApi.create(values as unknown as EquipCreateRequest);
    message.success('Equipment created');
    setAddOpen(false);
    tableRef.current?.reload();
  }

  async function handleEdit(values: Record<string, unknown>) {
    if (!editRecord) return;
    await equipApi.update(editRecord.id, values as Partial<EquipCreateRequest>);
    message.success('Equipment updated');
    setEditOpen(false);
    setEditRecord(null);
    tableRef.current?.reload();
  }

  async function handleDelete(ids: string[]) {
    await equipApi.delete(ids);
    message.success(`Deleted ${ids.length} record(s)`);
    setSelectedKeys([]);
    tableRef.current?.reload();
  }

  async function handleExport() {
    setExporting(true);
    try {
      await equipApi.exportExcel();
      message.success('Excel exported');
    } catch {
      message.error('Export failed');
    } finally {
      setExporting(false);
    }
  }

  // ─── Toolbar ─────────────────────────────────────────────────────────────

  const toolbar = (
    <Space>
      <Button
        type="primary"
        icon={<PlusOutlined />}
        onClick={() => setAddOpen(true)}
      >
        Add
      </Button>
      <Popconfirm
        title={`Delete ${selectedKeys.length} selected record(s)?`}
        disabled={selectedKeys.length === 0}
        onConfirm={() => handleDelete(selectedKeys)}
        okText="Yes"
        cancelText="No"
      >
        <Button
          danger
          icon={<DeleteOutlined />}
          disabled={selectedKeys.length === 0}
        >
          Delete Selected
        </Button>
      </Popconfirm>
      <Button
        icon={<DownloadOutlined />}
        loading={exporting}
        onClick={handleExport}
      >
        Export Excel
      </Button>
    </Space>
  );

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        Equipment Management
      </Typography.Title>

      <DataTable<EquipDto>
        columns={columns}
        fetchData={equipApi.list}
        rowKey="id"
        toolbar={toolbar}
        searchPlaceholder="Search by name…"
        onSelectionChange={(keys) => setSelectedKeys(keys)}
        tableRef={tableRef}
      />

      {/* Add modal */}
      <ModalForm
        open={addOpen}
        title="Add Equipment"
        onOk={handleAdd}
        onCancel={() => setAddOpen(false)}
      >
        <EquipFormFields />
      </ModalForm>

      {/* Edit modal */}
      <ModalForm
        open={editOpen}
        title="Edit Equipment"
        onOk={handleEdit}
        onCancel={() => { setEditOpen(false); setEditRecord(null); }}
        initialValues={editRecord as Record<string, unknown> | null}
      >
        <EquipFormFields />
      </ModalForm>
    </div>
  );
}

// ─── Shared form fields ───────────────────────────────────────────────────────

function EquipFormFields() {
  return (
    <>
      <Form.Item
        name="model"
        label="Model"
        rules={[{ required: true, message: 'Model is required' }]}
      >
        <Input placeholder="e.g. TX-2000" />
      </Form.Item>
      <Form.Item name="name" label="Name">
        <Input placeholder="Equipment name" />
      </Form.Item>
      <Form.Item name="producer" label="Producer">
        <Input placeholder="Manufacturer" />
      </Form.Item>
      <Form.Item name="quantity" label="Quantity">
        <InputNumber min={0} style={{ width: '100%' }} placeholder="0" />
      </Form.Item>
      <Form.Item name="description" label="Description">
        <Input.TextArea rows={3} placeholder="Optional description" />
      </Form.Item>
    </>
  );
}
