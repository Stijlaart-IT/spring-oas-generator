# Agents

## Tasks
* When working on GH issues use the branch name `issue-<number>/<short-description>`

## Verification

* To verify the whole project run `./scripts/verfify.sh`
* To verify individual tests run ` mvn -pl <module> -am -Dtest=<test-name> test -Dsurefire.failIfNoSpecifiedTests=false -q`
* To verify individual modules for the example validation run ` mvn -f example-validation/pom.xml verify` 
