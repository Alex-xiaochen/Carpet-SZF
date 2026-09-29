# Carpet-SZF

一个给SZF服务器写的carpet拓展，(虽然写的非常何意味)，里面有不少功能可供大家探索

## 多版本构建

本项目使用 [Stonecutter](https://stonecutter.kikugie.dev/) 维护一份共享源码，同时构建多个 Minecraft 版本。
当前支持的版本和对应的依赖写在 `versions/<版本>/gradle.properties`，注册在 `settings.gradle.kts`。

| Minecraft | Carpet | Fabric API | Java |
|---|---|---|---|
| 1.21.11 | `1.21.11-1.4.194+v260107` | `0.141.6+1.21.11` | 21 |
| 26.1.2 | `26.1+v260401` | `0.155.3+26.1.2` | 25 |
| 26.2 | `26.2+v260616` | `0.161.0+26.2` | 25 |
| 26.3 | `maven.modrinth:carpet:26.3` | `0.161.0+26.3` | 25 |

Fabric Loader 统一用 `0.19.5`：Carpet 26.3 拒绝在更低的版本上加载。

> Carpet 的 [masa.dy.fi maven](https://masa.dy.fi/maven) 上**没有 26.3 正式版**
> （只到 `26.3-beta-3`，那是针对 26.3-snapshot-9 构建的，在 26.3 正式版上会因
> `NoClassDefFoundError: DensityFunction$FunctionContext` 崩溃）。26.3 正式版的
> `fabric-carpet-26.3+v260915` 只发布在 Modrinth 和 GitHub 上，所以这个节点走
> `maven.modrinth` 渠道。详见 `versions/26.3/gradle.properties`。

### 常用命令

```bash
./gradlew buildAndCollect      # 构建全部版本，产物收集到 build/libs/<版本>/
./gradlew :26.2:build          # 只构建某个版本
./gradlew :26.2:runServer      # 以某个版本启动开发用服务端

./gradlew "Set active project to 26.2"   # 切换 src/ 对应的版本（会自动处理注释）
./gradlew "Reset active project"         # 切回 26.1.2，提交前跑一下
```

### 写版本相关的代码

`src/` 里保存的是**当前 active 版本**（默认 26.1.2）的形态，非当前版本的分支会被注释掉。
用 `//?` 注释写条件分支，改完跑一次 `Refresh active project` 让 Stonecutter 规范化：

```java
//? if >=26.2 {
import net.minecraft.advancements.triggers.CriteriaTriggers;
//?} else {
import net.minecraft.advancements.CriteriaTriggers;
//?}
```

1.21.11 是混淆版本，本项目对它使用 **Mojang 官方映射**（不是 Yarn），
这样类名才和 26.x 保持一致 —— 这也是整个项目只有一处条件分支的原因。
