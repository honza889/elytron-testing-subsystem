How to test
===========

1. `mvn install` this testsuite
2. prepare Wildfy installation with Elytron installed
3. copy/make symlink from `$THIS/target/module/org/wildfly/security/elytron-test` to `$WILDFLY_HOME/modules/system/layers/base/org/wildfly/security/elytron-test`
4. start Wildfly
5. run testsuit: `mvn test -Dtest=org.wildfly.security.testing.KeyStoreTest`

