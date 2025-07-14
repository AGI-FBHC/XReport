FROM python:3.10

# 设置工作目录
WORKDIR /app

# 复制依赖文件（单独复制可利用Docker缓存）
COPY requirements.txt .

# 全局配置阿里云镜像（同时作用于pip upgrade和依赖安装）
RUN pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/ && \
    pip config set global.trusted-host mirrors.aliyun.com && \
    # 升级pip时也使用阿里云镜像，大幅降低超时概率
    pip install --upgrade pip --timeout 300 --retries 10 && \
    # 安装依赖，保持无缓存以减小镜像体积
    pip install --no-cache-dir -r requirements.txt --timeout 300 --retries 10

# 复制项目文件（放在依赖安装后，避免代码修改触发依赖重装）
COPY . /app

# 暴露端口
EXPOSE 6024

# 启动命令
CMD ["python", "main.py"]