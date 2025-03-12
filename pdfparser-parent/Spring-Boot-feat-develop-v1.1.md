版本说明
====
| 版本 | 更新日期 | 说明 | 修订人 | 备注 | 
|---	| :---	| :---	| :---| :---
| 1.0	| 20180711 | 创建文档 | 三文鱼 | 用于架构改造参考
| 1.1	| 20180802 | 添加aws服务支持 | 三文鱼 | 支持S3\kinesis\dynamoDB
| 1.2	| 20180828 | 调整redis,db配置 | 三文鱼 | kid方案
| 1.3	| 20180901 | 配置中心lastVersion | 三文鱼 | spring-cloud-config-server登记deployid&lastVersion
| 1.4	| 20180930 | 编译说明 | 三文鱼 | docker打包方案



目录
====
```
Spring Boot简介
项目改造日志
相关系统版本说明
关键字说明
改造步骤
功能组件使用
备注
```


---
---
---


Spring Boot简介
====
官方文档地址：
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-documentation

```
文档中有特意提到Spring Boot能够极大地提高开发效率，是因为Spring Boot强调“约定大于配置”！这一点可以理解为，采用Spring Boot约定的整合方式，会比采用xml配置更高效，主要体现在：
（1）更少的代码，尤其是避免了编写繁琐的xml配置
（2）更统一的集成，即该配置可以升级到公共组件，由各个系统直接依赖使用
```

---
---
---


项目改造日志
====
| 里程碑| 时间 | 发起者 | 执行者 | 备注 | 
|---	| :---	| :---	| :---| :---
| 初版	| 201803 | 官爷 | 余莎 | 基于SpringBoot1.5.9
| 升级SB	| 201805 | 官爷 | 三文鱼 | 升级到2.0.2，同时升级SpringCloud
| 试行系统上线	| 201807 | |SB项目组 | 
| 扩大试行、推广	| 201808 |  | SB项目组 | 
| 添加aws服务组件	| 201808 |  | SB项目组 | 
| 调整redis,db配置| 201808 |  | SB项目组 | 
| 添加redis-session组件| 201808 |  | SB项目组 | 
| 配置中心登记lastVersion| 201809 |  | SB项目组 | 


---
---
---




相关系统版本说明
====

1.1 各后台系统版本
	  	
```
由于4.x及此前的版本都用于业务开发，本次系统架构调整，改动范围较大，建议直接升级到5.0.0。指令如下：
mvn versions:set -DnewVersion=5.0.0-SNAPSHOT
mvn versions:update-child-modules
mvn versions:commit
（此外，建议按照gitlab flow规范，创建分支：develop-spring-boot-framework）
```

1.2 roshan、kael、tiny工程与版本
	  	
```
· roshan作为父级依赖，功能包括：
（1）管理dependencyManagement
（2）提供一系列starter功能组件
```
```
· kael作为功能模块集合，封装了多个功能，供后台系统使用。
```
```
· tiny是最顶层的dependencyManagement，仅作为roshan、kael的依赖，并向下影响着所有的子系统。
```


---
---
---


关键字说明
====
```
applicationName: 即serviceName、系统简称，同时会进一步用于配置中心目录、监控等等
```


---
---
---

改造步骤
====

###【前言】
```
本次架构改造，除了升级使用Spring Boot框架之外，同时采用了更加简便的方式集成了checkstyle等控件。

本文档中提及到的功能修改，都会尽量在demo工程-templateProject呈现，该demo工程地址为：
https://code.xhqb.io/xhgroup/templateProject
(分支：dev-spring-boot-framework)

此外，该demo工程在配置中心也有相应的配置，使其能够完好运行。其配置文件地址为：
https://code.xhqb.io/configuration/service-config2/blob/master/templateproject

下载templateProject后，可以直接启动。另外，也可以考虑添加jvm参数（替换缺省值）：-Dserver.port=8080 -Dcontext.path=/t -Dkael.application.env=dev -Dkael.application.stack=dev

目前templateProject系统已整合了redis、多数据源(非单数据源)、elasticjob、JMS组件，有设计DemoController作为业务入口。该工程也作为demo系统提供给后台开发同事使用。

下面是关于Spring Boot的步骤说明，请仔细阅读。
```


