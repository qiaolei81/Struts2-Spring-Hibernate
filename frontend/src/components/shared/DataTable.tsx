/**
 * DataTable — generic paginated, sortable table with search bar and toolbar.
 *
 * Usage:
 *   <DataTable
 *     columns={columns}
 *     fetchData={myApi.list}
 *     searchPlaceholder="Search by name"
 *     toolbar={<Button onClick={handleAdd}>Add</Button>}
 *     rowKey="id"
 *   />
 */

import { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Input, Space, Table } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import type { PageRequest, PageResult } from '@/types';

type FetchFn<T> = (params: PageRequest & { name?: string }) => Promise<PageResult<T>>;

interface DataTableProps<T extends object> {
  columns: ColumnsType<T>;
  fetchData: FetchFn<T>;
  rowKey: keyof T | ((row: T) => string);
  toolbar?: React.ReactNode;
  searchPlaceholder?: string;
  /** Expose selected rows to parent */
  onSelectionChange?: (selectedKeys: string[], selectedRows: T[]) => void;
  /** Ref-based reload trigger (call tableRef.current?.reload()) */
  tableRef?: React.RefObject<{ reload: () => void }>;
  pageSize?: number;
}

export default function DataTable<T extends object>({
  columns,
  fetchData,
  rowKey,
  toolbar,
  searchPlaceholder = 'Search…',
  onSelectionChange,
  tableRef,
  pageSize: initialPageSize = 10,
}: DataTableProps<T>) {
  const [data, setData]         = useState<T[]>([]);
  const [loading, setLoading]   = useState(false);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [sortField, setSortField] = useState<string | undefined>();
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc' | undefined>();
  const [searchText, setSearchText] = useState('');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const searchInputRef = useRef<string>('');

  const load = useCallback(
    async (p = page, ps = pageSize, sort = sortField, order = sortOrder, name = searchInputRef.current) => {
      setLoading(true);
      try {
        const result = await fetchData({
          page: p - 1, // convert to 0-based
          size: ps,
          sort: sort && order ? `${sort},${order}` : undefined,
          name: name || undefined,
        });
        setData(result.content);
        setTotal(result.totalElements);
      } finally {
        setLoading(false);
      }
    },
    [fetchData, page, pageSize, sortField, sortOrder]
  );

  useEffect(() => { load(); }, []); // initial load

  // Expose reload via ref
  useEffect(() => {
    if (tableRef && 'current' in tableRef) {
      (tableRef as React.MutableRefObject<{ reload: () => void }>).current = {
        reload: () => load(page, pageSize, sortField, sortOrder),
      };
    }
  });

  function handleTableChange(
    pagination: TablePaginationConfig,
    _filters: Record<string, unknown>,
    sorter: SorterResult<T> | SorterResult<T>[]
  ) {
    const p  = pagination.current  ?? 1;
    const ps = pagination.pageSize ?? pageSize;
    const s  = Array.isArray(sorter) ? sorter[0] : sorter;
    const field = typeof s.field === 'string' ? s.field : undefined;
    const order = s.order === 'ascend' ? 'asc' : s.order === 'descend' ? 'desc' : undefined;

    setPage(p);
    setPageSize(ps);
    setSortField(field);
    setSortOrder(order);
    load(p, ps, field, order, searchInputRef.current);
  }

  function handleSearch() {
    setPage(1);
    load(1, pageSize, sortField, sortOrder, searchText);
  }

  function handleSearchChange(val: string) {
    setSearchText(val);
    searchInputRef.current = val;
    if (!val) {
      setPage(1);
      load(1, pageSize, sortField, sortOrder, '');
    }
  }

  const rowSelection = onSelectionChange
    ? {
        selectedRowKeys,
        onChange: (keys: React.Key[], rows: T[]) => {
          setSelectedRowKeys(keys);
          onSelectionChange(keys as string[], rows);
        },
      }
    : undefined;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <Space wrap style={{ justifyContent: 'space-between', width: '100%' }}>
        <Space>
          <Input
            placeholder={searchPlaceholder}
            value={searchText}
            onChange={(e) => handleSearchChange(e.target.value)}
            onPressEnter={handleSearch}
            allowClear
            style={{ width: 240 }}
          />
          <Button icon={<SearchOutlined />} onClick={handleSearch} type="primary">
            Search
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => load(page, pageSize, sortField, sortOrder, searchText)}
          />
        </Space>
        <Space>{toolbar}</Space>
      </Space>

      <Table<T>
        columns={columns}
        dataSource={data}
        rowKey={rowKey as string}
        loading={loading}
        rowSelection={rowSelection}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `Total ${t} records`,
          pageSizeOptions: ['10', '20', '50'],
        }}
        onChange={handleTableChange}
        size="middle"
        scroll={{ x: 'max-content' }}
      />
    </div>
  );
}
