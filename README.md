## 地面站DSP

*本项目基于IDEA，其他IDE以及编译器均无法正常工作*

本端作为socket的服务端，承担接收客户端请求并发送应答报文。
##更新说明
21.9.5:  
1.添加数字证书  
2.优化SM4加解密流程  
3.增加空地秘钥分配

21.8.30：

1.更新README  
2.添加发送非应答报文功能

###安装项目
>git clone https://github.com/thirdxiaozhu/Acars  
使用 IDEA 打开
###工作流程
0. 点击右侧，生成DSP和CMU的证书，以及加解密秘钥
1. 输入地面站DSP地址以及开放的端口,获取CMU连接之后，将0中生成的内容传输到CMU中
2. 模式字符： “2” (A模式)
3. 飞机注册号：不超过7位，不足7位则前面填充“.”
4. 标签： 两位字符，具体参照ARINC 620
5. 上下行标识： “A-Z”或“a-z”之间的一位字符，或为空（自动填充为<NAK>）
6. 应答标识： 0-9之间的一位字符，或为空
7. 正文：220个字符之内，具体参照Config类中的6bit表
8. 预览或发送
9. 当接收到地面站DSP转发的报文后，报文列表将添加该报文，点击报文后在下方可以看到ARINC620格式的报文
10. 选择下行报文进行应答或发送非应答报文

###工作原理
同CMU

###规划
同CMU

###相关项目
[机载CMU](https://github.com/thirdxiaozhu/Acars_CMU)  
[L2P_Transporter](https://github.com/thirdxiaozhu/L2PTransporter_Swing)


