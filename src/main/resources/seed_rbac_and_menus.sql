-- ==============================================================================
-- SCRIPT SEED DỮ LIỆU PHÂN QUYỀN (RBAC) & DANH MỤC MENU HỆ THỐNG ĐATN
-- Chuẩn hóa theo thiết kế: Menu gắn với Permission, Role là tập hợp Permission.
-- CSDL: PostgreSQL
-- ==============================================================================

-- 0. ĐẢM BẢO CỘT ID CHO USER_ROLES (Đồng bộ với BaseEntity)
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS id VARCHAR(36) DEFAULT gen_random_uuid()::text;
UPDATE user_roles SET id = gen_random_uuid()::text WHERE id IS NULL;
ALTER TABLE user_roles ALTER COLUMN id SET NOT NULL;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc 
        JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name 
        WHERE tc.table_name = 'user_roles' AND tc.constraint_type = 'PRIMARY KEY' AND kcu.column_name != 'id'
    ) THEN
        ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_pkey;
        ALTER TABLE user_roles ADD PRIMARY KEY (id);
    END IF;
END $$;

-- 1. BẢNG ROLES (Các vai trò trong hệ thống)
INSERT INTO roles (id, role_code, role_name, created_date, last_modified_date)
VALUES 
    (gen_random_uuid()::text, 'ADMIN',         'Quản trị viên hệ thống (Super Admin)', NOW(), NOW()),
    (gen_random_uuid()::text, 'FACULTY_STAFF', 'Cán bộ Quản lý Khoa / Đào tạo',        NOW(), NOW()),
    (gen_random_uuid()::text, 'LECTURER',      'Giảng viên',                            NOW(), NOW()),
    (gen_random_uuid()::text, 'STUDENT',       'Sinh viên làm ĐATN',                   NOW(), NOW()),
    (gen_random_uuid()::text, 'COMMITTEE',     'Thành viên Hội đồng bảo vệ',            NOW(), NOW())
ON CONFLICT (role_code) DO UPDATE 
SET role_name = EXCLUDED.role_name;

-- 2. BẢNG PERMISSIONS (Tất cả quyền hạn chi tiết khớp với Menu thực tế trên FE)
INSERT INTO permissions (id, code, name, module, created_date, last_modified_date)
VALUES
    -- Phân hệ Tổng quan
    (gen_random_uuid()::text, 'DASHBOARD_VIEW',          'Xem Dashboard tổng quan',              'DASHBOARD', NOW(), NOW()),
    
    -- Phân hệ Vận hành
    (gen_random_uuid()::text, 'THESIS_ROUND_MANAGE',     'Quản lý Đợt đồ án & Mốc thời gian',    'ROUND',     NOW(), NOW()),
    (gen_random_uuid()::text, 'TOPIC_VIEW',              'Xem danh sách đề tài',                 'TOPIC',     NOW(), NOW()),
    (gen_random_uuid()::text, 'TOPIC_CREATE',            'Đề xuất đề tài mới (Đề tài của tôi)',  'TOPIC',     NOW(), NOW()),
    (gen_random_uuid()::text, 'TOPIC_APPROVE',           'Phê duyệt đề tài mới',                 'TOPIC',     NOW(), NOW()),
    (gen_random_uuid()::text, 'TOPIC_SUGGEST',           'Ngân hàng đề tài gợi ý',               'TOPIC',     NOW(), NOW()),
    (gen_random_uuid()::text, 'ADVISOR_ASSIGN',          'Phân công Giảng viên hướng dẫn',       'ASSIGN',    NOW(), NOW()),
    (gen_random_uuid()::text, 'OUTLINE_APPROVE',         'Phê duyệt Đề cương chi tiết',          'PROGRESS',  NOW(), NOW()),
    (gen_random_uuid()::text, 'OVERDUE_TRACK',           'Theo dõi hồ sơ trễ hạn tự động',       'PROGRESS',  NOW(), NOW()),
    (gen_random_uuid()::text, 'SIMILARITY_CHECK',        'Kiểm tra Trùng lắp & Đạo văn',         'PROGRESS',  NOW(), NOW()),
    (gen_random_uuid()::text, 'EXCEPTION_HANDLE',        'Can thiệp ngoại lệ & Đơn từ sinh viên','PROGRESS',  NOW(), NOW()),
    (gen_random_uuid()::text, 'COMMITTEE_MANAGE',        'Quản lý danh sách Hội đồng & Xếp lịch', 'COMMITTEE', NOW(), NOW()),
    (gen_random_uuid()::text, 'COMMITTEE_SCORE',         'Nhập điểm đánh giá bảo vệ',            'COMMITTEE', NOW(), NOW()),

    -- Phân hệ Cấu hình & Tự động hóa
    (gen_random_uuid()::text, 'WORKFLOW_CONFIG',         'Cấu hình quy trình các bước & Ngưỡng', 'CONFIG',    NOW(), NOW()),
    (gen_random_uuid()::text, 'TEMPLATE_MANAGE',         'Quản lý Biểu mẫu & Mẫu email nhắc hạn','TEMPLATE',  NOW(), NOW()),

    -- Phân hệ Hệ thống
    (gen_random_uuid()::text, 'USER_MANAGE',             'Quản lý Người dùng & Phân quyền',       'SYSTEM',    NOW(), NOW()),
    (gen_random_uuid()::text, 'REPORT_VIEW',             'Báo cáo & Thống kê kết quả tốt nghiệp', 'REPORT',    NOW(), NOW()),
    (gen_random_uuid()::text, 'SYSTEM_CONFIG',           'Cài đặt hệ thống & Xem Audit Logs',    'SYSTEM',    NOW(), NOW()),

    -- Phân hệ Sinh viên
    (gen_random_uuid()::text, 'STUDENT_PROPOSAL',        'Đăng ký đề tài & Chọn GVHD',           'STUDENT',   NOW(), NOW()),
    (gen_random_uuid()::text, 'STUDENT_PROGRESS_REPORT', 'Nộp báo cáo tiến độ tuần',             'STUDENT',   NOW(), NOW()),
    (gen_random_uuid()::text, 'STUDENT_FINAL_SUBMIT',    'Nộp báo cáo chính thức & Source code', 'STUDENT',   NOW(), NOW())
