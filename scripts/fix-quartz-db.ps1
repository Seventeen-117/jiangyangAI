# Quartz 数据库修复脚本
# 用于检查和修复 Quartz 相关数据库表

param(
    [Parameter(Mandatory = $true)] [string]$DbUrl,
    [Parameter(Mandatory = $true)] [string]$DbUser,
    [Parameter(Mandatory = $true)] [string]$DbPassword,
    [Parameter(Mandatory = $false)] [string]$SchedulerName = "msScheduler"
)

# 数据库连接测试
function Test-DatabaseConnection {
    param($Url, $User, $Password)
    
    try {
        $connectionString = "${Url};user=${User};password=${Password}"
        $connection = New-Object System.Data.Odbc.OdbcConnection($connectionString)
        $connection.Open()
        $connection.Close()
        return $true
    }
    catch {
        Write-Error "数据库连接失败: $($_.Exception.Message)"
        return $false
    }
}

# 检查表是否存在
function Test-QuartzTables {
    param($Url, $User, $Password, $SchedulerName)
    
    $connectionString = "${Url};user=${User};password=${Password}"
    $connection = New-Object System.Data.Odbc.OdbcConnection($connectionString)
    $connection.Open()
    
    $tables = @(
        "QRTZ_SCHEDULER_STATE",
        "QRTZ_TRIGGERS", 
        "QRTZ_JOB_DETAILS",
        "QRTZ_SIMPLE_TRIGGERS",
        "QRTZ_CRON_TRIGGERS",
        "QRTZ_BLOB_TRIGGERS",
        "QRTZ_CALENDARS",
        "QRTZ_PAUSED_TRIGGER_GRPS",
        "QRTZ_LOCKS",
        "QRTZ_FIRED_TRIGGERS"
    )
    
    $missingTables = @()
    
    foreach ($table in $tables) {
        try {
            $command = $connection.CreateCommand()
            $command.CommandText = "SELECT COUNT(*) FROM $table WHERE SCHED_NAME = ?"
            $command.Parameters.Add("@schedName", [System.Data.Odbc.OdbcType]::VarChar, 120).Value = $SchedulerName
            $command.ExecuteScalar() | Out-Null
            Write-Host "✓ 表 $table 存在且可访问"
        }
        catch {
            Write-Warning "✗ 表 $table 不存在或无权限: $($_.Exception.Message)"
            $missingTables += $table
        }
    }
    
    $connection.Close()
    return $missingTables
}

# 执行 SQL 脚本
function Invoke-SqlScript {
    param($Url, $User, $Password, $ScriptPath)
    
    try {
        $connectionString = "${Url};user=${User};password=${Password}"
        $connection = New-Object System.Data.Odbc.OdbcConnection($connectionString)
        $connection.Open()
        
        $script = Get-Content $ScriptPath -Raw
        $statements = $script -split ";" | Where-Object { $_.Trim() -ne "" }
        
        foreach ($statement in $statements) {
            if ($statement.Trim() -ne "") {
                try {
                    $command = $connection.CreateCommand()
                    $command.CommandText = $statement.Trim()
                    $command.ExecuteNonQuery() | Out-Null
                    Write-Host "✓ 执行成功: $($statement.Substring(0, [Math]::Min(50, $statement.Length)))..."
                }
                catch {
                    Write-Warning "⚠ 跳过语句: $($_.Exception.Message)"
                }
            }
        }
        
        $connection.Close()
        return $true
    }
    catch {
        Write-Error "执行 SQL 脚本失败: $($_.Exception.Message)"
        return $false
    }
}

# 主执行流程
Write-Host "=== Quartz 数据库修复工具 ===" -ForegroundColor Green

# 1. 测试数据库连接
Write-Host "`n1. 测试数据库连接..." -ForegroundColor Yellow
if (-not (Test-DatabaseConnection -Url $DbUrl -User $DbUser -Password $DbPassword)) {
    exit 1
}

# 2. 检查现有表
Write-Host "`n2. 检查 Quartz 表状态..." -ForegroundColor Yellow
$missingTables = Test-QuartzTables -Url $DbUrl -User $DbUser -Password $DbPassword -SchedulerName $SchedulerName

if ($missingTables.Count -eq 0) {
    Write-Host "`n✓ 所有 Quartz 表都存在且可访问" -ForegroundColor Green
    exit 0
}

# 3. 执行修复脚本
Write-Host "`n3. 执行表结构修复..." -ForegroundColor Yellow
$scriptPath = Join-Path $PSScriptRoot "..\quartz-tables.sql"
if (-not (Test-Path $scriptPath)) {
    Write-Error "找不到 SQL 脚本: $scriptPath"
    exit 1
}

if (Invoke-SqlScript -Url $DbUrl -User $DbUser -Password $DbPassword -ScriptPath $scriptPath) {
    Write-Host "`n✓ 表结构修复完成" -ForegroundColor Green
} else {
    Write-Error "`n✗ 表结构修复失败"
    exit 1
}

# 4. 再次验证
Write-Host "`n4. 验证修复结果..." -ForegroundColor Yellow
$remainingMissing = Test-QuartzTables -Url $DbUrl -User $DbUser -Password $DbPassword -SchedulerName $SchedulerName

if ($remainingMissing.Count -eq 0) {
    Write-Host "`n✓ 修复成功！所有 Quartz 表现在都可正常访问" -ForegroundColor Green
} else {
    Write-Warning "`n⚠ 仍有 $($remainingMissing.Count) 个表存在问题: $($remainingMissing -join ', ')"
    Write-Host "请检查数据库用户权限或手动执行 SQL 脚本" -ForegroundColor Yellow
}
