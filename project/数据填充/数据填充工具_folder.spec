# -*- mode: python ; coding: utf-8 -*-
"""
数据填充工具 - PyInstaller 配置文件（文件夹模式）
打包成包含 exe 和依赖的文件夹
"""

block_cipher = None

a = Analysis(
    ['数据填充工具_exe.py'],
    pathex=[],
    binaries=[],
    datas=[
        ('templates', 'templates'),
        ('static', 'static'),
        ('suppliers_config.json', '.'),
        ('数据填充工具.py', '.'),
    ],
    hiddenimports=[
        'openpyxl',
        'openpyxl.cell._writer',
        'pandas',
        'pandas._libs.tslibs.np_datetime',
        'pandas._libs.tslibs.nattype',
        'pandas._libs.tslibs.offsets',
        'pandas._libs.tslibs.timestamps',
        'msoffcrypto',
        'flask',
        'werkzeug',
        'jinja2',
        'werkzeug.serving',
        'openpyxl.styles',
        'openpyxl.utils',
        'openpyxl.utils.cell',
    ],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[
        'tkinter',
        'matplotlib',
        'numpy.tests',
        'scipy',
    ],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    [],
    exclude_binaries=True,
    name='数据填充工具',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    console=True,  # 显示控制台
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=None,
)

coll = COLLECT(
    exe,
    a.binaries,
    a.zipfiles,
    a.datas,
    strip=False,
    upx=True,
    upx_exclude=[],
    name='数据填充工具',
)