ON CONFLICT (code) DO UPDATE 
SET name = EXCLUDED.name, module = EXCLUDED.module;

-- 3. BẢNG ROLE_PERMISSIONS (Gán quyền động theo role_code)
DELETE FROM role_permissions;

-- 3.1. ADMIN: FULL TOÀN BỘ QUYỀN HỆ THỐNG
INSERT INTO role_permissions (id, role_id, permission_id, created_date, last_modified_date)
SELECT gen_random_uuid()::text, r.id, p.id, NOW(), NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.role_code = 'ADMIN';

-- 3.2. FACULTY_STAFF (Cán bộ Khoa): Toàn bộ Vận hành + Cấu hình quy trình + Biểu mẫu + Báo cáo
INSERT INTO role_permissions (id, role_id, permission_id, created_date, last_modified_date)
SELECT gen_random_uuid()::text, r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'DASHBOARD_VIEW',
    'THESIS_ROUND_MANAGE',
    'TOPIC_VIEW',
    'TOPIC_APPROVE',
    'TOPIC_SUGGEST',
    'ADVISOR_ASSIGN',
    'OUTLINE_APPROVE',
    'OVERDUE_TRACK',
    'SIMILARITY_CHECK',
    'EXCEPTION_HANDLE',
    'COMMITTEE_MANAGE',
    'WORKFLOW_CONFIG',
    'TEMPLATE_MANAGE',
    'REPORT_VIEW'
)
WHERE r.role_code = 'FACULTY_STAFF';

-- 3.3. LECTURER (Giảng viên): Xem đề tài + Đề xuất đề tài + Duyệt đề cương + Chấm bảo vệ
INSERT INTO role_permissions (id, role_id, permission_id, created_date, last_modified_date)
SELECT gen_random_uuid()::text, r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'DASHBOARD_VIEW',
    'TOPIC_VIEW',
    'TOPIC_CREATE',
    'OUTLINE_APPROVE',
    'COMMITTEE_SCORE'
)
WHERE r.role_code = 'LECTURER';

-- 3.4. STUDENT (Sinh viên): Đăng ký đề tài + Báo cáo tiến độ + Nộp đồ án
INSERT INTO role_permissions (id, role_id, permission_id, created_date, last_modified_date)
SELECT gen_random_uuid()::text, r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'DASHBOARD_VIEW',
    'STUDENT_PROPOSAL',
    'STUDENT_PROGRESS_REPORT',
    'STUDENT_FINAL_SUBMIT'
)
WHERE r.role_code = 'STUDENT';

