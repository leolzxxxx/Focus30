## 准备阶段
下载安装Launch4j(https://pan.quark.cn/s/4d11acae2c5f#/list/share)

---

## 运行项目
1. IDEA中先点击Build Artifacts。
2. 运行Launch4j，可直接导入项目根目录下的Focus30.xml文件。
3. 点击Build Wrapper生成Focus30.exe可执行文件。
4. 如需开机自启动，`Win+R` 后输入 `shell:startup`，把 `Focus30.exe` 复制快捷方式放入该目录下即可。

---




## 用于后续在Gitlab中发布exe文件的操作

### 创建Release示例
```
curl --header "Content-Type: application/json" \
--header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" \
--data '{"name": "Focus30", "tag_name": "v1.0.0", "ref": "master", "description": "Focus30首次版本发布"}' \
--request POST "http://10.0.2.250/api/v4/projects/311/releases"
```

### 删除Release示例
```
curl --request DELETE --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" "http://10.0.2.250/api/v4/projects/311/releases/v1.0.0"
```

### 添加Asset链接示例
```
curl --request POST --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" \
--data "name=Focus30.exe" \
--data "url=https://yearling-aquamarine-wkzo77heh4.edgeone.app/Focus30.exe" \
"http://10.0.2.250/api/v4/projects/311/releases/v1.0.0/assets/links"
```

### 删除Asset链接示例
```
curl --request DELETE --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" "http://10.0.2.250/api/v4/projects/311/releases/v1.0.0/assets/links/1"
```

### 也可在gitlab中直接操作，先创建Tags，再创建releases，更简单，推荐