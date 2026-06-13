USE auth_system;

-- 手机卡菜单合并：在用手机卡、备用手机卡统一为"手机卡"
UPDATE sys_menu
SET menu_name = '手机卡',
    menu_path = '/phone/list',
    menu_icon = 'Tickets',
    perm_code = 'phone:list:view',
    sort_order = 1
WHERE id = 201;

DELETE FROM sys_menu WHERE id IN (202, 20201, 20202, 20203, 20204);

UPDATE sys_menu SET menu_name = '手机卡查询', perm_code = 'phone:list:view' WHERE id = 20101;
UPDATE sys_menu SET menu_name = '手机卡新增', perm_code = 'phone:list:add' WHERE id = 20102;
UPDATE sys_menu SET menu_name = '手机卡编辑', perm_code = 'phone:list:edit' WHERE id = 20103;
UPDATE sys_menu SET menu_name = '手机卡删除', perm_code = 'phone:list:delete' WHERE id = 20104;

UPDATE sys_menu SET sort_order = 2 WHERE id = 203;
UPDATE sys_menu SET sort_order = 3 WHERE id = 204;

DELETE FROM sys_role_menu WHERE menu_id IN (202, 20201, 20202, 20203, 20204);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (201, 20101, 20102, 20103, 20104);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE id IN (201, 20101, 20102, 20103, 20104);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE id = 201;

-- 套餐字段已从页面和接口维护逻辑中取消。
-- 如确认历史数据不再需要，可手动执行以下语句删除数据库字段：
-- ALTER TABLE phone_card DROP COLUMN package;