-- 3.5. COMMITTEE (Hội đồng bảo vệ): Chấm điểm bảo vệ
INSERT INTO role_permissions (id, role_id, permission_id, created_date, last_modified_date)
SELECT gen_random_uuid()::text, r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'DASHBOARD_VIEW',
    'COMMITTEE_SCORE'
)
WHERE r.role_code = 'COMMITTEE';

-- 4. BẢNG MENUS (Cây thư mục Menu, khớp chính xác FE)
-- Lưu ý: Menu cha KHÔNG gắn permission_code (NULL), tự động hiển thị nếu có >= 1 con hợp lệ
DELETE FROM menus;

-- 4.1. TỔNG QUAN
INSERT INTO menus (id, parent_id, code, label, icon, path, sort_order, permission_code, active, created_date, last_modified_date)
VALUES 
    ('menu-dashboard', NULL, 'tong-quan', 'Tổng quan (Dashboard)', 'DashboardOutlined', '/dashboard', 10, 'DASHBOARD_VIEW', true, NOW(), NOW());

-- 4.2. VẬN HÀNH
INSERT INTO menus (id, parent_id, code, label, icon, path, sort_order, permission_code, active, created_date, last_modified_date)
VALUES 
    ('menu-dot-do-an', NULL, 'dot-do-an', 'Đợt đồ án', 'CalendarOutlined', '/rounds', 20, 'THESIS_ROUND_MANAGE', true, NOW(), NOW()),
    
    -- Nhóm Đề tài (Menu cha: permission_code = NULL)
    ('menu-de-tai-parent', NULL, 'quan-ly-de-tai', 'Đề tài', 'BookOutlined', '/topics', 30, NULL, true, NOW(), NOW()),
    ('menu-ds-de-tai', 'menu-de-tai-parent', 'ds-de-tai', 'Tất cả đề tài', NULL, '/topics/all', 31, 'TOPIC_VIEW', true, NOW(), NOW()),
    ('menu-duyet-de-tai', 'menu-de-tai-parent', 'duyet-de-tai', 'Phê duyệt đề tài mới', NULL, '/topics/approve', 32, 'TOPIC_APPROVE', true, NOW(), NOW()),
    ('menu-ngan-hang-de-tai', 'menu-de-tai-parent', 'ngan-hang-de-tai', 'Ngân hàng đề tài gợi ý', NULL, '/topics/suggested', 33, 'TOPIC_SUGGEST', true, NOW(), NOW()),

    -- Phân công hướng dẫn
    ('menu-phan-cong', NULL, 'phan-cong', 'Phân công hướng dẫn', 'UserSwitchOutlined', '/advisor-assign', 40, 'ADVISOR_ASSIGN', true, NOW(), NOW()),

    -- Nhóm Hồ sơ & Tiến độ (Menu cha: permission_code = NULL)
    ('menu-ho-so-parent', NULL, 'ho-so-tien-do', 'Hồ sơ & Tiến độ', 'ClockCircleOutlined', '/progress', 50, NULL, true, NOW(), NOW()),
    ('menu-duyet-de-cuong', 'menu-ho-so-parent', 'duyet-de-cuong', 'Duyệt đề cương', 'CheckSquareOutlined', '/progress/outline', 51, 'OUTLINE_APPROVE', true, NOW(), NOW()),
    ('menu-theo-doi-tre-han', 'menu-ho-so-parent', 'theo-doi-tre-han', 'Theo dõi trễ hạn', 'ClockCircleOutlined', '/progress/overdue', 52, 'OVERDUE_TRACK', true, NOW(), NOW()),
    ('menu-trung-lap', 'menu-ho-so-parent', 'trung-lap', 'Trùng lặp & Đạo văn', 'SafetyCertificateOutlined', '/progress/similarity', 53, 'SIMILARITY_CHECK', true, NOW(), NOW()),
    ('menu-can-thiep-ngoai-le', 'menu-ho-so-parent', 'can-thiep-ngoai-le', 'Can thiệp ngoại lệ', 'ExclamationCircleOutlined', '/progress/exceptions', 54, 'EXCEPTION_HANDLE', true, NOW(), NOW()),

    -- Nhóm Hội đồng bảo vệ (Menu cha: permission_code = NULL)
    ('menu-hoi-dong-parent', NULL, 'hoi-dong', 'Hội đồng bảo vệ', 'ScheduleOutlined', '/committee', 60, NULL, true, NOW(), NOW()),
    ('menu-ds-hoi-dong', 'menu-hoi-dong-parent', 'ds-hoi-dong', 'Danh sách Hội đồng', NULL, '/committee/list', 61, 'COMMITTEE_MANAGE', true, NOW(), NOW()),
    ('menu-xep-lich-phong', 'menu-hoi-dong-parent', 'xep-lich-phong', 'Xếp lịch & Phòng bảo vệ', NULL, '/committee/schedule', 62, 'COMMITTEE_MANAGE', true, NOW(), NOW()),
    ('menu-cham-diem', 'menu-hoi-dong-parent', 'cham-diem-hoi-dong', 'Nhập điểm bảo vệ', NULL, '/committee/scoring', 63, 'COMMITTEE_SCORE', true, NOW(), NOW());

