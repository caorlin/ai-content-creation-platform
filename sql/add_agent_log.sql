-- 智能体执行日志表
create table if not exists agent_log
(
    id              bigint auto_increment comment 'id' primary key,
    task_id          varchar(64)                        not null comment '任务ID',
    agent_name       varchar(50)                        not null comment '智能体名称',
    start_time       datetime                           not null comment '开始时间',
    end_time         datetime                           null comment '结束时间',
    duration_ms      int                                null comment '耗时（毫秒）',
    status          varchar(20)                        not null comment '状态：SUCCESS/FAILED',
    error_message    text                               null comment '错误信息',
    prompt          text                               null comment '使用的Prompt',
    input_data       json                               null comment '输入数据（JSON格式）',
    output_data      json                               null comment '输出数据（JSON格式）',
    create_time      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint     default 0              not null comment '是否删除',
    INDEX idx_task_id (task_id),
    INDEX idx_agent_name (agent_name),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
    ) comment '智能体执行日志表' collate = utf8mb4_unicode_ci;


