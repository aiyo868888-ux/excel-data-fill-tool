from setuptools import setup, find_packages

setup(
    name="smart-todo-parser",
    version="0.1.0",
    description="智能待办事项解析器 - 支持中文自然语言时间和任务提取",
    author="Your Name",
    packages=find_packages(),
    install_requires=[
        "python-dateutil>=2.8.2",
        "regex>=2023.0.0",
        "pytz>=2023.3",
    ],
    extras_require={
        "dev": [
            "pytest>=7.4.0",
            "pytest-cov>=4.1.0",
        ]
    },
    python_requires=">=3.8",
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
    ],
)
