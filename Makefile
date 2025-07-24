artifact_name       := psc-verification-api
version             := unversioned

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
sonar-pr-analysis:
	mvn sonar:sonar -P sonar-pr-analysis