-- 4.3. CẤU HÌNH & TỰ ĐỘNG HÓA
INSERT INTO menus (id, parent_id, code, label, icon, path, sort_order, permission_code, active, created_date, last_modified_date)
VALUES 
    ('menu-cau-hinh-quy-trinh', NULL, 'cau-hinh-quy-trinh', 'Cấu hình quy trình', 'ControlOutlined', '/workflow-config', 70, 'WORKFLOW_CONFIG', true, NOW(), NOW()),
    ('menu-bieu-mau-thong-bao', NULL, 'bieu-mau-thong-bao', 'Biểu mẫu & Thông báo', 'MailOutlined', '/templates', 80, 'TEMPLATE_MANAGE', true, NOW(), NOW());

-- 4.4. HỆ THỐNG
INSERT INTO menus (id, parent_id, code, label, icon, path, sort_order, permission_code, active, created_date, last_modified_date)
VALUES 
    ('menu-nguoi-dung', NULL, 'nguoi-dung-phan-quyen', 'Người dùng & Phân quyền', 'TeamOutlined', '/users', 90, 'USER_MANAGE', true, NOW(), NOW()),
    ('menu-admin-users', 'menu-nguoi-dung', 'users', 'Tài khoản người dùng', 'UserOutlined', '/admin/users', 91, 'USER_MANAGE', true, NOW(), NOW()),
    ('menu-admin-roles', 'menu-nguoi-dung', 'roles', 'Nhóm Vai trò (Roles)', 'SafetyCertificateOutlined', '/admin/roles', 92, 'USER_MANAGE', true, NOW(), NOW()),
    ('menu-admin-permissions', 'menu-nguoi-dung', 'permissions', 'Danh mục Quyền', 'KeyOutlined', '/admin/permissions', 93, 'USER_MANAGE', true, NOW(), NOW()),
    ('menu-admin-menus', 'menu-nguoi-dung', 'menus', 'Danh mục Menu & URL', 'MenuOutlined', '/admin/menus', 94, 'USER_MANAGE', true, NOW(), NOW()),
    ('menu-bao-cao', NULL, 'bao-cao-thong-ke', 'Báo cáo & Thống kê', 'BarChartOutlined', '/reports', 100, 'REPORT_VIEW', true, NOW(), NOW()),
    ('menu-cai-dat', NULL, 'cai-dat-he-thong', 'Cài đặt hệ thống & Audit', 'SettingOutlined', '/settings', 110, 'SYSTEM_CONFIG', true, NOW(), NOW());

-- 4.5. ĐỒ ÁN CỦA TÔI (Dành riêng cho Sinh viên)
INSERT INTO menus (id, parent_id, code, label, icon, path, sort_order, permission_code, active, created_date, last_modified_date)
VALUES 
    ('menu-sv-dang-ky', NULL, 'dang-ky-de-tai', 'Đăng ký đề tài & Chọn GVHD', 'BookOutlined', '/student/proposal', 120, 'STUDENT_PROPOSAL', true, NOW(), NOW()),
    ('menu-sv-tien-do', NULL, 'nhat-ky-tien-do', 'Báo cáo tiến độ tuần', 'ClockCircleOutlined', '/student/progress', 130, 'STUDENT_PROGRESS_REPORT', true, NOW(), NOW()),
    ('menu-sv-nop-bai', NULL, 'nop-bao-cao', 'Nộp báo cáo chính thức', 'UploadOutlined', '/student/submit', 140, 'STUDENT_FINAL_SUBMIT', true, NOW(), NOW());

