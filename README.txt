WallRide BSn (Wallride V1.0.0.BUILD-SNAPSHOT 的改進版)
============

Wallride V1.0.0.BUILD-SNAPSHOT 下載於 GitHub：wallride-master_2020.12.15.zip。

使用的主要技術有：
1、後端技術：Java 8、Spring MVC 5.1.6、Spring Boot 2.1.4、Spring Security 5.1.6、Hibernate Entitymanager 5.3.9、Spring Data JPA 2.1.6、Hibernate orm 5.3.9、Hibernate Search 5.10.5、JSON、Lucene-5.5.5、Infinispan 9.4.11、etc；
2、前端技術：HTML、CSS、JavaScript、Thymeleaf 3.0.11、Webpack、Froala Editer 2.9.x、Bootstrap 3、etc；
3、數據庫和其它：MySQL 8、Maven、HikariCP-3.2.0.jar、Tomcat 9.0.17、etc。

主要改進：
1、後端：
⑴ i18n，多國語言，拡展了方言，例如：en_CA、zh_CN、zh_TW。
⑵ Jasper Reporter ?
⑶ Article Search 結果變色 ？

2、前端：
⑴ Bootstrap@3 改成 Bootstrap@4.6.0，HTML 做了大量的改動，但是會有遺漏的；
⑵ Froala Editer@2.9.x 改成 Froala Editer@3.2.6；
⑶ moment@2.20.1 改成 date-fns@^2.19.0；
⑷ webpack@3.3.0 改成 webpack@^5.20.0；
⑸ 其它的 JavaScript 包也作了升級。

注：
1、後備時為了省空間，執行「$./mvnw clean;」、進入 wallride-ui-admin 和 wallride-ui-guest，刪除 dist 及 node_modules 二個目錄。
2、設置 application.properties 看安裝說明，~/work/webroot/wallride/config/application.properties。 

2021年3月11日
2021年3月18日 修改


