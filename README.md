# cicada
<img src="https://raw.githubusercontent.com/exceting/OSSRH-96790/main/cicada-tools/log-trace/cicada-logo_00000.png">

java微服务脚手架，施工中...

目标：
* 各模块可观测（基于`OpenTelemetry`协议）
* 各模块基于协议实现，可灵活插拔
* 提供多种工具集：
  * [LogTrace](/tools/logtrace/README.md)：自动植入业务逻辑追踪日志，使排错更简单
  * 代码自动生成，统一代码风格，提升开发效率