#!/usr/bin/env bash
set -euo pipefail

# Quartz 数据库修复脚本 (Linux/macOS)
# 用于检查和修复 Quartz 相关数据库表

DB_URL="${DB_URL:-}"
DB_USER="${DB_USER:-}"
DB_PASSWORD="${DB_PASSWORD:-}"
SCHEDULER_NAME="${SCHEDULER_NAME:-msScheduler}"

if [[ -z "$DB_URL" || -z "$DB_USER" || -z "$DB_PASSWORD" ]]; then
  echo "用法: DB_URL=... DB_USER=... DB_PASSWORD=... $0" >&2
  echo "示例: DB_URL=jdbc:hsqldb:mem:testdb DB_USER=sa DB_PASSWORD= $0" >&2
  exit 1
fi

echo "=== Quartz 数据库修复工具 ==="

# 检查必要的工具
if ! command -v sqlcmd >/dev/null 2>&1 && ! command -v mysql >/dev/null 2>&1 && ! command -v psql >/dev/null 2>&1; then
  echo "错误: 需要安装数据库客户端工具 (sqlcmd/mysql/psql)" >&2
  exit 1
fi

# 检测数据库类型
DB_TYPE=""
if [[ "$DB_URL" == *"hsqldb"* ]]; then
  DB_TYPE="hsqldb"
elif [[ "$DB_URL" == *"mysql"* ]]; then
  DB_TYPE="mysql"
elif [[ "$DB_URL" == *"postgresql"* ]]; then
  DB_TYPE="postgresql"
else
  echo "警告: 无法识别数据库类型，尝试通用方法" >&2
  DB_TYPE="generic"
fi

# 执行 SQL 语句
execute_sql() {
  local sql="$1"
  case "$DB_TYPE" in
    "hsqldb")
      echo "$sql" | java -cp "$(find ~/.m2/repository -name "hsqldb*.jar" | head -1)" org.hsqldb.util.SqlTool --inlineRc=url=$DB_URL,user=$DB_USER,password=$DB_PASSWORD
      ;;
    "mysql")
      echo "$sql" | mysql -h localhost -u "$DB_USER" -p"$DB_PASSWORD" --database="${DB_URL##*/}"
      ;;
    "postgresql")
      echo "$sql" | psql "$DB_URL" -U "$DB_USER"
      ;;
    *)
      echo "请手动执行以下 SQL:" >&2
      echo "$sql" >&2
      ;;
  esac
}

# 检查表是否存在
check_table() {
  local table="$1"
  local check_sql="SELECT COUNT(*) FROM $table WHERE SCHED_NAME = '$SCHEDULER_NAME' LIMIT 1;"
  
  if execute_sql "$check_sql" >/dev/null 2>&1; then
    echo "✓ 表 $table 存在且可访问"
    return 0
  else
    echo "✗ 表 $table 不存在或无权限"
    return 1
  fi
}

# 主执行流程
echo "1. 检查 Quartz 表状态..."

TABLES=(
  "QRTZ_SCHEDULER_STATE"
  "QRTZ_TRIGGERS"
  "QRTZ_JOB_DETAILS"
  "QRTZ_SIMPLE_TRIGGERS"
  "QRTZ_CRON_TRIGGERS"
  "QRTZ_BLOB_TRIGGERS"
  "QRTZ_CALENDARS"
  "QRTZ_PAUSED_TRIGGER_GRPS"
  "QRTZ_LOCKS"
  "QRTZ_FIRED_TRIGGERS"
)

MISSING_TABLES=()
for table in "${TABLES[@]}"; do
  if ! check_table "$table"; then
    MISSING_TABLES+=("$table")
  fi
done

if [[ ${#MISSING_TABLES[@]} -eq 0 ]]; then
  echo "✓ 所有 Quartz 表都存在且可访问"
  exit 0
fi

echo "2. 执行表结构修复..."

SCRIPT_PATH="$(dirname "$0")/../quartz-tables.sql"
if [[ ! -f "$SCRIPT_PATH" ]]; then
  echo "错误: 找不到 SQL 脚本: $SCRIPT_PATH" >&2
  exit 1
fi

if execute_sql "$(cat "$SCRIPT_PATH")"; then
  echo "✓ 表结构修复完成"
else
  echo "✗ 表结构修复失败" >&2
  exit 1
fi

echo "3. 验证修复结果..."
REMAINING_MISSING=()
for table in "${TABLES[@]}"; do
  if ! check_table "$table"; then
    REMAINING_MISSING+=("$table")
  fi
done

if [[ ${#REMAINING_MISSING[@]} -eq 0 ]]; then
  echo "✓ 修复成功！所有 Quartz 表现在都可正常访问"
else
  echo "⚠ 仍有 ${#REMAINING_MISSING[@]} 个表存在问题: ${REMAINING_MISSING[*]}"
  echo "请检查数据库用户权限或手动执行 SQL 脚本"
  exit 1
fi
