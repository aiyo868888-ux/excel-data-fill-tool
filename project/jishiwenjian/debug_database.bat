@echo off
echo =====================================
echo 即时剪贴板 - 数据库调试
echo =====================================
echo.

set ADB="C:\Users\15085\AppData\Local\Android\Sdk\platform-tools\adb.exe"

echo [1] 检查剪贴板记录...
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT id, substr(content, 1, 30) as content_preview, createdAt FROM clipboard_entities ORDER BY createdAt DESC LIMIT 10;\"'"

echo.
echo [2] 检查标签定义...
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT id, name, color, parentId, level FROM tag_definitions;\"'"

echo.
echo [3] 检查标签关联...
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT cr.id, cr.clipboardId, cr.tagDefinitionId, td.name FROM clipboard_tag_relations cr LEFT JOIN tag_definitions td ON cr.tagDefinitionId = td.id ORDER BY cr.clipboardId DESC LIMIT 10;\"'"

echo.
echo [4] 统计数据...
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT COUNT(*) as clipboard_count FROM clipboard_entities;\"'"
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT COUNT(*) as tag_count FROM tag_definitions;\"'"
%ADB% shell "su -c 'sqlite3 /data/data/com.jishi.clipboard/databases/jishi_clipboard.db \"SELECT COUNT(*) as relation_count FROM clipboard_tag_relations;\"'"

echo.
pause
