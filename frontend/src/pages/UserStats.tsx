import { useEffect, useState } from 'react';
import { Card, Col, Empty, Row, Spin, Typography } from 'antd';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
} from 'recharts';
import type { PieLabelRenderProps } from 'recharts';
import { userApi } from '@/api/modules';
import type { UserRoleStat } from '@/types';

const COLORS = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16'];

function renderPieLabel({ name, percent }: PieLabelRenderProps) {
  const pct = percent != null ? (percent * 100).toFixed(1) : '0.0';
  return `${String(name)} ${pct}%`;
}

export default function UserStats() {
  const [stats, setStats]     = useState<UserRoleStat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);

  useEffect(() => {
    userApi.statsByRole()
      .then(setStats)
      .catch(() => setError('Failed to load statistics'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return <Empty description={error} style={{ marginTop: 64 }} />;
  }

  const totalUsers = stats.reduce((sum, s) => sum + s.userCount, 0);

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        User Statistics
      </Typography.Title>

      <Row gutter={[24, 24]}>
        {/* Bar chart */}
        <Col xs={24} lg={14}>
          <Card title="Users per Role" variant="outlined">
            {stats.length === 0 ? (
              <Empty description="No data" />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart
                  data={stats}
                  margin={{ top: 8, right: 24, left: 0, bottom: 8 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="roleName" tick={{ fontSize: 13 }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 13 }} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="userCount" name="User Count" fill="#1890ff" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>

        {/* Pie chart */}
        <Col xs={24} lg={10}>
          <Card title={`Role Distribution  (total: ${totalUsers})`} variant="outlined">
            {stats.length === 0 ? (
              <Empty description="No data" />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  <Pie
                    data={stats}
                    dataKey="userCount"
                    nameKey="roleName"
                    cx="50%"
                    cy="50%"
                    outerRadius={110}
                    label={renderPieLabel}
                    labelLine={true}
                  >
                    {stats.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>

        {/* Summary tiles */}
        <Col xs={24}>
          <Card title="Role Summary" variant="outlined">
            <Row gutter={[16, 16]}>
              {stats.map((s, index) => (
                <Col key={s.roleName} xs={12} sm={8} md={6} lg={4}>
                  <Card
                    size="small"
                    style={{ borderTop: `3px solid ${COLORS[index % COLORS.length]}`, textAlign: 'center' }}
                  >
                    <Typography.Text
                      strong
                      style={{ fontSize: 28, color: COLORS[index % COLORS.length], display: 'block' }}
                    >
                      {s.userCount}
                    </Typography.Text>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {s.roleName}
                    </Typography.Text>
                  </Card>
                </Col>
              ))}
              {stats.length === 0 && <Col xs={24}><Empty description="No roles found" /></Col>}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