###【约定applicationName】
```
applicationName（我们也有称为serviceName），是系统的简称，会使用在pom.mane、配置中心目录、发布脚本，同时也用于技术人员之间（主要是开发与运维之间）的日常交流、发布约定
```

###【修改依赖】

```
1. 对于parent pom，声明的parent修改为
<parent>
     <artifactId>roshan-starter-parent</artifactId>
     <groupId>com.xhqb.roshan</groupId>
     <version>5.0.0-SNAPSHOT</version>
     <relativePath/>
</parent>

2. 对于打包模块的pom，需要跟进：
  （1）修改packaging，由war改成jar
  （2）在底部添加plugin如下：
      <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>


```   

###【移除依赖】

```
1. 由于roshan500有依赖kael组件，因此对于系统中打包模块的pom(如app-biz-service-impl)，可移除原声明的kael依赖，包括properties中声明的version，使系统使用roshan5.0.0内声明的kael
```


###【添加依赖】

```
（1）对于app-common-service的pom，要添加kael-util依赖（在编写公共tostring方法会用到XhJsonToStringStyle）

（2）对于app-biz-service-impl（或其他打包模块的pom）需要根据功能添加依赖：
        <!--roshan最小工程（注意：添加javassist的exclusion使避免和dubbo的发生冲突）-->
        <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-dubbo</artifactId>
        </dependency>

        <!--redis，按需依赖-->        
       <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-redis</artifactId>
        </dependency>

        <!--redis session，按需依赖-->        
       <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-redis-session</artifactId>
        </dependency>

        <!--druid，按需依赖-->
        <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-mybatis</artifactId>
        </dependency>

        <!--jms，按需依赖-->
        <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-messaging</artifactId>
        </dependency>

        <!--elasticjob，按需依赖-->
        <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-elasticjob</artifactId>
        </dependency>
        
        <!--sequence，按需依赖-->
        <dependency>
            <groupId>com.xhqb.roshan</groupId>
            <artifactId>roshan-starter-sequence</artifactId>
        </dependency> 
        
        （上述的功能组件会在下一章[功能组件使用]做详细说明）
```


###【移除Spring配置】

```
这里需要移除原先的XML配置，包括：
Aws服务
Redis
Druid，Mybatis
elasticjob等等
备注：移除之后，还需要进一步集成这些功能组件，请参考上面【添加依赖】部分，以及下一章[功能组件使用]
```

###【修改Spring配置】  

```
保留dubbo配置，需要规范一下properties的key，具体可参考templateProject工程
```


###【其他配置】

```
· 移除webapp目录（因此也顺带移除了内含的web.xml）
· 可以删除原有的properties
· 移除原logback.xml文件（由于logback组件有升级，原配置已不适用）
· 发布时若需要指定新的logback.xml文件，需要通过添加启动变量实现(by using the "logging.config" property, guided with: https://docs.spring.io/spring-boot/docs/current/reference/html/howto-logging.html )，该配置需要运维同事跟进。注意：本地调试可以不指定，此时所有日志都会打到控制台
· 关于计数metrics，原定义的ServiceCounter已不适用，需要通过注入@Autowired private ServiceCounter serviceCounter做metrics计数埋点（可参考templateproject的DemoServiceImpl.java）
· 关于计时metrics，可以通过两种方式实现：注解@Timed方式、声明Timer.Sample的方式（可参考templateproject的DemoServiceImpl.java）。其中，后者是原生的Timer的计时方式，比较适用于变化的tag；注意若使用计时metrics，可不必再使用计数metrics（因为对于promethuse来说，Timer也可以实现统计）
· 移除本地的checkstyle控件，包括：
  （1）在parent-pom中移除maven-checkstyle-plugin
  （2）移除系统根目录的checkstyle.xml文件
· 新增bootstrap.properties到resources根目录，其配置项包括：
  （1）spring.application.name=templateproject
  （2）spring.cloud.config.uri=http://10.110.33.201:8888
  （3）logging.level.org.springframework.boot.autoconfigure=DEBUG
· 对于web系统可添加过滤器SetLoggingContextFilter（可参考templateproject的WebFilterConfiguration.java）
```

