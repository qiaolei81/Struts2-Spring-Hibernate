import { useRef, useState } from 'react';
import {
  Button, Form, Input, InputNumber, Popconfirm, Space, Tag, Tooltip, Typography,
  Upload, message,
} from 'antd';
import {
  DeleteOutlined, DownloadOutlined, EditOutlined, PlusOutlined, UploadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { UploadFile } from 'antd/es/upload/interface';
import DataTable from '@/components/shared/DataTable';
import ModalForm from '@/components/shared/ModalForm';
import { docApi } from '@/api/modules';
import type { DocDto, DocCreateRequest } from '@/types';

export default function DocManagement() {
  const tableRef = useRef<{ reload: () => void }>(null);

  const [addOpen, setAddOpen]       = useState(false);
  const [editOpen, setEditOpen]     = useState(false);
  const [editRecord, setEditRecord] = useState<DocDto | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  // upload state for the manual-upload modal
  const [uploadTarget, setUploadTarget] = useState<DocDto | null>(null);
  const [uploadOpen, setUploadOpen]     = useState(false);
  const [fileList, setFileList]         = useState<UploadFile[]>([]);
  const [uploading, setUploading]       = useState(false);

  // ─── Column definitions ───────────────────────────────────────────────────

  const columns: ColumnsType<DocDto> = [
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
      title: 'Manual',
      dataIndex: 'manualFilename',
      key: 'manualFilename',
      width: 160,
      render: (filename: string | undefined, record) =>
        filename ? (
          <Button
            type="link"
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => docApi.downloadManual(filename)}
            style={{ padding: 0 }}
          >
            {filename}
          </Button>
        ) : (
          <Button
            size="small"
            icon={<UploadOutlined />}
            onClick={() => {
              setUploadTarget(record);
              setFileList([]);
              setUploadOpen(true);
            }}
          >
            Upload
          </Button>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      fixed: 'right',
      width: 140,
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
          {record.manualFilename && (
            <Tooltip title="Upload new manual">
              <Button
                size="small"
                icon={<UploadOutlined />}
                onClick={() => {
                  setUploadTarget(record);
                  setFileList([]);
                  setUploadOpen(true);
                }}
              />
            </Tooltip>
          )}
          <Tooltip title="Delete">
            <Popconfirm
              title="Delete this document?"
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
    await docApi.create(values as unknown as DocCreateRequest);
    message.success('Document created');
    setAddOpen(false);
    tableRef.current?.reload();
  }

  async function handleEdit(values: Record<string, unknown>) {
    if (!editRecord) return;
    await docApi.update(editRecord.id, values as Partial<DocCreateRequest>);
    message.success('Document updated');
    setEditOpen(false);
    setEditRecord(null);
    tableRef.current?.reload();
  }

  async function handleDelete(ids: string[]) {
    await docApi.delete(ids);
    message.success(`Deleted ${ids.length} record(s)`);
    setSelectedKeys([]);
    tableRef.current?.reload();
  }

  async function handleManualUpload() {
    if (!uploadTarget || fileList.length === 0) {
      message.warning('Please select a file first');
      return;
    }
    const file = fileList[0].originFileObj as File;
    setUploading(true);
    try {
      await docApi.uploadManual(uploadTarget.id, file);
      message.success('Manual uploaded successfully');
      setUploadOpen(false);
      setUploadTarget(null);
      setFileList([]);
      tableRef.current?.reload();
    } catch {
      message.error('Upload failed');
    } finally {
      setUploading(false);
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
    </Space>
  );

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        Document Management
      </Typography.Title>

      <DataTable<DocDto>
        columns={columns}
        fetchData={docApi.list}
        rowKey="id"
        toolbar={toolbar}
        searchPlaceholder="Search by name…"
        onSelectionChange={(keys) => setSelectedKeys(keys)}
        tableRef={tableRef}
      />

      {/* Add modal */}
      <ModalForm
        open={addOpen}
        title="Add Document"
        onOk={handleAdd}
        onCancel={() => setAddOpen(false)}
      >
        <DocFormFields />
      </ModalForm>

      {/* Edit modal */}
      <ModalForm
        open={editOpen}
        title="Edit Document"
        onOk={handleEdit}
        onCancel={() => { setEditOpen(false); setEditRecord(null); }}
        initialValues={editRecord as Record<string, unknown> | null}
      >
        <DocFormFields />
      </ModalForm>

      {/* Manual upload modal */}
      <ModalForm
        open={uploadOpen}
        title={`Upload Manual — ${uploadTarget?.name ?? uploadTarget?.model ?? ''}`}
        onOk={handleManualUpload}
        onCancel={() => { setUploadOpen(false); setUploadTarget(null); setFileList([]); }}
        okText="Upload"
        width={400}
      >
        <Form.Item label="Manual File" required>
          <Upload
            beforeUpload={() => false}
            fileList={fileList}
            maxCount={1}
            onChange={({ fileList: fl }) => setFileList(fl)}
            accept=".pdf,.doc,.docx,.xls,.xlsx,.zip"
          >
            <Button icon={<UploadOutlined />} loading={uploading}>
              Select File
            </Button>
          </Upload>
        </Form.Item>
      </ModalForm>
    </div>
  );
}

// ─── Shared form fields ───────────────────────────────────────────────────────

function DocFormFields() {
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
        <Input placeholder="Document name" />
      </Form.Item>
      <Form.Item name="producer" label="Producer">
        <Input placeholder="Manufacturer" />
      </Form.Item>
      <Form.Item name="quantity" label="Quantity">
        <InputNumber min={0} style={{ width: '100%' }} placeholder="0" />
      </Form.Item>
    </>
  );
}
