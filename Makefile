# Spring Retry NES 本地维护入口
#
# 本文件封装可审计的 Maven/OpenSpec 操作；deploy 支持 SNAPSHOT 和 RELEASE，
# RELEASE 必须显式确认，且不提供 Git tag 或 push。
# 工具路径均可通过命令行变量覆盖，避免把某台维护机的路径写死到构建逻辑中。

.PHONY: help clean test verify verify-official install-local deploy validate check-tools check-deploy

DEV_ROOT ?= $(HOME)/dev
MAVEN ?= $(DEV_ROOT)/apache-maven-3.8.2/bin/mvn
JAVA8_HOME ?= $(HOME)/.sdkman/candidates/java/8.0.472-amzn
OPENSPEC ?= openspec
OPEN_SPEC_CHANGE ?=
MAVEN_FLAGS ?=
ALLOW_RELEASE_DEPLOY ?= false

# 只为子进程设置 JAVA_HOME/PATH，不改变调用者当前 shell 的 Java 版本。
JAVA8_ENV = JAVA_HOME="$(JAVA8_HOME)" PATH="$(JAVA8_HOME)/bin:$$PATH"

help: ## 显示可用的本地维护命令
	@echo "可用命令:"
	@echo "  make clean           - 使用真实 JDK 8 清理 target 构建产物"
	@echo "  make test            - 使用真实 JDK 8 运行 JUnit 4 测试"
	@echo "  make verify          - 默认 NES Framework 执行 clean verify"
	@echo "  make verify-official - 官方 Spring Framework 5.3.39 隔离执行 clean verify"
	@echo "  make install-local   - 执行 clean install，仅写入本地 Maven repository"
	@echo "  make deploy          - 完整验证并按版本部署到 Nexus snapshots/releases"
	@echo "  make validate        - OpenSpec strict 校验与 git diff --check"
	@echo "  make help            - 显示本帮助"
	@echo ""
	@echo "RELEASE 必须显式使用: make deploy ALLOW_RELEASE_DEPLOY=true"
	@echo "可覆盖变量: DEV_ROOT JAVA8_HOME MAVEN MAVEN_FLAGS ALLOW_RELEASE_DEPLOY OPENSPEC OPEN_SPEC_CHANGE"
	@echo "Gradle 仅用于临时消费者，不由本仓库 Makefile 构建。"

check-tools:
	@command -v "$(MAVEN)" >/dev/null 2>&1 || test -x "$(MAVEN)" || { \
		echo "错误：找不到 Maven：$(MAVEN)" >&2; \
		echo "请在 ~/dev 放置已验证版本，或通过 MAVEN=/path/to/mvn 覆盖。" >&2; \
		exit 1; \
	}
	@test -x "$(JAVA8_HOME)/bin/java" || { \
		echo "错误：找不到 JDK 8：$(JAVA8_HOME)" >&2; \
		echo "请通过 JAVA8_HOME=/path/to/jdk8 覆盖。" >&2; \
		exit 1; \
	}

check-deploy: check-tools
	@version="$$( $(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) -q -Dstyle.color=never -DforceStdout help:evaluate -Dexpression=project.version )"; \
	case "$$version" in \
		*-SNAPSHOT) repository_kind="SNAPSHOT"; url_property="nexusSnapshotUrl" ;; \
		*) \
			if [ "$(ALLOW_RELEASE_DEPLOY)" != "true" ]; then \
				echo "错误：RELEASE deploy 必须显式设置 ALLOW_RELEASE_DEPLOY=true，当前版本为 $$version" >&2; \
				exit 1; \
			fi; \
			repository_kind="RELEASE"; url_property="nexusReleaseUrl" ;; \
	esac; \
	repository_url="$$( $(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) -q -Dstyle.color=never -DforceStdout help:evaluate -Dexpression=$$url_property )"; \
	case "$$repository_url" in \
		http://*|https://*) ;; \
		*) echo "错误：Maven settings 未提供可用的 $$url_property。" >&2; exit 1 ;; \
	esac; \
	echo "部署门禁通过：版本 $$version 将使用 $$repository_kind repository。"

clean: check-tools ## 使用真实 JDK 8 清理构建产物
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) clean

test: check-tools ## 使用真实 JDK 8 运行测试
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) test

verify: check-tools ## 默认 NES Framework 执行完整 clean verify
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) clean verify

verify-official: check-tools ## 使用官方 Spring Framework 5.3.39 隔离执行完整 clean verify
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) \
		-Dspring.framework.group-id=org.springframework \
		-Dspring.framework.artifact-prefix=spring- \
		-Dspring.framework.version=5.3.39 \
		clean verify

install-local: check-tools ## 编译并安装 SNAPSHOT 到本地 Maven repository
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) clean install

deploy: check-deploy ## 完整验证并按版本部署到 Nexus
	$(JAVA8_ENV) "$(MAVEN)" $(MAVEN_FLAGS) clean deploy

validate: ## 执行 OpenSpec strict 校验和工作区格式检查
	@if [ -n "$(OPEN_SPEC_CHANGE)" ]; then \
		command -v "$(OPENSPEC)" >/dev/null 2>&1 || { echo "错误：找不到 openspec：$(OPENSPEC)" >&2; exit 1; }; \
		"$(OPENSPEC)" validate "$(OPEN_SPEC_CHANGE)" --type change --strict --no-interactive; \
	fi
	@command -v "$(OPENSPEC)" >/dev/null 2>&1 || { echo "错误：找不到 openspec：$(OPENSPEC)" >&2; exit 1; }
	"$(OPENSPEC)" validate --all --strict --no-interactive
	git diff --check
