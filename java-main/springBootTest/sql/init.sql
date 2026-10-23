-- ============================================================================
-- 第五人格·祭司主题 注册功能 - 建库建表脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 说明：本文件为建库建表的文本留存，可直接在 MySQL 客户端执行。
-- ============================================================================

-- 1. 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS `idv_auth`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 2. 使用数据库
USE `idv_auth`;

-- 3. 创建用户表（若已存在则跳过）。注意：表名不使用 user（MySQL 保留字），改用 t_user
CREATE TABLE IF NOT EXISTS `t_user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nickname`   VARCHAR(50)  NOT NULL                COMMENT '昵称',
    `password`   VARCHAR(100) NOT NULL                COMMENT '密码（BCrypt 哈希存储）',
    `status`     TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0 正常，1 禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  COMMENT = '用户表';

-- 3.1 为用户表增加角色字段（幂等：已存在则跳过）。USER 普通用户，ADMIN 管理员
SET @exist_role := (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 't_user' AND column_name = 'role');
SET @sql_role := IF(@exist_role = 0,
  "ALTER TABLE `t_user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER 普通用户，ADMIN 管理员'",
  'SELECT 1');
PREPARE stmt_role FROM @sql_role;
EXECUTE stmt_role;
DEALLOCATE PREPARE stmt_role;

-- ============================================================================
-- 门户首页 & 搜索 - 分级分类与内容表
-- ============================================================================

