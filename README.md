# 天天爱K歌

本项目为上海交通大学软件工程专业**SE123-软件工程原理与实践**大作业。关于项目的具体需求、实现细节，请参考[项目文档](https://github.com/Gun9niR/Karaoke/tree/master/FinalRelease/docs)。

# 1. 开发者

- [Gun9niR](https://github.com/Gun9niR/Karaoke)
- [xx01cyx](https://www.youtube.com/)
- [jyz-1201](https://github.com/jyz-1201)

# 2. 目录结构

```
.
├── FinalRelease      // 第三次迭代文档及软件最终代码
│   ├── code          // 软件最终版代码
│   └── docs          // 软件最终版文档
├── TechPrototype     // 第二次技术原型迭代文档
├── UIPrototype       // 第一次界面原型迭代文档
├── .gitignore        // gitignore文件
└── README.md         // 本README文件
```

# 3. 安装方法

本项目并未购买云服务器，因此管理员系统后端需要部署在自己的计算机上。

1. 克隆本仓库

```
git clone https://github.com/Gun9niR/Karaoke.git
```
2. 运行管理员系统后端

```
cd FinalRelease/code/WebUpload/server
pip install -r requirements.txt
mysql.server start
python wsgi.py
```
3. 运行管理员系统前端

```
cd ../client
npm install
npm run serve
```
4. 安装手机端APP

首先，需要修改服务器地址为本机地址。


# 4. git提交prefix规范

- 新增功能: `feat`
- 修复bug：`fix`
- 更新文档：`docs`
- 微调：`tweak`
