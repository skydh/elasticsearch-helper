# elasticsearch-helper
这是几个基于springboot封装es增删改查的工具类。

第一种方案
       <dependency>
			<groupId>elasticsearch-helper</groupId>
			<artifactId>elasticsearch-helper-001</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
第二种方案
<dependency>
			<groupId>elasticsearch-helper</groupId>
			<artifactId>elasticsearch-helper-002</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>


配置文件添加如下

# redis config
spring.redis.database=0
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=
spring.redis.pool.max-active=8
spring.redis.pool.max-wait=-1
spring.redis.pool.max-idle=8
spring.redis.pool.min-idle=0
spring.redis.pool.timeout=0

elasticsearch.ip=127.0.0.1   这个是你的es集群地址
elasticsearch.port=9300        这个是你的服务器端口号，默认为9300
elasticsearch.cluster.name=elasticsearch  这个是你的es集群的名字，如果没有修改，默认为elasticsearch  

elasticsearch.pool=1   配置多少个连接池，默认为一个，性能需要优化在改进

elasticsearch.scanPackage=com.pingan.haofang  包扫描路径

elasticsearch.isScan=no  是否扫描
elasticsearch.limitSize=100000 每个表最多一次导入多少数据，看你服务器分配。
elasticsearch.logfile=eslog  日志相对路径
elasticsearch.months=2   保留多少个月的日志




2.给需要加入es的数据建立vo。
EsClass
该注解包括2个信息，esIndex是es的index和type, tableName是mysql对应的table名。
EsField
该注解包含2个信息，esField是es对应的列名，mysqlField是mysql对应的列名
EsId
该注解包含一个信息。我们想要哪个字段作为es的主键。
Demo如下。

 



添加扫描es的类，加入到spring容器里面
@ComponentScan(basePackages = {"elasticsearch,com.pingan.haofang"})


后面对其增删改都做了封装

在新增的service方法上面加上@EsAdd
同时必须返回已保存的vo,该vo为加了上述条件的vo


在删除的service方法上面加上, @EsDelete
 约定大于配置，我们要求被删除注解的方法，传递的参数必须是对应vo,我们前置删除,第一个参数必须是对应vo


在修改的service方法上面加上@EsUpdate
同时必须返回已保存的vo,该vo为加了上述条件的vo


如此开发将极大的方便使用es去查询，而不需要关心其繁琐的增删改，以及数据初始化


数据增量丢失，第一种方案。
我们用spring事务来保证其一致性。Mysql执行失败，是不会到es缓存那一步，没问题。
Mysql 执行成功，es缓存执行失败。我们就直接抛出运行时异常，让mysql直接回滚。从而保证数据强一致性。




数据增量丢失，第二种方案。

数据失败先打入到本地日志，再打入到redis的list里面。
List 实时消费消息。
同时采用本地日志将在夜晚12点重新刷数据来保证数据最终强一致性。
因为是最终覆盖操作，可能有的数据已经插入，或者已经删除，对于这种异常不予处理，但是对于其他异常，如网络抖动，尝试3次后.。。。依旧不行，则将剩余数据按顺序加入到新的文档里面（为了保证消息的顺序执行消费。）都要加入到，新的日志文档里面。同时定时器每月1号删除2个月的信息 
