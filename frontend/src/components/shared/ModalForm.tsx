/**
 * ModalForm — reusable Ant Design Modal + Form wrapper.
 *
 * Usage:
 *   <ModalForm
 *     open={open}
 *     title="Add User"
 *     onOk={handleSubmit}
 *     onCancel={() => setOpen(false)}
 *     initialValues={editRecord}
 *   >
 *     <Form.Item name="username" label="Username" rules={[{required:true}]}>
 *       <Input />
 *     </Form.Item>
 *   </ModalForm>
 */

import { useEffect } from 'react';
import { Form, Modal } from 'antd';
import type { FormInstance } from 'antd';

interface ModalFormProps {
  open: boolean;
  title: string;
  onOk: (values: Record<string, unknown>, form: FormInstance) => Promise<void> | void;
  onCancel: () => void;
  initialValues?: Record<string, unknown> | null;
  children: React.ReactNode;
  width?: number;
  okText?: string;
}

export default function ModalForm({
  open,
  title,
  onOk,
  onCancel,
  initialValues,
  children,
  width = 480,
  okText = 'Save',
}: ModalFormProps) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else {
        form.resetFields();
      }
    }
  }, [open, initialValues, form]);

  async function handleOk() {
    const values = await form.validateFields();
    await onOk(values as Record<string, unknown>, form);
  }

  return (
    <Modal
      title={title}
      open={open}
      onOk={handleOk}
      onCancel={() => {
        form.resetFields();
        onCancel();
      }}
      okText={okText}
      destroyOnClose
      width={width}
    >
      <Form
        form={form}
        layout="vertical"
        style={{ marginTop: 16 }}
      >
        {children}
      </Form>
    </Modal>
  );
}