###【默认的系统配置】

```
server.port=8080
context.path=/
kael.application.env=local
kael.application.stack=local
备注：
* 上述配置是系统运维相关，可通过启动变量做修改（覆盖），在正式发布时需要联系运维同事帮忙调整
* 更多的系统配置见kael-autoconfigure/resource

```

###【最终的配置目录结构（以templateproject为例）】
```
resources
├── config
│   ├── templateproject.yml			
│   └── templateproject-local.yml
├── applicationContext.xml
├── bootstrap.properties
├── dubbo-application.xml
├── dubbo-consumer.xml
└── dubbo-provider.xml

注意：上面的关键字“templateproject是demo工程（即templateProject，简介见[前言]章节）的applicationName
```


### docker打包支持
```
1.系统根目录的Jenkinsfile修改为如下：
    
    @Library('xh-build2') _
    // Jenkinsfile
    xhBuildPluginV2 {
        // skipTests = true
    }
    
2.对于需要编译成可执行包的pom，需要添加plugin如下：
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>dockerfile-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    
    同时，需要把packaging配置由war改为jar（目前暂时没有需要打成war包的Springboot工程）

```

---
---
---


功能组件使用
====
##【配置中心】

1.前言
	
	本次配置中心，是采用Spring Cloud Config重新开发的配置中心，是作为一台配置server部署到相应环境，该server还是以gitlab形式保存配置(后面简称“配置库”)。本次Spring Boot已集成Spring Cloud Config，各系统作为client端，需要配置该配置server的地址，并把系统配置做好分类，归档到本地、配置库。

2.配置的分类说明
	
	一般来说，后台系统会有一系列的配置，如db配置、redis配置、job配置、某某业务配置等等，显然，像db、redis、host这些配置，有明显的环境特点（即不同环境就应该不一样），可以视为环境配置，另外一些有着明显业务性质的配置，则是业务配置。	
	对于环境配置，建议放到gitlab配置库；对于业务配置，建议保留到本地，当然在业务配置中，是包含一些开关性质的配置，也是可以考虑放到gitlab配置库。后续配置中心会开发热配置功能，使开关配置能够实时更新。

3.配置库文件目录结构说明
	
	配置中心根目录结构：

    .
    ├── common
    │   ├── application.yaml
    │   ├── application.properties
    │   ├── application-dev.properties
    │   ├── application-test1.properties
    │   ├── application-pre-prod.properties
    │   └── application-prod.properties
    ├── templateproject
    │   ├── templateproject.properties
    │   ├── templateproject-dev.yml
    │   ├── templateproject-test1.yml
    │   ├── templateproject-pre-prod.yml
    │   └── templateproject-prod.yml
    ├── weixin-out
    │   ├── weixin-out.properties
    │   ├── weixin-out-dev.yml
    │   ├── weixin-out-test1.yml
    │   ├── weixin-out-pre-prod.yml
    │   └── weixin-out-prod.yml
    ├── ...
    ├── ...
    └── ...
    
    说明：
	common: 存放公共配置
	    - application.yml: 存放公共配置、环境配置，一般由DBA/运维跟进；
	
	[application]目录（如templateproject）：存放应用的配置，由开发者跟进
	    - [application]-[env].yml(properties)主要存放环境敏感的组件配置（如host地址、DB地址、redis等等）；
	    - [application].yml(properties)主要存放不需要因为环境不同需要另外配置的key（因此很大部分是业务配置、业务开关等等）。

	命名注意事项：
	（1）common配置文件以application开头，如：applicaiton.yml,application-xxx.yaml,application-xxx.properties;
	（2）系统所属的配置目录，其文件命名前缀与applicationName一致，整体格式为:[applicationName]-[env].yml(properties)；
	（3）建议applicationName的命名格式为：小写字母+中划线；



