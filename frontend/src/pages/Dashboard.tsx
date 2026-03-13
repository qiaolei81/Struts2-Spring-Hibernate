import { useEffect, useState } from 'react';
import { Card, Col, List, Row, Statistic, Typography } from 'antd';
import { UserOutlined, SafetyOutlined, ToolOutlined, FileTextOutlined } from '@ant-design/icons';
import { onlineApi } from '@/api/modules';
import type { OnlineUserDto } from '@/types';

export default function Dashboard() {
  const [onlineUsers, setOnlineUsers] = useState<OnlineUserDto[]>([]);

  useEffect(() => {
    onlineApi.list().then((res) => setOnlineUsers(res.rows ?? [])).catch(() => {});
  }, []);

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        Dashboard
      </Typography.Title>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Online Users"
              value={onlineUsers.length}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Modules" value={7} prefix={<SafetyOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Equipment" value="—" prefix={<ToolOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Documents" value="—" prefix={<FileTextOutlined />} />
          </Card>
        </Col>
      </Row>

      <Card
        title={`Online Users (${onlineUsers.length})`}
        style={{ marginTop: 16 }}
        size="small"
      >
        <List
          dataSource={onlineUsers}
          renderItem={(u) => (
            <List.Item>
              <List.Item.Meta
                avatar={<UserOutlined />}
                title={u.username}
                description={`IP: ${u.ip}  |  Login: ${u.loginTime}`}
              />
            </List.Item>
          )}
          locale={{ emptyText: 'No users online' }}
        />
      </Card>
    </div>
  );
}