-- 4. 分类表（支持分级：level=1 一级，level=2 二级）
CREATE TABLE IF NOT EXISTS `t_category` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`  BIGINT       NOT NULL DEFAULT 0      COMMENT '父级 id；一级分类为 0',
    `name`       VARCHAR(50)  NOT NULL                COMMENT '分类名称',
    `code`       VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '分类编码（英文）',
    `level`      TINYINT      NOT NULL DEFAULT 1      COMMENT '层级：1 一级，2 二级',
    `sort`       INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  COMMENT = '内容分类表';

-- 5. 内容表（角色图鉴 / 资讯 / 攻略 / 赛事 / 社区 / 商城 统一存储）
CREATE TABLE IF NOT EXISTS `t_content` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '叶子（二级）分类 id',
    `top_category_id` BIGINT       NOT NULL DEFAULT 0      COMMENT '一级分类 id（冗余，便于按大类检索）',
    `title`           VARCHAR(120) NOT NULL                COMMENT '标题',
    `summary`         VARCHAR(255) NOT NULL DEFAULT ''      COMMENT '摘要',
    `cover_url`       VARCHAR(255) DEFAULT NULL            COMMENT '封面图 URL',
    `content`         TEXT                                COMMENT '正文',
    `author`          VARCHAR(80)  DEFAULT NULL            COMMENT '作者 / 来源',
    `tags`            VARCHAR(255) DEFAULT NULL            COMMENT '标签（逗号分隔）',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0 下架/草稿，1 已发布',
    `view_count`      INT          NOT NULL DEFAULT 0      COMMENT '浏览量',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_top_category` (`top_category_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_status_created` (`status`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  COMMENT = '门户内容表';

-- 6. 种子数据：分类（一级 + 二级）。INSERT IGNORE 保证脚本可重复执行。
INSERT IGNORE INTO `t_category` (`id`, `parent_id`, `name`, `code`, `level`, `sort`) VALUES
(1, 0, '角色图鉴', 'character', 1, 1),
(2, 0, '资讯公告', 'news',      1, 2),
(3, 0, '玩法攻略', 'guide',     1, 3),
(4, 0, '赛事中心', 'esports',   1, 4),
(5, 0, '玩家社区', 'community', 1, 5),
(6, 0, '周边商城', 'shop',      1, 6),
(7, 1, '监管者',   'supervisor', 2, 1),
(8, 1, '求生者',   'survivor',  2, 2),
(9, 1, '联动角色', 'collab',     2, 3),
(10, 2, '版本更新', 'version',   2, 1),
(11, 2, '活动公告', 'activity',  2, 2),
(12, 2, '官方动态', 'official',  2, 3),
(13, 3, '地图机制', 'map',       2, 1),
(14, 3, '角色技巧', 'skill',     2, 2),
(15, 3, '高阶打法', 'advanced',  2, 3),
(16, 4, '职业联赛', 'pro-league',2, 1),
(17, 4, '杯赛',     'cup',       2, 2),
(18, 4, '全民赛事', 'open',      2, 3),
(19, 5, '同人二创', 'fanart',    2, 1),
(20, 5, '组队招募', 'recruit',   2, 2),
(21, 5, '攻略分享', 'share',     2, 3),
(22, 6, '手办模型', 'figure',    2, 1),
(23, 6, '数字礼包', 'pack',      2, 2),
(24, 6, '赛季通行证', 'pass',    2, 3);

-- 7. 种子数据：内容（status=1 已发布）。INSERT IGNORE 保证脚本可重复执行。
INSERT IGNORE INTO `t_content`
  (`id`, `category_id`, `top_category_id`, `title`, `summary`, `author`, `tags`, `status`, `view_count`, `created_at`) VALUES
(1,  7,  1, '红蝶·美智子', '监管者，蝶影翩跹的祭祀之女。', '官方', '监管者,红蝶,技巧', 1, 1280, '2026-09-01 10:00:00'),
(2,  8,  1, '祭司·菲欧娜', '求生者，能开启连接两地的通道。', '官方', '求生者,祭司,通道', 1, 1530, '2026-09-03 10:00:00'),
(3,  7,  1, '杰克', '监管者，优雅而致命的绅士。', '官方', '监管者,杰克', 1, 980, '2026-09-05 10:00:00'),
(4,  9,  1, '第五人格 × 联动企划', '全新跨界联动角色即将登场。', '官方', '联动,活动', 1, 2210, '2026-09-10 10:00:00'),
(5,  10, 2, '版本更新公告 12.0', '新增地图「永眠镇」与平衡性调整。', '官方', '版本,更新', 1, 3400, '2026-09-12 10:00:00'),
(6,  11, 2, '限时活动：深渊回响', '参与活动赢取专属时装。', '官方', '活动,时装', 1, 1870, '2026-09-15 10:00:00'),
(7,  14, 3, '祭司通道进阶技巧', '如何利用通道实现极限转点。', '玩家-夜航', '祭司,技巧', 1, 760, '2026-09-18 10:00:00'),
(8,  15, 3, '红教堂速修流打法', '求生者四人速修通关思路。', '玩家-阿白', '速修,打法', 1, 1120, '2026-09-20 10:00:00'),
(9,  13, 3, '永眠镇地图机制解析', '新地图关键点位与转点路线。', '官方', '地图,永眠镇', 1, 1340, '2026-09-22 10:00:00'),
(10, 16, 4, 'IVL 夏季赛总决赛', '顶尖战队争夺赛季冠军。', '赛事组', '赛事,IVL', 1, 5600, '2026-09-25 10:00:00'),
(11, 18, 4, '深渊的呼唤 VI 海选', '全民赛事海选正式开启。', '赛事组', '赛事,海选', 1, 2980, '2026-10-01 10:00:00'),
(12, 19, 5, '同人创作大赛征稿', '投稿你的专属第五人格二创。', '社区', '同人,活动', 1, 640, '2026-10-03 10:00:00'),
(13, 20, 5, '组队开黑招募帖', '寻找稳定的排位队友。', '玩家-小岄', '组队,招募', 1, 410, '2026-10-06 10:00:00'),
(14, 22, 6, '祭司手办预售', '高精度雕像限量发售。', '周边商城', '周边,手办', 1, 880, '2026-10-08 10:00:00'),
(15, 24, 6, '赛季通行证上架', '解锁专属时装与奖励。', '周边商城', '通行证,礼包', 1, 1520, '2026-10-10 10:00:00');

-- ============================================================================
-- 附：仅当数据库表是「旧版（含 phone 字段）」时，才需要执行下面的升级语句，
--     用于移除手机号字段与唯一索引（注册不再需要手机号与短信验证码）。
--     全新初始化无需执行。
-- ============================================================================
-- ALTER TABLE `t_user` DROP INDEX `uk_phone`;
-- ALTER TABLE `t_user` DROP COLUMN `phone`;
