## 慕课网秒杀项目

### 创建工程
1. 使用maven创建工程
```
mvn archetype:generate -DgroupId=org.seckill -DartifactId=seckill -DarchetypeArtifactId=maven-archetype-webapp -DarchetypeCatalog=local
```
2. 完善目录


##### 说明

###### dto 与 entity区别

DTO数据传输层：用于Web层和Service层之间传递的数据封装。

entity：用于业务数据的封装，比如数据库中的数据。