4.配置中心读取顺序

	本次配置中心采用Spring Cloud Config方案，在拉取远程配置时会有传参[applicationName]、[env]、[tag]，因此能够拉下的远程配置文件包括：
	common/application.properties(yml)
	common/application-[env].properties(yml)
	[applicationName]/[applicationName].properties(yml)
	[applicationName]/[applicationName]-[env].properties(yml)
	
	另外，系统也会加载本地配置：
	resource/config/[applicationName].properties(yml)
	resource/config/[applicationName]-local.properties(yml)

	整体而言，其加载配置优先级排序如下：
	（1）符合[applicationName]-[stack]格式的远程配置，如：
		[applicationName]/[applicationName]-[env].properties(yml)
		[applicationName]/[applicationName].properties(yml)
	（2）符合[applicationName]-[env]格式的远程配置，即本地的配置，如：
		resource/config/[applicationName]-local.properties(yml)
		resource/config/[applicationName].properties(yml)
	（3）远程配置库common部分，如：
		common/application.properties(yml)
		common/application-[env].properties(yml)
	注意：
	（1）该优先级是根据与[application]-[env]的符合程度而排序的，而且高优先级配置会覆盖低优先级配置。因此本地的[applicationName]-local.properties(yml)的配置有可能会被远程配置覆盖。实际应用中，建议多使用线上配置库，尽量避免本地的[applicationName]-local.properties有重复的配置！
	（2）当kael.application.stack为缺省值（默认值）时，此时远程配置库无local配置文件，不会覆盖本地的[applicationName]-local.properties(yml)
	（3）当kael.application.stack被覆盖时，其值不再是默认值（local），此时将不再读取本地的[applicationName]-local.properties(yml)
	
	
5.运维配置及运维工作
    
    目前关于系统运行资源，是交由运维管理，包括增删、运行时问题的维护，因此，资源的相关配置也应该由运维管理。
    这类资源包括：
    * 数据库（账号密码），暂命名为：数据库KID方案
    * redis（host,端口），暂命名为：redisKID方案
    
    在配置方面，运维需要添加到配置中心，需要遵循格式如下：
    ########## redis ##################
    kael.redis:
      [app1-test1]:
        hostName: redis-test1.o4r4dx.0001.cnn1.cache.amazonaws.com.cn
        port: 6379
      [app1-test2]:
        hostName: redis-test2.o4r4dx.0001.cnn1.cache.amazonaws.com.cn
        port: 6379
    ————注意：
    * 上面的app1-test1、app1-test2作为kid标识值，要映射到一组hostName+port，运维人员仅需向开发者提供这个kid即可
    * kid格式建议[appFlag]-[env]，其中appFlag可以最大限度参考applicationName，也可以进一步做精简
    
    ########## mysql ##################
    kael.datasource.druid:
      [app2-test6]:
        url: jdbc:mysql://xhtestdb01.cq5skzcvpodr.rds.cn-north-1.amazonaws.com.cn:3306/weixin?useUnicode=true&characterEncoding=utf8
        username: xhtest
        password: DuFQY5VLVjuIdUPNS7o9eoiBazmwP2IGH+iqYvxfIUeC79o6RUZcRxRvw0JLdTRyBqLgNZybLbn/de+4tpBdzw==
        connectionProperties: config.decrypt=true;config.decrypt.key=xxx
      [app2-prod]:
        url: jdbc:mysql://xhtestdb01.cq5skzcvpodr.rds.cn-north-1.amazonaws.com.cn:3306/weixin?useUnicode=true&characterEncoding=utf8
        username: xhtest
        password: DuFQY5VLVjuIdUPNS7o9eoiBazmwP2IGH+iqYvxfIUeC79o6RUZcRxRvw0JLdTRyBqLgNZybLbn/de+4tpBdzw==
        connectionProperties: config.decrypt=true;config.decrypt.key=xxx
    ————注意：上面的app2-test6、app2-prod作为kid标识值，要映射到一组数据库账号密码（密码要采用'动物园'加密方案），运维人员仅需向开发者提供数据库的kid即可
    
    以上示例可参考：/common/application.yaml	



##【druid数据源】

