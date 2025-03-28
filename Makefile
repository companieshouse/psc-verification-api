artifact_name       := psc-verification-api
version             := unversioned

dependency_check_base_suppressions:=common_suppressions_spring_6.xml
dependency_check_suppressions_repo_branch:=main
dependency_check_minimum_cvss := 4
dependency_check_assembly_analyzer_enabled := false
dependency_check_suppressions_repo_url:=git@github.com:companieshouse/dependency-check-suppressions.git
suppressions_file := target/suppressions.xml

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(artifact_name).jar
	rm -f ./$(artifact_name)-*.zip
	rm -rf ./build-*
	rm -rf ./build.log-*

.PHONY: submodules
submodules:
	git submodule init
	git submodule update

.PHONY: test-unit
test-unit:
	echo "make test-unit does nothing, use build target instead"

.PHONY: test-integration
test-integration: clean
	mvn integration-test -Dskip.unit.tests=true

.PHONY: verify
verify: clean
	mvn verify

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	$(info Packaging version: $(version))
	@test -s ./$(artifact_name).jar || { echo "ERROR: Service JAR not found"; exit 1; }
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./routes.yaml $(tmpdir)
	cp ./$(artifact_name).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: build
build: clean submodules
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn verify
	cp ./target/$(artifact_name)-$(version).jar ./$(artifact_name).jar

.PHONY: dist
dist: clean coverage submodules verify

.PHONY: coverage
coverage:
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn verify
	cp ./target/$(artifact_name)-$(version).jar ./$(artifact_name).jar

.PHONY: sonar
sonar:
	mvn sonar:sonar -Dsonar.dependencyCheck.htmlReportPath=./target/dependency-check-report.html

.PHONY: sonar-pr-analysis
sonar-pr-analysis: dependency-check
	mvn sonar:sonar -P sonar-pr-analysis -Dsonar.dependencyCheck.htmlReportPath=./target/dependency-check-report.html

.PHONY: dependency-check
dependency-check:
	@ if [ -d "$(DEPENDENCY_CHECK_SUPPRESSIONS_HOME)" ]; then \
		suppressions_home="$${DEPENDENCY_CHECK_SUPPRESSIONS_HOME}"; \
	fi; \
	if [ ! -d "$${suppressions_home}" ]; then \
	    suppressions_home_target_dir="./target/dependency-check-suppressions"; \
		if [ -d "$${suppressions_home_target_dir}" ]; then \
			suppressions_home="$${suppressions_home_target_dir}"; \
		else \
			mkdir -p "./target"; \
			git clone $(dependency_check_suppressions_repo_url) "$${suppressions_home_target_dir}" && \
				suppressions_home="$${suppressions_home_target_dir}"; \
			if [ -d "$${suppressions_home_target_dir}" ] && [ -n "$(dependency_check_suppressions_repo_branch)" ]; then \
				cd "$${suppressions_home}"; \
				git checkout $(dependency_check_suppressions_repo_branch); \
				cd -; \
			fi; \
		fi; \
	fi; \
	suppressions_path="$${suppressions_home}/suppressions/$(dependency_check_base_suppressions)"; \
	if [  -f "$${suppressions_path}" ]; then \
		cp -av "$${suppressions_path}" $(suppressions_file); \
		mvn org.owasp:dependency-check-maven:check -Dformats="json,html" -DprettyPrint -DfailBuildOnCVSS=$(dependency_check_minimum_cvss) -DassemblyAnalyzerEnabled=$(dependency_check_assembly_analyzer_enabled) -DsuppressionFiles=$(suppressions_file); \
	else \
		printf -- "\n ERROR Cannot find suppressions file at '%s'\n" "$${suppressions_path}" >&2; \
		exit 1; \
	fi

.PHONY: security-check
security-check: dependency-check

