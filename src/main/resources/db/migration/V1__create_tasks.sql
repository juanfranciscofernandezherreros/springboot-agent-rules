CREATE TABLE tasks (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NULL,
    completed BIT NOT NULL CONSTRAINT df_tasks_completed DEFAULT 0,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_tasks_created_at DEFAULT SYSUTCDATETIME()
);

CREATE INDEX idx_tasks_title ON tasks (title);
CREATE INDEX idx_tasks_completed ON tasks (completed);