###单数据源
1.添加依赖：

	<dependency>
        <groupId>com.xhqb.roshan</groupId>
        <artifactId>roshan-starter-mybatis</artifactId>
    </dependency>

2.添加数据源的configuration类，并通过继承AbstractDataSourceConfiguration来实现：

    //（下面以templateproject的DruidDataSourceConfiguration.java为例）
    @Configuration
    @ConditionalOnProperty(name = "spring.datasource.druid.kid")
    @EnableTransactionManagement
    @MapperScan(basePackages = "com.xhqb.templateproject.common.dal.weixin01.dao",
            sqlSessionTemplateRef = "weixin01SqlSessionTemplate")
    public static class Weixin01DataSourceConfiguration extends AbstractDataSourceConfiguration {

        private static final String MAPPER_LOCATION_PATH =
                "classpath:com/xhqb/templateproject/common/dal/mapper/weixin01/*.xml";

        /**
         * constructor.
         */
        protected Weixin01DataSourceConfiguration(
                @Value("${spring.datasource.druid.kid}") String kid, Environment environment) {
            super(kid, environment, MAPPER_LOCATION_PATH);
        }

        @Resource(name = "weixin01DataSource")
        private DruidDataSource dataSource;

        @Override
        @Primary
        @Bean(initMethod = "init", name = "weixin01DataSource")
        public DruidDataSource dataSource() {
            return createDataSource();
        }

        @Override
        @Primary
        @Bean(name = "transactionTemplate")
        public TransactionTemplate transactionTemplate() {
            return createTransactionTemplate(dataSource);
        }

        @Override
        @Primary
        @Bean(name = "weixin01SqlSessionTemplate")
        public SqlSessionTemplate sqlSessionTemplate() throws Exception {
            return createSqlSessionTemplate(dataSource);
        }
    }
    
	通过AbstractDataSourceConfiguration对相应的声明代码的实现，其Condig子类的代码大大减少。
	这里需要注意：对于主库（或者单数据源）必须添加注解@Primary
	
	该配置方案支持配置多数据源，具体做法为：
	(1) 添加另一组kid，此时需要在kid前面添加标识，如spring.datasource.druid.weixin02.kid=weixin02
	(2) 视情况配置weixin02数据源连接数（覆盖缺省值，参见下面步骤）
	(3) 在数据源的configuration类添加weixin02数据源的声明代码，注意从库不添加注解@Primary、不需要声明transactionTemplate
    
    提示：templateproject的DruidDataSourceConfiguration.java里面有做多数据源配置，可参考使用

3.在系统本地[application]配置mybatis参数：
	
	spring:
      datasource:
        druid:
          kid: "weixin01-test1"
	
	注意：该kid由运维提供（见【配置中心】章节），可以映射到一组数据库账号，开发者不需要关心账号密码，只需要kid即可实现jdbc连接

4.覆盖缺省值的办法

    上面提到kid，可以作为数据库配置的标识。通过系统自定义配置，可以通过重写配置来覆盖dataSource的属性值，配置如下：
        spring:
          datasource:
            druid:
                [kid]:
                    username: xhtest
                    initialSize: 50
                    minIdle: 50
                    maxActive: 100
                    maxWait: 3000
    说明：
    * 上述的[kid]表示spring.datasource.druid.kid，如上面的weixin01-test1，接下来的属性配置则可以直接替换dataSource的缺省值       
    提示：可以参考templateproject的templateproject-local.yaml，一般建议重写连接数配置，以便匹配线上业务量
    
5.相关缺省值
    
    initialSize=10
    minIdle=3
    maxActive=30
    maxWait=3000
    timeBetweenEvictionRunsMillis=120000
    minEvictableIdleTimeMillis=300000
    testWhileIdle=true
    testOnBorrow=false
    testOnReturn=false
    （所有的缺省值列表见kael-autoconfigure/resources/..../druid/default.properties）

完成上述步骤后，系统即可做数据库操作，同时也有metrics监控埋点（key的关键字包含："jdbc_connections"）

###多数据源
```
通过另一个子类继承AbstractDataSourceConfiguration，可以参考templateproject工程的DruidDataSourceConfiguration.java
同时要注意调整common-dal模块（添加相应的dao,dto,mapper）
```

##【redis】
###单组redis
1.添加依赖：

	<dependency>
		<groupId>com.xhqb.roshan</groupId>
		<artifactId>roshan-starter-redis</artifactId>
	</dependency>

2.在系统本地[application]配置redis参数：
	
	spring:
      redis:
        kid: "weixin01"
	
	随后系统启动即可注入redisConnection、redisTemplate
	注意：该kid由运维提供（见【配置中心】章节），可以映射到一组redis连接参数

3.覆盖缺省值的办法

    上面提到kid，可以作为数据库配置的标识。通过系统自定义配置，可以通过重写配置来覆盖dataSource的属性值，配置如下：
        spring:
          redis:
            [kid]:
                min-idle: 10
                maxWaitMillis: 3500
    说明：
    * 上述的[kid]是由运维提供的，功能和druid数据源配置类似，都是作为一种约定的key，映射一组对应的连接配置      
    提示：可以参考templateproject的templateproject-local.yaml，redis可以视情况重写连接数配置
    
4.相关缺省值

	kael.redis.jedis.pool.max-total=50
    kael.redis.jedis.pool.max-idle=50
    kael.redis.jedis.pool.min-idle=5
    kael.redis.jedis.pool.maxWaitMillis=2000
	（所有的缺省值列表见kael-autoconfigure/resources/..../redis/default.properties）

	
###多组redis

```
（做法与多数据源的类似）通过另一个子类继承AbstractRedisConnectionConfiguration，可以参考templateproject工程的RedisConfiguration.java  
```

###redis session
1.添加依赖：

    <dependency>
        <groupId>com.xhqb.roshan</groupId>
        <artifactId>roshan-starter-redis-session</artifactId>
    </dependency>
    
2.在系统的spring-boot的启动类上面添加注解@EnableRedisHttpSession，如：

    @SpringBootApplication
    @ImportResource(value = "classpath:applicationContext.xml")
    @EnableRedisHttpSession
    public class TemplateWebApplication extends SpringBootServletInitializer {
        ......
    }


##【sequence】

添加依赖：  

    <dependency>
        <groupId>com.xhqb.roshan</groupId>
        <artifactId>roshan-starter-sequence</artifactId>
    </dependency>

此时会有默认赋值，包括：
	
	kael.sequence.env = ${kael.application.env}
	kael.sequence.namespace = ${kael.application.name}

需要额外添加配置（环境相关）：

	kael.sequence.url = 10.110.3.56:2181

随即可以注入使用：

    @Autowired
    private DistributedSequence sequence;

    
##【elasticjob】
1.添加依赖：

    <dependency>
        <groupId>com.xhqb.roshan</groupId>
        <artifactId>roshan-starter-elasticjob</artifactId>
    </dependency>
    
2.修改job实现类，由原继承AbstractSimpleElasticJob修改为实现com.dangdang.ddframe.job.api.simple.SimpleJob接口，示例：
	
	@Component
	public class DemoJob implements SimpleJob {
	
	    private static Logger logger = LoggerFactory.getLogger(DemoJob.class);
	
	    public void execute(ShardingContext shardingContext) {
	        logger.info("Running at [{}]", System.currentTimeMillis());
	    }
	
	}

3.需要添加配置

    elastic-job:
      	reg-config:
        	server-lists: 10.28.2.11:2181,10.28.2.12:2181
      	common-config:
        	overwrite: true
      	jobs:
			demoJob:
			  cron: "0 0/1 * * * ?"
			  shardingTotalCount: 1
			  shardingItemParameters: 0=A
			  disabled: false
			  description: just a demo
	(注意：上部分环境相关的配置应该转移到配置中心，剩余jobs业务特性的配置保留在本地，具体可参考templateproject的DemoJob.java)
	
	
##【SQS】
1.Q的发送端
	
	【系统的config配置类的配置片段】
		@Bean
        public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSqs, ResourceIdResolver resourceIdResolver) {
            QueueMessagingTemplate queueMessagingTemplate = new QueueMessagingTemplate(amazonSqs, resourceIdResolver);
            queueMessagingTemplate.setMessageConverter(new HessianMessageConverter());
            return queueMessagingTemplate;
        }
	
	【发送消息代码片段】
		@Autowired
		private QueueMessagingTemplate queue;
		
		/**
		* @param queueName 	: Q名
		* @param message 	: 能被序列化的自定义的业务类
		**/
		@Override
		public void doSend(ObjectDemo message) {
		 	queue.convertAndSend(queueName, message);
		}
		
	关于Q的发送端，可以参考templateproject工程的DemoSqsSender

2.Q的消费端

	【系统的config配置类中声明QueueMessageHandlerConfigurer】
		@Bean
        public QueueMessageHandlerConfigurer  queueMessageHandlerConfigurer() {
            return new AbstractQueueMessageHandlerConfigurer() {
                @Override
                public void configPayloadArgumentConverters(List<MessageConverter> converters, Map<Class, MessageConverter> specialClassConverters) {
                    converters.add(new HessianMessageConverter());
                }
            };
        }
        
       （注意：如果不配置，则使用默认的ObjectToMessageConvert，JackToMessageMappingConvert作为默认的message convert）


	【消息listener代码片段】
		@SqsListener(value = "${templateproject.quque.for-demo.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
		public void subscriber(ObjectDemo message, Acknowledgment acknowledgment) {
		    Log.info("receive message at {}",System.currentTimeMillis());
		    //do something
		    acknowledgment.acknowledge(); //delete Q
		}
    （注意：这里关于Q的配置的key，约定key规范为[applicationName].quque.[service-bean].name，其中[service-bean]为业务特性描述，如contact、sendsms等等）
		
	关于Q的消费端，可以参考templateproject工程的DemoSqsListener
    

	
备注：这里用到的credentials，是由Spring Cloud自动注入，类型包含:instance、profile


##【其他aws服务】
添加依赖：

    <dependency>
        <groupId>com.xhqb.roshan</groupId>
        <artifactId>roshan-starter-aws</artifactId>
    </dependency>
    
该组件已声明了：
	
	com.amazonaws.auth.AWSCredentialsProvider credentialsProvider;
	com.amazonaws.ClientConfiguration clientConfiguration;
	com.amazonaws.regions.Region region;
	com.xhqb.kael.aws.sqs.SqsMessagingTemplate sqsMessagingTemplate;
	com.amazonaws.services.s3.AmazonS3Client amazonS3Client;
	com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient amazonDynamoDBClient;
	com.amazonaws.services.kinesis.AmazonKinesisAsyncClient amazonKinesisAsyncClient;
	因此，该组件可以支持的aws服务包括：Q群发、S3、dynamoDB、kinesis，请在需要的地方自行注入即可。
	若需要添加其他aws服务，可以参考templateproject的AwsServiceConfiguration.class，通过Spring Boot方式声明一个实例！

此外，该组件已添加了一些配置的缺省值，包括：

	aws.region = cn-north-1
	aws.maxConnections = 50
	aws.connectionTimeout = 3000
	aws.socketTimeout = 3000	
	（注意：region可以通过启动变量做覆盖，以便灵活支持北京区宁夏区的适配发布，该参数由运维同事管理）


##【SpringMVC】
```
Spring Boot默认集成了SpringMVC组件，可以直接编辑Controller
```

##【Spring Scurity】
```
todo  
```


---
---
---


备注
====
##配置规范
* 每个工程都要定一个基础的config配置类，命名一般是[application]Application.java，用于作为Spring Boot的启动类（内含main函数）
* 在工程的config配置类下面应创建包config，用于存放一些有热修改趋向的配置，同时这些配置也要采用Spring Boot注入方式
* 关于dubbo配置，因为没有实现注解方式，需要通过xml配置完成，配置key需要规范
* 关于Q配置的key的命名规范，应该遵循：[applicationName].quque.[service-logic].name
* 关于Q配置的value的命名规范，应该遵循[applicationName]-[service-logic]-${kael.application.stack} (这里因为注入kael.application.stack参数，因此能够适配环境)

